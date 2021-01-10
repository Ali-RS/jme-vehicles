package com.jayfella.jme.vehicle.gui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.MyAsset;

/**
 * AppState to manage an analog compass.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CompassState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CompassState.class.getName());
    // *************************************************************************
    // fields

    /**
     * dimensions of the GUI viewport (in pixels)
     */
    private float viewPortHeight, viewPortWidth;

    private Geometry geometry;
    /**
     * reusable temporary Quaternion
     */
    final private Quaternion tmpRotation = new Quaternion();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled compass.
     */
    public CompassState() {
        super("Compass");
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void cleanup(Application app) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param app the application instance (not null)
     */
    @Override
    protected void initialize(Application app) {
        Camera camera = app.getCamera();
        viewPortHeight = camera.getHeight();
        viewPortWidth = camera.getWidth();
        AssetManager manager = app.getAssetManager();
        initCompass(manager);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        geometry.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        showCompass();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateCompass();
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        Node guiNode = simpleApp.getGuiNode();
        guiNode.attachChild(spatial);
    }

    /**
     * Construct the Geometry for the compass.
     */
    private void initCompass(AssetManager assetManager) {
        int circumferenceSamples = 40;
        float radius = 180f / FastMath.PI; // in pixels
        float height = 40f; // in pixels
        Mesh mesh = new Cylinder(2, circumferenceSamples, radius, height);
        /*
         * Rewrite the 2nd texture coordinate so that it ranges from 0 to 1.
         */
        int numVertices = mesh.getVertexCount();
        FloatBuffer texCoords = mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer positions = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        for (int vertexIndex = 0; vertexIndex < numVertices; ++vertexIndex) {
            float z = positions.get(3 * vertexIndex + 2);
            float v = 0.5f + z / height;
            texCoords.put(2 * vertexIndex + 1, v);
        }

        geometry = new Geometry("compass", mesh);
        String assetPath = "/Textures/Georg/compass.png";
        Texture texture = assetManager.loadTexture(assetPath);
        Material material
                = MyAsset.createUnshadedMaterial(assetManager, texture);
        geometry.setMaterial(material);
    }

    /**
     * Display the compass.
     */
    private void showCompass() {
        attachToGui(geometry);
        /*
         * Position the compass in the viewport.
         */
        float x = 0.5f * viewPortWidth;
        float y = 0.95f * viewPortHeight;
        geometry.setLocalTranslation(x, y, 0f);
    }

    /**
     * Re-orient the compass.
     */
    private void updateCompass() {
        Camera camera = getApplication().getCamera();
        Vector3f direction = camera.getDirection();
        // +X is north and +Z is east
        float azimuth = FastMath.atan2(direction.z, direction.x);
        float yRotation = -azimuth - FastMath.HALF_PI;
        tmpRotation.fromAngles(-FastMath.HALF_PI, yRotation, 0f);
        geometry.setLocalRotation(tmpRotation);

        //float azimuthDegrees = MyMath.toDegrees(azimuth);
        //azimuthDegrees = MyMath.modulo(azimuthDegrees, 360f);
        //System.out.printf("azimuth = %.0f%n", azimuthDegrees);
    }
}
