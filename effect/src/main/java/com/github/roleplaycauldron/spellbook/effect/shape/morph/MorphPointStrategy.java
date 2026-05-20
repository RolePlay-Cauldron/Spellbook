package com.github.roleplaycauldron.spellbook.effect.shape.morph;

/**
 * Selects output size and source/target point indexes for morph sampling.
 * <p>
 * Strategies define how two sampled child shapes with possibly different point
 * counts are paired. {@link MorphShape} asks the strategy for the output point
 * count, then asks for one source index and one target index for each output
 * index.
 */
public interface MorphPointStrategy {

    /**
     * Returns the number of interpolated points the morph should emit.
     *
     * @param sourceSize sampled source point count
     * @param targetSize sampled target point count
     * @return output point count
     */
    int outputSize(int sourceSize, int targetSize);

    /**
     * Maps an output point index to a source sample index.
     *
     * @param outputIndex current output point index
     * @param outputSize  total output point count
     * @param sourceSize  sampled source point count
     * @return source point index
     */
    int sourceIndex(int outputIndex, int outputSize, int sourceSize);

    /**
     * Maps an output point index to a target sample index.
     *
     * @param outputIndex current output point index
     * @param outputSize  total output point count
     * @param targetSize  sampled target point count
     * @return target point index
     */
    int targetIndex(int outputIndex, int outputSize, int targetSize);
}
