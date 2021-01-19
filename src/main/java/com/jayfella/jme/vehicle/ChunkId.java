package com.jayfella.jme.vehicle;

import java.util.logging.Logger;

/**
 * Identify a single rectangular chunk in a 3-D world. Immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ChunkId {
    // *************************************************************************
    // constants and loggers

    /**
     * ID of (0,0,0)
     */
    final public static ChunkId zero = new ChunkId(0, 0, 0);
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(ChunkId.class.getName());
    // *************************************************************************
    // fields

    /**
     * X coordinate
     */
    final private int x;
    /**
     * Y coordinate
     */
    final private int y;
    /**
     * Z coordinate
     */
    final private int z;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an ID.
     *
     * @param x the desired X coordinate
     * @param y the desired Y coordinate
     * @param z the desired Z coordinate
     */
    public ChunkId(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the X coordinate.
     *
     * @return the coordinate value
     */
    public int x() {
        return x;
    }

    /**
     * Compare the X coordinate to that of another chunk.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int xDiff(ChunkId other) {
        int result = x - other.x;
        return result;
    }

    /**
     * Determine the Y coordinate.
     *
     * @return the coordinate value
     */
    public int y() {
        return y;
    }

    /**
     * Compare the Y coordinate to that of another chunk.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int yDiff(ChunkId other) {
        int result = y - other.y;
        return result;
    }

    /**
     * Determine the Z coordinate.
     *
     * @return the coordinate value
     */
    public int z() {
        return z;
    }

    /**
     * Compare the Z coordinate to that of another chunk.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int zDiff(ChunkId other) {
        int result = z - other.z;
        return result;
    }
    // *************************************************************************
    // Object methods

    /**
     * Test for exact equivalence with another Object.
     *
     * @param otherObject the object to compare to (may be null, unaffected)
     * @return true if the objects are equivalent, otherwise false
     */
    @Override
    public boolean equals(Object otherObject) {
        boolean result;
        if (otherObject == this) {
            result = true;
        } else if (otherObject != null
                && otherObject.getClass() == getClass()) {
            ChunkId otherId = (ChunkId) otherObject;
            result = (otherId.x == x)
                    && (otherId.y == y)
                    && (otherId.z == z);
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for this ID.
     *
     * @return the value to use for hashing
     */
    @Override
    public int hashCode() {
        int result = 707;
        result = 29 * result + x;
        result = 29 * result + y;
        result = 29 * result + z;

        return result;
    }
}
