package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import org.joml.Vector3f;

/**
 * A transformation that applies a translation to a 3D point.
 */
public class TranslateTransform implements Transform {

    private final Vector3f translation;

    /**
     * Creates a new TranslateTransform with the given translation vector.
     *
     * @param translation the translation vector to apply
     */
    public TranslateTransform(Vector3f translation) {
        this.translation = new Vector3f(translation);
    }

    /**
     * Creates a new TranslateTransform with the given translation values.
     *
     * @param x the x-coordinate of the translation
     * @param y the y-coordinate of the translation
     * @param z the z-coordinate of the translation
     */
    public TranslateTransform(float x, float y, float z) {
        this.translation = new Vector3f(x, y, z);
    }

    @Override
    public void apply(PointBuffer points, int index, EffectContext context) {
        points.translate(index, translation.x, translation.y, translation.z);
    }
}
