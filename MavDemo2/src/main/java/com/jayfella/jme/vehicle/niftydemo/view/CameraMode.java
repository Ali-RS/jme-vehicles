package com.jayfella.jme.vehicle.niftydemo.view;

/**
 * Enumerate the camera-control modes in MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum CameraMode {
    /**
     * OrbitCamera with the StrictFollow option
     */
    Chase,
    /**
     * forward-facing CameraNode
     */
    Dash,
    /**
     * DynamicCamera
     */
    Dynamic,
    /**
     * FlyByCamera
     */
    Fly,
    /**
     * OrbitCamera with the FreeOrbit option
     */
    Orbit,
    /**
     * PlanCamera
     */
    Plan
}
