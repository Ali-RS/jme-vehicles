package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;

public class GearBox {

    private boolean isInReverse;
    private int activeGear;
    private Gear[] gears;

    public GearBox(int gearCount) {
        gears = new Gear[gearCount];

        for (int i = 0; i < gearCount; i++) {
            gears[i] = new Gear(0, 0);
        }
    }

    public boolean isInReverse() {
        return isInReverse;
    }

    public void setReversing(boolean setting) {
        isInReverse = setting;
    }

    public Gear getGear(int gearNum) {
        return gears[gearNum];
    }

    public void setGear(int gearNum, float start, float end) {

        Gear gear = gears[gearNum];

        gear.setMinKph(start);
        gear.setMaxKph(end);
    }

    public int getActiveGearNum() {
        return activeGear;
    }

    public void setActiveGearNum(int activeGear) {
        this.activeGear = activeGear;
    }

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
}
