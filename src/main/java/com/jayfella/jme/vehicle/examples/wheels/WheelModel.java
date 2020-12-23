package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 * A computer-graphics (C-G) model for a wheel.
 *
 * By convention, the +X axis points inward, toward the middle of the axle.
 */
abstract public class WheelModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(WheelModel.class.getName());
    // *************************************************************************
    // fields

    /**
     * parent of the model's root spatial
     */
    final private Node wheelNode = new Node("Wheel Node");
    /**
     * root spatial of the C-G model
     */
    private Spatial wheelSpatial;
    // *************************************************************************
    // new methods exposed

    /**
     * Access the parent of the model's root spatial.
     *
     * @return the pre-existing Node (not null)
     */
    public Node getWheelNode() {
        return wheelNode;
    }

    /**
     * Access the model's root spatial.
     *
     * @return the pre-existing instance
     */
    public Spatial getSpatial() {
        return wheelSpatial;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Initialize the C-G model.
     *
     * @param wheelSpatial the desired C-G model (not null)
     */
    protected void setSpatial(Spatial wheelSpatial) {
        this.wheelSpatial = wheelSpatial;
        this.wheelNode.attachChild(wheelSpatial);
    }
}
