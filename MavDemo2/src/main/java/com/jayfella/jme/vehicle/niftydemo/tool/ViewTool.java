package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.opengl.GLRenderer;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "View" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ViewTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ViewTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    ViewTool(GuiScreenController screenController) {
        super(screenController, "view");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's check boxes.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listCheckBoxes() {
        List<String> result = super.listCheckBoxes();
        result.add("viewActorPhysics");
        result.add("viewActorSpheres");
        result.add("viewActorTargets");
        result.add("viewBlockPhysics");
        result.add("viewCursor");
        result.add("viewFloor");
        result.add("viewParticles");
        result.add("viewParticlePhysics");
        result.add("viewPhysicsJoints");
        result.add("viewPropPhysics");
        result.add("viewPropSpheres");
        result.add("viewPropTargets");
        result.add("viewShadows");
        result.add("viewSky");
        result.add("viewTerrainPhysics");
        result.add("viewWater");

        return result;
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        View view = MavDemo2.findAppState(View.class);

        Renderer renderer = MavDemo2.getApplication().getRenderer();
        GLRenderer glRenderer = (GLRenderer) renderer;
        int degree = glRenderer.getDefaultAnisotropicFilter();
        String anisoStatus = Integer.toString(degree);
        setButtonText("viewDefaultAniso", anisoStatus);

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        float axesLength = bas.debugAxisLength();
        String axesLengthStatus = String.format("%.0f cm",
                100f * axesLength);
        setButtonText("viewPhysicsAxes", axesLengthStatus);
    }
}
