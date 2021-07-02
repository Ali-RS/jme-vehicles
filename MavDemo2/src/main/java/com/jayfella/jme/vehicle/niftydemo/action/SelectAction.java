package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.menu.Menus;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.logging.Logger;
import jme3utilities.MyString;

/**
 * Process actions that start with the word "select".
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SelectAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SelectAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SelectAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "select".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        DemoState demoState = MavDemo2.getDemoState();

        switch (actionString) {
            default:
                handled = false;
        }
        if (handled) {
            return true;
        }
        handled = true;

        String arg;
        if (actionString.startsWith(ActionPrefix.selectMenuItem)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectMenuItem);
            handled = Menus.selectMenuItem(arg);
        } else {
            handled = false;
        }

        return handled;
    }
}
