package com.jayfella.jme.vehicle.tire;

import com.jme3.math.FastMath;

// https://github.com/chrisoco/M120/blob/master/RaceCar/RCAS/src/rcas/model/MagicFormulaTireModel.java

public class PajeckaTireModel {

    private TyreSettings.ChangeListener changeListener;

    private String name;

    private TyreSettings lateral;
    private TyreSettings longitudinal;
    private TyreSettings alignMoment;

    // the maximun load the tire can handle.
    private float maxLoad;
    // the amount of load on the tire.
    private float load;

    private float lateralValue;
    private float longitudinalValue;
    private float momentValue;

    // friction circle result.
    // a method of combining Fx and Fy together.
    private float frictionCircle;


    public PajeckaTireModel(String name,
                            TyreSettings lateral, TyreSettings longitudinal, TyreSettings alignMoment,
                            float maxLoad) {

        this.name = name;
        this.lateral = lateral;
        this.longitudinal = longitudinal;
        this.alignMoment = alignMoment;
        this.maxLoad = maxLoad;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TyreSettings getLateral() {
        return lateral;
    }

    public void setLateral(TyreSettings lateral) {
        this.lateral = lateral;
    }

    public TyreSettings getLongitudinal() {
        return longitudinal;
    }

    public void setLongitudinal(TyreSettings longitudinal) {
        this.longitudinal = longitudinal;
    }

    public TyreSettings getAlignMoment() {
        return alignMoment;
    }

    public void setAlignMoment(TyreSettings alignMoment) {
        this.alignMoment = alignMoment;
    }

    public float getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(float maxLoad) {
        this.maxLoad = maxLoad;
        changeListener.valueChanged();
    }

    // slipAngle is in RADIANS
    private float calcSlipAngleFactor(float slipAngle, TyreSettings settings) {
        // float x = slipAngle * FastMath.DEG_TO_RAD;
        // float x = slipAngle;

        return FastMath.sin(settings.getSlipAngleCoefficientC()
                * FastMath.atan(settings.getSlipAngleCoefficientB() * slipAngle - settings.getSlipAngleCoefficientE()
                * (settings.getSlipAngleCoefficientB() * slipAngle - FastMath.atan(settings.getSlipAngleCoefficientB() * slipAngle))));
    }

    private float calcLoadForce(float load, TyreSettings settings) {
        return settings.getLoadCoefficientKA() * (1 - settings.getLoadCoefficientKB() * load) * load;
    }

    /**
     * Calculates the lateral cornering force for this tire in N.<br>
     * <br>
     * <b>CAUTION:</b> this function returns a value in Newton N!
     *
     * @param slipAngle
     *            - the slip angle in degrees (°).
     *
     * @return - lateral tire force in N.
     */
    public float calcLateralTireForce(float slipAngle) {
        this.lateralValue = calcSlipAngleFactor(slipAngle, lateral) * calcLoadForce(load, lateral);
        return lateralValue;
    }

    public float calcLongtitudeTireForce(float slipAngle) {
        this.longitudinalValue = calcSlipAngleFactor(slipAngle, longitudinal) * calcLoadForce(load, longitudinal);
        return longitudinalValue;
    }

    public float calcAlignMoment(float slipAngle) {
        this.momentValue = calcSlipAngleFactor(slipAngle, alignMoment) * calcLoadForce(load, alignMoment);
        return momentValue;
    }

    public float calculateFrictionCircle() {
        /*
            A simple method to combine Fx and Fy (when both are non-zero) is an elliptical approach:

            Calculate Fx and Fy separately (as usual)
            Cut down Fy so that the vector (Fx,Fy) doesn't exceed the maximum magnitude:
            Fy=Fy0*sqrt(1-(Fx/Fx0)^2)
            Where:
                - Fy is the resulting combined slip lateral force
                - Fy0 is the lateral force as calculated using the normal Fy formula
                - Fx is the longitudinal force as calculated using the normal Fx formula
                - Fx0 is the MAXIMUM longitudinal force possible (calculated as D+Sv in the Pacejka Fx formula).

            This method favors longitudinal forces over lateral ones (cuts down the lateral force and leaves Fx intact).
             */

        this.frictionCircle = lateralValue * FastMath.sqrt(
                1.0f - FastMath.pow((longitudinalValue / 7800), 2)
        );

        this.frictionCircle = Math.max(0.1f, this.frictionCircle);

        return frictionCircle;
    }

    public float getLateralValue() {
        return this.lateralValue;
    }

    public float getLongitudinalValue() {
        return longitudinalValue;
    }

    public float getMomentValue() {
        return momentValue;
    }

    public float getFrictionCircle() {
        return frictionCircle;
    }

    @Override
    public String toString() {

        String format = "%s: \"%s\" : %s (C=%.2f, B=%.2f, E=%.2f, KA=%.2f, KB=%.6f)";

        String lat = String.format(format, this.getClass().toString(),
                name, "Lateral",
                lateral.getSlipAngleCoefficientC(), lateral.getSlipAngleCoefficientB(), lateral.getSlipAngleCoefficientE(),
                lateral.getLoadCoefficientKA(), lateral.getLoadCoefficientKB());

        String lng = String.format(format, this.getClass().toString(),
                name, "Longitudinal",
                longitudinal.getSlipAngleCoefficientC(), longitudinal.getSlipAngleCoefficientB(), longitudinal.getSlipAngleCoefficientE(),
                longitudinal.getLoadCoefficientKA(), longitudinal.getLoadCoefficientKB());

        String mnt = String.format(format, this.getClass().toString(),
                name, "Align Moment",
                alignMoment.getSlipAngleCoefficientC(), alignMoment.getSlipAngleCoefficientB(), alignMoment.getSlipAngleCoefficientE(),
                alignMoment.getLoadCoefficientKA(), alignMoment.getLoadCoefficientKB());

        return lat + System.lineSeparator() + lng + System.lineSeparator() + mnt;
    }

    public TyreSettings.ChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(TyreSettings.ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public float getLoad() {
        return load;
    }

    public void setLoad(float load) {
        this.load = load;
        if (changeListener != null) {
            changeListener.valueChanged();
        }

    }

}
