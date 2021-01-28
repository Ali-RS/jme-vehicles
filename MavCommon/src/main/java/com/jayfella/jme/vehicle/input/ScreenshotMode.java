package com.jayfella.jme.vehicle.input;

import com.jme3.app.Application;
import com.jme3.app.state.ScreenshotAppState;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;

/**
 * An InputMode to capture screenshots.
 */
public class ScreenshotMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_SCREEN_SHOT
            = new FunctionId("Capture ScreenShot");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(ScreenshotMode.class.getName());
    // *************************************************************************
    // fields

    /**
     * AppState to perform the capture
     */
    final private ScreenshotAppState screenshotAppState;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to capture screenshots.
     */
    public ScreenshotMode() {
        super("Screenshot Mode", F_SCREEN_SHOT);

        String directory = "./";
        String filenamePrefix = "screen_shot";
        screenshotAppState = new ScreenshotAppState(directory, filenamePrefix);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                screenshotAppState.takeScreenshot();
            }
        }, F_SCREEN_SHOT);
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        super.initialize(application);
        application.getStateManager().detach(screenshotAppState);
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        super.initialize(application);
        application.getStateManager().attach(screenshotAppState);
    }
}
