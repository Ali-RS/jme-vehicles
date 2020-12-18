package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;

/**
 * A VehicleCamera to control a Camera that orbits a Vehicle, jumping forward as
 * needed to maintain a clear line of sight in the vehicle's CollisionSpace. Not
 * intended for moving vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class OrbitCamera
        extends VehicleCamera
        implements AnalogListener {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum magnitude of the dot product between the camera's look direction
     * and its preferred "up" direction (default=cos(0.3))
     */
    final private static double maxAbsDot = Math.cos(0.3);
    /**
     * orbiting rate (in radians per second, &ge;0, default=0.5)
     */
    final private static float orbitRate = 0.5f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(OrbitCamera.class.getName());
    /**
     * names of analog events
     */
    final private static String analogOrbitCcw = "orbit ccw";
    final private static String analogOrbitCw = "orbit cw";
    final private static String analogOrbitDown = "orbit down";
    final private static String analogOrbitUp = "orbit up";
    /**
     * camera's preferred "up" direction (unit vector in world coordinates)
     */
    final private static Vector3f preferredUpDirection
            = new Vector3f(0f, 1f, 0f);
    // *************************************************************************
    // fields

    /**
     * test whether a collision object can obstruct the line of sight, or null
     * to treat all non-target PCOs as obstructions
     */
    final private BulletDebugAppState.DebugAppStateFilter obstructionFilter;
    /**
     * accumulated analog pitch input since the last update (in 1024-pixel
     * units, measured downward from the look direction)
     */
    private float pitchAnalogSum = 0f;
    /**
     * distance from the target vehicle if the camera had X-ray vision
     */
    private float preferredRange = 10f;
    /**
     * accumulated analog yaw input since the last update (in 1024-pixel units,
     * measured leftward from the look direction)
     */
    private float yawAnalogSum = 0f;
    /**
     * accumulated analog zoom amount since the last update (in clicks)
     */
    private float zoomAnalogSum = 0f;
    /**
     * reusable Quaternion
     */
    final private static Quaternion tmpRotation = new Quaternion();
    /**
     * camera's offset relative to the target vehicle (in world coordinates)
     */
    final private static Vector3f offset = new Vector3f();
    /**
     * reusable vectors
     */
    final private static Vector3f tmpCameraLocation = new Vector3f();
    final private static Vector3f tmpLeft = new Vector3f();
    final private static Vector3f tmpLook = new Vector3f();
    final private static Vector3f tmpProj = new Vector3f();
    final private static Vector3f tmpRej = new Vector3f();
    final private static Vector3f tmpTargetLocation = new Vector3f();
    final private static Vector3f tmpUp = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a VehicleCamera that orbits the selected Vehicle.
     *
     * @param camera the Camera to control (not null, alias created)
     * @param tracker the status tracker for named signals (not null, alias
     * created)
     * @param obstructionFilter to determine which collision objects obstruct
     * the camera's view (alias created) or null to treat all non-target PCOs as
     * obstructions
     */
    public OrbitCamera(Camera camera, SignalTracker tracker,
            BulletDebugAppState.DebugAppStateFilter obstructionFilter) {
        super(Main.getVehicle(), camera, tracker);
        this.obstructionFilter = obstructionFilter;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the offset of the camera from the target vehicle.
     *
     * @param desiredOffset the desired offset (in world coordinates)
     */
    public void setOffset(Vector3f desiredOffset) {
        Validate.finite(desiredOffset, "offset");
        offset.set(desiredOffset);
    }

    /**
     * Alter which Vehicle is associated with this camera.
     */
    @Override
    public void setVehicle(Vehicle newVehicle) {
        super.setVehicle(newVehicle);

        tmpCameraLocation.set(camera.getLocation());
        vehicle.targetLocation(tmpTargetLocation);
        tmpCameraLocation.subtract(tmpTargetLocation, offset);
    }
    // *************************************************************************
    // AnalogListener methods

    /**
     * Callback to receive an analog input event.
     *
     * @param eventName the name of the input event (not null, not empty)
     * @param reading the input reading (&ge;0)
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAnalog(String eventName, float reading, float tpf) {
        Validate.nonEmpty(eventName, "event name");
        Validate.nonNegative(reading, "reading");
        Validate.nonNegative(tpf, "time per frame");

        boolean isDragToOrbit = isActive(CcFunctions.DragToOrbit);
        switch (eventName) {
            case analogOrbitCcw:
                if (isDragToOrbit) {
                    yawAnalogSum += reading;
                }
                break;

            case analogOrbitCw:
                if (isDragToOrbit) {
                    yawAnalogSum -= reading;
                }
                break;

            case analogOrbitDown:
                if (isDragToOrbit) {
                    pitchAnalogSum += reading;
                }
                break;

            case analogOrbitUp:
                if (isDragToOrbit) {
                    pitchAnalogSum -= reading;
                }
                break;

            case analogZoomIn:
                zoomAnalogSum += reading;
                break;

            case analogZoomOut:
                zoomAnalogSum -= reading;
                break;

            default:
                throw new IllegalArgumentException(eventName);
        }
    }
    // *************************************************************************
    // VehicleCamera methods

    @Override
    public void attach() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup(NonDrivingInputState.G_ORBIT);

        enable();
    }

    @Override
    public void detach() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup(NonDrivingInputState.G_ORBIT);

        disable();
    }

    /**
     * Callback to update this state prior to rendering. (Invoked once per frame
     * while the state is attached and enabled.)
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        /*
         * Hide the cursor if dragging.
         */
        boolean cursorVisible = !isActive(CcFunctions.DragToOrbit);
        InputManager inputManager = Main.getApplication().getInputManager();
        inputManager.setCursorVisible(cursorVisible);
        /*
         * Sum the discrete inputs (signals).
         */
        int forwardSum = 0;
        int orbitUpSign = 0;
        int orbitCwSign = 0;
        int zoomSignalDirection = 0;
        for (CcFunctions function : CcFunctions.values()) {
            if (isActive(function)) {
                switch (function) {
                    case Back:
                        --forwardSum;
                        break;

                    case DragToOrbit:
                    case Xray:
                        // do nothing
                        break;

                    case Forward:
                        ++forwardSum;
                        break;

                    case OrbitCcw:
                        --orbitCwSign;
                        break;

                    case OrbitCw:
                        ++orbitCwSign;
                        break;

                    case OrbitDown:
                        --orbitUpSign;
                        break;

                    case OrbitUp:
                        ++orbitUpSign;
                        break;

                    case ZoomIn:
                        ++zoomSignalDirection;
                        break;

                    case ZoomOut:
                        --zoomSignalDirection;
                        break;

                    default:
                        throw new RuntimeException(function.toString());
                }
            }
        }
        /*
         * Apply the orbital inputs to the camera offset:
         * first the discrete signals and then the analog values.
         */
        float range = offset.length();
        if (orbitCwSign != 0 || orbitUpSign != 0) {
            float rootSumSquares = MyMath.hypotenuse(orbitCwSign, orbitUpSign);
            float dist = range * orbitRate * tpf / rootSumSquares;

            camera.getLeft(tmpLeft);
            assert tmpLeft.isUnitVector();
            MyVector3f.accumulateScaled(offset, tmpLeft, orbitCwSign * dist);

            camera.getUp(tmpUp);
            assert tmpUp.isUnitVector();
            MyVector3f.accumulateScaled(offset, tmpUp, orbitUpSign * dist);

            float factor = range / offset.length();
            offset.multLocal(factor);
        }
        if (pitchAnalogSum != 0f || yawAnalogSum != 0f) {
            float frustumYTangent = MyCamera.yTangent(camera);
            float multiplier = camera.getHeight() * frustumYTangent / 1024f;
            float pitchAngle = multiplier * pitchAnalogSum;
            float yawAngle = multiplier * yawAnalogSum;
            tmpRotation.fromAngles(pitchAngle, yawAngle, 0f);
            tmpRotation.mult(offset, offset);

            pitchAnalogSum = 0f;
            yawAnalogSum = 0f;
        }
        /*
         * Avoid looking too near the preferred "up" direction or its opposite.
         */
        offset.mult(-1f, tmpLook);
        tmpLook.normalizeLocal();
        double dot = MyVector3f.dot(tmpLook, preferredUpDirection);
        if (Math.abs(dot) > maxAbsDot) {
            preferredUpDirection.mult((float) dot, tmpProj);
            tmpLook.subtract(tmpProj, tmpRej);
            double rejL2 = MyVector3f.lengthSquared(tmpRej);
            if (rejL2 > 0.0) { // not directly above or below
                double newDot = MyMath.clamp(dot, maxAbsDot);
                double projCoeff = newDot / dot;
                double rejCoeff = Math.sqrt((1.0 - newDot * newDot) / rejL2);
                tmpProj.mult((float) projCoeff, tmpLook);
                MyVector3f.accumulateScaled(tmpLook, tmpRej, (float) rejCoeff);
            } else {
                MyVector3f.generateBasis(tmpLook, tmpProj, tmpRej);
                tmpLook.set(tmpProj);
            }
        }
        /*
         * Apply the new "look" direction to the Camera.
         */
        assert tmpLook.isUnitVector() : tmpLook;
        camera.lookAtDirection(tmpLook, preferredUpDirection);

        boolean xrayVision = isActive(CcFunctions.Xray);
        if (forwardSum != 0) {
            range *= FastMath.exp(-tpf * forwardSum); // TODO move rate?
            if (forwardSum > 0 || xrayVision) {
                preferredRange = range;
            }
        } else if (range < preferredRange) {
            range = preferredRange;
        }
        /*
         * Limit the range to reduce the risk of far-plane clipping.
         */
        float maxRange = 0.5f * camera.getFrustumFar();
        if (range > maxRange) {
            range = maxRange;
        }

        vehicle.targetLocation(tmpTargetLocation);
        if (!xrayVision) {
            /*
             * Test the sightline for obstructions, from target to camera.
             */
            PhysicsCollisionObject targetPco = vehicle.getVehicleControl();
            CollisionSpace collisionSpace = targetPco.getCollisionSpace();
            float rayRange = Math.max(range, preferredRange);
            tmpLook.mult(-rayRange, offset);
            tmpTargetLocation.add(offset, tmpCameraLocation);
            List<PhysicsRayTestResult> hits = collisionSpace.rayTestRaw(
                    tmpTargetLocation, tmpCameraLocation);

            float minFraction = 1f;
            for (PhysicsRayTestResult hit : hits) {
                PhysicsCollisionObject pco = hit.getCollisionObject();
                boolean isObstruction = (pco != targetPco)
                        && (obstructionFilter == null
                        || obstructionFilter.displayObject(pco));
                if (isObstruction) {
                    float hitFraction = hit.getHitFraction();
                    if (hitFraction < minFraction) {
                        minFraction = hitFraction;
                    }
                }
            }
            float obstructRange = rayRange * minFraction;
            if (obstructRange < range) {
                range = obstructRange;
            }
        }
        /*
         * Calculate the new camera offset and apply it to the Camera.
         */
        tmpLook.mult(-range, offset);
        tmpTargetLocation.add(offset, tmpCameraLocation);
        camera.setLocation(tmpCameraLocation);
        /*
         * Apply focal zoom, if any:
         * first the discrete signals and then the analog values.
         */
        if (zoomSignalDirection != 0) {
            float zoomFactor = FastMath.exp(zoomSignalDirection * tpf);
            magnify(zoomFactor);
        }
        if (zoomAnalogSum != 0f) {
            float zoomFactor = FastMath.exp(zoomMultiplier * zoomAnalogSum);
            magnify(zoomFactor);
            zoomAnalogSum = 0f;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Disable this camera controller. Assumes it is initialized and enabled.
     */
    private void disable() {
        /*
         * Configure the analog inputs.
         */
        InputManager inputManager = Main.getApplication().getInputManager();
        inputManager.deleteMapping(analogOrbitCcw);
        inputManager.deleteMapping(analogOrbitCw);
        inputManager.deleteMapping(analogOrbitDown);
        inputManager.deleteMapping(analogOrbitUp);
        inputManager.deleteMapping(analogZoomIn);
        inputManager.deleteMapping(analogZoomOut);
        inputManager.removeListener(this);

        inputManager.setCursorVisible(true);
    }

    /**
     * Enable this camera controller. Assumes it is initialized and disabled.
     */
    private void enable() {
        camera.setName("orbit camera");
        /*
         * Initialize the camera offset and preferred range.
         */
        tmpCameraLocation.set(camera.getLocation());
        vehicle.targetLocation(tmpTargetLocation);
        tmpCameraLocation.subtract(tmpTargetLocation, offset);
        preferredRange = offset.length();

        float yDegrees;
        if (camera.isParallelProjection()) {
            /*
             * Configure perspective.
             */
            yDegrees = 30f;
            float aspectRatio = MyCamera.viewAspectRatio(camera);
            float near = camera.getFrustumNear();
            float far = camera.getFrustumFar();
            camera.setFrustumPerspective(yDegrees, aspectRatio, near, far);
        }
        /*
         * Configure the analog inputs.
         */
        InputManager inputManager = Main.getApplication().getInputManager();
        inputManager.addMapping(analogOrbitCcw,
                new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(analogOrbitCw,
                new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(analogOrbitDown,
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(analogOrbitUp,
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping(analogZoomIn,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(analogZoomOut,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(this, analogOrbitCcw, analogOrbitCw,
                analogOrbitDown, analogOrbitUp, analogZoomIn, analogZoomOut);
    }
}
