package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;

public class VehicleThirdPersonCamera implements VehicleCamera, AnalogListener, ActionListener {

    private static final String MOUSE_MOVE_RIGHT = "Right";
    private static final String MOUSE_MOVE_LEFT = "Left";
    private static final String MOUSE_MOVE_UP = "Up";
    private static final String MOUSE_MOVE_DOWN = "Down";

    private static final String TOGGLE_ROTATE = "Toggle_Rotate";
    private static final String TOGGLE_TRANSLATE = "Toggle_Translate";
    private static final String RESET_OFFSET = "Reset_Offset";

    private static final String ZOOM_IN = "Zoom_In";
    private static final String ZOOM_OUT = "Zoom_Out";

    private InputManager inputManager;
    private Camera cam;

    private Spatial focusPoint;
    private Vector3f offset = new Vector3f();

    private float rotationSpeed = FastMath.TWO_PI;

    private float zoomDistance = 10;
    private float zoomSpeed = 1;

    private float minZoom = 5;
    private float maxZoom = 30;

    private boolean invertY = false;

    private Vector3f direction = new Vector3f();
    private boolean rotate, translate;

    // we have a vehicle rotation + a custom rotation.
    private float[] viewAngles = new float[] { -FastMath.QUARTER_PI * 0.5f, 0, 0 }; // custom rotation
    private final float[] angles = new float[3]; // the final rotation

    private final Vehicle vehicle;

    public VehicleThirdPersonCamera(Vehicle vehicle, Camera camera) {
        this.vehicle = vehicle;
        this.cam = camera;
        this.inputManager = vehicle.getApplication().getInputManager();

        setFocusPoint(vehicle.getNode());
    }

    private void registerInput() {


        inputManager.addMapping(TOGGLE_ROTATE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(TOGGLE_TRANSLATE, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        inputManager.addMapping(RESET_OFFSET, new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

        inputManager.addMapping(MOUSE_MOVE_RIGHT, new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(MOUSE_MOVE_LEFT, new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping(MOUSE_MOVE_UP, new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(MOUSE_MOVE_DOWN, new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addMapping(ZOOM_IN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(ZOOM_OUT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(this,
                MOUSE_MOVE_UP, MOUSE_MOVE_DOWN,
                MOUSE_MOVE_LEFT, MOUSE_MOVE_RIGHT,
                TOGGLE_ROTATE, TOGGLE_TRANSLATE,
                ZOOM_IN, ZOOM_OUT, RESET_OFFSET);
    }

    private void unregisterInput() {
        inputManager.deleteMapping(TOGGLE_ROTATE);
        inputManager.deleteMapping(TOGGLE_TRANSLATE);

        inputManager.deleteMapping(MOUSE_MOVE_RIGHT);
        inputManager.deleteMapping(MOUSE_MOVE_LEFT);
        inputManager.deleteMapping(MOUSE_MOVE_UP);
        inputManager.deleteMapping(MOUSE_MOVE_DOWN);

        inputManager.deleteMapping(ZOOM_IN);
        inputManager.deleteMapping(ZOOM_OUT);

        inputManager.removeListener(this);
    }

    /**
     * Returns the spatial that the camera is focused on.
     * @return
     */
    public Spatial getFocusPoint() {
        return focusPoint;
    }

    /**
     * Set the spatial to focus the camera on.
     * @param focusPoint
     */
    public void setFocusPoint(Spatial focusPoint) {
        this.focusPoint = focusPoint;
        lookAt();
    }

    /**
     * Sets the rotation speed in radians.
     * @return the speed of rotation in radians.
     */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Gets the rotation speed in radians.
     * @param rotationSpeed the speed of rotation in radians.
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Returns the current distance from the world location of the focused spatial.
     * @return the zoom distance.
     */
    public float getZoomDistance() {
        return zoomDistance;
    }

    /**
     * Sets the zoom distance from the focused spatial.
     * @param zoomDistance the distance from the world location of the focused spatial.
     */
    public void setZoomDistance(float zoomDistance) {
        this.zoomDistance = zoomDistance;
        lookAt();
    }

    /**
     * The speed in world units that the camera will zoom in and out.
     * @return the speed in world units.
     */
    public float getZoomSpeed() {
        return zoomSpeed;
    }

    /**
     * Sets the speed of the zoom action in world units.
     * @param zoomSpeed the speed in world units.
     */
    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    /**
     * The minimum distance the camera can zoom in.
     * @return the minimum distance of zoom.
     */
    public float getMinZoom() {
        return minZoom;
    }

    /**
     * Sets the minimum distance the camera will zoom in to the spatial.
     * @param minZoom the minimum zoom distance.
     */
    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    /**
     * The maximum distance the camera can zoom out.
     * @return the maximum zoom distance.
     */
    public float getMaxZoom() {
        return maxZoom;
    }

    /**
     * Sets the maximum zoom distance the camera can zoom out.
     * @param maxZoom the maximum zoom distance.
     */
    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    /**
     * Returns whether or not the Up/Down rotation is flipped.
     * @return whether or not the up/down rotation is flipped.
     */
    public boolean isInvertY() {
        return invertY;
    }

    /**
     * Sets whether or not to flip the up/down rotation.
     * @param invertY whether or not to flip the up/down rotation.
     */
    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    /**
     * Gets the offset of the focus point.
     * @return the offset of the focus point.
     */
    public Vector3f getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the focus point.
     * @param offset the offset of the focus point.
     */
    public void setOffset(Vector3f offset) {
        this.offset.set(offset);
    }

    /**
     * Sets the offset point to 0,0,0
     */
    public void resetOffset() {
        this.offset.set(0, 0, 0);
        lookAt();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(TOGGLE_ROTATE)) {
            rotate = isPressed;
        }
        else if (name.equals(TOGGLE_TRANSLATE)) {
            translate = isPressed;
        }
        else if (name.equals(RESET_OFFSET) && isPressed) {
            resetOffset();
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {

        if (focusPoint == null) {
            return;
        }

        // Vector3f vehicleDir = focusPoint.getLocalRotation().mult(Vector3f.UNIT_Z);
        float[] vehicleRot = focusPoint.getLocalRotation().toAngles(null);

        direction.set(cam.getDirection())
                .normalizeLocal();

        if (rotate) {

            if (MOUSE_MOVE_UP.equals(name) || MOUSE_MOVE_DOWN.equals(name)) {

                int dirState = MOUSE_MOVE_UP.equals(name) ? 1 : -1;

                if (invertY) {
                    dirState *= -1;
                }

                // angles[0] += dirState * (rotationSpeed * tpf);
                viewAngles[0] += dirState * (rotationSpeed * tpf);

                // 89 degrees. Avoid the "flip" problem.
                float maxRotX = FastMath.HALF_PI - FastMath.DEG_TO_RAD;

                // limit camera rotation.
                if (viewAngles[0] < -maxRotX) {
                    viewAngles[0] = -maxRotX;
                }

                if (viewAngles[0] > maxRotX) {
                    viewAngles[0] = maxRotX;
                }
            }

            if (MOUSE_MOVE_RIGHT.equals(name) || MOUSE_MOVE_LEFT.equals(name)) {

                int dirState = MOUSE_MOVE_RIGHT.equals(name) ? 1 : -1;
                viewAngles[1] += dirState * (rotationSpeed * tpf);

                // stop the angles from becoming too big.
                if (viewAngles[1] > FastMath.TWO_PI) {
                    viewAngles[1] -= FastMath.TWO_PI;
                } else if (viewAngles[1] < -FastMath.TWO_PI) {
                    viewAngles[1] += FastMath.TWO_PI;
                }
            }
            lookAt();
        }
        if (translate) {
            if (MOUSE_MOVE_UP.equals(name) || MOUSE_MOVE_DOWN.equals(name)) {

                int dirState = MOUSE_MOVE_UP.equals(name) ? 1 : -1;
                offset.addLocal(0, dirState * (rotationSpeed * tpf), 0);
            }

            if (MOUSE_MOVE_RIGHT.equals(name) || MOUSE_MOVE_LEFT.equals(name)) {

                int dirState = MOUSE_MOVE_RIGHT.equals(name) ? 1 : -1;

                Vector3f left = rotation.mult(Vector3f.UNIT_X);
                offset.addLocal(left.mult(dirState * (rotationSpeed * tpf)));
            }

            lookAt();
        }

        if (ZOOM_IN.equals(name)) {
            zoomDistance = Math.max(minZoom, zoomDistance - zoomSpeed);
            lookAt();
        }
        else if (ZOOM_OUT.equals(name)) {
            zoomDistance = Math.min(maxZoom, zoomDistance + zoomSpeed);
            lookAt();
        }
    }

    private Quaternion rotation = new Quaternion();

    private void lookAt() {

        rotation = new Quaternion().fromAngles(angles);

        Vector3f direction = rotation.mult(Vector3f.UNIT_Z);

        if (followVehicle) {
            Vector3f loc = direction.mult(zoomDistance).add(focusPoint.getWorldTranslation().add(offset));
            cam.setLocation(loc);
        }

        cam.lookAt(focusPoint.getWorldTranslation().add(offset), Vector3f.UNIT_Y);
    }

    @Override
    public void enableInputMappings() {
        registerInput();
    }

    @Override
    public void disableInputMappings() {
        unregisterInput();
    }

    @Override
    public void attach() {
        enableInputMappings();

        // if we set the focus point before the state was initialized, set the focus now.
        if (focusPoint != null) {
            lookAt();
        }
    }

    @Override
    public void detach() {
        disableInputMappings();
    }

    @Override
    public void update(float tpf) {

        if (followVehicle) {
            setFocusPoint(focusPoint);
        }

        angles[0] = viewAngles[0];
        angles[1] = viewAngles[1];

        if (followVehicleRotation) {
            float[] focusRot = focusPoint.getLocalRotation().toAngles(null);
            angles[1] += (focusRot[1] + FastMath.PI);
        }

        lookAt();
    }

    private boolean followVehicleRotation = true;
    private boolean followVehicle = true;

    public boolean isFollowVehicleRotation() {
        return followVehicleRotation;
    }

    public void setFollowVehicleRotation(boolean followVehicleRotation) {
        this.followVehicleRotation = followVehicleRotation;
    }

    public boolean isFollowVehicle() {
        return followVehicle;
    }

    public void setFollowVehicle(boolean followVehicle) {
        this.followVehicle = followVehicle;
    }
}
