package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.Menus;
import com.jayfella.jme.vehicle.niftydemo.state.PropProposal;
import com.jayfella.jme.vehicle.niftydemo.state.PropType;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.PopupMenuBuilder;

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

        switch (actionString) {
            case Action.selectPropType:
                selectPropType();
                break;

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
        } else if (actionString.startsWith(ActionPrefix.selectPropType)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectPropType);
            handled = selectPropType(arg);
        } else {
            handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Display a menu to set the type of the proposed Prop using the "select
     * propType " action prefix.
     */
    private static void selectPropType() {
        PopupMenuBuilder builder = new PopupMenuBuilder();

        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        PropType selectedType = proposal.type();
        for (PropType type : PropType.values()) {
            if (type != selectedType) {
                String name = type.toString();
                builder.add(name);
            }
        }

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectPropType, builder);
    }

    /**
     * Process a "select propType" action with an argument.
     */
    private static boolean selectPropType(String argument) {
        PropType type = PropType.valueOf(argument);
        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        proposal.setType(type);

        return true;
    }
}
