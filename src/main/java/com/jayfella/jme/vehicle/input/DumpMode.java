package com.jayfella.jme.vehicle.input;

import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.minie.PhysicsDumper;

/**
 * An InputMode to dump information to the standard output stream, for
 * debugging. Each new instance is disabled by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DumpMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_DUMP_APPSTATES
            = new FunctionId("Dump AppStates");
    final public static FunctionId F_DUMP_CAMERA
            = new FunctionId("Dump Camera");
    final public static FunctionId F_DUMP_GUI_VIEWPORT
            = new FunctionId("Dump GUI Viewport");
    final public static FunctionId F_DUMP_PHYSICS
            = new FunctionId("Dump Physics");
    final public static FunctionId F_DUMP_RENDER_MANAGER
            = new FunctionId("Dump Render Manager");
    final public static FunctionId F_DUMP_VIEWPORT
            = new FunctionId("Dump Viewport");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(DumpMode.class.getName());
    // *************************************************************************
    // fields

    /**
     * dump debugging information to System.out
     */
    final private static PhysicsDumper dumper = new PhysicsDumper();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode.
     */
    public DumpMode() {
        super("Dump Mode", F_DUMP_APPSTATES, F_DUMP_CAMERA, F_DUMP_GUI_VIEWPORT,
                F_DUMP_PHYSICS, F_DUMP_RENDER_MANAGER, F_DUMP_VIEWPORT);

        dumper.setDumpBucket(true)
                .setDumpCull(true)
                .setDumpShadow(true)
                .setDumpTransform(true);
//        dumper.setDumpMatParam(true);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                AppStateManager stateManager
                        = getApplication().getStateManager();
                dumper.dump(stateManager);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_APPSTATES);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                Camera camera = getApplication().getCamera();
                dumper.dump(camera);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_CAMERA);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                ViewPort guiViewPort = getApplication().getGuiViewPort();
                dumper.dump(guiViewPort);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_VIEWPORT);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                BulletAppState bas = getState(BulletAppState.class);
                dumper.dump(bas);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_PHYSICS);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                RenderManager renderManager
                        = getApplication().getRenderManager();
                dumper.dump(renderManager);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_RENDER_MANAGER);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                ViewPort viewPort = getApplication().getViewPort();
                dumper.dump(viewPort);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_VIEWPORT);
    }
}
