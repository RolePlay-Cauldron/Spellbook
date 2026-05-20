package com.github.roleplaycauldron.spellbook.effect;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable frame-local storage for effect points.
 * <p>
 * A {@code PointBuffer} is owned by the effect render pipeline for a single frame.
 * Shapes, transforms, and modifiers may mutate the buffer while that frame is being
 * rendered, but must not retain references to the buffer or its point values beyond
 * the current callback unless they copy the values.
 */
public final class PointBuffer {

    private static final int COMPONENTS = 3;

    private float[] coordinates;

    private int size;

    /**
     * Creates a new point buffer with a small default capacity.
     */
    public PointBuffer() {
        this(32);
    }

    /**
     * Creates a new point buffer with the given initial point capacity.
     *
     * @param initialCapacity the initial number of points the buffer can hold
     */
    public PointBuffer(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0");
        }
        this.coordinates = new float[Math.max(1, initialCapacity) * COMPONENTS];
    }

    /**
     * Removes all points while retaining allocated storage for reuse.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Returns the number of points in the buffer.
     *
     * @return point count
     */
    public int size() {
        return size;
    }

    /**
     * Returns whether the buffer has no points.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Ensures capacity for at least the requested number of points.
     *
     * @param pointCapacity point capacity to reserve
     */
    public void ensureCapacity(int pointCapacity) {
        if (pointCapacity < 0) {
            throw new IllegalArgumentException("pointCapacity must be >= 0");
        }
        int required = pointCapacity * COMPONENTS;
        if (required <= coordinates.length) {
            return;
        }

        int newLength = coordinates.length;
        while (newLength < required) {
            newLength *= 2;
        }

        float[] expanded = new float[newLength];
        System.arraycopy(coordinates, 0, expanded, 0, size * COMPONENTS);
        coordinates = expanded;
    }

    /**
     * Appends a point to the buffer.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void add(float x, float y, float z) {
        ensureCapacity(size + 1);
        int offset = offset(size);
        coordinates[offset] = x;
        coordinates[offset + 1] = y;
        coordinates[offset + 2] = z;
        size++;
    }

    /**
     * Appends a copy of the given point to the buffer.
     *
     * @param point point to copy
     */
    public void add(Vector3f point) {
        Objects.requireNonNull(point, "point");
        add(point.x, point.y, point.z);
    }

    /**
     * Replaces a point in the buffer.
     *
     * @param index point index
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     */
    public void set(int index, float x, float y, float z) {
        int offset = checkedOffset(index);
        coordinates[offset] = x;
        coordinates[offset + 1] = y;
        coordinates[offset + 2] = z;
    }

    /**
     * Copies a point into the destination vector.
     *
     * @param index       point index
     * @param destination destination vector
     * @return the destination vector
     */
    public Vector3f get(int index, Vector3f destination) {
        Objects.requireNonNull(destination, "destination");
        int offset = checkedOffset(index);
        return destination.set(
                coordinates[offset],
                coordinates[offset + 1],
                coordinates[offset + 2]
        );
    }

    public float x(int index) {
        return coordinates[checkedOffset(index)];
    }

    public float y(int index) {
        return coordinates[checkedOffset(index) + 1];
    }

    public float z(int index) {
        return coordinates[checkedOffset(index) + 2];
    }

    public void setX(int index, float x) {
        coordinates[checkedOffset(index)] = x;
    }

    public void setY(int index, float y) {
        coordinates[checkedOffset(index) + 1] = y;
    }

    public void setZ(int index, float z) {
        coordinates[checkedOffset(index) + 2] = z;
    }

    /**
     * Adds deltas to the point at the given index.
     *
     * @param index point index
     * @param dx    x delta
     * @param dy    y delta
     * @param dz    z delta
     */
    public void translate(int index, float dx, float dy, float dz) {
        int offset = checkedOffset(index);
        coordinates[offset] += dx;
        coordinates[offset + 1] += dy;
        coordinates[offset + 2] += dz;
    }

    /**
     * Removes the point at the given index while preserving order.
     *
     * @param index point index
     */
    public void remove(int index) {
        checkedOffset(index);
        int trailingPoints = size - index - 1;
        if (trailingPoints > 0) {
            System.arraycopy(
                    coordinates,
                    offset(index + 1),
                    coordinates,
                    offset(index),
                    trailingPoints * COMPONENTS
            );
        }
        size--;
    }

    /**
     * Swaps two points in the buffer.
     *
     * @param first  first point index
     * @param second second point index
     */
    public void swap(int first, int second) {
        int firstOffset = checkedOffset(first);
        int secondOffset = checkedOffset(second);
        if (first == second) {
            return;
        }

        for (int i = 0; i < COMPONENTS; i++) {
            float temp = coordinates[firstOffset + i];
            coordinates[firstOffset + i] = coordinates[secondOffset + i];
            coordinates[secondOffset + i] = temp;
        }
    }

    /**
     * Iterates over the current point values.
     *
     * @param consumer consumer receiving point coordinates
     */
    public void forEach(PointConsumer consumer) {
        Objects.requireNonNull(consumer, "consumer");
        for (int i = 0; i < size; i++) {
            int offset = offset(i);
            consumer.accept(i, coordinates[offset], coordinates[offset + 1], coordinates[offset + 2]);
        }
    }

    /**
     * Creates a defensive vector snapshot of the current buffer contents.
     *
     * @return copied vector list
     */
    public List<Vector3f> toVectorList() {
        List<Vector3f> points = new ArrayList<>(size);
        forEach((index, x, y, z) -> points.add(new Vector3f(x, y, z)));
        return points;
    }

    private int checkedOffset(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds for size " + size);
        }
        return offset(index);
    }

    private int offset(int index) {
        return index * COMPONENTS;
    }

    /**
     * Receives point coordinates from a point buffer.
     */
    @FunctionalInterface
    public interface PointConsumer {
        void accept(int index, float x, float y, float z);
    }
}
