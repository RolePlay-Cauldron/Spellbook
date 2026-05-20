package com.github.roleplaycauldron.spellbook.effect;

import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import org.joml.Vector3f;

import java.util.List;

/**
 * Represents an instance of an effect with its shape, transforms, modifiers, particle emitter, and direction provider.
 */
public class EffectInstance {

    private final Shape shape;

    private final List<Transform> transforms;

    private final List<EffectModifier> modifiers;

    private final ParticleEmitter particleEmitter;

    private final DirectionProvider directionProvider;

    /**
     * Creates a new EffectInstance
     *
     * @param shape             the shape of the effect
     * @param transforms        the transforms to apply to the effect
     * @param modifiers         the modifiers to apply to the effect
     * @param particleEmitter   the particle emitter to use for the effect
     * @param directionProvider the direction provider to use for the effect
     */
    public EffectInstance(Shape shape,
                          List<Transform> transforms,
                          List<EffectModifier> modifiers,
                          ParticleEmitter particleEmitter,
                          DirectionProvider directionProvider) {
        this.shape = shape;
        this.transforms = List.copyOf(transforms);
        this.modifiers = List.copyOf(modifiers);
        this.particleEmitter = particleEmitter;
        this.directionProvider = directionProvider;
    }

    /**
     * Renders the effect at the given context
     *
     * @param context the context to render the effect at
     */
    public void render(EffectContext context) {
        render(context, new EffectRenderState());
    }

    /**
     * Renders the effect using caller-owned reusable frame state.
     *
     * @param context the context to render the effect at
     * @param state   mutable render state owned by the running effect
     */
    public void render(EffectContext context, EffectRenderState state) {
        PointBuffer points = state.points();
        points.clear();
        shape.sample(
                state.shapeContext(
                        context.step(),
                        context.timeSeconds(),
                        context.origin(),
                        context.target()
                ),
                points
        );

        for (Transform transform : transforms) {
            Transform.PreparedTransform prepared = transform.prepare(context);
            for (int i = 0; i < points.size(); i++) {
                prepared.apply(points, i);
            }
        }

        for (EffectModifier modifier : modifiers) {
            modifier.apply(points, context);
        }

        boolean requiresDirection = particleEmitter.requiresDirection();
        Vector3f direction = state.direction();
        double originX = context.origin().getX();
        double originY = context.origin().getY();
        double originZ = context.origin().getZ();

        for (int i = 0; i < points.size(); i++) {
            float localX = points.x(i);
            float localY = points.y(i);
            float localZ = points.z(i);
            if (requiresDirection) {
                directionProvider.getDirection(localX, localY, localZ, context, direction);
            } else {
                direction.set(0, 0, 0);
            }

            particleEmitter.spawn(
                    context,
                    localX,
                    localY,
                    localZ,
                    originX + localX,
                    originY + localY,
                    originZ + localZ,
                    direction.x,
                    direction.y,
                    direction.z
            );
        }
    }
}
