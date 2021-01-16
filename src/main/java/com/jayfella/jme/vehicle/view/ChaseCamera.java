package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
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
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;

/**
 * A CameraController to orbit a target vehicle, jumping forward as needed to
 * maintain a clear line of sight in the vehicle's CollisionSpace. Two chasing
 * behaviors are implemented: FreeOrbit and StrictChase.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ChaseCamera
        extends CameraController
        implements AnalogListener {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum magnitude of the dot product between the camera's look direction
     * and its preferred "up" direction)
     */
    final private static double maxAbsDot = Math.cos(0.3);
    /**
     * orbiting rate (in radians per second, &ge;0)
     */
    final private static float orbitRate = 0.5f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ChaseCamera.class.getName());
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
     * configured target-chasing behavior (not null)
     */
    final private ChaseOption chaseOption;
    /**
     * how much to displace the camera target (0=center of mass, 1=back bumper)
     */
    final private float rearBias;
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
     * Instantiate a VehicleCamera that orbits (and optionally chases) the
     * selected Vehicle.
     *
     * @param camera the Camera to control (not null, alias created)
     * @param tracker the status tracker for named signals (not null, alias
     * created)
     * @param chaseOption to configure chase behavior (not null)
     * @param rearBias how much to displace the camera target (0=center of mass,
     * 1=back bumper)
     * @param obstructionFilter to determine which collision objects obstruct
     * the camera's view (alias created) or null to treat all non-target PCOs as
     * obstructions
     */
    public ChaseCamera(Camera camera, SignalTracker tracker,
            ChaseOption chaseOption, float rearBias,
            BulletDebugAppState.DebugAppStateFilter obstructionFilter) {
        super(Main.getVehicle(), camera, tracker);
        Validate.nonNull(chaseOption, "chase option");

        this.chaseOption = chaseOption;
        this.rearBias = rearBias;
        this.obstructionFilter = obstructionFilter;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the configured ChaseOption.
     *
     * @return the enum value
     */
    public ChaseOption getChaseOption() {
        return chaseOption;
    }

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
     * Alter the preferred range.
     *
     * @param range the desired distance (in world units, &gt;0)
     */
    public void setPreferredRange(float range) {
        Validate.positive(range, "range");
        this.preferredRange = range;
    }

    /**
     * Alter which Vehicle the camera is targeting. May modify the "offset" and
     * "tmpCameraLocation" fields.
     *
     * @param newVehicle the desired target vehicle (not null)
     */
    @Override
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");
        super.setVehicle(newVehicle);

        tmpCameraLocation.set(camera.getLocation());
        vehicle.targetLocation(rearBias, tmpTargetLocation);
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

        boolean isDragToOrbit = isActive(CameraSignal.DragToOrbit);
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
        enable();
    }

    @Override
    public void detach() {
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
        boolean cursorVisible = !isActive(CameraSignal.DragToOrbit);
        InputManager inputManager = Main.getApplication().getInputManager();
        inputManager.setCursorVisible(cursorVisible);
        /*
         * Sum the discrete inputs (signals).
         */
        int forwardSum = 0;
        int orbitUpSign = 0;
        int orbitCwSign = 0;
        int zoomSignalDirection = 0;
        for (CameraSignal function : CameraSignal.values()) {
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
        if (chaseOption == ChaseOption.StrictChase) {
            yawAnalogSum = 0f;
        }
        if (pitchAnalogSum != 0f || yawAnalogSum != 0f) {
            float multiplier = camera.getHeight() / 1024f;
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
        if (chaseOption == ChaseOption.StrictChase) {
            /*
             * Rotate the "look" direction to stay
             * directly behind the target Vehicle.
             */
            vehicle.forwardDirection(tmpRej);
            assert preferredUpDirection.equals(Vector3f.UNIT_Y) :
                    preferredUpDirection;
            float thetaForward = FastMath.atan2(tmpRej.x, tmpRej.z);
            float thetaLook = FastMath.atan2(tmpLook.x, tmpLook.z);
            float angle = thetaForward - thetaLook;
            if (Float.isFinite(angle)) {
                tmpRotation.fromAngles(0f, angle, 0f);
                tmpRotation.mult(tmpLook, tmpLook);
            }
        }
        /*
         * Apply the new "look" direction to the Camera.
         */
        assert tmpLook.isUnitVector() : tmpLook;
        camera.lookAtDirection(tmpLook, preferredUpDirection);

        boolean xrayVision = isActive(CameraSignal.Xray);
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

        vehicle.targetLocation(rearBias, tmpTargetLocation);
        if (!xrayVision) {
            /*
             * Test the sightline for obstructions.
             */
            PhysicsCollisionObject targetPco = vehicle.getVehicleControl();
            range = testSightline(range, targetPco);
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
        if (chaseOption == ChaseOption.FreeOrbit) {
            inputManager.deleteMapping(analogOrbitCcw);
            inputManager.deleteMapping(analogOrbitCw);
        }
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
        if (chaseOption == ChaseOption.FreeOrbit) {
            camera.setName("orbit camera");
        } else {
            camera.setName("chase camera");
        }
        /*
         * Initialize the camera offset and preferred range.
         */
        tmpCameraLocation.set(camera.getLocation());
        vehicle.targetLocation(rearBias, tmpTargetLocation);
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
        if (chaseOption == ChaseOption.FreeOrbit) {
            inputManager.addMapping(analogOrbitCcw,
                    new MouseAxisTrigger(MouseInput.AXIS_X, false));
            inputManager.addMapping(analogOrbitCw,
                    new MouseAxisTrigger(MouseInput.AXIS_X, true));
            inputManager.addListener(this, analogOrbitCcw, analogOrbitCw);
        }
        inputManager.addMapping(analogOrbitDown,
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(analogOrbitUp,
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping(analogZoomIn,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(analogZoomOut,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(this, analogOrbitDown, analogOrbitUp,
                analogZoomIn, analogZoomOut);
    }

    /**
     * Test the sightline for obstructions, from the target to the camera, using
     * the obstructionFilter (if any). May modify the "offset" and
     * "tmpCameraLocation" fields.
     *
     * @param range the distance between the target and the camera (in world
     * units, &ge;0)
     * @param targetPco the collision object of the target (not null)
     * @return a modified distance (in world units, &ge;0)
     */
    private float testSightline(float range, PhysicsCollisionObject targetPco) {
        CollisionSpace collisionSpace = targetPco.getCollisionSpace();
        if (collisionSpace == null) {
            return range;
        }

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

        return range;
    }
}
