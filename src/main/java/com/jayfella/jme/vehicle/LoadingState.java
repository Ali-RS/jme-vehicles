package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.examples.cars.DuneBuggy;
import com.jayfella.jme.vehicle.examples.cars.GTRNismo;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.examples.cars.HatchBack;
import com.jayfella.jme.vehicle.examples.cars.PickupTruck;
import com.jayfella.jme.vehicle.examples.environments.Playground;
import com.jayfella.jme.vehicle.examples.environments.Racetrack;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.gui.CameraNameState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import jme3utilities.mesh.RectangleMesh;

/**
 * An AppState to display a spinning texture while warming up the AssetCache and
 * initializing Lemur.
 */
class LoadingState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * spinner's angular rate (in radians/second)
     */
    final private static float spinRate = 3f;
    /**
     * enumerate all known loadables
     */
    final private static Loadable[] allLoadables = new Loadable[]{
        new AnimatedNightSky(),
        new CameraNameState(),
        new DuneBuggy(),
        new GrandTourer(),
        new GTRNismo(),
        new HatchBack(),
        new Playground(),
        new PickupTruck(),
        new QuarrySky(),
        new Racetrack()
    };
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LoadingState.class.getName());
    // *************************************************************************
    // fields

    /**
     * monitor how many asynchronous assets loads are in progress
     */
    final private CountDownLatch latch
            = new CountDownLatch(allLoadables.length + 1);
    /**
     * conceals whatever's going on in the main scene
     */
    private Geometry backgroundGeom;
    final private Node node = new Node("Loading Node");
    private Node spinnerNode;
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
        AssetManager assetManager = app.getAssetManager();
        spinnerNode = createSpinnerNode(assetManager);
        backgroundGeom = createBackgroundGeom(assetManager);

        node.attachChild(backgroundGeom);
        node.attachChild(spinnerNode);
        node.setQueueBucket(RenderQueue.Bucket.Gui);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(node);
        /*
         * Start threads to warm up the AssetCache.
         */
        for (Loadable loadable : allLoadables) {
            Thread thread = new Preloader(loadable, latch);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        /*
         * Start a thread to initialize Lemur.
         */
        Thread thread = new Thread() {
            @Override
            public void run() {
                initializeLemur();
                latch.countDown();
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        spinnerNode.rotate(0, 0, -tpf * spinRate);

        long latchCount = latch.getCount();
        if (latchCount < 1L) {
            /*
             * Lemur has been initialized and all asynchronous asset loads
             * have completed.
             */
            Main.getApplication().doneLoading();
            getStateManager().detach(this);
        }
    }
    // *************************************************************************
    // private methods

    private Geometry createBackgroundGeom(AssetManager assetManager) {
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setColor("Color", ColorRGBA.Black.clone());

        Camera camera = getApplication().getCamera();
        Mesh mesh = new Quad(camera.getWidth(), camera.getHeight());
        Geometry result = new Geometry("Background", mesh);
        result.setMaterial(material);

        return result;
    }

    private Node createSpinnerNode(AssetManager assetManager) {
        Texture texture = assetManager.loadTexture("loading.png");
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setTexture("ColorMap", texture);
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);

        float radius = 25f;
        Mesh square = new RectangleMesh(-radius, +radius, -radius, +radius, 1f);
        Geometry spinnerGeom = new Geometry("Spinner", square);
        spinnerGeom.setMaterial(material);

        Node result = new Node("Spinner Node");
        result.attachChild(spinnerGeom);
        /*
         * Center the spinner in the display.
         */
        Camera camera = getApplication().getCamera();
        float x = camera.getWidth() / 2f;
        float y = camera.getHeight() / 2f;
        result.move(x, y, 1f);

        return result;
    }

    /**
     * Initialize the Lemur library with the "glass" style.
     */
    private void initializeLemur() {
        long startMillis = System.currentTimeMillis();

        Main application = Main.getApplication();
        GuiGlobals.initialize(application);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // This consumes joystick input. Why?
        Main.findAppState(FocusNavigationState.class).setEnabled(false);

        long latencyMillis = System.currentTimeMillis() - startMillis;
        float seconds = latencyMillis / 1_000f;
        System.out.println("initialized Lemur in " + seconds + " seconds");
    }
}
