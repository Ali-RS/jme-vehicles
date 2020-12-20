package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.Main;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.debug.Dumper;
import jme3utilities.minie.PhysicsDumper;

/**
 * A keyboard input state to dump information to the standard output stream, for
 * debugging. Each new instance is enabled by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DumpInputState
        extends BaseAppState
        implements StateFunctionListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DumpInputState.class.getName());

    final private static String G_DUMP = "G_DUMP";
    /**
     * function IDs
     */
    private static final FunctionId F_DUMP_CAMERA
            = new FunctionId(G_DUMP, "Dump Camera");
    private static final FunctionId F_DUMP_PHYSICS
            = new FunctionId(G_DUMP, "Dump Physics");
    private static final FunctionId F_DUMP_VIEWPORT
            = new FunctionId(G_DUMP, "Dump Viewport");
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        /*
         * Remove all input mappings/listeners in G_DUMP.
         */
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_DUMP:
                    Set<InputMapper.Mapping> mappings
                            = inputMapper.getMappings(function);
                    for (InputMapper.Mapping mapping : mappings) {
                        inputMapper.removeMapping(mapping);
                    }
                    inputMapper.removeStateListener(this, function);
            }
        }
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map(F_DUMP_CAMERA, KeyInput.KEY_C);
        inputMapper.map(F_DUMP_PHYSICS, KeyInput.KEY_O);
        inputMapper.map(F_DUMP_VIEWPORT, KeyInput.KEY_P);
        /*
         * Add listeners for all functions in G_DUMP.
         */
        Set<FunctionId> functions = inputMapper.getFunctionIds();
        for (FunctionId function : functions) {
            String group = function.getGroup();
            switch (group) {
                case G_DUMP:
                    inputMapper.addStateListener(this, function);
            }
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup(G_DUMP);
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup(G_DUMP);
    }
    // *************************************************************************
    // StateFunctionListener methods

    @Override
    public void valueChanged(FunctionId function, InputState value,
            double tpf) {
        boolean pressed = (value == InputState.Positive);

        if (function == F_DUMP_CAMERA && pressed) {
            dumpCamera();

        } else if (function == F_DUMP_PHYSICS && pressed) {
            BulletAppState bas = Main.findAppState(BulletAppState.class);
            new PhysicsDumper().dump(bas);
            System.out.println();
            System.out.flush();

        } else if (function == F_DUMP_VIEWPORT && pressed) {
            ViewPort viewPort = getApplication().getViewPort();
            new Dumper().setDumpShadow(true).dump(viewPort);
            System.out.println();
            System.out.flush();
        }
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
