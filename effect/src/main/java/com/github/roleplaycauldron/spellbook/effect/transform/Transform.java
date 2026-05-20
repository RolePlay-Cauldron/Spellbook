package com.github.roleplaycauldron.spellbook.effect.transform;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;

/**
 * Represents a transformation that mutates buffered effect points.
 */
@FunctionalInterface
public interface Transform {

    /**
     * Prepares a frame-specific transform operation.
     *
     * @param context the effect context containing relevant information, such as
     *                the origin, target location, current tick, and associated viewers
     * @return prepared transform operation for this frame
     */
    default PreparedTransform prepare(EffectContext context) {
        return (points, index) -> apply(points, index, context);
    }

    /**
     * Applies this transform to a point in the provided buffer.
     *
     * @param points  frame-local point buffer
     * @param index   point index to mutate
     * @param context effect context
     */
    void apply(PointBuffer points, int index, EffectContext context);

    /**
     * Prepared transform operation for one render frame.
     */
    @FunctionalInterface
    interface PreparedTransform {
        void apply(PointBuffer points, int index);
    }
}
