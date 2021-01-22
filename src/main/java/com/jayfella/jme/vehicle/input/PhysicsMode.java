package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.gui.PhysicsHud;
import com.jme3.bullet.BulletAppState;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;

/**
 * An InputMode to control the physics simulation.
 */
public class PhysicsMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_PAUSE
            = new FunctionId("Pause Simulation");
    final public static FunctionId F_SINGLE_STEP
            = new FunctionId("Single-step Simulation");
    final public static FunctionId F_TOGGLE_PHYSICS_DEBUG
            = new FunctionId("Toggle Physics Debug");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(PhysicsMode.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to control the physics simulation.
     */
    public PhysicsMode() {
        super("Physics Mode", F_PAUSE, F_SINGLE_STEP, F_TOGGLE_PHYSICS_DEBUG);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                getState(PhysicsHud.class).togglePhysicsPaused();
            }
        }, F_PAUSE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                getState(PhysicsHud.class).singleStepPhysics();
            }
        }, F_SINGLE_STEP);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                BulletAppState bas = getState(BulletAppState.class);
                boolean wasEnabled = bas.isDebugEnabled();
                bas.setDebugEnabled(!wasEnabled);
            }
        }, F_TOGGLE_PHYSICS_DEBUG);
    }
}
