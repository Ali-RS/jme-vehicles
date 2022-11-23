package com.jayfella.jme.vehicle.probe;

import com.jayfella.jme.vehicle.examples.skies.PurpleNebulaSky;
import com.jme3.app.SimpleApplication;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.export.JmeExporter;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.LightProbe;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.io.File;
import java.io.IOException;
import jme3utilities.MyAsset;

/**
 * A SimpleApplication to generate a LightProbe from a cubemapped sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class MakePurpleNebulaProbe extends SimpleApplication {
    // *************************************************************************
    // fields

    final private EnvironmentCamera envCam = new EnvironmentCamera();
    final private JobProgressAdapter<LightProbe> adapter
            = new JobProgressAdapter<LightProbe>() {
        @Override
        public void done(LightProbe result) {
            // do nothing
        }
    };
    private LightProbe lightProbe;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the MakePurpleNebulaProbe application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String[] args) {
        new MakePurpleNebulaProbe().start();
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        stateManager.attach(envCam);

        Spatial sky = MyAsset.createStarMapSphere(assetManager,
                PurpleNebulaSky.cubemapName, 100f);
        rootNode.attachChild(sky);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (lightProbe == null) {
            lightProbe = LightProbeFactory.makeProbe(envCam, rootNode, adapter);
            lightProbe.setPosition(Vector3f.ZERO);
            lightProbe.getArea().setRadius(9_999f);

        } else if (lightProbe.isReady()) {
            JmeExporter exporter = BinaryExporter.getInstance();
            String filePath = "src/main/resources"
                    + PurpleNebulaSky.lightProbeAssetPath;
            File file = new File(filePath);
            try {
                exporter.save(lightProbe, file);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            stop();
        }
    }
}
