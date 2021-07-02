package com.jayfella.jme.vehicle.niftydemo.action;

import java.util.logging.Logger;

/**
 * Action-string prefixes for MavDemo2's "main" screen. Each prefix describes a
 * user-interface action requiring one or more (textual) arguments. By
 * convention, action prefixes end with a space (' ').
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ActionPrefix {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ActionPrefix.class.getName());
    /**
     * argument is the menu path of a menu item
     */
    final public static String selectMenuItem = "select menuItem ";
    /**
     * argument is a PropCgm name
     */
    final static String selectPropType = "select propType ";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ActionPrefix() {
    }
}
