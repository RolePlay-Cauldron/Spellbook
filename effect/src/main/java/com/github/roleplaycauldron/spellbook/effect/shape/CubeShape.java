package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;

/**
 * A {@code CubeShape} represents a cubic-shaped structure that generates a series of 3D points
 * along the edges of the cube. The cube is defined by its size and the number of points sampled
 * per edge.
 * <p>
 * This shape distributes points evenly along the 12 edges of the cube, which is centered at the origin.
 */
public final class CubeShape implements Shape {

    private final float[] cachedPoints;

    /**
     * Constructs a {@code CubeShape} object with a specified size and the number of points
     * to be generated along each edge of the cube. The cube is centered at the origin.
     *
     * @param size          the length of each edge of the cube; must be greater than 0
     * @param pointsPerEdge the number of points to be generated along each edge of the cube; must be at least 2
     * @throws IllegalArgumentException if {@code size} is less than or equal to 0
     * @throws IllegalArgumentException if {@code pointsPerEdge} is less than 2
     */
    public CubeShape(float size, int pointsPerEdge) {
        if (size <= 0f) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (pointsPerEdge < 2) {
            throw new IllegalArgumentException("pointsPerEdge must be >= 2");
        }

        this.cachedPoints = buildPoints(size, pointsPerEdge);
    }

    @Override
    public void sample(ShapeContext context, PointBuffer points) {
        points.ensureCapacity(points.size() + cachedPoints.length / 3);
        for (int i = 0; i < cachedPoints.length; i += 3) {
            points.add(cachedPoints[i], cachedPoints[i + 1], cachedPoints[i + 2]);
        }
    }

    private float[] buildPoints(float size, int pointsPerEdge) {
        float h = size / 2f;
        float[] result = new float[12 * pointsPerEdge * 3];
        int index = 0;

        float[][] corners = new float[][]{
                {-h, -h, -h},
                {h, -h, -h},
                {h, -h, h},
                {-h, -h, h},

                {-h, h, -h},
                {h, h, -h},
                {h, h, h},
                {-h, h, h}
        };

        int[][] edges = new int[][]{
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            float[] start = corners[edge[0]];
            float[] end = corners[edge[1]];

            for (int i = 0; i < pointsPerEdge; i++) {
                float t = (float) i / (pointsPerEdge - 1);

                result[index++] = start[0] + (end[0] - start[0]) * t;
                result[index++] = start[1] + (end[1] - start[1]) * t;
                result[index++] = start[2] + (end[2] - start[2]) * t;

            }
        }
        return result;
    }
}
