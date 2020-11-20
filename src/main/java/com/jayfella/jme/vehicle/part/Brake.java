package com.jayfella.jme.vehicle.part;

/**
 * Model a single brake, for example on a Wheel.
 */
public class Brake {

    private float strength;

    public Brake(float strength) {
        this.strength = strength;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }
}
