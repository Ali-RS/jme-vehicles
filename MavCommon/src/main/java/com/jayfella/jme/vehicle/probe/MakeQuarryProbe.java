package com.jayfella.jme.vehicle.probe;

import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jme3.app.SimpleApplication;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.export.JmeExporter;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.LightProbe;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import java.io.File;
import java.io.IOException;

/**
 * A SimpleApplication to generate a LightProbe from an EquirectMap sky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class MakeQuarryProbe extends SimpleApplication {
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
     * Main entry point for the MakeQuarryProbe application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String[] args) {
        new MakeQuarryProbe().start();
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        stateManager.attach(envCam);

        Spatial sky = SkyFactory.createSky(assetManager,
                QuarrySky.imageAssetPath, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (lightProbe == null) {
            lightProbe = LightProbeFactory.makeProbe(envCam, rootNode, adapter);
            lightProbe.setPosition(new Vector3f(0f, 3f, 0f));
            lightProbe.getArea().setRadius(100f);

        } else if (lightProbe.isReady()) {
            JmeExporter exporter = BinaryExporter.getInstance();
            String filePath = "src/main/resources"
                    + QuarrySky.lightProbeAssetPath;
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
