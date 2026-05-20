package com.github.roleplaycauldron.spellbook.effect.shape;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.EffectModifier;
import com.github.roleplaycauldron.spellbook.effect.PointBuffer;
import com.github.roleplaycauldron.spellbook.effect.ShapeContext;
import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphPointStrategies;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphProgress;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.MorphShape;
import com.github.roleplaycauldron.spellbook.effect.shape.morph.TriggeredMorphProgress;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MorphShapeTest {

    private static Shape points(float... coordinates) {
        return (context, points) -> {
            for (int i = 0; i < coordinates.length; i += 3) {
                points.add(coordinates[i], coordinates[i + 1], coordinates[i + 2]);
            }
        };
    }

    private static ShapeContext context(int step, double timeSeconds) {
        return new ShapeContext(step, timeSeconds, null, null);
    }

    private static List<Vector3f> sample(Shape shape, ShapeContext context) {
        PointBuffer buffer = new PointBuffer();
        shape.sample(context, buffer);
        return buffer.toVectorList();
    }

    private static void assertPoint(Vector3f point, float x, float y, float z) {
        assertEquals(x, point.x, 1e-6f);
        assertEquals(y, point.y, 1e-6f);
        assertEquals(z, point.z, 1e-6f);
    }

    @Test
    void testProgressZeroSamplesSource() {
        Shape shape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .fixedProgress(0f)
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertPoint(points.getFirst(), 0, 0, 0);
    }

    @Test
    void testProgressOneSamplesTarget() {
        Shape shape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .fixedProgress(1f)
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertPoint(points.getFirst(), 10, 0, 0);
    }

    @Test
    void testPartialProgressInterpolatesPoints() {
        Shape shape = MorphShape.between(points(0, 2, 4), points(10, 6, 0))
                .fixedProgress(0.25f)
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertPoint(points.getFirst(), 2.5f, 3f, 3f);
    }

    @Test
    void testProgressIsClamped() {
        Shape belowZero = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .fixedProgress(-1f)
                .build();
        Shape aboveOne = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .fixedProgress(2f)
                .build();

        assertPoint(sample(belowZero, context(0, 0)).getFirst(), 0, 0, 0);
        assertPoint(sample(aboveOne, context(0, 0)).getFirst(), 10, 0, 0);
    }

    @Test
    void testDelayedStepAndTimeProgress() {
        Shape stepShape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .afterStep(5, 10)
                .build();
        Shape timeShape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .afterSeconds(2.0, 4.0)
                .build();

        assertPoint(sample(stepShape, context(4, 0)).getFirst(), 0, 0, 0);
        assertPoint(sample(stepShape, context(10, 0)).getFirst(), 5, 0, 0);
        assertPoint(sample(timeShape, context(0, 1.0)).getFirst(), 0, 0, 0);
        assertPoint(sample(timeShape, context(0, 4.0)).getFirst(), 5, 0, 0);
    }

    @Test
    void testTriggeredProgressBeforeAndAfterStart() {
        TriggeredMorphProgress progress = MorphProgress.triggeredOverSteps(10);
        Shape shape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .progress(progress)
                .build();

        assertPoint(sample(shape, context(5, 0)).getFirst(), 0, 0, 0);

        progress.start(5);

        assertPoint(sample(shape, context(10, 0)).getFirst(), 5, 0, 0);
    }

    @Test
    void testAnimatedChildShapesUseCurrentContext() {
        Shape source = (context, points) -> points.add(context.step(), 0, 0);
        Shape target = (context, points) -> points.add(context.step() + 10, 0, 0);
        Shape shape = MorphShape.between(source, target)
                .fixedProgress(0.5f)
                .build();

        assertPoint(sample(shape, context(2, 0)).getFirst(), 7, 0, 0);
        assertPoint(sample(shape, context(4, 0)).getFirst(), 9, 0, 0);
    }

    @Test
    void testEqualCountsPairByIndex() {
        Shape shape = MorphShape.between(points(0, 0, 0, 10, 0, 0), points(10, 0, 0, 30, 0, 0))
                .fixedProgress(0.5f)
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertEquals(2, points.size());
        assertPoint(points.get(0), 5, 0, 0);
        assertPoint(points.get(1), 20, 0, 0);
    }

    @Test
    void testMismatchedCountsCanResampleToMax() {
        Shape shape = MorphShape.between(points(0, 0, 0, 10, 0, 0), points(0, 10, 0, 10, 10, 0, 20, 10, 0))
                .fixedProgress(0.5f)
                .strategy(MorphPointStrategies.resampleToMax())
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertEquals(3, points.size());
        assertPoint(points.get(0), 0, 5, 0);
        assertPoint(points.get(1), 10, 5, 0);
        assertPoint(points.get(2), 15, 5, 0);
    }

    @Test
    void testEmptySourceOrTargetProducesNoPoints() {
        Shape empty = (context, points) -> {
        };

        assertTrue(sample(MorphShape.between(empty, points(1, 0, 0)).build(), context(0, 0)).isEmpty());
        assertTrue(sample(MorphShape.between(points(1, 0, 0), empty).build(), context(0, 0)).isEmpty());
    }

    @Test
    void testNestedMorphShapesKeepParentSamplesIntact() {
        Shape nested = MorphShape.between(points(100, 0, 0), points(200, 0, 0))
                .fixedProgress(0.5f)
                .build();
        Shape shape = MorphShape.between(points(0, 0, 0), nested)
                .fixedProgress(0.5f)
                .build();

        List<Vector3f> points = sample(shape, context(0, 0));

        assertPoint(points.getFirst(), 75, 0, 0);
    }

    @Test
    void testPipelineAppliesTransformsModifiersAndEmissionAfterMorphSampling() {
        Shape shape = MorphShape.between(points(0, 0, 0), points(10, 0, 0))
                .fixedProgress(0.5f)
                .build();
        RecordingEmitter emitter = new RecordingEmitter();
        EffectModifier modifier = (points, context) -> points.translate(0, 0, 1, 0);
        World world = Mockito.mock(World.class);
        EffectInstance effect = new EffectInstance(
                shape,
                List.of(new TranslateTransform(1, 0, 0)),
                List.of(modifier),
                emitter,
                (localX, localY, localZ, context, destination) -> destination.set(localX, localY, localZ)
        );
        EffectContext context = new EffectContext(world, new Location(world, 10, 20, 30), null, List.of(), 0, 0, 0);

        effect.render(context);

        assertEquals(1, emitter.calls);
        assertEquals(6f, emitter.localX, 1e-6f);
        assertEquals(1f, emitter.localY, 1e-6f);
        assertEquals(16.0, emitter.worldX, 1e-6);
        assertEquals(21.0, emitter.worldY, 1e-6);
    }

    private static final class RecordingEmitter implements ParticleEmitter {
        private int calls;

        private float localX;

        private float localY;

        private double worldX;

        private double worldY;

        @Override
        public void spawn(
                EffectContext context,
                float localX,
                float localY,
                float localZ,
                double worldX,
                double worldY,
                double worldZ,
                float directionX,
                float directionY,
                float directionZ
        ) {
            calls++;
            this.localX = localX;
            this.localY = localY;
            this.worldX = worldX;
            this.worldY = worldY;
        }
    }
}
