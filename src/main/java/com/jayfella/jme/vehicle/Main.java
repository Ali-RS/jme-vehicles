package com.jayfella.jme.vehicle;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jayfella.jme.vehicle.examples.cars.GrandTourer;
import com.jayfella.jme.vehicle.gui.DriverHud;
import com.jayfella.jme.vehicle.gui.LoadingState;
import com.jayfella.jme.vehicle.input.NonDrivingInputState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioListenerState;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.focus.FocusNavigationState;
import com.simsilica.lemur.style.BaseStyles;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.SignalTracker;
import jme3utilities.mesh.Octasphere;

public class Main extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Main.class.getName());
    // *************************************************************************
    // fields

    /**
     * directional light
     */
    private static DirectionalLight directionalLight;
    /**
     * current game environment/world
     */
    private static Environment environment;
    /**
     * application instance
     */
    private static Main application;
    /**
     * vehicle currently selected
     */
    private static Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a SimpleApplication without FlyCam or debug keys.
     */
    private Main() {
        super(
                new AudioListenerState(),
                new ConstantVerifierState(),
                new StatsAppState()
        );
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Attach the selected environment and vehicle to the scene.
     */
    public void attachAllToScene() {
        float intensity = environment.directLightIntensity();
        ColorRGBA directColor = ColorRGBA.White.mult(intensity);
        directionalLight.setColor(directColor);

        environment.resetCameraPosition();
        environment.add(rootNode);

        vehicle.attachToScene(rootNode);
    }

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T> the kind of AppState
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        T appState = manager.getState(subclass);

        assert appState != null;
        return appState;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Main getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the environment from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Environment getEnvironment() {
        assert environment != null;
        return environment;
    }

    /**
     * Access the selected vehicle from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Vehicle getVehicle() {
        assert vehicle != null;
        return vehicle;
    }

    /**
     * Main entry point for the More Advanced Vehicles application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String... args) {
        boolean forceDialog = false;
        /*
         * Process any command-line arguments.
         */
        for (String arg : args) {
            switch (arg) {
                case "-f":
                case "--forceDialog":
                    forceDialog = true;
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }
        SignalTracker.logger.setLevel(Level.WARNING);

        AppSettings appSettings = new AppSettings(true);
        appSettings.setResolution(1280, 720);
        appSettings.setTitle("More Advanced Vehicles");
        appSettings.setUseJoysticks(true);
        appSettings.setVSync(true);

        application = new Main();
        application.setDisplayStatView(false);
        application.setDisplayFps(false);
        application.setSettings(appSettings);
        application.setShowSettings(forceDialog);
        application.start();
    }

    /**
     * Replace the current Environment with a new one.
     *
     * @param newEnvironment the desired environment (not null, loaded)
     */
    public void setEnvironment(Environment newEnvironment) {
        vehicle.detachFromScene();
        environment.remove();

        environment = newEnvironment;
        attachAllToScene();
        /*
         * Re-use the existing input state with the new Vehicle instance.
         */
        NonDrivingInputState inputState
                = Main.findAppState(NonDrivingInputState.class);
        inputState.setVehicle(vehicle);
    }

    /**
     * Replace the current Vehicle with a new one.
     *
     * @param newVehicle the desired Vehicle (not null, loaded)
     */
    public void setVehicle(Vehicle newVehicle) {
        vehicle.detachFromScene();
        vehicle = newVehicle;
        vehicle.attachToScene(rootNode);
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        renderer.setDefaultAnisotropicFilter(4);

        inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
            @Override
            public void onConnected(Joystick joystick) {
                System.out.println("Joystick connected: " + joystick);
            }

            @Override
            public void onDisconnected(Joystick joystick) {
                System.out.println("Joystick disconnected: " + joystick);
            }
        });

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();
        /*
         * The dash camera sits close to the bodywork, so set its near clipping
         * plane accordingly.
         */
        float near = 0.1f;
        float far = 1800f;
        MyCamera.setNearFar(cam, near, far);

        // initialize Lemur with the "glass" style
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        environment = new Racetrack();

        // Let there be light
        float intensity = environment.directLightIntensity();
        ColorRGBA directColor = ColorRGBA.White.mult(intensity);
        directionalLight = new DirectionalLight(
                new Vector3f(1f, -0.45f, 0.5f).normalizeLocal(), directColor);
        rootNode.addLight(directionalLight);

        String probeName = "/Probes/quarry_03.j3o";
        LightProbe probe = (LightProbe) assetManager.loadAsset(probeName);
        probe.setPosition(Vector3f.ZERO);
        probe.getArea().setRadius(9_999f);
        rootNode.addLight(probe);

        // display a rotating texture to entertain users
        CountDownLatch latch = new CountDownLatch(3);
        LoadingState loadingState = new LoadingState(latch);
        stateManager.attach(loadingState);

        // initialize physics with debug disabled
        BulletAppState bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        stateManager.attach(bulletAppState);

        // create the driver's heads-up display (disabled)
        DriverHud driverHud = new DriverHud();
        stateManager.attach(driverHud);

        // enable screenshots
        ScreenshotAppState screenshotAppState
                = new ScreenshotAppState("./", "screen_shot");
        stateManager.attach(screenshotAppState);

        // Load the sky asynchronously.
        CompletableFuture
                .supplyAsync(() -> {
                    Spatial sky = createSky(assetManager,
                            "Textures/Sky/quarry_03_4k.jpg");
                    return sky;
                })
                .whenComplete((spatial, ex) -> {
                    enqueue(() -> {
                        rootNode.attachChild(spatial);
                        latch.countDown();
                    });
                });

        // Load the Environment asynchronously.
        CompletableFuture
                .supplyAsync(() -> {
                    Node node = environment.load();
                    return node;
                })
                .whenComplete((node, ex) -> {
                    enqueue(() -> {
                        environment.add(rootNode);
                        latch.countDown();
                    });
                });

        environment.resetCameraPosition();

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        addPostProcessing(directionalLight);

        // Load the default Vehicle asynchronously.
        vehicle = new GrandTourer();
        assert vehicle.getVehicleControl() == null;
        CompletableFuture
                .supplyAsync(() -> {
                    vehicle.load();
                    Node node = vehicle.getNode();
                    return node;
                })
                .whenComplete((node, ex) -> {
                    enqueue(() -> {
                        latch.countDown();
                    });
                });

        // this consumes joystick input. I'll have to investigate why.
        stateManager.getState(FocusNavigationState.class).setEnabled(false);
    }
    // *************************************************************************
    // private methods

    private void addPostProcessing(DirectionalLight directionalLight) {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 4);
        shadowFilter.setLight(directionalLight);
        shadowFilter.setShadowIntensity(0.3f);
        shadowFilter.setShadowZExtend(256);
        shadowFilter.setShadowZFadeLength(128);
        // shadowFilter.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        fpp.addFilter(shadowFilter);

        SSAOFilter ssaoFilter = new SSAOFilter();
        fpp.addFilter(ssaoFilter);

        // LightScatteringFilter lightScattering = new LightScatteringFilter();
        // lightScattering.setLightPosition(directionalLight.getDirection());
        // lightScattering.setLightDensity(1);
        // lightScattering.setBlurWidth(1.1f);
        // fpp.addFilter(lightScattering);
        // DepthOfFieldFilter dof = new DepthOfFieldFilter();
        // dof.setFocusDistance(0);
        // dof.setFocusRange(384);
        // dof.setEnabled(false);
        // fpp.addFilter(dof);
        // BloomFilter bloomFilter = new BloomFilter();
        // bloomFilter.setExposurePower(55);
        // bloomFilter.setBloomIntensity(1.2f);
        // fpp.addFilter(bloomFilter);
        viewPort.addProcessor(fpp);
    }

    /**
     * Generate a sky Geometry from an Equirectangular texture asset.
     *
     * @param assetManager (not null)
     * @param assetPath the asset path to the texture (not null, not empty)
     * @return a new Geometry
     */
    private Spatial createSky(AssetManager assetManager, String assetPath) {
        boolean flipY = true;
        TextureKey textureKey = new TextureKey(assetPath, flipY);
        Texture texture = assetManager.loadTexture(textureKey);
        texture.setAnisotropicFilter(1);

        Material skyMat = new Material(assetManager, "MatDefs/SkyEquirec.j3md");
        skyMat.setTexture("Texture", texture);
        skyMat.setVector3("NormalScale", new Vector3f(1f, 1f, 1f));

        int numRefineSteps = 1;
        float meshRadius = 10f;
        Octasphere sphereMesh = new Octasphere(numRefineSteps, meshRadius);
        MyMesh.reverseNormals(sphereMesh);
        MyMesh.reverseWinding(sphereMesh);

        Geometry result = new Geometry("Sky", sphereMesh);
        result.setCullHint(Spatial.CullHint.Never);
        result.setMaterial(skyMat);
        result.setQueueBucket(RenderQueue.Bucket.Sky);
        result.setShadowMode(RenderQueue.ShadowMode.Off);

        float boundRadius = Float.POSITIVE_INFINITY;
        BoundingSphere bound = new BoundingSphere(boundRadius, Vector3f.ZERO);
        result.setModelBound(bound);

        return result;
    }
}
