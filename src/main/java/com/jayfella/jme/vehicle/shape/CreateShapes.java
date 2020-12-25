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
        createChassisShape("/Models/GT/", "scene.gltf");
        createChassisShape("/Models/ford_ranger/", "pickup");
        createChassisShape("/Models/gtr_nismo/", "scene.gltf");
        createChassisShape("/Models/hcr2_buggy/", "dune-buggy");
        createChassisShape("/Models/modern_hatchback/", "hatchback");
        /*
         * Create a collision shape for each Environment.
         */
        createEnvShape("/Models/race1/", "race1");
        createEnvShape("/Models/vehicle-playground/", "vehicle-playground");
    }
    // *************************************************************************
    // private methods

    /**
     * Create a collision shape for a vehicle chassis.
     *
     * @param folderPath an asset path to the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createChassisShape(String folderPath,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = folderPath + cgmBaseFileName + ".j3o";
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        System.out.printf("%nCreate shape for %s chassis ... ", cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createDynamicMeshShape(cgmRoot);

        System.out.printf("  done!%n", cgmBaseFileName);
        System.out.flush();
        new PhysicsDumper().dump(collisionShape, "    ");
        /*
         * Save the shape in J3O format.
         */
        String writeFilePath = "./src/main/resources" + folderPath
                + "shapes/chassis-shape.j3o";
        Heart.writeJ3O(writeFilePath, collisionShape);
    }

    /**
     * Create a collision shape for an Environment.
     *
     * @param folderPath an asset path to the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createEnvShape(String folderPath,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = folderPath + cgmBaseFileName + ".j3o";
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        System.out.printf("%nCreate shape for the %s environment ... ",
                cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createMergedMeshShape(cgmRoot);

        System.out.printf("  done!%n", cgmBaseFileName);
        System.out.flush();
        new PhysicsDumper().dump(collisionShape, "    ");
        /*
         * Save the shape in J3O format.
         */
        String writeFilePath = "./src/main/resources" + folderPath
                + "shapes/env-shape.j3o";
        Heart.writeJ3O(writeFilePath, collisionShape);
    }
}
