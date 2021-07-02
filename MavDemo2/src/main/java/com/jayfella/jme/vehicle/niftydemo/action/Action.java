package com.jayfella.jme.vehicle.niftydemo.action;

import java.util.logging.Logger;
import jme3utilities.ui.InputMode;

/**
 * Action strings for MavDemo2's "main" screen. Each String describes a
 * user-interface action. Each String defined here should appear somewhere in
 * "main.xml" and/or "default.properties". By convention, action strings begin
 * with a verb in all lowercase and never end with a space (' ').
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Action {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Action.class.getName());

    final static String deleteAllProps = "delete allProps";
    final static String deleteProp = "delete prop";

    final static String dumpAppStates = "dump appStates";
    final static String dumpPhysicsSpace = "dump physicsSpace";
    final static String dumpProp = "dump prop";
    final static String dumpPropNode = "dump propNode";
    final static String dumpRenderer = "dump renderer";
    final static String dumpRootNode = "dump rootNode";
    final static String dumpViewPort = "dump viewPort";

    final static String editBindings = "edit bindings";
    final static String editDisplaySettings = "edit displaySettings";

    final static String newProp = "new prop";

    final static String nextPerformanceMode = "next performanceMode";
    final static String nextTimeStep = "next timeStep";

    final static String pickAny = "pick any";

    final static String resetElapsedTime = "reset elapsedTime";

    final static String selectProp = "select prop";
    final static String selectPropType = "select propType";

    final static String setBlinkRate = "set blinkRate";
    final static String setDefaultAniso = "set defaultAniso";
    final static String setDumpIndentSpaces = "set dumpIndentSpaces";
    final static String setDumpMaxChildren = "set dumpMaxChildren";
    final static String setPhysicsAxes = "set physicsAxes";
    final static String setPhysicsIterations = "set physicsIterations";
    final static String setPhysicsMargin = "set physicsMargin";
    final static String setPhysicsSpeed = "set physicsSpeed";
    final static String setPhysicsTimeStep = "set physicsTimeStep";

    final static String toggleCamera = "toggle camera";
    final static String togglePause = "toggle pause";
    final static String togglePhysicsDebug = "toggle physicsDebug";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Action() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add actions to the specified InputMode without binding them.
     *
     * @param m the InputMode to modify (not null)
     */
    public static void addUnbound(InputMode m) {
        m.addActionName(dumpAppStates);
        m.addActionName(dumpPhysicsSpace);
        m.addActionName(dumpRenderer);
        m.addActionName(editBindings);
        m.addActionName(editDisplaySettings);
        m.addActionName(nextPerformanceMode);
        m.addActionName(pickAny);
        m.addActionName(togglePause);
        m.addActionName(togglePhysicsDebug);
        // TODO signals
    }

    /**
     * Process a non-ongoing action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action has been handled, otherwise false
     */
    public static boolean processNotOngoing(String actionString) {
        boolean handled = false;
        String[] words = actionString.split(" ");
        String firstWord = words[0];
        switch (firstWord) {
            case "new":
//                handled = NewAction.processNotOngoing(actionString);
                break;
        }

        return handled;
    }

    /**
     * Process an ongoing action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action has been handled, otherwise false
     */
    public static boolean processOngoing(String actionString) {
        boolean handled = false;
        String[] words = actionString.split(" ");
        String firstWord = words[0];
        switch (firstWord) {
            case "delete":
//                handled = DeleteAction.processOngoing(actionString);
                break;

            case "dump":
//                handled = DumpAction.processOngoing(actionString);
                break;

            case "edit":
//                handled = EditAction.processOngoing(actionString);
                break;

            case "load":
//                handled = LoadAction.processOngoing(actionString);
                break;

            case "new":
//                handled = NewAction.processOngoing(actionString);
                break;

            case "next":
//                handled = NextAction.processOngoing(actionString);
                break;

            case "pick":
//                handled = PickAction.processOngoing(actionString);
                break;

            case "previous":
//                handled = PreviousAction.processOngoing(actionString);
                break;

            case "reset":
//                handled = ResetAction.processOngoing(actionString);
                break;

            case "save":
//                handled = SaveAction.processOngoing(actionString);
                break;

            case "select":
                handled = SelectAction.processOngoing(actionString);
                break;

            case "set":
//                handled = SetAction.processOngoing(actionString);
                break;

            case "toggle":
//                handled = ToggleAction.processOngoing(actionString);
                break;
        }

        return handled;
    }
}
