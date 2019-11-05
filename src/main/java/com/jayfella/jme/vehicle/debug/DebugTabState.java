package com.jayfella.jme.vehicle.debug;

import com.jayfella.jme.vehicle.MagicFormulaState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.props.PropertyPanel;

public class DebugTabState extends BaseAppState {

    public DebugTabState() {

    }

    @Override
    protected void initialize(Application app) {

        VehicleEditorState editorState = getState(VehicleEditorState.class);
        TabbedPanel tabbedPanel = editorState.getTabbedPanel();

        Container container = new Container();

        EnginePowerGraphState enginePowerGraphState = getState(EnginePowerGraphState.class);
        TyreDataState tyreDataState = getState(TyreDataState.class);

        MagicFormulaState magicFormulaState = getState(MagicFormulaState.class);

        PropertyPanel propertyPanel = container.addChild(new PropertyPanel("glass"));
        propertyPanel.addBooleanProperty("Engine Graph", enginePowerGraphState, "enabled");
        propertyPanel.addBooleanProperty("Tyre Graph", tyreDataState, "enabled");
        propertyPanel.addBooleanProperty("Tyre Data", magicFormulaState, "vehicleDataEnabled");
        propertyPanel.addBooleanProperty("Center of Gravity", magicFormulaState, "centerOfGravityEnabled");



        tabbedPanel.addTab("Debug", container);

        tabbedPanel.setLocalTranslation(
                app.getCamera().getWidth() - 420,
                app.getCamera().getHeight() - 20,
                0
        );
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
