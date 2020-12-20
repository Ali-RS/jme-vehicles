package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.SpeedUnit;
import static com.jayfella.jme.vehicle.Vehicle.KMH_TO_MPH;

public class GearBox {

    private boolean isInReverse;
    private int activeGear;
    private Gear[] gears;

    public GearBox(int gearCount) {
        this.gears = new Gear[gearCount];

        for (int i = 0; i < gearCount; i++) {
            this.gears[i] = new Gear(0, 0);
        }

    }

    public GearBox(Gear[] gears) {
        this.gears = gears;
    }

    public Gear[] getGears() {
        return gears;
    }

    public void setGears(Gear[] gears) {
        this.gears = gears;
    }

    public boolean isReversing() {
        return isInReverse;
    }

    public void setReversing(boolean setting) {
        isInReverse = setting;
    }

    public Gear getGear(int gearNum) {
        return this.gears[gearNum];
    }

    public void setGear(int gearNum, Gear gear) {
        this.gears[gearNum] = gear;
    }

    public void setGear(int gearNum, float start, float end) {

        Gear gear = this.gears[gearNum];

        gear.setStart(start);
        gear.setEnd(end);
    }

    public Gear getActiveGear() {
        return this.gears[activeGear];
    }

    public int getActiveGearNum() {
        return activeGear;
    }

    public void setActiveGearNum(int activeGear) {
        this.activeGear = activeGear;
    }

    public int getGearCount() {
        return this.gears.length;
    }

    public float getMaxSpeed(SpeedUnit speedUnit) {
        switch (speedUnit) {
            case KMH: return gears[getGearCount() - 1].getEnd();
            case MPH: return gears[getGearCount() - 1].getEnd() * KMH_TO_MPH;
            default: return -1;
        }
    }

}
