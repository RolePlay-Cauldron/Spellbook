package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D spiral helix shape composed of multiple strands.
 * This class is designed to generate a series of points in three-dimensional space
 * that form a spiral structure, which can be used for particle effects or other spatial representations.
 * The distribution of points is determined by various parameters such as the number of strands,
 * the number of particles per strand, the helix radius, height, curve, and rotation speed.
 */
public final class SpiralHelixShape implements Shape {

    private final int strands;

    private final int particlesPerStrand;

    private final float radius;

    private final float height;

    private final float curve;

    private final float rotationSpeed;

    private final boolean reverse;

    /**
     * Constructs a new SpiralHelixShape that represents a 3D spiral helix structure.
     * This structure consists of multiple strands of particles arranged in a helical pattern.
     * The distribution of particles and the structure of the helix are determined by
     * the specified parameters.
     *
     * @param strands            the number of strands in the helix; must be greater than 0
     * @param particlesPerStrand the number of particles along each strand; must be greater than 0
     * @param radius             the maximum radius of the helix; must be greater than 0
     * @param height             the total height of the helix; can be 0 or positive
     * @param curve              the curvature factor of the strands, which determines the rate of rotation and spread
     * @param rotationSpeed      the speed of rotation applied to the helix structure over time
     *                           (this determines dynamic transformations during animation or simulation)
     * @param reverse            whether the helix should be generated in the reverse direction
     * @throws IllegalArgumentException if strands, particlesPerStrand, or radius are less than or equal to 0
     */
    public SpiralHelixShape(
            int strands,
            int particlesPerStrand,
            float radius,
            float height,
            float curve,
            float rotationSpeed,
            boolean reverse
    ) {
        if (strands <= 0) {
            throw new IllegalArgumentException("strands must be > 0");
        }
        if (particlesPerStrand <= 0) {
            throw new IllegalArgumentException("particlesPerStrand must be > 0");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be > 0");
        }

        this.strands = strands;
        this.particlesPerStrand = particlesPerStrand;
        this.radius = radius;
        this.height = height;
        this.curve = curve;
        this.rotationSpeed = rotationSpeed;
        this.reverse = reverse;
    }

    /**
     * Constructs a new SpiralHelixShape that represents a 3D spiral helix structure.
     * This structure consists of multiple strands of particles arranged in a helical pattern.
     * The distribution of particles and the structure of the helix are determined by
     * the specified parameters.
     *
     * @param strands            the number of strands in the helix; must be greater than 0
     * @param particlesPerStrand the number of particles along each strand; must be greater than 0
     * @param radius             the maximum radius of the helix; must be greater than 0
     * @param height             the total height of the helix; can be 0 or positive
     * @param curve              the curvature factor of the strands, which determines the rate of rotation and spread
     * @param rotationSpeed      the speed of rotation applied to the helix structure over time
     *                           (this determines dynamic transformations during animation or simulation)
     * @throws IllegalArgumentException if strands, particlesPerStrand, or radius are less than or equal to 0
     */
    public SpiralHelixShape(
            int strands,
            int particlesPerStrand,
            float radius,
            float height,
            float curve,
            float rotationSpeed
    ) {
        this(strands, particlesPerStrand, radius, height, curve, rotationSpeed, false);
    }

    @Override
    public List<Vector3f> sample(ShapeContext context) {
        List<Vector3f> result = new ArrayList<>(strands * particlesPerStrand);

        float rotation = context.step() * rotationSpeed;
        float curveDirection = reverse ? -1.0f : 1.0f;

        for (int i = 0; i < strands; i++) {
            for (int j = 1; j <= particlesPerStrand; j++) {
                float ratio = (float) j / particlesPerStrand;

                double angle =
                        curveDirection * curve * ratio * 2.0 * Math.PI / strands
                                + (2.0 * Math.PI * i / strands)
                                + rotation;

                float currentRadius = ratio * radius;

                float x = (float) (Math.cos(angle) * currentRadius);
                float z = (float) (Math.sin(angle) * currentRadius);
                float y = ratio * height;

                result.add(new Vector3f(x, y, z));
            }
        }

        return result;
    }
}