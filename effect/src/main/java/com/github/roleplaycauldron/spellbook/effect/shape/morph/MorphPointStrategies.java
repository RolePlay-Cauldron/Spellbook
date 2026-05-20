package com.github.roleplaycauldron.spellbook.effect.shape.morph;

/**
 * Built-in morph point pairing and resampling strategies.
 */
public final class MorphPointStrategies {

    /**
     * Pairs source and target points by equal index and emits the smaller point
     * count.
     */
    public static final MorphPointStrategy MATCH_INDEX = new BuiltInStrategy() {
        @Override
        public int outputSize(int sourceSize, int targetSize) {
            return Math.min(sourceSize, targetSize);
        }

        @Override
        public int sourceIndex(int outputIndex, int outputSize, int sourceSize) {
            return outputIndex;
        }

        @Override
        public int targetIndex(int outputIndex, int outputSize, int targetSize) {
            return outputIndex;
        }
    };

    /**
     * Emits the target point count and maps source indexes proportionally onto
     * that output size.
     */
    public static final MorphPointStrategy RESAMPLE_SOURCE_TO_TARGET = new BuiltInStrategy() {
        @Override
        public int outputSize(int sourceSize, int targetSize) {
            return targetSize;
        }

        @Override
        public int sourceIndex(int outputIndex, int outputSize, int sourceSize) {
            return proportionalIndex(outputIndex, outputSize, sourceSize);
        }

        @Override
        public int targetIndex(int outputIndex, int outputSize, int targetSize) {
            return outputIndex;
        }
    };

    /**
     * Emits the source point count and maps target indexes proportionally onto
     * that output size.
     */
    public static final MorphPointStrategy RESAMPLE_TARGET_TO_SOURCE = new BuiltInStrategy() {
        @Override
        public int outputSize(int sourceSize, int targetSize) {
            return sourceSize;
        }

        @Override
        public int sourceIndex(int outputIndex, int outputSize, int sourceSize) {
            return outputIndex;
        }

        @Override
        public int targetIndex(int outputIndex, int outputSize, int targetSize) {
            return proportionalIndex(outputIndex, outputSize, targetSize);
        }
    };

    /**
     * Emits the larger point count and maps both source and target indexes
     * proportionally onto that output size.
     */
    public static final MorphPointStrategy RESAMPLE_TO_MAX = new BuiltInStrategy() {
        @Override
        public int outputSize(int sourceSize, int targetSize) {
            return Math.max(sourceSize, targetSize);
        }

        @Override
        public int sourceIndex(int outputIndex, int outputSize, int sourceSize) {
            return proportionalIndex(outputIndex, outputSize, sourceSize);
        }

        @Override
        public int targetIndex(int outputIndex, int outputSize, int targetSize) {
            return proportionalIndex(outputIndex, outputSize, targetSize);
        }
    };

    private MorphPointStrategies() {
    }

    /**
     * Returns the index-matching strategy.
     *
     * @return strategy that pairs equal indexes and emits the smaller point count
     */
    public static MorphPointStrategy matchIndex() {
        return MATCH_INDEX;
    }

    /**
     * Returns the source-to-target resampling strategy.
     *
     * @return strategy that emits the target point count
     */
    public static MorphPointStrategy resampleSourceToTarget() {
        return RESAMPLE_SOURCE_TO_TARGET;
    }

    /**
     * Returns the target-to-source resampling strategy.
     *
     * @return strategy that emits the source point count
     */
    public static MorphPointStrategy resampleTargetToSource() {
        return RESAMPLE_TARGET_TO_SOURCE;
    }

    /**
     * Returns the max-count resampling strategy.
     *
     * @return strategy that emits the larger child point count
     */
    public static MorphPointStrategy resampleToMax() {
        return RESAMPLE_TO_MAX;
    }

    private static int proportionalIndex(int outputIndex, int outputSize, int inputSize) {
        if (inputSize <= 1 || outputSize <= 1) {
            return 0;
        }
        return Math.round(outputIndex * (inputSize - 1) / (float) (outputSize - 1));
    }

    private abstract static class BuiltInStrategy implements MorphPointStrategy {
    }
}
