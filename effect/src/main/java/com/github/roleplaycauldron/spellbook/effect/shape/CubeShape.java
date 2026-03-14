package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class CubeShape implements Shape {

    private final float size;

    private final int pointsPerEdge;

    public CubeShape(float size, int pointsPerEdge) {
        if (size <= 0f) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (pointsPerEdge < 2) {
            throw new IllegalArgumentException("pointsPerEdge must be >= 2");
        }

        this.size = size;
        this.pointsPerEdge = pointsPerEdge;
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        float h = size / 2f;
        List<Vector3f> result = new ArrayList<>(12 * pointsPerEdge);

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-h, -h, -h),
                new Vector3f(h, -h, -h),
                new Vector3f(h, -h, h),
                new Vector3f(-h, -h, h),

                new Vector3f(-h, h, -h),
                new Vector3f(h, h, -h),
                new Vector3f(h, h, h),
                new Vector3f(-h, h, h)
        };

        int[][] edges = new int[][]{
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            Vector3f start = corners[edge[0]];
            Vector3f end = corners[edge[1]];

            for (int i = 0; i < pointsPerEdge; i++) {
                float t = (float) i / (pointsPerEdge - 1);

                float x = start.x + (end.x - start.x) * t;
                float y = start.y + (end.y - start.y) * t;
                float z = start.z + (end.z - start.z) * t;

                result.add(new Vector3f(x, y, z));
            }
        }
        return result;
    }
}