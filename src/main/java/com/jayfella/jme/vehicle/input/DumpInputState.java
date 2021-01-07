package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.minie.PhysicsDumper;

/**
 * An InputMode to dump information to the standard output stream, for
 * debugging. Each new instance is disabled by default. TODO rename DumpMode
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DumpInputState extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_DUMP_CAMERA
            = new FunctionId("Dump Camera");
    final public static FunctionId F_DUMP_PHYSICS
            = new FunctionId("Dump Physics");
    final public static FunctionId F_DUMP_VIEWPORT
            = new FunctionId("Dump Viewport");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(DumpInputState.class.getName());
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
    public DumpInputState() {
        super("Dump Mode", F_DUMP_CAMERA, F_DUMP_PHYSICS, F_DUMP_VIEWPORT);

        dumper.setDumpShadow(true)
                .setDumpTransform(true);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                dumpCamera();
            }
        }, F_DUMP_CAMERA);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                BulletAppState bas
                        = Main.findAppState(BulletAppState.class);
                dumper.dump(bas);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_PHYSICS);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                ViewPort viewPort = getApplication().getViewPort();
                dumper.dump(viewPort);
                System.out.println();
                System.out.flush();
            }
        }, F_DUMP_VIEWPORT);
    }
    // *************************************************************************
    // private methods

    /**
     * Dump a camera description to the standard output stream.
     */
    private void dumpCamera() {
        Camera camera = getApplication().getCamera();
        String description = MyCamera.describe(camera);
        System.out.println(description);

        description = MyCamera.describeMore(camera);
        System.out.println(description);

        float degrees = MyCamera.yDegrees(camera);
        System.out.printf("fovY=%.1f deg%n%n", degrees);
        System.out.flush();
    }
}
