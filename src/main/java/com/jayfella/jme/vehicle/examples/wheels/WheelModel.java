package com.jayfella.jme.vehicle.examples.wheels;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A computer-graphics (C-G) model for a wheel.
 *
 * By convention, the model has radius=0.5 model units, and the local +X axis
 * points inward, toward the middle of the axle.
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
     * wheel diameter (in local units)
     */
    final private float diameter;
    /**
     * parent of the model's root spatial: scaling is applied here
     */
    final private Node node;
    /**
     * root spatial of the C-G model
     */
    private Spatial cgmRoot;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a model with the specified diameter.
     *
     * @param diameter the desired diameter (in local units, &gt;0)
     */
    protected WheelModel(float diameter) {
        Validate.positive(diameter, "diameter");
        this.diameter = diameter;

        this.node = new Node("Wheel Node"); // TODO distinct names
        node.setLocalScale(diameter);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Access the parent of the model's root spatial.
     *
     * @return the pre-existing Node (not null)
     */
    public Node getWheelNode() {
        return node;
    }

    /**
     * Access the model's root spatial.
     *
     * @return the pre-existing instance
     */
    public Spatial getSpatial() {
        return cgmRoot;
    }

    /**
     * Determine the wheel's radius.
     *
     * @return the radius (in local units, &gt;0)
     */
    public float radius() {
        return diameter / 2;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Initialize the C-G model.
     *
     * @param wheelSpatial the desired C-G model (not null)
     */
    protected void setSpatial(Spatial wheelSpatial) {
        this.cgmRoot = wheelSpatial;

        node.detachAllChildren();
        node.attachChild(wheelSpatial);
    }
}
