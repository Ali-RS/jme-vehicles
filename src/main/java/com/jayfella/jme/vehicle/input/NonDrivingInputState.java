package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.view.CameraSignal;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jayfella.jme.vehicle.view.VehicleCamera;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;
import jme3utilities.minie.FilterAll;

/**
 * An AppState to handle input when not driving a Vehicle.
 */
public class NonDrivingInputState
        extends BaseAppState
        implements StateFunctionListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(NonDrivingInputState.class.getName());

    final public static String G_ORBIT = "GROUP_ORBIT";
    /**
     * Function IDs
     */
    final private static FunctionId F_CAMERA_RESET_FOV
            = new FunctionId(G_ORBIT, "Camera Reset FOV");
    final private static FunctionId F_CAMERA_ZOOM_IN1
            = new FunctionId(G_ORBIT, CameraSignal.ZoomIn.toString());
    final private static FunctionId F_CAMERA_ZOOM_OUT1
            = new FunctionId(G_ORBIT, CameraSignal.ZoomOut.toString());
    final private static FunctionId F_RETURN
            = new FunctionId(G_ORBIT, "Return to Main Menu");
    final private static FunctionId F_SCREEN_SHOT
            = new FunctionId(G_ORBIT, "ScreenShot");
    final private static FunctionId F_CAMERA_BACK1
            = new FunctionId(G_ORBIT, CameraSignal.Back.toString());
    final private static FunctionId F_CAMERA_DOWN1
            = new FunctionId(G_ORBIT, CameraSignal.OrbitDown.toString());
    final private static FunctionId F_CAMERA_DRAG_TO_ORBIT1
            = new FunctionId(G_ORBIT, CameraSignal.DragToOrbit.toString());
    final private static FunctionId F_CAMERA_FORWARD1
            = new FunctionId(G_ORBIT, CameraSignal.Forward.toString());
    final private static FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId(G_ORBIT, "Camera Reset Offset");
    final private static FunctionId F_CAMERA_UP1
            = new FunctionId(G_ORBIT, CameraSignal.OrbitUp.toString());
    // *************************************************************************
    // fields

    private InputMapper inputMapper;
    final private SignalTracker signalTracker;
    final private VehicleCamera activeCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an input state to orbit the selected Vehicle.
     */
    public NonDrivingInputState() {
        Camera cam = Main.getApplication().getCamera();

        signalTracker = new SignalTracker();
        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            signalTracker.add(signalName);
        }

        FilterAll filter = new FilterAll(true);
        activeCam = new ChaseCamera(cam, signalTracker, ChaseOption.FreeOrbit,
                filter);
        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            activeCam.setSignalName(function, signalName);
        }
    }
    // *************************************************************************
    // public methods

    /**
     * Access the SignalTracker.
     *
     * @return the pre-existing instance (not null)
     */
    public SignalTracker getSignalTracker() {
        return signalTracker;
    }

    /**
     * Alter which Vehicle is associated with the camera.
     *
     * @param newVehicle the vehicle to associate (not null)
     */
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");
        activeCam.setVehicle(newVehicle);
    }
    // *************************************************************************
    // BaseAppState methods

    @Override
    protected void cleanup(Application app) {
        /*
         * Remove all input mappings/listeners in G_ORBIT.
         */
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_ORBIT:
                    Set<InputMapper.Mapping> mappings
                            = inputMapper.getMappings(function);
                    for (InputMapper.Mapping mp : mappings) {
                        inputMapper.removeMapping(mp);
                    }
                    inputMapper.removeStateListener(this, function);
            }
        }
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map(F_CAMERA_BACK1, KeyInput.KEY_NUMPAD1);
        inputMapper.map(F_CAMERA_DOWN1, KeyInput.KEY_NUMPAD2);
        inputMapper.map(F_CAMERA_DRAG_TO_ORBIT1, Button.MOUSE_BUTTON3);
        inputMapper.map(F_CAMERA_FORWARD1, KeyInput.KEY_NUMPAD7);
        inputMapper.map(F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6);
        inputMapper.map(F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2);
        inputMapper.map(F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5);
        inputMapper.map(F_CAMERA_UP1, KeyInput.KEY_NUMPAD8);
        inputMapper.map(F_CAMERA_ZOOM_IN1, KeyInput.KEY_NUMPAD9);
        inputMapper.map(F_CAMERA_ZOOM_OUT1, KeyInput.KEY_NUMPAD3);

        inputMapper.map(F_RETURN, KeyInput.KEY_ESCAPE);
        // Some Linux window managers block SYSRQ/PrtSc, so we map F12 instead.
        inputMapper.map(F_SCREEN_SHOT, KeyInput.KEY_F12);
        /*
         * Add listeners for all functions in G_ORBIT.
         */
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_ORBIT:
                    inputMapper.addStateListener(this, function);
            }
        }

        Camera cam = app.getCamera();
        MyCamera.setYTangent(cam, 1f);
        resetCameraOffset();
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        activeCam.detach();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        activeCam.attach();
    }

    @Override
    public void update(float tpf) {
        activeCam.update(tpf);
    }
    // *************************************************************************
    // StateFunctionListener methods

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        boolean pressed = (value == InputState.Positive);

        if (func == F_CAMERA_BACK1) {
            signalTracker.setActive(CameraSignal.Back.toString(), 1, pressed);

        } else if (func == F_CAMERA_DOWN1) {
            signalTracker.setActive(CameraSignal.OrbitDown.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_DRAG_TO_ORBIT1) {
            signalTracker.setActive(CameraSignal.DragToOrbit.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_FORWARD1) {
            signalTracker.setActive(CameraSignal.Forward.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_RESET_OFFSET && pressed) {
            resetCameraOffset();

        } else if (func == F_CAMERA_RESET_FOV && pressed) {
            Camera cam = getApplication().getCamera();
            MyCamera.setYTangent(cam, 1f);

        } else if (func == F_CAMERA_UP1) {
            signalTracker.setActive(CameraSignal.OrbitUp.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_ZOOM_IN1) {
            signalTracker.setActive(CameraSignal.ZoomIn.toString(),
                    1, pressed);

        } else if (func == F_CAMERA_ZOOM_OUT1) {
            signalTracker.setActive(CameraSignal.ZoomOut.toString(),
                    1, pressed);

        } else if (func == F_SCREEN_SHOT && pressed) {
            ScreenshotAppState screenshotAppState
                    = Main.findAppState(ScreenshotAppState.class);
            screenshotAppState.takeScreenshot();
        }
    }
    // *************************************************************************
    // private methods

    private void resetCameraOffset() {
        if (activeCam instanceof ChaseCamera) {
            /*
             * Locate the camera 20 wu behind and 5 wu above the target vehicle.
             */
            Vector3f offset = Main.getVehicle().forwardDirection(null);
            offset.multLocal(-20f);
            offset.y += 5f;

            ChaseCamera orbitCam = (ChaseCamera) activeCam;
            orbitCam.setOffset(offset);
        }
    }
}
