package com.jayfella.jme.vehicle.view;

import com.github.stephengold.garrett.OrbitCamera;
import com.jme3.math.FastMath;

/**
 * Enumerate some configuration options for an OrbitCamera.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum ChaseOption {
    // *************************************************************************
    // values

    /**
     * Ignore the target's forward direction.
     *
     * Freely orbit the target, including left/right, based on user input.
     *
     * If the target moves, maintain a constant offset from it.
     */
    FreeOrbit(Float.POSITIVE_INFINITY, 0f, "orbit camera"),
    /**
     * Stay directly behind the target.
     *
     * Orbit the target based on user input, but only up/down.
     *
     * If the target moves, maintain a constant distance and elevation from it.
     */
    StrictFollow(0f, 0f, "chase camera"),
    /**
     * Stay directly ahead of the target.
     *
     * Orbit the target based on user input, but only up/down.
     *
     * If the target moves, maintain a constant distance and elevation from it.
     */
    StrictLead(0f, FastMath.PI, "lead camera"),
    /**
     * Stay precisely on the target's left flank.
     *
     * Orbit the target based on user input, but only up/down.
     *
     * If the target moves, maintain a constant distance and elevation from it.
     */
    StrictLeft(0f, -FastMath.HALF_PI, "left camera"),
    /**
     * Stay precisely on the target's right flank.
     *
     * Orbit the target based on user input, but only up/down.
     *
     * If the target moves, maintain a constant distance and elevation from it.
     */
    StrictRight(0f, FastMath.HALF_PI, "right camera");
    // *************************************************************************
    // fields

    /**
     * time constant for horizontal rotation (in seconds, 0 &rarr; locked on
     * deltaAzimuthSetpoint, +Infinity &rarr; free horizontal rotation)
     */
    final private float azimuthTau;
    /**
     * setpoint for the azimuth difference between the Camera and the Target (in
     * radians, 0 &rarr;camera following target, Pi/2 &rarr; camera on target's
     * right flank)
     */
    final private float deltaAzimuth;
    /**
     * name to apply to the Camera
     */
    final private String cameraName;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a ChaseOption.
     *
     * @param timeConstant the desired time constant (in seconds)
     * @param deltaAzimuth the desired azimuth difference (in radians)
     * @param cameraName the desired camera name
     */
    private ChaseOption(float timeConstant, float deltaAzimuth,
            String cameraName) {
        this.azimuthTau = timeConstant;
        this.deltaAzimuth = deltaAzimuth;
        this.cameraName = cameraName;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Configure the specified camera controller.
     * 
     * @param cameraController the controller to configure (not null)
     */
    public void configure(OrbitCamera cameraController) {
        cameraController.setAzimuthTau(azimuthTau);
        cameraController.setCameraName(cameraName);
        cameraController.setDeltaAzimuth(deltaAzimuth);
    }
}
