package com.jayfella.jme.vehicle.test;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.system.AppSettings;

import java.util.List;

public class TestJoystickConsole extends SimpleApplication implements RawInputListener {

    public static void main(String... args) {

        TestJoystickConsole testJoystick = new TestJoystickConsole();

        AppSettings appSettings = new AppSettings(true);
        appSettings.setFrameRate(120);
        testJoystick.setSettings(appSettings);

        appSettings.setUseJoysticks(true);

        testJoystick.setShowSettings(false);
        testJoystick.start();
    }

    private float deadzone = 0.1f;

    public TestJoystickConsole() {
        super(new BaseAppState[0]);
    }

    @Override
    public void simpleInitApp() {

        inputManager.addRawInputListener(this);

        for (Joystick joystick : inputManager.getJoysticks()) {

            System.out.println("Name: " + joystick.getName());

            List<JoystickButton> buttons = joystick.getButtons();

            for (JoystickButton button : buttons) {
                System.out.println(button);
            }

            List<JoystickAxis> axes = joystick.getAxes();

            for (JoystickAxis axis : axes) {
                System.out.println(axis);
            }



        }

    }


    @Override
    public void beginInput() {

    }

    @Override
    public void endInput() {

    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {

        if ( ( evt.getValue() != 1 && evt.getValue() != -1 &&  evt.getValue() != 0 ) &&  (evt.getValue() < -deadzone || evt.getValue() > deadzone) ) {
            System.out.println("Joystick Axis: " + evt.getAxis() + " : " + evt.getValue());
        }

    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {
        System.out.println(evt.getButtonIndex());
    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {

    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {

    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {

    }

    @Override
    public void onTouchEvent(TouchEvent evt) {

    }
}
