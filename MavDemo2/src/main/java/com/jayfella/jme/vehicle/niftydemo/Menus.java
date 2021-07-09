package com.jayfella.jme.vehicle.niftydemo;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.vehicles.DuneBuggy;
import com.jayfella.jme.vehicle.examples.vehicles.GTRNismo;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.examples.vehicles.HatchBack;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.vehicles.PickupTruck;
import com.jayfella.jme.vehicle.examples.vehicles.Rotator;
import com.jayfella.jme.vehicle.examples.worlds.EndlessPlain;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.niftydemo.action.ActionPrefix;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.PopupMenuBuilder;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.ui.InputMode;

/**
 * Menus in the main heads-up display (HUD) of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Menus {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Menus.class.getName());
    /**
     * level separator in menu paths
     */
    final static String menuPathSeparator = " -> ";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Menus() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Handle all "select menuItem " actions.
     *
     * @param menuPath
     * @return true if handled, otherwise false
     */
    public static boolean selectMenuItem(String menuPath) {
        boolean handled;
        int separatorBegin = menuPath.indexOf(menuPathSeparator);
        if (separatorBegin == -1) { // top-level menu
            handled = menuBar(menuPath);
        } else { // submenu
            int separatorEnd = separatorBegin + menuPathSeparator.length();
            String menuName = menuPath.substring(0, separatorBegin);
            String remainder = menuPath.substring(separatorEnd);
            handled = menu(menuName, remainder);
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Vehicle menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    public static boolean menuVehicle(String remainder) {
        Vehicle vehicle;
        switch (remainder) {
            case "Dune Buggy":
                vehicle = new DuneBuggy();
                break;

            case "Grand Tourer":
                vehicle = new GrandTourer();
                break;

            case "GTR Nismo":
                vehicle = new GTRNismo();
                break;

            case "Hatchback":
                vehicle = new HatchBack();
                break;

            case "Hovertank":
                vehicle = new HoverTank();
                break;

            case "Pickup Truck":
                vehicle = new PickupTruck();
                break;

            case "Rotator":
                vehicle = new Rotator();
                break;

            default:
                return false;
        }

        MavDemo2.getDemoState().setVehicle(vehicle);
        return true;
    }

    /**
     * Handle a "select menuItem" action from the World menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    public static boolean menuWorld(String remainder) {
        World world;
        switch (remainder) {
            case "Endless Plain":
                world = new EndlessPlain();
                break;

            case "Mountains":
                world = new Mountains();
                break;

            case "Playground":
                world = new Playground();
                break;

            case "Racetrack":
                world = new Racetrack();
                break;

            default:
                return false;
        }

        MavDemo2.getDemoState().setWorld(world);
        return true;
    }
    // *************************************************************************
    // private methods

    /**
     * Build a "Help" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildHelpMenu(PopupMenuBuilder builder) {
        builder.add("About", "Textures/icons/dialog.png");
        builder.add("Attribution", "Textures/icons/dialog.png");
    }

    /**
     * Build a "Props" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildPropsMenu(PopupMenuBuilder builder) {
        builder.add("Add prop", "Textures/icons/submenu.png");
        builder.add("Remove prop", "Textures/icons/submenu.png");
    }

    /**
     * Build a "Settings" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildSettingsMenu(PopupMenuBuilder builder) {
        builder.add("Display", "Textures/icons/dialog.png");
        builder.add("Engine sound", "Textures/icons/submenu.png");
        builder.add("Hotkeys", "Textures/icons/dialog.png");
        builder.add("Sky", "Textures/icons/submenu.png");
        builder.add("Speedometer", "Textures/icons/submenu.png");
        builder.add("Tire smoke", "Textures/icons/submenu.png");
        builder.add("View", "Textures/icons/tool.png");
        builder.add("Wheels", "Textures/icons/submenu.png");
    }

    /**
     * Build a "Tools" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildToolsMenu(PopupMenuBuilder builder) {
        builder.add("Hide all tools");
        builder.add("Show all tools", "Textures/icons/tool.png");
        builder.add("Show the tools tool", "Textures/icons/tool.png");
    }

    /**
     * Build a "Vehicle" pop-up menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildVehicleMenu(PopupMenuBuilder builder) {
        builder.add("Dune Buggy");
        builder.add("Grand Tourer");
        builder.add("GTR Nismo");
        builder.add("Hatchback");
        builder.add("Hovertank");
        builder.add("Pickup Truck");
        builder.add("Rotator");
    }

    /**
     * Build a "World" pop-up menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildWorldMenu(PopupMenuBuilder builder) {
        builder.add("Endless Plain");
        builder.add("Mountains");
        builder.add("Playground");
        builder.add("Racetrack");
    }

    /**
     * Handle a "select menuItem" action for a submenu.
     *
     * @param menuName name of the top-level menu (not null)
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menu(String menuName, String remainder) {
        assert menuName != null;
        assert remainder != null;

        boolean handled;
        switch (menuName) {
            case "Props":
                handled = menuProps(remainder);
                break;

            case "Settings":
                handled = menuSettings(remainder);
                break;

            case "Tools":
                handled = menuTools(remainder);
                break;

            case "Vehicle":
                handled = menuVehicle(remainder);
                break;

            case "World":
                handled = menuWorld(remainder);
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action for a top-level menu, typically from
     * the menu bar.
     *
     * @param menuName name of the menu to open (not null)
     * @return true if handled, otherwise false
     */
    private static boolean menuBar(String menuName) {
        assert menuName != null;
        /**
         * Dynamically generate the menu's list of items.
         */
        PopupMenuBuilder builder = new PopupMenuBuilder();
        switch (menuName) {
            case "Drive":
                // TODO
                break;

            case "Help":
                buildHelpMenu(builder);
                break;

            case "Quit":
                MavDemo2.getApplication().stop();
                return true;

            case "Props":
                buildPropsMenu(builder);
                break;

            case "Settings":
                buildSettingsMenu(builder);
                break;

            case "Tools":
                buildToolsMenu(builder);
                break;

            case "Vehicle":
                buildVehicleMenu(builder);
                break;

            case "World":
                buildWorldMenu(builder);
                break;

            default:
                return false;
        }

        if (builder.isEmpty()) {
            logger.log(Level.WARNING, "no items for the {0} menu",
                    MyString.quote(menuName));
        } else {
            String actionPrefix = ActionPrefix.selectMenuItem + menuName
                    + menuPathSeparator;
            MainHud mainHud = MavDemo2.findAppState(MainHud.class);
            mainHud.showPopupMenu(actionPrefix, builder);
        }

        return true;
    }

    /**
     * Handle a "select menuItem" action from the Help menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuHelp(String remainder) {
        boolean handled = true;
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);

        switch (remainder) {
            case "About":
                // TODO
                break;

            case "Attribution":
                // TODO
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Props menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuProps(String remainder) {
        boolean handled = true;

        switch (remainder) {
            case "Add prop":
                // TODO
                break;

            case "Remove prop":
                // TODO
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Settings menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuSettings(String remainder) {
        boolean handled = true;
        MainHud hud = MavDemo2.findAppState(MainHud.class);

        switch (remainder) {
            case "Display":
                hud.closeAllPopups();
                DsScreen dss = MavDemo2.findAppState(DsScreen.class);
                dss.activate();
                break;

            case "Engine sound":
                // TODO
                break;

            case "Hotkeys":
                hud.closeAllPopups();
                BindScreen bindScreen = MavDemo2.findAppState(BindScreen.class);
                InputMode current = InputMode.getActiveMode();
                bindScreen.activate(current);
                break;

            case "Sky":
                // TODO
                break;

            case "Speedometer":
                // TODO
                break;

            case "Tire smoke":
                // TODO
                break;

            case "View":
                hud.tools.select("view");
                break;

            case "Wheels":
                // TODO
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Tools menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuTools(String remainder) {
        boolean handled = true;
        MainHud hud = MavDemo2.findAppState(MainHud.class);

        switch (remainder) {
            case "Hide all tools":
                // TODO
                break;

            case "Show all tools":
                // TODO
                break;

            case "Show the tools tool":
                hud.tools.select("tools");
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
