package com.github.roleplaycauldron.spellbook.effect;

import com.github.roleplaycauldron.spellbook.effect.shape.HelixShape;
import com.github.roleplaycauldron.spellbook.effect.shape.LineShape;
import com.github.roleplaycauldron.spellbook.effect.shape.Shape;
import com.github.roleplaycauldron.spellbook.effect.shape.SphereShape;
import com.github.roleplaycauldron.spellbook.effect.transform.LookAtTransform;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectPipelinePerformanceTest {

    @Test
    void testRepresentativeEffectsRenderThroughReusableBuffer() {
        ShapeCase[] shapes = new ShapeCase[]{
                new ShapeCase(new LineShape(32), 32),
                new ShapeCase(new SphereShape(1f, 64), 64),
                new ShapeCase(new HelixShape(2, 32, 1f, 2f, 2f, 0.1f), 64)
        };

        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(
                world,
                new Location(world, 0, 0, 0),
                new Location(world, 0, 5, 0),
                List.of(),
                1,
                1,
                0.05
        );

        for (ShapeCase shapeCase : shapes) {
            CountingEmitter emitter = new CountingEmitter();
            EffectInstance effect = new EffectInstance(
                    shapeCase.shape(),
                    List.of(new LookAtTransform()),
                    List.of(),
                    emitter,
                    (localX, localY, localZ, effectContext, destination) -> destination.set(0, 0, 0)
            );

            for (int i = 0; i < 10; i++) {
                effect.render(context);
            }

            assertEquals(shapeCase.pointsPerFrame() * 10, emitter.calls);
        }
    }

    private record ShapeCase(Shape shape, int pointsPerFrame) {
    }

    private static final class CountingEmitter implements com.github.roleplaycauldron.spellbook.effect.emitter.ParticleEmitter {
        private int calls;

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
        }
    }
}
