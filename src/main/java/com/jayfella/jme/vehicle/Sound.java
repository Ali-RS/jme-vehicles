package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * A collection of audio nodes used to render a sound at different pitches
 * (fundamental frequencies) and volumes. Each node handles a specific range of
 * pitches. At any given moment, at most one node is playing.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Sound {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Sound.class.getName());
    // *************************************************************************
    // fields

    /**
     * AudioNode that's playing, or null if none
     */
    private AudioNode activeNode;
    /**
     * audio nodes mapped from their fundamental frequencies in cycles per
     * second
     */
    final private Map<Float, AudioNode> pitchToNode = new HashMap<>(5);
    // *************************************************************************
    // new methods exposed

    /**
     * Attach the audio nodes to the specified scene-graph node.
     *
     * @param parent where to attach (not null, modified)
     */
    public void attachTo(Node parent) {
        for (AudioNode node : pitchToNode.values()) {
            parent.attachChild(node);
        }
    }

    /**
     * Detach the audio nodes from the scene graph.
     */
    public void detach() {
        for (AudioNode node : pitchToNode.values()) {
            node.removeFromParent();
        }
    }

    /**
     * Configure the audio nodes for silence.
     */
    public void mute() {
        if (activeNode != null) {
            activeNode.stop();
            activeNode = null;
        }
    }

    /**
     * Configure the audio nodes for the specified pitch and volume.
     *
     * @param pitch the desired fundamental frequency (in cycles per second,
     * &gt;0)
     * @param volume the desired relative volume (linear scale, &ge;0)
     */
    public void setPitchAndVolume(float pitch, float volume) {
        Validate.positive(pitch, "pitch");
        Validate.nonNegative(volume, "volume");

        if (volume == 0f) {
            mute();
            return;
        }

        if (activeNode == null || !canAccuratelySimulate(activeNode, pitch)) {
            /*
             * Find the best AudioNode for the desired pitch.
             */
            AudioNode bestNode = findBestNode(pitch);
            if (bestNode != activeNode && activeNode != null) {
                activeNode.stop();
            }
            activeNode = bestNode;
        }

        float recordedPitch = recordedPitch(activeNode);
        float playbackSpeed = pitch / recordedPitch;
        float clampedSpeed = FastMath.clamp(playbackSpeed, 0.5f, 2f);
        if (playbackSpeed != clampedSpeed) {
            logger.log(Level.WARNING,
                    "Clamped playback speed: sound={0}, pitch={1} Hz",
                    new Object[]{getClass().getSimpleName(), pitch}
            );
        }
        float oldSpeed = activeNode.getPitch();
        if (clampedSpeed != oldSpeed) {
            activeNode.setPitch(clampedSpeed);
        }

        float oldVolume = activeNode.getVolume();
        if (volume != oldVolume) {
            activeNode.setVolume(volume);
        }

        if (activeNode.getStatus() != AudioSource.Status.Playing) {
            activeNode.play();
        }
    }
    // *************************************************************************
    // new protected methods

    /**
     * Load an OGG asset and add it to this collection.
     *
     * @param baseFilename the filename portion of the asset path, without the
     * ".ogg" extension (not null, not empty)
     * @param recordedPitch the fundamental frequency of the asset (in cycles
     * per second, &gt;0)
     * @return a new instance
     */
    protected AudioNode addOgg(String baseFilename, float recordedPitch) {
        Validate.nonEmpty(baseFilename, "base filename");
        Validate.positive(recordedPitch, "fundamental");

        AssetManager assetManager = Main.getApplication().getAssetManager();
        String assetPath = String.format("/Audio/%s.ogg", baseFilename);
        AudioNode node = new AudioNode(assetManager, assetPath,
                AudioData.DataType.Buffer);
        node.setDirectional(false);
        node.setLooping(true);
        node.setPositional(false);

        pitchToNode.put(recordedPitch, node);

        return node;
    }
    // *************************************************************************
    // private methods

    /**
     * Test whether the specified AudioNode can accurately simulate the
     * specified pitch.
     *
     * @param node
     * @param pitch
     * @return
     */
    private boolean canAccuratelySimulate(AudioNode node, float pitch) {
        float recorded = recordedPitch(node);
        float minPitch = 0.5f * recorded;
        float maxPitch = 2f * recorded;
        boolean result = MyMath.isBetween(minPitch, pitch, maxPitch);

        return result;
    }

    /**
     * Find the best AudioNode for simulating the specified pitch.
     *
     * @param pitch
     * @return a pre-existing instance, or null if none found
     */
    private AudioNode findBestNode(float pitch) {
        AudioNode result = null;
        float bestRatio = 0f;
        for (Map.Entry<Float, AudioNode> entry : pitchToNode.entrySet()) {
            float recorded = entry.getKey();
            float ratio;
            if (pitch > recorded) {
                ratio = recorded / pitch;
            } else {
                ratio = pitch / recorded;
            }
            assert ratio > 0f && ratio <= 1f : ratio;
            if (ratio > bestRatio) {
                bestRatio = ratio;
                result = entry.getValue();
            }
        }

        return result;
    }

    /**
     * Determine the recorded pitch of the specified AudioNode.
     *
     * @param node (not null)
     * @return the fundamental frequency (in cycles per second, &gt;0)
     */
    private float recordedPitch(AudioNode node) {
        for (Map.Entry<Float, AudioNode> entry : pitchToNode.entrySet()) {
            if (entry.getValue() == node) {
                float result = entry.getKey();
                return result;
            }
        }

        String message = "Not a member of this collection: " + node;
        throw new IllegalArgumentException(message);
    }
}
