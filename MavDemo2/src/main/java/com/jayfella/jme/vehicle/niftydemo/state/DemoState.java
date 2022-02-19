package com.jayfella.jme.vehicle.niftydemo.state;

import com.jayfella.jme.vehicle.GlobalAudio;
import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.Timer;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;
import jme3utilities.math.noise.Generator;

/**
 * The "game state" of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DemoState
        implements GlobalAudio, PhysicsTickListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DemoState.class.getName());
    // *************************************************************************
    // fields

    /**
     * elapsed physics time (in seconds, &ge;0)
     */
    private double elapsedTime;
    /**
     * pseudo-random number generator
     */
    final private Generator prGenerator;
    /**
     * number of timer ticks during the most recent physics tick
     */
    private long numTicks;
    /**
     * timer tick count at the start of the most recent physics tick
     */
    private long preTickCount;
    /**
     * selected Prop, or null if none
     */
    private Prop selectedProp;
    /**
     * proposal for the next Prop
     */
    private PropProposal propProposal;
    /**
     * state of all vehicles
     */
    final private Vehicles vehicles;
    /**
     * selected World (not null)
     */
    private World world = new Mountains();
    // *************************************************************************
    // constructors

    /**
     * Instantiate an initial state with no vehicle and default settings.
     *
     * @param physicsSpace (not null)
     */
    public DemoState(PhysicsSpace physicsSpace) {
        elapsedTime = 0.0;
        prGenerator = new Generator();
        numTicks = 0;

        physicsSpace.addTickListener(this);

        MavDemo2 application = MavDemo2.getApplication();
        Sky.setApplication(application);
        Sky.initialize();

        Node rootNode = application.getRootNode();
        world.attach(application, rootNode, physicsSpace);

        vehicles = new Vehicles(this);

        View view = MavDemo2.findAppState(View.class);
        Sky sky = view.getSky();
        sky.addToWorld(world);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * TODO
     *
     * @param vehicle (not null)
     */
    final public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicles.select(vehicle);
    }

    /**
     * TODO
     */
    public void deleteAllProps() {
        Collection<Prop> collection = world.listProps();
        int numProps = collection.size();
        Prop[] array = new Prop[numProps];
        collection.toArray(array);
        for (Prop prop : array) {
            prop.removeFromWorld();
        }

        selectedProp = null;
    }

    /**
     * Read the elapsed time.
     *
     * @return elapsed physics time (in seconds, &ge;0)
     */
    public double elapsedTime() {
        assert elapsedTime >= 0.0 : elapsedTime;
        return elapsedTime;
    }

    /**
     * Access the pseudo-random generator.
     *
     * @return the pre-existing instance (not null)
     */
    public Generator getPrGenerator() {
        assert prGenerator != null;
        return prGenerator;
    }

    /**
     * Access the PropProposal.
     *
     * @return the pre-existing instance
     */
    public PropProposal getPropProposal() {
        return propProposal;
    }

    /**
     * Access the selected Prop.
     *
     * @return the pre-existing instance, or null if none
     */
    public Prop getSelectedProp() {
        return selectedProp;
    }

    /**
     * Access the list of vehicles.
     *
     * @return the pre-existing instance (not null)
     */
    public Vehicles getVehicles() {
        assert vehicles != null;
        return vehicles;
    }

    /**
     * Access the loaded World.
     *
     * @return the pre-existing instance (not null)
     */
    public World getWorld() {
        assert world != null;
        return world;
    }

    /**
     * Pick the nearest Prop under the mouse cursor using a physics ray.
     *
     * @return the pre-existing instance, or null of none found
     */
    public static Prop pickProp() {
        Vector3f near = new Vector3f();
        Vector3f far = new Vector3f();
        MavDemo2.findAppState(View.class).mouseRay(near, far);

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        List<PhysicsRayTestResult> results = physicsSpace.rayTest(near, far);
        /*
         * Calculate the offset from near end to the far end.
         */
        Vector3f offset = far.subtract(near);
        /*
         * Collision results are sorted by increasing distance from the camera,
         * so the first result is also the nearest one.
         */
        for (PhysicsRayTestResult result : results) {
            /*
             * If the dot product of the normal with the offset is negative,
             * then the triangle is facing the camera.
             */
            Vector3f worldNormal = result.getHitNormalLocal(null);
            PhysicsCollisionObject pco = result.getCollisionObject();
            float dotProduct = offset.dot(worldNormal);
            if (dotProduct < 0f) {
                Object appData = pco.getApplicationData();
                if (appData instanceof Prop) {
                    return (Prop) appData;
                }
            }
        }

        return null;
    }

    /**
     * Pick the nearest supporting surface under the mouse cursor using a
     * physics ray.
     *
     * @param minCosine the minimum cosine of the slope angle (&le;1, &gt;-1)
     * @param spacing the extra separation in the normal direction (in psu)
     * @param storeLocation storage for the location in physics-space
     * coordinates (not null)
     * @return a pre-existing rigid body, or null if none found
     */
    public PhysicsRigidBody pickSupportBody(float minCosine, float spacing,
            Vector3f storeLocation) {
        Validate.inRange(minCosine, "min cosine", -1f, 1f);
        Validate.nonNull(storeLocation, "storage for location");

        Vector3f near = new Vector3f();
        Vector3f far = new Vector3f();
        View view = MavDemo2.findAppState(View.class);
        view.mouseRay(near, far);

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        List<PhysicsRayTestResult> hits = physicsSpace.rayTestRaw(near, far);
        /*
         * Find the closest contact that's flat enough.
         */
        Vector3f closestWorldNormal = null;
        float closestFraction = 9f;
        PhysicsRigidBody closestBody = null;
        for (PhysicsRayTestResult hit : hits) {
            Vector3f worldNormal = hit.getHitNormalLocal(null);
            if (worldNormal.y > minCosine) {
                float hitFraction = hit.getHitFraction();
                if (hitFraction < closestFraction) {
                    PhysicsCollisionObject pco = hit.getCollisionObject();
                    if (pco instanceof PhysicsRigidBody) {
                        closestWorldNormal = worldNormal; // alias
                        closestFraction = hitFraction;
                        closestBody = (PhysicsRigidBody) pco;
                    }
                }
            }
        }

        if (closestBody != null) {
            Vector3f location
                    = MyVector3f.lerp(closestFraction, near, far, null);
            MyVector3f.accumulateScaled(location, closestWorldNormal, spacing);
            storeLocation.set(location);
        }

        return closestBody;
    }

    /**
     * Reset the elapsed-time accumulator.
     */
    public void resetElapsedTime() {
        elapsedTime = 0.0;
    }

    /**
     * TODO
     *
     * @param prop
     */
    public void selectProp(Prop prop) {
        selectedProp = prop;
    }

    /**
     * TODO
     *
     * @param newVehicle (not loaded)
     */
    public void setVehicle(Vehicle newVehicle) {
        AssetManager assetManager = world.getAssetManager();
        newVehicle.load(assetManager);

        vehicles.removeSelected();

        addVehicle(newVehicle);
        Cameras.update();
    }

    /**
     * TODO
     *
     * @param newWorld (not loaded)
     */
    public void setWorld(World newWorld) {
        AssetManager assetManager = world.getAssetManager();
        newWorld.load(assetManager);

        Vehicle selectedVehicle = vehicles.getSelected();
        vehicles.removeAll();

        View view = MavDemo2.findAppState(View.class);
        Sky sky = view.getSky();
        sky.removeFromWorld();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        Node parentNode = world.getParentNode();
        world.detach();
        world = newWorld;
        Application application = MavDemo2.getApplication();
        world.attach(application, parentNode, physicsSpace);

        sky.addToWorld(world);
        if (selectedVehicle != null) {
            addVehicle(selectedVehicle);
        }
    }

    /**
     * Calculate the duration of the most recent physics tick.
     *
     * @return the duration (in seconds, &ge;0)
     */
    public float tickDuration() {
        Timer timer = MavDemo2.getApplication().getTimer();
        long numTicksPerSecond = timer.getResolution();
        float result = numTicks / (float) numTicksPerSecond;

        assert result >= 0f : result;
        return result;
    }
    // *************************************************************************
    // GlobalAudio methods

    /**
     * Determine the effective global audio volume.
     *
     * @return the volume (linear scale, &ge;0, &le;1)
     */
    @Override
    public float effectiveVolume() {
        return 1f; // TODO
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just after the physics has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        assert space == world.getPhysicsSpace();
        assert timeStep >= 0f : timeStep;

        elapsedTime += timeStep;

        Timer timer = MavDemo2.getApplication().getTimer();
        numTicks = timer.getTime() - preTickCount;
    }

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        assert space == world.getPhysicsSpace();

        Timer timer = MavDemo2.getApplication().getTimer();
        preTickCount = timer.getTime();
    }
}
