package com.jayfella.jme.vehicle.input;

import com.jayfella.jme.vehicle.view.CameraSignal;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.SignalTracker;

/**
 * An InputMode to manage a SignalTracker.
 */
public class SignalMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_CAMERA_BACK1
            = new FunctionId(CameraSignal.Back.toString());
    final public static FunctionId F_CAMERA_CCW1
            = new FunctionId(CameraSignal.OrbitCcw.toString());
    final public static FunctionId F_CAMERA_CW1
            = new FunctionId(CameraSignal.OrbitCw.toString());
    final public static FunctionId F_CAMERA_DOWN1
            = new FunctionId(CameraSignal.OrbitDown.toString());
    final public static FunctionId F_CAMERA_DRAG_TO_ORBIT1
            = new FunctionId(CameraSignal.DragToOrbit.toString());
    final public static FunctionId F_CAMERA_FORWARD1
            = new FunctionId(CameraSignal.Forward.toString());
    final public static FunctionId F_CAMERA_UP1
            = new FunctionId(CameraSignal.OrbitUp.toString());
    final public static FunctionId F_CAMERA_XRAY1
            = new FunctionId(CameraSignal.Xray.toString());
    final public static FunctionId F_CAMERA_ZOOM_IN1
            = new FunctionId(CameraSignal.ZoomIn.toString());
    final public static FunctionId F_CAMERA_ZOOM_OUT1
            = new FunctionId(CameraSignal.ZoomOut.toString());
    final public static FunctionId F_HORN1
            = new FunctionId("Sound Horn");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(SignalMode.class.getName());
    // *************************************************************************
    // fields

    /**
     * track the signal states
     */
    final private SignalTracker signalTracker = new SignalTracker();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to manage a SignalTracker.
     */
    public SignalMode() {
        super("Signal Mode", F_CAMERA_BACK1, F_CAMERA_CCW1, F_CAMERA_CW1,
                F_CAMERA_DOWN1, F_CAMERA_DRAG_TO_ORBIT1, F_CAMERA_FORWARD1,
                F_CAMERA_UP1, F_CAMERA_XRAY1, F_CAMERA_ZOOM_IN1,
                F_CAMERA_ZOOM_OUT1, F_HORN1);

        for (CameraSignal function : CameraSignal.values()) {
            String signalName = function.toString();
            signalTracker.add(signalName);
        }
        signalTracker.add(F_HORN1.getId());

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                signalTracker.setActive(function.getId(), 1, true);
            } else {
                signalTracker.setActive(function.getId(), 1, false);
            }
        }, F_CAMERA_BACK1, F_CAMERA_CCW1, F_CAMERA_CW1, F_CAMERA_DOWN1,
                F_CAMERA_DRAG_TO_ORBIT1, F_CAMERA_FORWARD1, F_CAMERA_UP1,
                F_CAMERA_XRAY1, F_CAMERA_ZOOM_IN1, F_CAMERA_ZOOM_OUT1, F_HORN1);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the SignalTracker.
     *
     * @return the pre-existing instance (not null)
     */
    public SignalTracker getSignalTracker() {
        return signalTracker;
    }
}
