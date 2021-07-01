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
 * A Prop for a low barrier painted black and orange, built around a portion of
 * Sabri Ayeş's "Barrier & Traffic Cone Pack".
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ShortBarrierPainted extends Prop {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(ShortBarrierPainted.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate a barrier with the specified scale factor.
     *
     * @param scaleFactor the desired scale factor (world units per model unit,
     * &gt;0)
     */
    public ShortBarrierPainted(float scaleFactor) {
        super("ShortBarrierPainted", scaleFactor);
    }
    // *************************************************************************
    // Prop methods

    /**
     * Load this Prop from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        if (getMainBody() != null) {
            logger2.log(Level.SEVERE, "Already loaded.");
            return;
        }
        String assetPath
                = "/Models/Props/barrier_pack/short_barrier_painted.j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);

        assetPath = "/Models/Props/barrier_pack/short_barrier_painted_hull.j3o";
        ConvexShape hullShape;
        try {
            hullShape = (ConvexShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            hullShape = CollisionShapeFactory.createMergedHullShape(cgmRoot);
        }

        CollisionShape bodyShape = hullShape;
        float massKg = 600f;
        configureSingle(cgmRoot, hullShape, bodyShape, massKg);

        PhysicsRigidBody body = getMainBody();
        body.setFriction(5f);
        body.setLinearDamping(0.01f);
    }
}
