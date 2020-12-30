package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.logging.Logger;

public class VehicleAudioState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(VehicleAudioState.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&arr;sound globally muted, false&rarr;sound enabled
     */
    private static boolean isGloballyMuted = false;

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    public VehicleAudioState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether sound is globally muted.
     *
     * @return true&rarr;muted, false&rarr;enabled
     */
    public static boolean isMuted() {
        return isGloballyMuted;
    }

    /**
     * Toggle the sound between muted and enabled. TODO re-order methods
     */
    public static void toggleMuted() {
        isGloballyMuted = !isGloballyMuted;
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        // do nothing
    }

    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        Sound sound = vehicle.getEngineSound();
        if (sound != null) {
            sound.mute();
        }
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        Sound engineAudio = vehicle.getEngineSound();
        if (engineAudio != null) {
            Engine engine = vehicle.getEngine();
            boolean mute = isGloballyMuted || !engine.isRunning();
            if (mute) {
                engineAudio.mute();
            } else {
                float pitch = engine.getRpm() / 60;
                float volume = 1f; // TODO
                engineAudio.setPitchAndVolume(pitch, volume);
            }
        }
    }
}
