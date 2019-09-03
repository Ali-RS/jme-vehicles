package com.jayfella.jme.vehicle.part;

/**
 * Defines a break component, for example on a vehicle wheel or propellant.
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
