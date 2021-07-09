package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.Menus;
import java.util.logging.Logger;
import jme3utilities.MyString;

/**
 * Process actions that start with the word "load".
 *
 * @author Stephen Gold sgold@sonic.net
 */
class LoadAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LoadAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private LoadAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "load".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled;

        String arg;
        if (actionString.startsWith(ActionPrefix.loadVehicle)) {
            arg = MyString.remainder(actionString, ActionPrefix.loadVehicle);
            handled = Menus.menuWorld(arg);

        } else if (actionString.startsWith(ActionPrefix.loadWorld)) {
            arg = MyString.remainder(actionString, ActionPrefix.loadWorld);
            handled = Menus.menuWorld(arg);

        } else {
            handled = false;
        }

        return handled;
    }
}
