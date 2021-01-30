package com.jayfella.jme.vehicle.gui.lemur;

import com.jayfella.jme.vehicle.gui.CartoucheState;
import com.jayfella.jme.vehicle.input.DrivingInputMode;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.simsilica.lemur.event.MouseEventControl;

/**
 * A CartoucheState to display the name of the default camera.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CameraNameState extends CartoucheState {
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled CartoucheState.
     */
    public CameraNameState() {
        super("Camera Name", 0.615f, 0.95f);
    }
    // *************************************************************************
    // CartoucheState methods

    /**
     * Repopulate the Node from scratch.
     *
     * @param text the text to display (may be null)
     */
    @Override
    protected void repopulateNode(String text) {
        super.repopulateNode(text);
        /*
         * Add an Expander to advance to the next camera mode.
         */
        Node node = getNode();
        Expander listener = new Expander(node) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    DrivingInputMode mode = getState(DrivingInputMode.class);
                    if (mode.isEnabled()) {
                        mode.nextCameraMode();
                    }
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        node.addControl(control);
    }

    /**
     * Compare the default camera name to the displayed text and repopulate the
     * Node if they differ.
     */
    @Override
    protected void updateNode() {
        Camera defaultCamera = getApplication().getCamera();
        String name = defaultCamera.getName();
        displayText(name);
    }
}
