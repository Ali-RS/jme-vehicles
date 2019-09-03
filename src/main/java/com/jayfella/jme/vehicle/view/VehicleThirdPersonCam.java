package com.jayfella.jme.vehicle.view;

import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;

public class VehicleThirdPersonCam implements VehicleCamera, AnalogFunctionListener {

    private static final String G_FP_VEHICLE = "First Person Vehicle Camera";

    private static final FunctionId F_ZOOM      = new FunctionId(G_FP_VEHICLE, "Camera Zoom");
    private static final FunctionId F_HEIGHT    = new FunctionId(G_FP_VEHICLE, "Camera Height");
    private static final FunctionId F_SIDE      = new FunctionId(G_FP_VEHICLE, "Camera Size");

    private final Vehicle vehicle;
    private final Camera camera;

    private float zoomLevel;
    private float zoomStep = 0.5f;
    private float zoomLevelMin = 10;
    private float zoomLevelMax = 20;

    private float camHeight;
    private float camHeightMin = 3;
    private float camHeightMax = 20;

    private float camSide = 0;
    private float camSideMin = -10;
    private float camSideMax = 10;

    public VehicleThirdPersonCam(Vehicle vehicle, Camera camera) {
        this.vehicle = vehicle;
        this.camera = camera;

        this.zoomLevel = zoomLevelMin;
        this.camHeight = camHeightMin;
    }


    @Override
    public void enableInputMappings() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.map( F_ZOOM, Axis.MOUSE_WHEEL );
        inputMapper.map( F_HEIGHT, Axis.MOUSE_Y );
        inputMapper.map( F_SIDE, Axis.MOUSE_X );

        inputMapper.addAnalogListener(this, F_ZOOM, F_HEIGHT, F_SIDE);

        inputMapper.activateGroup(G_FP_VEHICLE);
    }

    @Override
    public void disableInputMappings() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        inputMapper.deactivateGroup(G_FP_VEHICLE);

        inputMapper.removeMapping( F_ZOOM, Axis.MOUSE_WHEEL );
        inputMapper.removeMapping( F_HEIGHT, Axis.MOUSE_Y );
        inputMapper.removeMapping( F_SIDE, Axis.MOUSE_X );

        inputMapper.removeAnalogListener(this, F_ZOOM, F_HEIGHT, F_SIDE);
    }

    @Override
    public void attach() {

        enableInputMappings();
    }

    @Override
    public void detach() {

        disableInputMappings();
    }

    @Override
    public void update(float tpf) {

        Vector3f vehicleLoc = vehicle.getNode().getLocalTranslation();

        Quaternion side = new Quaternion().fromAngles(new float[] { 0, FastMath.HALF_PI, 0 });

        Vector3f vehicleDir = vehicle.getNode().getLocalRotation()
                //.mult(side)
                .getRotationColumn(2);


        Vector3f camLoc = vehicleLoc
                .add(vehicleDir
                    .negate()
                    .multLocal(zoomLevel))
                .add(camSide, camHeight, 0);

        camera.lookAt(vehicleLoc, Vector3f.UNIT_Y);

        camera.setLocation(camLoc);

    }

    @Override
    public void valueActive(FunctionId func, double value, double tpf) {

        if (func == F_ZOOM) {

            if (value > 0) {
                zoomLevel = Math.max(zoomLevelMin, zoomLevel - zoomStep);
            }
            else {
                zoomLevel = Math.min(zoomLevelMax, zoomLevel + zoomStep);
            }

        }

        /*
        else if (func == F_HEIGHT) {

            float amount = (float) (tpf * 10);

            if (value > 0) {
                camHeight = Math.min(camHeightMax, camHeight + amount);
            }
            else {
                camHeight = Math.max(camHeightMin, camHeight - amount);
            }
        }


        else if (func == F_SIDE) {

            float amount = (float) (tpf * 100);

            if (value > 0) {
                camSide = Math.min(camSideMax, camSide + amount);
            }
            else {
                camSide = Math.max(camSideMin, camSide - amount);
            }

        }
        */

    }
}
