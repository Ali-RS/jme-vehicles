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
import com.jayfella.jme.vehicle.gui.GearNameState;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.AnimationFactory;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.AnimationEvent;
import com.jme3.cinematic.events.CinematicEvent;
import com.jme3.cinematic.events.CinematicEventListener;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * An AppState to display a Cinematic while warming up the AssetCache and
 * initializing Lemur.
 */
class LoadingState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * enumerate all known loadables
     */
    final private static Loadable[] allLoadables = new Loadable[]{
        new AnimatedNightSky(),
        new CameraNameState(),
        new DuneBuggy(),
        new GearNameState(),
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
     * entertain the user
     */
    Cinematic cinematic;
    /**
     * monitor how many locally-created created threads are running
     */
    final CountDownLatch latch = new CountDownLatch(allLoadables.length + 1);
    /**
     * hide what happens in the main scene
     */
    private Geometry shutter;
    /**
     * count update()s for scheduling
     */
    private int updateCount = 0;
    /**
     * secondary lighting for the Cinematic
     */
    private PointLight pointLight;
    /**
     * primary lighting for the Cinematic
     */
    private SpotLight spotlight;
    /**
     * shadows for the Cinematic
     */
    private SpotLightShadowRenderer shadowRenderer;
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
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        if (shutter != null) {
            shutter.removeFromParent();
            shutter = null;
        }
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        // do nothing
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        ++updateCount;
        switch (updateCount) {
            case 1:
                setupStage();
                break;
            case 2:
                startThreads();
                break;
            case 3:
                startCinematic();
                break;
            default:
                if (cinematic.getPlayState() == PlayState.Playing) {
                    return;
                }
        }
        /*
         * The Cinematic has completed.
         */
        long latchCount = latch.getCount();
        if (latchCount < 1L) {
            /*
             * Lemur has been initialized, and all asynchronous asset loads
             * have completed.
             */
            setupShutter();

            Main application = Main.getApplication();
            Node rootNode = application.getRootNode();
            rootNode.detachAllChildren();
            if (pointLight != null) {
                rootNode.removeLight(pointLight);
            }
            if (spotlight != null) {
                rootNode.removeLight(spotlight);
            }
            if (shadowRenderer != null) {
                application.getViewPort().removeProcessor(shadowRenderer);
            }

            Main.getApplication().doneLoading();

            getStateManager().detach(this);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Initialize the Lemur library with the "glass" style.
     */
    private static void initializeLemur() {
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

    /**
     * Load the Jaime model with an extra animation.
     */
    private static Node loadJaime() {
        AssetManager assetManager = Main.getApplication().getAssetManager();
        Node result = (Node) assetManager.loadModel("/Models/Jaime/Jaime.j3o");
        result.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        /*
         * Add an 0.7-second Animation to translate Jaime forward in a jump.
         */
        AnimationFactory af = new AnimationFactory(0.7f, "JumpForward");
        af.addTimeTranslation(0f, new Vector3f(0f, 0f, -3f));
        af.addTimeTranslation(0.35f, new Vector3f(0f, 1f, -1.5f));
        af.addTimeTranslation(0.7f, Vector3f.ZERO);
        Animation spatialAnimation = af.buildAnimation();
        AnimControl animControl = result.getControl(AnimControl.class);
        animControl.addAnim(spatialAnimation);
        /*
         * Add an 0.7-second Animation to translate Jaime upward in a jump.
         */
        af = new AnimationFactory(0.7f, "JumpUpward");
        af.addTimeTranslation(0.0f, Vector3f.ZERO);
        af.addTimeTranslation(0.7f, new Vector3f(0f, 3f, 0f));
        spatialAnimation = af.buildAnimation();
        animControl.addAnim(spatialAnimation);

        return result;
    }

    /**
     * Set up the Cinematic.
     *
     * @param jaime the root of the monkey's C-G model (not null)
     */
    private void setupCinematic(Node jaime) {
        Node rootNode = Main.getApplication().getRootNode();
        float duration = 60f; // seconds
        cinematic = new Cinematic(rootNode, duration);

        cinematic.enqueueCinematicEvent(
                new AnimationEvent(jaime, "Idle", 2f, LoopMode.DontLoop));
        float jumpStart = cinematic.enqueueCinematicEvent(
                new AnimationEvent(jaime, "JumpStart")
        );
        cinematic.addCinematicEvent(jumpStart + 0.2f,
                new AnimationEvent(jaime, "JumpForward", 1)
        );
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "JumpEnd"));
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "Taunt"));
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "Punches"));
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "SideKick"));
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "SideKick"));
        cinematic.enqueueCinematicEvent(
                new AnimationEvent(jaime, "Idle", 1f, LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "Wave"));
        float jumpStart2 = cinematic.enqueueCinematicEvent(
                new AnimationEvent(jaime, "JumpStart")
        );
        cinematic.addCinematicEvent(jumpStart2 + 0.2f,
                new AnimationEvent(jaime, "JumpUpward", 1)
        );
        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "JumpEnd"));
        cinematic.enqueueCinematicEvent(
                new AnimationEvent(jaime, "Idle", 0.2f, LoopMode.DontLoop));

        cinematic.addListener(new CinematicEventListener() {
            public void onPlay(CinematicEvent c) {
                // do nothing
            }

            public void onPause(CinematicEvent c) {
                // do nothing
            }

            public void onStop(CinematicEvent c) {
                jaime.removeFromParent();
            }
        });
        cinematic.fitDuration();
        cinematic.setSpeed(1.2f);
    }

    private static Geometry setupFloor() {
        Main application = Main.getApplication();
        AssetManager assetManager = application.getAssetManager();

        Texture tex = assetManager.loadTexture("/Textures/powered-by.png");
        Material material = new Material(assetManager, Materials.LIGHTING);
        material.setTexture("DiffuseMap", tex);

        Quad mesh = new Quad(2.2f, 2.2f);
        Geometry result = new Geometry("floor", mesh);
        result.rotate(-FastMath.HALF_PI, 0f, 0f);
        result.center();
        result.setMaterial(material);
        result.setShadowMode(RenderQueue.ShadowMode.Receive);

        return result;
    }

    /**
     * Add lights and shadows to the specified scene.
     *
     * @param scene (not null)
     */
    private void setupLightsAndShadows(Node scene) {
        spotlight = new SpotLight();
        scene.addLight(spotlight);

        Vector3f position = new Vector3f(1f, 10f, 4f);
        Vector3f direction = position.normalize().negateLocal();
        spotlight.setDirection(direction);
        spotlight.setPosition(position);
        spotlight.setSpotInnerAngle(0.004f);
        spotlight.setSpotOuterAngle(0.1f);

        // a PointLight to fake indirect lighting from the ground
        pointLight = new PointLight();
        scene.addLight(pointLight);

        pointLight.setColor(ColorRGBA.White.mult(1.5f));
        pointLight.setPosition(Vector3f.UNIT_Z);
        pointLight.setRadius(2f);

        Main application = Main.getApplication();
        AssetManager assetManager = application.getAssetManager();
        shadowRenderer = new SpotLightShadowRenderer(assetManager, 512);
        application.getViewPort().addProcessor(shadowRenderer);
        shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
        shadowRenderer.setLight(spotlight);
        shadowRenderer.setShadowIntensity(0.3f);
    }

    /**
     * Create and attach a Quad to hide what's happening in the main scene.
     *
     * @return a new instance
     */
    private void setupShutter() {
        Main application = Main.getApplication();
        AssetManager assetManager = application.getAssetManager();
        Material material = new Material(assetManager, Materials.UNSHADED);
        material.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 1f));

        Camera camera = application.getCamera();
        Mesh mesh = new Quad(camera.getWidth(), camera.getHeight());
        shutter = new Geometry("shutter", mesh);
        shutter.setMaterial(material);
        shutter.setQueueBucket(RenderQueue.Bucket.Gui);

        application.getGuiNode().attachChild(shutter);
    }

    /**
     * Set the stage for the Cinematic.
     */
    private void setupStage() {
        Main application = Main.getApplication();
        Node rootNode = application.getRootNode();
        setupLightsAndShadows(rootNode);

        Camera camera = application.getCamera();
        camera.setLocation(new Vector3f(0f, 1.2f, 2.7f));
        camera.lookAt(new Vector3f(0f, 0.5f, 0f), Vector3f.UNIT_Y);

        Geometry floor = setupFloor();
        rootNode.attachChild(floor);
    }

    /**
     * Set up and play a short Cinematic of Jaime.
     */
    private void startCinematic() {
        Main application = Main.getApplication();
        Node jaime = loadJaime();
        application.getRootNode().attachChild(jaime);
        jaime.move(0f, 0f, -3f);
        setupCinematic(jaime);

        application.getStateManager().attach(cinematic);
        cinematic.play();
    }

    private void startThreads() {
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
}
