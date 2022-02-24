package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.view.CameraMode;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Camera" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class CameraTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CameraTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    CameraTool(GuiScreenController screenController) {
        super(screenController, "camera");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        CameraMode mode = Cameras.getMode();
        String name = mode.toString();
        setButtonText("cameraName", name);
    }
}
