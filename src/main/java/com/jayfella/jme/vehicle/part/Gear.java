package com.jayfella.jme.vehicle.part;

import jme3utilities.Validate;

/**
 * A single configuration of a GearBox.
 */
public class Gear {
    // *************************************************************************
    // fields

    /**
     * vehicle speed that triggers a downshift (in KPH, &ge;0, &lt;maxKph)
     */
    private float minKph;
    /**
     * vehicle speed that triggers an upshift (in KPH, &gt;minKph) currently
     * this corrsponds to engine redline
     */
    private float maxKph;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Gear for the specified range of speeds.
     *
     * @param minKph speed that triggers a downshift (in KPH, &ge;0, &lt;maxKph)
     * @param maxKph speed that triggers an upshift (in KPH, &gt;minKph)
     */
    public Gear(float minKph, float maxKph) {// TODO also specify name and ratio
        Validate.inRange(minKph, "min kph", 0f, maxKph);

        this.minKph = minKph;
        this.maxKph = maxKph;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the vehicle speed that triggers a downshift.
     *
     * @return the speed (in KPH, &ge;0)
     */
    public float getStart() { // TODO rename getMinKph()
        return minKph;
    }

    /**
     * Alter the vehicle speed that triggers a downshift.
     *
     * @param kph the desired speed (in KPH, &ge;0)
     */
    public void setStart(float kph) { // TODO rename setMinKph()
        Validate.nonNegative(kph, "kph");
        this.minKph = kph;
    }

    /**
     * Determine the vehicle speed that triggers an upshift.
     *
     * @return the speed (in KPH, &ge;0)
     */
    public float getEnd() { // TODO rename getMaxKph()
        return maxKph;
    }

    /**
     * Alter the vehicle speed that triggers an upshift.
     *
     * @param kph the desired speed (in KPH, &ge;0)
     */
    public void setEnd(float kph) { // TODO rename setMaxKph()
        Validate.nonNegative(kph, "kph");
        this.maxKph = kph;
    }
}
