package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.Vehicles;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jayfella.jme.vehicle.niftydemo.view.ViewFlags;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import java.awt.DisplayMode;
import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.DsUtils;

/**
 * Process actions that start with the word "toggle".
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ToggleAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ToggleAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ToggleAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "toggle".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        DemoState demoState = MavDemo2.getDemoState();
        Vehicles vehicles = demoState.getVehicles();
        Vehicle vehicle = vehicles.getSelected();

        switch (actionString) {
            case Action.toggleEngine:
                Engine engine = vehicle.getEngine();
                boolean wasRunning = engine.isRunning();
                engine.setRunning(!wasRunning);
                break;

            case Action.togglePause:
                togglePause();
                break;

            case Action.togglePhysicsDebug:
                togglePhysicsDebug();
                break;

            case Action.toggleRecorder:
                toggleRecorder();
                break;

            case Action.toggleReverse:
                GearBox box = vehicle.getGearBox();
                boolean wasReversing = box.isInReverse();
                box.setReversing(!wasReversing);
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Generate a timestamp.
     *
     * @return the timestamp value
     */
    private static String hhmmss() {
        Calendar rightNow = Calendar.getInstance();
        int hours = rightNow.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        String result = String.format("%02d%02d%02d", hours, minutes, seconds);

        return result;
    }

    /**
     * Process a "toggle pause" action.
     */
    private static void togglePause() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);

        float speed = bas.getSpeed();
        if (speed > 0f) { // was running
            bas.setSpeed(0f);
        } else {
            bas.setSpeed(1f);
        }
    }

    /**
     * Process a "toggle physics" action.
     */
    private static void togglePhysicsDebug() {
        View view = MavDemo2.findAppState(View.class);

        boolean wasEnabled = view.isEnabled(ViewFlags.PhysicsJoints)
                || view.isEnabled(ViewFlags.PropShapes)
                || view.isEnabled(ViewFlags.VehicleShapes)
                || view.isEnabled(ViewFlags.WorldShapes);
        boolean enable = !wasEnabled;

        view.setEnabled(ViewFlags.PhysicsJoints, enable);
        view.setEnabled(ViewFlags.PropShapes, enable);
        view.setEnabled(ViewFlags.VehicleShapes, enable);
        view.setEnabled(ViewFlags.WorldShapes, enable);
    }

    /**
     * Process a "toggle recorder" action.
     */
    private static void toggleRecorder() {
        MavDemo2 app = MavDemo2.getApplication();
        AppStateManager manager = app.getStateManager();
        VideoRecorderAppState vras
                = manager.getState(VideoRecorderAppState.class);

        if (vras == null) {
            String hhmmss = hhmmss();
            String fileName = String.format("recording-%s.avi", hhmmss);
            String path = ActionApplication.filePath(fileName);
            File file = new File(path);

            DisplayMode mode = DsUtils.displayMode();
            int frameRate = mode.getRefreshRate();
            assert frameRate > 0 : frameRate;

            float quality = 0.5f;
            VideoRecorderAppState recorder
                    = new VideoRecorderAppState(file, quality, frameRate);
            manager.attach(recorder);

            String quotedPath = MyString.quote(file.getAbsolutePath());
            logger.log(Level.WARNING, "Began recording to {0}", quotedPath);

        } else {
            File file = vras.getFile();
            manager.detach(vras);

            String quotedPath = MyString.quote(file.getAbsolutePath());
            logger.log(Level.WARNING, "Stopped recording to {0}", quotedPath);
        }
    }
}
