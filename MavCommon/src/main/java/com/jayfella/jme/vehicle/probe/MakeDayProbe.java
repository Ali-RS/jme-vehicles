package com.jayfella.jme.vehicle.probe;

import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jme3.app.SimpleApplication;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.export.JmeExporter;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.LightProbe;
import com.jme3.math.Vector3f;
import java.io.File;
import java.io.IOException;
import jme3utilities.sky.SkyControl;

/**
 * A SimpleApplication to generate a LightProbe for AnimatedDaySky.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MakeDayProbe extends SimpleApplication {

    final private EnvironmentCamera envCam = new EnvironmentCamera();
    final private JobProgressAdapter<LightProbe> adapter
            = new JobProgressAdapter<LightProbe>() {
        @Override
        public void done(LightProbe result) {
            // do nothing
        }
    };
    private LightProbe lightProbe;

    public static void main(String[] args) {
        new MakeDayProbe().start();
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(envCam);

        SkyControl skyControl = AnimatedDaySky.createSkyControl(this);
        /*
         * Disable cloud motion for reproducibility.
         */
        skyControl.setCloudsRate(0f);

        rootNode.addControl(skyControl);
        skyControl.setEnabled(true);
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
                    + AnimatedDaySky.lightProbeAssetPath;
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
