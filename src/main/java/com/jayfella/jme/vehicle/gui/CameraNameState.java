package com.jayfella.jme.vehicle.gui;

import com.jme3.renderer.Camera;

/**
 * A CartoucheState to display the name of the default camera.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CameraNameState extends CartoucheState {
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState.
     */
    public CameraNameState() {
        super("Camera Name", 0.62f, 0.95f);
    }
    // *************************************************************************
    // CartoucheState methods

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
