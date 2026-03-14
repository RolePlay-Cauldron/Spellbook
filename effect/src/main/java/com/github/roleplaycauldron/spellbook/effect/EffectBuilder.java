package com.github.roleplaycauldron.spellbook.effect;

import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.emitter.StandardParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.LookAtTransform;
import com.github.roleplaycauldron.spellbook.effect.transform.RotationTransform;
import com.github.roleplaycauldron.spellbook.effect.transform.Transform;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.Particle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder class for constructing {@link EffectInstance} objects.
 */
public class EffectBuilder {

    private final List<Transform> transforms = new ArrayList<>();

    private final List<EffectModifier> modifiers = new ArrayList<>();

    private Shape shape;

    private ParticleEmitter particleEmitter;

    private DirectionProvider directionProvider = (point, context) -> new Vector3f(0, 0, 0);

    private EffectBuilder() {
    }

    /**
     * Creates a new EffectBuilder instance
     *
     * @return a new EffectBuilder instance
     */
    public static EffectBuilder create() {
        return new EffectBuilder();
    }

    /**
     * Sets the shape of the effect
     *
     * @param shape the {@link Shape} to set. Cannot be null.
     * @return the {@link EffectBuilder} instance.
     */
    public EffectBuilder shape(Shape shape) {
        this.shape = shape;
        return this;
    }

    /**
     * Adds a transform to the effect
     *
     * @param transform the {@link Transform} to add
     * @return the {@link EffectBuilder} instance
     */
    public EffectBuilder transform(Transform transform) {
        this.transforms.add(transform);
        return this;
    }

    /**
     * Applies a rotational transformation to the effect using the specified yaw, pitch,
     * and roll angles, and adds the rotation as a transform to the effect.
     *
     * @param yaw   the yaw angle in degrees, representing rotation around the vertical axis
     * @param pitch the pitch angle in degrees, representing rotation around the lateral axis
     * @param roll  the roll angle in degrees, representing rotation around the longitudinal axis
     * @return the {@link EffectBuilder} instance with the applied rotational transformation
     */
    public EffectBuilder rotate(float yaw, float pitch, float roll) {
        return transform(new RotationTransform(yaw, pitch, roll));
    }

    /**
     * Applies a rotational transformation to the effect using the specified quaternion rotation.
     * The rotation is added as a transform to the effect.
     *
     * @param rotation the {@link Quaternionf} representing the rotation to apply
     * @return the {@link EffectBuilder} instance with the applied rotational transformation
     */
    public EffectBuilder rotate(Quaternionf rotation) {
        return transform(new RotationTransform(rotation));
    }

    /**
     * Applies a look-at transformation to the effect, making it face towards the target.
     * The target is determined by the effect's position and the direction it is facing.
     *
     * @return the {@link EffectBuilder} instance with the applied look-at transformation
     */
    public EffectBuilder lookAtTarget() {
        return transform(new LookAtTransform());
    }

    /**
     * Applies a look-at transformation to the effect, aligning its forward direction
     * toward a target point based on the specified local forward axis. This transformation
     * can be useful for orienting effects to face a specific direction or target dynamically
     * in 3D space.
     *
     * @param localForwardAxis the forward axis vector representing the default forward direction
     *                         in the local coordinate space. Must be a normalized vector.
     * @return the {@link EffectBuilder} instance with the applied look-at transformation.
     */
    public EffectBuilder lookAtTarget(Vector3f localForwardAxis) {
        return transform(new LookAtTransform(localForwardAxis));
    }

    /**
     * Applies a translational transformation to the effect using the specified x, y, and z values.
     * This transformation shifts the effect's position in 3D space.
     *
     * @param x the x-coordinate of the translation
     * @param y the y-coordinate of the translation
     * @param z the z-coordinate of the translation
     * @return the {@link EffectBuilder} instance with the applied translational transformation
     */
    public EffectBuilder translate(float x, float y, float z) {
        return transform(new TranslateTransform(x, y, z));
    }

    /**
     * Applies a translational transformation to the effect using a specified translation vector.
     * This transformation shifts the effect's position in 3D space based on the given vector.
     *
     * @param translation the {@link Vector3f} specifying the translation to apply. Cannot be null.
     * @return the {@link EffectBuilder} instance with the applied translational transformation.
     */
    public EffectBuilder translate(Vector3f translation) {
        return transform(new TranslateTransform(translation));
    }

    /**
     * Adds an effect modifier to the effect. Effect modifiers can be used to modify the
     *
     * @param modifier the {@link EffectModifier} to add
     * @return the {@link EffectBuilder} instance
     */
    public EffectBuilder modifier(EffectModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    /**
     * Sets the particle emitter for the effect.
     *
     * @param particleEmitter the particle emitter to set
     * @return the {@link EffectBuilder} instance
     */
    public EffectBuilder particle(ParticleEmitter particleEmitter) {
        this.particleEmitter = particleEmitter;
        return this;
    }

    /**
     * Sets the particle for the effect.
     *
     * @param particle the particle to set
     * @return the {@link EffectBuilder} instance
     */
    public EffectBuilder particle(Particle particle) {
        return particle(StandardParticleEmitter.of(particle));
    }

    /**
     * Sets the particle for the effect.
     *
     * @param particle the particle to set
     * @param count    the number of particles to emit
     * @return the {@link EffectBuilder} instance
     */
    public EffectBuilder particle(Particle particle, int count) {
        return particle(StandardParticleEmitter.of(particle, count));
    }

    /**
     * Sets the particle for the effect using the specified particle specification.
     * The particle specification defines the type of particle, count, offsets, and other properties.
     * This method constructs a new {@link StandardParticleEmitter} based on the given specification.
     *
     * @param <T>  the type of data associated with the particle specification
     * @param spec the {@link ParticleSpec} defining the particle's properties. Cannot be null.
     * @return the {@link EffectBuilder} instance with the specified particle emitter set
     */
    public <T> EffectBuilder particle(ParticleSpec<T> spec) {
        return particle(new StandardParticleEmitter<>(spec));
    }

    /**
     * Sets the direction provider for the effect. The direction provider determines the directional
     * vector to be used during the rendering of the effect, allowing for dynamic or context-dependent
     * directional calculations.
     *
     * @param directionProvider the {@link DirectionProvider} to set. Cannot be null.
     *                          This provider is used to calculate the direction vector based
     *                          on the given point and rendering context.
     * @return the {@link EffectBuilder} instance with the specified direction provider set.
     */
    public EffectBuilder direction(DirectionProvider directionProvider) {
        this.directionProvider = directionProvider;
        return this;
    }

    /**
     * Builds the {@link EffectInstance} with the configured settings.
     * Validates that required components (shape, particleEmitter) are set before building.
     *
     * @return the {@link EffectInstance} with all configured settings.
     */
    public EffectInstance build() {

        if (shape == null)
            throw new IllegalStateException("Shape must be set");

        if (particleEmitter == null)
            throw new IllegalStateException("ParticleSpec must be set");

        return new EffectInstance(
                shape,
                transforms,
                modifiers,
                particleEmitter,
                directionProvider
        );
    }
}
