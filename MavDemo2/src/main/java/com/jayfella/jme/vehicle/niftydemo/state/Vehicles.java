package com.jayfella.jme.vehicle.niftydemo.state;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * The game state containing all vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Vehicles {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Vehicles.class.getName());
    // *************************************************************************
    // fields

    /**
     * instance that owns these vehicles
     */
    final private DemoState demoState;
    /**
     * list of Vehicle instances, from oldest to newest
     */
    private List<Vehicle> vehicles = new ArrayList<>(99);
    /**
     * selected Vehicle, or null if none
     */
    private Vehicle selected = null;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a collection of vehicles.
     *
     * @param owner (not null, alias created)
     */
    Vehicles(DemoState owner) {
        this.demoState = owner;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a Vehicle to the game.
     */
    void add(Vehicle newVehicle) {
        assert newVehicle != null;
        assert !vehicles.contains(newVehicle);

        vehicles.add(newVehicle);

        World world = demoState.getWorld();
        newVehicle.addToWorld(world, demoState);
    }

    /**
     * Count how many vehicles are in the game.
     *
     * @return the count (&ge;0)
     */
    public int countVehicles() {
        int count = vehicles.size();
        return count;
    }

    /**
     * Access the indexed Vehicle.
     *
     * @param index the index (&ge;0, &lt;numVehicles, 0=oldest)
     * @return the pre-existing instance (not null)
     */
    public Vehicle getVehicle(int index) {
        Validate.inRange(index, "index", 0, vehicles.size() - 1);
        Vehicle result = vehicles.get(index);

        assert result != null;
        return result;
    }

    /**
     * Access the selected Vehicle.
     *
     * @return the pre-existing Vehicle, or null if none
     */
    public Vehicle getSelected() {
        return selected;
    }

    /**
     * Test whether a Vehicle is selected.
     *
     * @return true if selected, otherwise false
     */
    public boolean isVehicleSelected() {
        if (selected == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Remove the specified Vehicle.
     */
    void remove(Vehicle vehicle) {
        vehicle.removeFromWorld();

        boolean success = vehicles.remove(vehicle);
        assert success;

        if (selected == vehicle) {
            selected = null;
        }
    }

    /**
     * Remove all vehicles.
     */
    void removeAll() {
        int numVehicles = vehicles.size();
        Vehicle[] array = new Vehicle[numVehicles];
        vehicles.toArray(array);

        for (Vehicle vehicle : array) {
            remove(vehicle);
        }
        assert vehicles.isEmpty();

        selected = null;
    }

    /**
     * Remove the selected Vehicle (if any).
     */
    public void removeSelected() {
        if (selected != null) {
            remove(selected);
        }
        assert selected == null;
    }

    /**
     * Select the specified Vehicle.
     *
     * @param vehicle the Vehicle to select (alias created) or null to deselect
     */
    public void select(Vehicle vehicle) {
        this.selected = vehicle;
    }

    /**
     * Select a Vehicle based on its cyclic index.
     *
     * @param cyclicIndex the index (0=oldest, may be negative)
     */
    public void selectIndex(int cyclicIndex) {
        if (vehicles.isEmpty()) {
            throw new IllegalStateException("There aren't any vehicles!");
        }

        int numVehicles = vehicles.size();
        int index = MyMath.modulo(cyclicIndex, numVehicles);
        selected = vehicles.get(index);
    }

    /**
     * Select a Vehicle based on its index relative to the selected Vehicle.
     *
     * @param indexOffset the amount to add cyclically to the index
     */
    public void selectNext(int indexOffset) {
        int oldIndex = vehicles.indexOf(selected);
        assert oldIndex >= 0 : oldIndex;
        int cyclicIndex = oldIndex + indexOffset;
        selectIndex(cyclicIndex);
    }
}
