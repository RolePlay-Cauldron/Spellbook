package com.github.roleplaycauldron.spellbook.effect;

import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import org.bukkit.Location;
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
        List<Vector3f> points = shape.sample(
                new ShapeContext(
                        context.step(),
                        context.timeSeconds(),
                        context.origin(),
                        context.target()
                )
        );

        for (Transform transform : transforms) {
            for (int i = 0; i < points.size(); i++) {
                points.set(i, transform.apply(points.get(i), context));
            }
        }

        for (EffectModifier modifier : modifiers) {
            points = modifier.apply(points, context);
        }

        for (Vector3f point : points) {
            Location location = context.origin().clone().add(
                    point.x(),
                    point.y(),
                    point.z()
            );

            SpawnContext spawnContext = new SpawnContext(
                    point,
                    new Vector3f((float) location.getX(), (float) location.getY(), (float) location.getZ()),
                    directionProvider.getDirection(point, context),
                    context
            );

            particleEmitter.spawn(spawnContext);
        }
    }
}