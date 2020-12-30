package com.jayfella.jme.vehicle.shape;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.export.binary.BinaryLoader;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.scene.Spatial;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.texture.plugins.AWTLoader;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.minie.PhysicsDumper;

/**
 * A console application to pre-compute collision shapes for assets.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CreateShapes {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CreateShapes.class.getName());
    // *************************************************************************
    // fields

    /**
     * AssetManager for loading C-G models
     */
    final private static AssetManager assetManager = new DesktopAssetManager();
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the CreateShapes application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        NativeLibraryLoader.loadNativeLibrary("bulletjme", true);
        NativeLibrary.setStartupMessageEnabled(false);
        /*
         * Configure the AssetManager (from scratch).
         */
        assetManager.registerLoader(AWTLoader.class, "jpeg", "png");
        assetManager.registerLoader(BinaryLoader.class, "j3o");
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLocator(null, ClasspathLocator.class);
        /*
         * Create a collision shape for each vehicle chassis.
         */
        createChassisShape("GT", "scene.gltf");
        createChassisShape("ford_ranger", "pickup");
        createChassisShape("gtr_nismo", "scene.gltf");
        createChassisShape("hcr2_buggy", "dune-buggy");
        createChassisShape("modern_hatchback", "hatchback");
        /*
         * Create a collision shape for each World.
         */
        createEnvShape("race1", "race1");
        createEnvShape("vehicle-playground", "vehicle-playground");
    }
    // *************************************************************************
    // private methods

    /**
     * Create a collision shape for a vehicle chassis.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createChassisShape(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/%s/%s.j3o", folderName,
                cgmBaseFileName);
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        System.out.printf("%nCreate shape for %s chassis ... ", cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createDynamicMeshShape(cgmRoot);

        System.out.printf("done!%n");
        System.out.flush();
        new PhysicsDumper().dump(collisionShape, "    ");
        /*
         * Save the shape in J3O format.
         */
        String writeFilePath = "src/main/resources/Models/" + folderName
                + "/shapes/chassis-shape.j3o";
        Heart.writeJ3O(writeFilePath, collisionShape);
    }

    /**
     * Create a collision shape for a World.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createEnvShape(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/%s/%s.j3o", folderName,
                cgmBaseFileName);
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        System.out.printf("%nCreate shape for the %s world ... ",
                cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createMergedMeshShape(cgmRoot);

        System.out.printf("done!%n");
        System.out.flush();
        new PhysicsDumper().dump(collisionShape, "    ");
        /*
         * Save the shape in J3O format.
         */
        String writeFilePath = "src/main/resources/Models/" + folderName
                + "/shapes/env-shape.j3o";
        Heart.writeJ3O(writeFilePath, collisionShape);
    }
}
