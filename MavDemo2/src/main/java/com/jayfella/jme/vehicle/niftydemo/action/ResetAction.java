package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "reset".
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ResetAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ResetAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ResetAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "reset".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        DemoState demoState = MavDemo2.getDemoState();

        switch (actionString) {
            case Action.resetElapsedTime:
                demoState.resetElapsedTime();
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
