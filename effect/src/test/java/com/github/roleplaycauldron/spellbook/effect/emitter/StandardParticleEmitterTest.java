package com.github.roleplaycauldron.spellbook.effect.emitter;

import com.github.roleplaycauldron.spellbook.effect.EffectContext;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StandardParticleEmitterTest {

    @Test
    void testNoViewersSkipsSpawnCalls() {
        Player player = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(), 0, 0, 0);

        StandardParticleEmitter.of(Particle.FLAME).spawn(context, 0, 0, 0, 1, 2, 3, 0, 0, 0);

        verify(player, never()).spawnParticle(any(), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }

    @Test
    void testConfiguredViewerReceivesParticleAtWorldCoordinates() {
        Player player = Mockito.mock(Player.class);
        World world = Mockito.mock(World.class);
        EffectContext context = new EffectContext(world, new Location(world, 0, 0, 0), null, List.of(player), 0, 0, 0);

        StandardParticleEmitter.of(Particle.FLAME, 2).spawn(context, 0, 0, 0, 1, 2, 3, 0, 0, 0);

        verify(player).spawnParticle(
                eq(Particle.FLAME),
                argThat(location -> location.getWorld() == world
                        && location.getX() == 1
                        && location.getY() == 2
                        && location.getZ() == 3),
                eq(2),
                eq(0.0),
                eq(0.0),
                eq(0.0),
                eq(0.0),
                isNull()
        );
    }
}
