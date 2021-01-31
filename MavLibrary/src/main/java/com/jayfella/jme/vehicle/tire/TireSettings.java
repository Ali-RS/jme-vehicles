package com.jayfella.jme.vehicle.tire;

/**
 * Derived from the TyreSettings class in the Advanced Vehicles project.
 */
public class TireSettings {

    // LATERAL: default settings
    // public static float DEFAULT_COEFF_C = 1.3f;
    // public static float DEFAULT_COEFF_B = 15.2f;
    // public static float DEFAULT_COEFF_E = -1.6f;
    // public static float DEFAULT_COEFF_KA = 2.0f;
    // public static float DEFAULT_COEFF_KB = 0.000055f;

    // a listener for changes so we can re-draw the graph.
    private ChangeListener changeListener;

    private float slipAngleCoefficientC; // coefficient C for the normalised slip-angle curve.
    private float slipAngleCoefficientB; // coefficient B for the normalised slip-angle curve.
    private float slipAngleCoefficientE; // coefficient E for the normalised slip-angle curve.
    private float loadCoefficientKA; // coefficient KA for the load function.
    private float loadCoefficientKB; // coefficient KB for the load function.

    /**
     * Creates a Race Car Tire Model Object with the given curve coefficients.
     * Use these example coefficient values for basic tire to start with:<br>
     * C = 1.3<br>
     * B = 15.2<br>
     * E = -1.6<br>
     * KA = 2.0<br>
     * KB = 0.000055
     *
     * @param slipAngleCoefficientC - coefficient C in the normalised slip angle
     * curve function f1.
     * @param slipAngleCoefficientB - coefficient B in the normalised slip angle
     * curve function f1.
     * @param slipAngleCoefficientE - coefficient E in the normalised slip angle
     * curve function f1.
     * @param loadCoefficientKA - coefficient KA in the load curve function f2.
     * @param loadCoefficientKB - coefficient KB in the load curve function f2.
     */
    public TireSettings(float slipAngleCoefficientC, float slipAngleCoefficientB, float slipAngleCoefficientE, float loadCoefficientKA, float loadCoefficientKB) {
        this.slipAngleCoefficientC = slipAngleCoefficientC;
        this.slipAngleCoefficientB = slipAngleCoefficientB;
        this.slipAngleCoefficientE = slipAngleCoefficientE;
        this.loadCoefficientKA = loadCoefficientKA;
        this.loadCoefficientKB = loadCoefficientKB;
    }

    public float getSlipAngleCoefficientC() {
        return slipAngleCoefficientC;
    }

    public void setSlipAngleCoefficientC(float slipAngleCoefficientC) {
        this.slipAngleCoefficientC = slipAngleCoefficientC;
        changeListener.valueChanged();
    }

    public float getSlipAngleCoefficientB() {
        return slipAngleCoefficientB;
    }

    public void setSlipAngleCoefficientB(float slipAngleCoefficientB) {
        this.slipAngleCoefficientB = slipAngleCoefficientB;
        changeListener.valueChanged();
    }

    public float getSlipAngleCoefficientE() {
        return slipAngleCoefficientE;
    }

    public void setSlipAngleCoefficientE(float slipAngleCoefficientE) {
        this.slipAngleCoefficientE = slipAngleCoefficientE;
        changeListener.valueChanged();
    }

    public float getLoadCoefficientKA() {
        return loadCoefficientKA;
    }

    public void setLoadCoefficientKA(float loadCoefficientKA) {
        this.loadCoefficientKA = loadCoefficientKA;
        changeListener.valueChanged();
    }

    public float getLoadCoefficientKB() {
        return loadCoefficientKB;
    }

    public void setLoadCoefficientKB(float loadCoefficientKB) {
        this.loadCoefficientKB = loadCoefficientKB;
        changeListener.valueChanged();
    }

    public ChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    abstract public static class ChangeListener {
        abstract public void valueChanged();
    }
}
