package com.github.roleplaycauldron.spellbook.effect.executor;

import com.github.roleplaycauldron.spellbook.effect.EffectInstance;
import com.github.roleplaycauldron.spellbook.effect.location.EffectAnchor;
import com.github.roleplaycauldron.spellbook.effect.location.EntityAnchor;
import com.github.roleplaycauldron.spellbook.effect.location.FixedAnchor;
import com.github.roleplaycauldron.spellbook.effect.viewer.FixedViewerSource;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Effect handler implementation for Paper servers.
 */
public final class PaperEffectHandler implements EffectHandler {

    private final EffectExecutor executor;

    /**
     * Creates a new PaperEffectHandler
     *
     * @param executor the {@link EffectExecutor} to use
     */
    public PaperEffectHandler(EffectExecutor executor) {
        this.executor = executor;
    }

    @Override
    public RunningEffect playAt(EffectInstance effect,
                                Location position,
                                Collection<? extends Player> viewers,
                                EffectExecutionConfig config) {
        return start(effect, new FixedAnchor(position), null, viewers, config);
    }

    @Override
    public RunningEffect playBetweenPoints(EffectInstance effect,
                                           Location from,
                                           Location to,
                                           Collection<? extends Player> viewers,
                                           EffectExecutionConfig config) {
        return start(effect, new FixedAnchor(from), new FixedAnchor(to), viewers, config);
    }

    @Override
    public RunningEffect playBetweenEntities(EffectInstance effect,
                                             Entity from,
                                             Entity to,
                                             Collection<? extends Player> viewers,
                                             EffectExecutionConfig config) {
        return start(effect, new EntityAnchor(from), new EntityAnchor(to), viewers, config);
    }

    @Override
    public RunningEffect playFromEntityToPoint(EffectInstance effect,
                                               Entity from,
                                               Location to,
                                               Collection<? extends Player> viewers,
                                               EffectExecutionConfig config) {
        return start(effect, new EntityAnchor(from), new FixedAnchor(to), viewers, config);
    }

    private RunningEffect start(EffectInstance effect,
                                EffectAnchor originAnchor,
                                EffectAnchor targetAnchor,
                                Collection<? extends Player> viewers,
                                EffectExecutionConfig baseConfig) {

        EffectExecutionConfig config = EffectExecutionConfig.builder()
                .delayTicks(baseConfig.delayTicks())
                .periodTicks(baseConfig.periodTicks())
                .maxRuns(baseConfig.maxRuns())
                .cancelIfOriginUnavailable(baseConfig.cancelIfOriginUnavailable())
                .cancelIfTargetUnavailable(baseConfig.cancelIfTargetUnavailable())
                .cancelIfWorldsDiffer(baseConfig.cancelIfWorldsDiffer())
                .originAnchor(originAnchor)
                .targetAnchor(targetAnchor)
                .viewerSource(new FixedViewerSource(viewers))
                .stepFunction(baseConfig.stepFunction())
                .build();

        return executor.start(effect, config);
    }
}