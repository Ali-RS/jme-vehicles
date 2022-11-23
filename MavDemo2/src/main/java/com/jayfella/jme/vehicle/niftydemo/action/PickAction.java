package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "pick".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class PickAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PickAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private PickAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "pick".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.pickProp:
                Prop picked = DemoState.pickProp();
                MavDemo2.getDemoState().selectProp(picked);
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
