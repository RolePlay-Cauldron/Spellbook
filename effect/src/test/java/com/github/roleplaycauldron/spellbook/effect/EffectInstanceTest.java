package com.github.roleplaycauldron.spellbook.effect;

import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.transform.TranslateTransform;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EffectInstanceTest {

    @Test
    void testRenderPipelineSamplesTransformsModifiesAndEmits() {
        Shape shape = (context, points) -> points.add(1, 2, 3);
        RecordingEmitter emitter = new RecordingEmitter();
        EffectModifier modifier = (points, context) -> points.add(2, 0, 0);

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(new TranslateTransform(1, 0, 0)),
                List.of(modifier),
                emitter,
                (localX, localY, localZ, context, destination) -> destination.set(localX, localY, localZ)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(
                world,
                new Location(world, 10, 20, 30),
                null,
                List.of(),
                0,
                0,
                0
        );

        effect.render(context);

        assertEquals(2, emitter.calls);
        assertEquals(2f, emitter.firstLocalX, 1e-6f);
        assertEquals(12.0, emitter.firstWorldX, 1e-6);
        assertEquals(20.0, emitter.secondWorldY, 1e-6);
        assertEquals(2f, emitter.firstDirectionX, 1e-6f);
    }

    @Test
    void testRenderReusesCallerOwnedPointBufferAcrossFrames() {
        int[] firstIdentity = new int[]{0};
        Shape shape = (context, points) -> {
            int identity = System.identityHashCode(points);
            if (firstIdentity[0] == 0) {
                firstIdentity[0] = identity;
            } else {
                assertEquals(firstIdentity[0], identity);
            }
            points.add(0, 0, 0);
        };

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                new RecordingEmitter(),
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);
        EffectRenderState state = new EffectRenderState();

        effect.render(context, state);
        effect.render(context, state);
    }

    @Test
    void testSharedEffectExecutionsUseSeparatePointBuffers() {
        int[] firstExecutionBuffer = new int[]{0};
        int[] secondExecutionBuffer = new int[]{0};
        int[] call = new int[]{0};
        Shape shape = (context, points) -> {
            call[0]++;
            int identity = System.identityHashCode(points);
            if (call[0] == 1) {
                firstExecutionBuffer[0] = identity;
            } else if (call[0] == 2) {
                secondExecutionBuffer[0] = identity;
            } else {
                assertEquals(firstExecutionBuffer[0], identity);
            }
            points.add(0, 0, 0);
        };

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                new RecordingEmitter(),
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);
        EffectRenderState firstExecution = new EffectRenderState();
        EffectRenderState secondExecution = new EffectRenderState();

        effect.render(context, firstExecution);
        effect.render(context, secondExecution);
        effect.render(context, firstExecution);

        assertNotEquals(firstExecutionBuffer[0], secondExecutionBuffer[0]);
    }

    @Test
    void testScratchBuffersAreReusedAcrossFramesAndCleared() {
        int[] firstScratch = new int[]{0};
        Shape shape = (context, points) -> {
            PointBuffer scratch = context.scratchBuffer(0);
            int identity = System.identityHashCode(scratch);
            if (firstScratch[0] == 0) {
                firstScratch[0] = identity;
            } else {
                assertEquals(firstScratch[0], identity);
                assertTrue(scratch.isEmpty());
            }
            scratch.add(1, 0, 0);
            points.add(0, 0, 0);
        };

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                new RecordingEmitter(),
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);
        EffectRenderState state = new EffectRenderState();

        effect.render(context, state);
        effect.render(context, state);
    }

    @Test
    void testDirectShapeContextReusesIndexedScratchBuffers() {
        ShapeContext context = new ShapeContext(0, 0, null, null);

        assertSame(context.scratchBuffer(0), context.scratchBuffer(0));
        assertNotSame(context.scratchBuffer(0), context.scratchBuffer(1));
    }

    @Test
    void testScratchBuffersAreIsolatedBetweenRenderStates() {
        int[] firstExecutionScratch = new int[]{0};
        int[] secondExecutionScratch = new int[]{0};
        int[] call = new int[]{0};
        Shape shape = (context, points) -> {
            call[0]++;
            int identity = System.identityHashCode(context.scratchBuffer(0));
            if (call[0] == 1) {
                firstExecutionScratch[0] = identity;
            } else if (call[0] == 2) {
                secondExecutionScratch[0] = identity;
            } else {
                assertEquals(firstExecutionScratch[0], identity);
            }
            points.add(0, 0, 0);
        };

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                new RecordingEmitter(),
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);
        EffectRenderState firstExecution = new EffectRenderState();
        EffectRenderState secondExecution = new EffectRenderState();

        effect.render(context, firstExecution);
        effect.render(context, secondExecution);
        effect.render(context, firstExecution);

        assertNotEquals(firstExecutionScratch[0], secondExecutionScratch[0]);
    }

    @Test
    void testDirectRenderUsesFreshPointBuffer() {
        int[] firstIdentity = new int[]{0};
        Shape shape = (context, points) -> {
            int identity = System.identityHashCode(points);
            if (firstIdentity[0] == 0) {
                firstIdentity[0] = identity;
            } else {
                assertNotEquals(firstIdentity[0], identity);
            }
            points.add(0, 0, 0);
        };

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                new RecordingEmitter(),
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);

        effect.render(context);
        effect.render(context);
    }

    @Test
    void testDirectionProviderRunsOnlyWhenEmitterRequiresDirection() {
        Shape shape = (context, points) -> points.add(1, 0, 0);
        int[] directionCalls = new int[]{0};
        RecordingEmitter emitter = new RecordingEmitter(false);

        EffectInstance effect = new EffectInstance(
                shape,
                List.of(),
                List.of(),
                emitter,
                (localX, localY, localZ, context, destination) -> {
                    directionCalls[0]++;
                    destination.set(9, 0, 0);
                }
        );

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);

        effect.render(context);

        assertEquals(0, directionCalls[0]);
        assertEquals(0f, emitter.firstDirectionX, 1e-6f);
    }

    private static final class RecordingEmitter implements ParticleEmitter {
        private final boolean requiresDirection;
        private int calls;
        private float firstLocalX;
        private double firstWorldX;
        private double secondWorldY;
        private float firstDirectionX;

        private RecordingEmitter() {
            this(true);
        }

        private RecordingEmitter(boolean requiresDirection) {
            this.requiresDirection = requiresDirection;
        }

        @Override
        public boolean requiresDirection() {
            return requiresDirection;
        }

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
            if (calls == 1) {
                firstLocalX = localX;
                firstWorldX = worldX;
                firstDirectionX = directionX;
            } else if (calls == 2) {
                secondWorldY = worldY;
            }
        }
    }
}
