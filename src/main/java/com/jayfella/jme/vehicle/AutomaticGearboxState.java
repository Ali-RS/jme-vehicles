package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import java.util.logging.Logger;

/**
 * The automatic transmission of a Car.
 */
public class AutomaticGearboxState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AutomaticGearboxState.class.getName());
    // *************************************************************************
    // fields

    private Car car;
    private GearBox gearBox;
    private int wheelCount;
    final private Vehicle vehicle;

    public AutomaticGearboxState(Vehicle vehicle) {
        this.vehicle = vehicle;

        if (vehicle instanceof Car) {
            car = (Car) vehicle;
        }
    }

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
        wheelCount = car.countWheels();
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        gearBox = vehicle.getGearBox();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        Engine engine = vehicle.getEngine();
        boolean isEngineRunning = engine.isRunning();
        if (!isEngineRunning) {
            engine.setRpmFraction(0f);
            return;
        }

        GearBox gearbox = vehicle.getGearBox();
        float signedKph = vehicle.getSpeed(SpeedUnit.KPH);

        int gearNum = gearbox.getEngagedGearNum();
        Gear gear = gearBox.getGear(gearNum);
        float minKph = gear.getMinKph();
        float maxKph = gear.getMaxKph();
        if (gear.isReverse()) {
            int numGears = gearbox.countReverseGears();
            if (signedKph > minKph && gearNum < -1) {
                ++gearNum;
                //System.out.println("Downshifting to " + gearNum);
            } else if (signedKph < maxKph && gearNum > -numGears) {
                --gearNum;
                //System.out.println("Upshifting to " + gearNum);
            }
            gearbox.engageGearNum(gearNum); // TODO not instantaneous

        } else if (gear.isForward()) {
            int numGears = gearbox.countForwardGears();
            if (signedKph < minKph && gearNum > 1) {
                --gearNum;
                //System.out.println("Downshifting to " + gearNum);
            } else if (signedKph > maxKph && gearNum < numGears) {
                ++gearNum;
                //System.out.println("Upshifting to " + gearNum);
            }
            gearbox.engageGearNum(gearNum); // TODO not instantaneous
        }
        // TODO handle neutral gear

        gear = gearBox.getGear(gearNum);
        float redlineKph = gear.getRedlineKph();
        float revs = signedKph / redlineKph; // as a fraction of redline
        float accelerateSignal = car.accelerateSignal();
        if (accelerateSignal == 0f) { // coasting
            revs = 0f;

        } else if (wheelCount > 0) {
            accelerateSignal = FastMath.abs(accelerateSignal);
            float boostRevs = 0f;
            for (int wheelIndex = 0; wheelIndex < wheelCount; ++wheelIndex) {
                Wheel wheel = car.getWheel(wheelIndex);
                float wheelFraction = wheel.getPowerFraction();
                float scaledSignal = wheelFraction * accelerateSignal;

                // how much the tire is slipping (0-1, 0=full traction)
                float slipFraction = 1f - wheel.getVehicleWheel().getSkidInfo();
                assert slipFraction >= 0f && slipFraction <= 1f : slipFraction;

                // If both accelerating and slipping, boost the revs.
                boostRevs += scaledSignal * slipFraction;
            }
            revs += boostRevs;
        }
        /*
         * The drivetrain has some rotational inertia, even when slipping,
         * so limit how quickly the engine speed can change.
         */
        float oldRevs = engine.getRpmFraction();
        revs = FastMath.clamp(revs, oldRevs - 2f * tpf, oldRevs + 0.2f * tpf);
        /*
         * Prevent the engine from stalling or exceeding its redline.
         */
        float idleFraction = engine.getIdleRpm() / engine.getRedlineRpm();
        revs = FastMath.clamp(revs, idleFraction, 1f);

        engine.setRpmFraction(revs);
    }
}
