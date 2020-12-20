package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

abstract public class WheelModel {

    final private Node wheelNode = new Node("Wheel Node");
    private Spatial wheelSpatial;

    protected void setSpatial(Spatial wheelSpatial) {
        this.wheelSpatial = wheelSpatial;
        this.wheelNode.attachChild(wheelSpatial);
    }

    public Node getWheelNode() {
        return wheelNode;
    }

    public Spatial getSpatial() {
        return wheelSpatial;
    }

}
