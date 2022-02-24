package com.jayfella.jme.vehicle.examples.props;

import com.jayfella.jme.vehicle.Prop;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.ConvexShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Prop for a construction barrel with a black base, built around a portion of
 * Sabri Ayeş's "Barrier &amp; Traffic Cone Pack". Note: bottomless.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Barrel2 extends Prop {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(Barrel2.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a barrel with the specified scale factor.
     *
     * @param scaleFactor the desired scale factor (world units per model unit,
     * &gt;0)
     * @param totalMass the desired total mass (in kilograms, &gt;0)
     */
    public Barrel2(float scaleFactor, float totalMass) {
        super("Barrel2", scaleFactor, totalMass);
    }
    // *************************************************************************
    // Prop methods

    /**
     * Determine the default total mass for scale=1, for this type of Prop.
     *
     * @return the mass (in kilograms, &gt;0)
     */
    @Override
    public float defaultDescaledMass() {
        return 60f;
    }

    /**
     * Load this Prop from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        if (isLoaded()) {
            logger2.log(Level.SEVERE, "Already loaded.");
            return;
        }
        String assetPath = "/Models/Props/barrier_pack/barrel2.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);

        assetPath = "/Models/Props/barrier_pack/barrel2_hull.j3o";
        ConvexShape hullShape;
        try {
            hullShape = (ConvexShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            hullShape = CollisionShapeFactory.createMergedHullShape(cgmRoot);
        }

        CollisionShape bodyShape = hullShape;
        configureSingle(cgmRoot, hullShape, bodyShape);

        PhysicsRigidBody body = getMainBody();
        body.setFriction(5f);
        body.setLinearDamping(0.01f);
    }
}
