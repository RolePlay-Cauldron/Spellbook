package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;

/**
 * Represents a helical shape for particle or point generation, defined by configurable strands,
 * radius, height, number of turns, and rotation speed. The helix is sampled by evenly
 * distributing points across multiple strands that twist around a central axis.
 */
public final class HelixShape implements Shape {

    private final int strands;

    private final int particlesPerStrand;

    private final float radius;

    private final float height;

    private final float turns;

    private final float rotationSpeed;

    /**
     * Constructs a HelixShape object with the specified parameters to define the properties
     * of a helical shape. The helix consists of multiple strands, with particles
     * uniformly distributed along each strand, spiraling around a central axis.
     *
     * @param strands            the number of strands in the helix; must be greater than 0
     * @param particlesPerStrand the number of particles per strand; must be greater than 0
     * @param radius             the radius of the helix defining the horizontal spread; must be greater than 0
     * @param height             the total height of the helix; must be greater than or equal to 0
     * @param turns              the total number of complete turns of the helix; must be greater than 0
     * @param rotationSpeed      the rotation speed of the helix, influencing its dynamic behavior
     *                           during sampling or animation
     * @throws IllegalArgumentException if any parameter value fails its respective validation
     */
    public HelixShape(
            int strands,
            int particlesPerStrand,
            float radius,
            float height,
            float turns,
            float rotationSpeed
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
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        if (turns <= 0) {
            throw new IllegalArgumentException("turns must be > 0");
        }

        this.strands = strands;
        this.particlesPerStrand = particlesPerStrand;
        this.radius = radius;
        this.height = height;
        this.turns = turns;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void sample(ShapeContext context, PointBuffer points) {
        points.ensureCapacity(points.size() + strands * particlesPerStrand);
        float baseRotation = context.step() * rotationSpeed;

        for (int i = 0; i < strands; i++) {
            float strandOffset = (float) (2.0 * Math.PI * i / strands);

            for (int j = 0; j < particlesPerStrand; j++) {
                float ratio = particlesPerStrand == 1
                        ? 0f
                        : (float) j / (particlesPerStrand - 1);

                float angle = baseRotation
                        + strandOffset
                        + ratio * turns * (float) (2.0 * Math.PI);

                float x = (float) Math.cos(angle) * radius;
                float y = ratio * height;
                float z = (float) Math.sin(angle) * radius;

                points.add(x, y, z);
            }
        }
    }
}
