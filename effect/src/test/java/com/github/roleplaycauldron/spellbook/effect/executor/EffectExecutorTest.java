package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.EffectRenderState;
import com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter;
import com.github.roleplaycauldron.spellbook.effect.location.EffectAnchor;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class EffectExecutorTest {

    @Test
    void testDefaultStepRendersWithRunIndexAndResolvesViewersBeforeRender() {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 1, 2, 3);
        Player viewer = Mockito.mock(Player.class);
        AtomicInteger viewerResolutions = new AtomicInteger();
        CapturingEmitter emitter = new CapturingEmitter();
        EffectInstance effect = pointEffect(emitter);

        EffectExecutionConfig config = baseConfig(() -> origin)
                .viewerSource(() -> {
                    viewerResolutions.incrementAndGet();
                    return List.of(viewer);
                })
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(effect, config, 3, new EffectRenderState());

        assertTrue(result.advance());
        assertFalse(result.cancel());
        assertEquals(3, emitter.context.step());
        assertEquals(3L, emitter.context.tick());
        assertEquals(0.15, emitter.context.timeSeconds(), 1e-6);
        assertEquals(List.of(viewer), emitter.context.viewers());
        assertEquals(1, viewerResolutions.get());
    }

    @Test
    void testCustomStepReceivesCompleteDefensiveFrameData() {
        World world = Mockito.mock(World.class);
        Location origin = new Location(world, 1, 2, 3);
        Location target = new Location(world, 4, 5, 6);
        CapturingEmitter emitter = new CapturingEmitter();
        AtomicInteger stepCalls = new AtomicInteger();
        Function<EffectExecutionConfig.ExecutionFrame, Integer> customStep = frame -> {
            stepCalls.incrementAndGet();
            assertEquals(2L, frame.runIndex());
            assertEquals(10L, frame.elapsedTicks());
            assertEquals(0.5, frame.elapsedSeconds(), 1e-6);
            assertEquals(1.0, frame.origin().getX(), 1e-6);
            assertEquals(4.0, frame.target().getX(), 1e-6);
            frame.origin().setX(99);
            frame.target().setX(99);
            return 42;
        };

        EffectExecutionConfig config = baseConfig(() -> origin)
                .periodTicks(5)
                .targetAnchor(() -> target)
                .skipEmptyViewerFrames(false)
                .stepFunction(customStep)
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(emitter),
                config,
                2,
                new EffectRenderState()
        );

        assertTrue(result.advance());
        assertFalse(result.cancel());
        assertEquals(1, stepCalls.get());
        assertEquals(42, emitter.context.step());
        assertEquals(1.0, emitter.context.origin().getX(), 1e-6);
        assertEquals(4.0, emitter.context.target().getX(), 1e-6);
        assertEquals(1.0, origin.getX(), 1e-6);
        assertEquals(4.0, target.getX(), 1e-6);
    }

    @Test
    void testOriginUnavailableCancellationBehavior() {
        AtomicInteger viewerResolutions = new AtomicInteger();
        EffectExecutionConfig config = baseConfig(() -> null)
                .viewerSource(() -> {
                    viewerResolutions.incrementAndGet();
                    return List.of();
                })
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(new CapturingEmitter()),
                config,
                0,
                new EffectRenderState()
        );

        assertFalse(result.advance());
        assertTrue(result.cancel());
        assertEquals(0, viewerResolutions.get());
    }

    @Test
    void testTargetUnavailableCancellationBehavior() {
        World world = Mockito.mock(World.class);
        EffectExecutionConfig config = baseConfig(() -> new Location(world, 0, 0, 0))
                .targetAnchor(() -> null)
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(new CapturingEmitter()),
                config,
                0,
                new EffectRenderState()
        );

        assertFalse(result.advance());
        assertTrue(result.cancel());
    }

    @Test
    void testWorldMismatchCancellationBehavior() {
        World originWorld = Mockito.mock(World.class);
        World targetWorld = Mockito.mock(World.class);
        EffectExecutionConfig config = baseConfig(() -> new Location(originWorld, 0, 0, 0))
                .targetAnchor(() -> new Location(targetWorld, 0, 0, 0))
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(new CapturingEmitter()),
                config,
                0,
                new EffectRenderState()
        );

        assertFalse(result.advance());
        assertTrue(result.cancel());
    }

    @Test
    void testEmptyViewerCollectionSkipsRenderByDefault() {
        World world = Mockito.mock(World.class);
        CapturingEmitter emitter = new CapturingEmitter();
        EffectExecutionConfig config = baseConfig(() -> new Location(world, 0, 0, 0))
                .viewerSource(List::of)
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(emitter),
                config,
                0,
                new EffectRenderState()
        );

        assertTrue(result.advance());
        assertFalse(result.cancel());
        assertNull(emitter.context);
    }

    @Test
    void testEmptyViewerCollectionCanRenderWhenSkipDisabled() {
        World world = Mockito.mock(World.class);
        CapturingEmitter emitter = new CapturingEmitter();
        EffectExecutionConfig config = baseConfig(() -> new Location(world, 0, 0, 0))
                .viewerSource(List::of)
                .skipEmptyViewerFrames(false)
                .build();

        EffectExecutor.FrameResult result = EffectExecutor.renderFrame(
                pointEffect(emitter),
                config,
                0,
                new EffectRenderState()
        );

        assertTrue(result.advance());
        assertFalse(result.cancel());
        assertNotNull(emitter.context);
        assertTrue(emitter.context.viewers().isEmpty());
    }

    private static EffectExecutionConfig.Builder baseConfig(EffectAnchor originAnchor) {
        return EffectExecutionConfig.builder()
                .originAnchor(originAnchor)
                .viewerSource(List::of);
    }

    private static EffectInstance pointEffect(ParticleEmitter emitter) {
        Shape shape = (context, points) -> points.add(0, 0, 0);
        return new EffectInstance(
                shape,
                List.of(),
                List.of(),
                emitter,
                (localX, localY, localZ, context, destination) -> destination.set(0, 0, 0)
        );
    }

    private static final class CapturingEmitter implements ParticleEmitter {
        private EffectContext context;

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
            this.context = context;
        }
    }
}
