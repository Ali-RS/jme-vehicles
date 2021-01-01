package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import java.util.logging.Logger;

/**
 * The gearbox (or transmission) of a Vehicle, including the differential.
 */
public class GearBox {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(GearBox.class.getName());
    // *************************************************************************
    // fields

    private boolean isInReverse;
    private Gear[] gears;
    private int activeGear;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a GearBox with the specified numbers of gears.
     *
     * @param gearCount the number of forward gears (&ge;1)
     */
    public GearBox(int gearCount) {
        gears = new Gear[gearCount];

        for (int i = 0; i < gearCount; i++) {
            gears[i] = new Gear();
        }
    }
    // *************************************************************************
    // new methods exposed

    public int getActiveGearNum() {
        return activeGear;
    }

    public Gear getGear(int gearNum) {
        return gears[gearNum];
    }

    /**
     * Count the forward gears.
     *
     * @return the count (&gt;0)
     */
    public int getGearCount() {
        return gears.length;
    }

    public float getMaxSpeed(SpeedUnit speedUnit) {
        int topGearIndex = getGearCount() - 1;
        Gear topGear = gears[topGearIndex];
        float kph = topGear.getMaxKph();

        float result;
        switch (speedUnit) {
            case KPH:
                result = kph;
                break;
            case MPH:
                result = kph * Vehicle.KPH_TO_MPH;
                break;
            case WUPS:
                result = kph * Vehicle.KPH_TO_WUPS;
                break;
            default:
                throw new RuntimeException("speedUnit = " + speedUnit);
        }

        return result;
    }

    /**
     * Test whether this GearBox is in reverse.
     *
     * @return true if a reverse gear is engaged, otherwise false
     */
    public boolean isInReverse() {
        return isInReverse;
    }

    public void setActiveGearNum(int activeGear) {
        this.activeGear = activeGear;
    }

    public void setGear(int gearNum, float start, float end) {
        Gear gear = gears[gearNum];

        gear.setMinKph(start);
        gear.setMaxKph(end);
        gear.setRedlineKph(end);
    }

    /**
     * Convenience method to switch between forward and reverse.
     *
     * @param reverse true for reverse, false for forward
     */
    public void setReversing(boolean setting) {
        isInReverse = setting;
    }
}
