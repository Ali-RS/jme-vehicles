package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.view.CcFunctions;
import com.jayfella.jme.vehicle.view.OrbitCamera;
import com.jayfella.jme.vehicle.view.VehicleCamera;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
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
import jme3utilities.debug.Dumper;
import jme3utilities.minie.FilterAll;
import jme3utilities.minie.PhysicsDumper;

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
    private static final FunctionId F_CAMERA_RESET_FOV
            = new FunctionId(G_ORBIT, "Camera Reset FOV");
    private static final FunctionId F_CAMERA_ZOOM_IN1
            = new FunctionId(G_ORBIT, CcFunctions.ZoomIn.toString());
    private static final FunctionId F_CAMERA_ZOOM_OUT1
            = new FunctionId(G_ORBIT, CcFunctions.ZoomOut.toString());
    private static final FunctionId F_DUMP_CAMERA
            = new FunctionId(G_ORBIT, "Dump Camera");
    private static final FunctionId F_DUMP_PHYSICS
            = new FunctionId(G_ORBIT, "Dump Physics");
    private static final FunctionId F_DUMP_VIEWPORT
            = new FunctionId(G_ORBIT, "Dump Viewport");
    private static final FunctionId F_RETURN
            = new FunctionId(G_ORBIT, "Return to Main Menu");
    private static final FunctionId F_SCREEN_SHOT
            = new FunctionId(G_ORBIT, "ScreenShot");
    private static final FunctionId F_CAMERA_BACK1
            = new FunctionId(G_ORBIT, CcFunctions.Back.toString());
    private static final FunctionId F_CAMERA_DOWN1
            = new FunctionId(G_ORBIT, CcFunctions.OrbitDown.toString());
    private static final FunctionId F_CAMERA_DRAG_TO_ORBIT1
            = new FunctionId(G_ORBIT, CcFunctions.DragToOrbit.toString());
    private static final FunctionId F_CAMERA_FORWARD1
            = new FunctionId(G_ORBIT, CcFunctions.Forward.toString());
    private static final FunctionId F_CAMERA_RESET_OFFSET
            = new FunctionId(G_ORBIT, "Camera Reset Offset");
    private static final FunctionId F_CAMERA_UP1
            = new FunctionId(G_ORBIT, CcFunctions.OrbitUp.toString());
    // *************************************************************************
    // fields

    private InputMapper inputMapper;
    final private SignalTracker signalTracker;
    final private VehicleCamera activeCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an input state to orbit the selected Vehicle.
     *
     * @param vehicle the Vehicle to orbit (not null)
     */
    public NonDrivingInputState() {
        Camera cam = Main.getApplication().getCamera();

        signalTracker = new SignalTracker();
        for (CcFunctions function : CcFunctions.values()) {
            String signalName = function.toString();
            signalTracker.add(signalName);
        }

        FilterAll filter = new FilterAll(true);
        activeCam = new OrbitCamera(cam, signalTracker, filter);
        for (CcFunctions function : CcFunctions.values()) {
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
     */
    public void setVehicle(Vehicle newVehicle) {
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

        inputMapper.map(F_DUMP_CAMERA, KeyInput.KEY_C);
        inputMapper.map(F_DUMP_PHYSICS, KeyInput.KEY_O);
        inputMapper.map(F_DUMP_VIEWPORT, KeyInput.KEY_P);

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
            signalTracker.setActive(CcFunctions.Back.toString(), 1, pressed);

        } else if (func == F_CAMERA_DOWN1) {
            signalTracker.setActive(
                    CcFunctions.OrbitDown.toString(), 1, pressed);

        } else if (func == F_CAMERA_DRAG_TO_ORBIT1) {
            signalTracker.setActive(
                    CcFunctions.DragToOrbit.toString(), 1, pressed);

        } else if (func == F_CAMERA_FORWARD1) {
            signalTracker.setActive(CcFunctions.Forward.toString(), 1, pressed);

        } else if (func == F_CAMERA_RESET_OFFSET && pressed) {
            resetCameraOffset();

        } else if (func == F_CAMERA_RESET_FOV && pressed) {
            Camera cam = getApplication().getCamera();
            MyCamera.setYTangent(cam, 1f);

        } else if (func == F_CAMERA_UP1) {
            signalTracker.setActive(CcFunctions.OrbitUp.toString(), 1, pressed);

        } else if (func == F_CAMERA_ZOOM_IN1) {
            signalTracker.setActive(CcFunctions.ZoomIn.toString(), 1, pressed);

        } else if (func == F_CAMERA_ZOOM_OUT1) {
            signalTracker.setActive(CcFunctions.ZoomOut.toString(), 1, pressed);

        } else if (func == F_DUMP_CAMERA && pressed) {
            dumpCamera();

        } else if (func == F_DUMP_PHYSICS && pressed) {
            BulletAppState bas = Main.findAppState(BulletAppState.class);
            new PhysicsDumper().dump(bas);

        } else if (func == F_DUMP_VIEWPORT && pressed) {
            ViewPort vp = getApplication().getViewPort();
            new Dumper().setDumpShadow(true).dump(vp);

        } else if (func == F_SCREEN_SHOT && pressed) {
            ScreenshotAppState screenshotAppState
                    = Main.findAppState(ScreenshotAppState.class);
            screenshotAppState.takeScreenshot();
        }
    }
    // *************************************************************************
    // private methods

    private void dumpCamera() {
        Camera camera = getApplication().getCamera();
        String desc1 = MyCamera.describe(camera);
        System.out.println(desc1);

        String desc2 = MyCamera.describeMore(camera);
        System.out.println(desc2);

        float degrees = MyCamera.yDegrees(camera);
        System.out.printf("fovY=%.1f deg%n", degrees);
    }

    private void resetCameraOffset() {
        if (activeCam instanceof OrbitCamera) {
            /*
             * Locate the camera 20 wu behind and 5 wu above the target vehicle.
             */
            Vector3f offset = Main.getVehicle().forwardDirection(null);
            offset.multLocal(-20f);
            offset.y += 5f;

            OrbitCamera orbitCam = (OrbitCamera) activeCam;
            orbitCam.setOffset(offset);
        }
    }
}
