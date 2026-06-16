package com.github.roleplaycauldron.spellbook.effect.config;

import com.github.roleplaycauldron.spellbook.effect.ParticleSpec;
import com.github.roleplaycauldron.spellbook.effect.emitter.StandardParticleEmitter;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Parses particle configuration sections into standard particle emitters.
 *
 * <p>This parser handles the common particle fields such as particle type,
 * count, offsets, extra value, and optional advanced particle data. Advanced
 * particle data is delegated to registered {@link ParticleDataConfigParser}
 * instances from the {@link EffectConfigRegistry}.</p>
 */
final class ParticleConfigParser {

    private final EffectConfigParser parser;

    private final EffectConfigRegistry registry;

    /**
     * Creates a particle configuration parser.
     *
     * @param parser   parent effect configuration parser used for nested parser contexts
     * @param registry registry used to resolve particle data parsers
     */
    /* default */
    ParticleConfigParser(EffectConfigParser parser, EffectConfigRegistry registry) {
        this.parser = parser;
        this.registry = registry;
    }

    /**
     * Parses a standard particle emitter from a particle configuration section.
     *
     * <p>The section must contain a {@code type} field. Optional fields are
     * {@code count}, {@code offset-x}, {@code offset-y}, {@code offset-z},
     * {@code extra}, and {@code data}. The {@code data} section is required only
     * for Bukkit particles whose data type is not {@link Void}.</p>
     *
     * @param section particle configuration section
     * @param path    configuration path used for error reporting
     * @return parsed standard particle emitter
     * @throws EffectConfigException if the particle configuration is invalid
     */
    /* default */
    StandardParticleEmitter<Object> parse(ConfigurationSection section, String path) {
        Particle particle = EffectConfigValues.parseEnum(
                Particle.class,
                EffectConfigValues.requireString(section, "type", path + ".type"),
                path + ".type"
        );
        int count = EffectConfigValues.getInt(section, "count", path + ".count", 1);
        double offsetX = EffectConfigValues.getDouble(section, "offset-x", path + ".offset-x", 0.0);
        double offsetY = EffectConfigValues.getDouble(section, "offset-y", path + ".offset-y", 0.0);
        double offsetZ = EffectConfigValues.getDouble(section, "offset-z", path + ".offset-z", 0.0);
        double extra = EffectConfigValues.getDouble(section, "extra", path + ".extra", 0.0);
        Object data = parseData(particle, section, path);

        validateParticleData(particle, data, path + ".data");
        return new StandardParticleEmitter<>(new ParticleSpec<>(particle, count, offsetX, offsetY, offsetZ, extra, data));
    }

    /**
     * Parses optional advanced particle data for the given particle.
     *
     * <p>If no {@code data} section exists, this method returns {@code null}.
     * Otherwise, the nested data section must contain a {@code type} field that
     * resolves to a registered {@link ParticleDataConfigParser}.</p>
     *
     * @param particle particle whose data is being parsed
     * @param section  parent particle configuration section
     * @param path     configuration path of the parent particle section
     * @return parsed particle data, or {@code null} if no data section exists
     * @throws EffectConfigException if the data section is invalid or the data type is unknown
     */
    private Object parseData(Particle particle, ConfigurationSection section, String path) {
        ConfigurationSection dataSection = EffectConfigValues.optionalSection(section, "data", path + ".data");
        if (dataSection == null) {
            return null;
        }

        String dataType = EffectConfigValues.requireString(dataSection, "type", path + ".data.type");
        ParticleDataConfigParser dataParser = registry.particleDataParser(dataType);
        if (dataParser == null) {
            throw new EffectConfigException(path + ".data.type",
                    String.format("Unknown particle data type '%s'. Registered types: %s", dataType, registry.knownParticleDataTypes()));
        }
        try {
            return dataParser.parse(particle, dataSection, new EffectConfigContext(parser, path + ".data"));
        } catch (EffectConfigException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new EffectConfigException(path + ".data", exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            throw new EffectConfigException(path + ".data", "Failed to parse particle data", exception);
        }
    }

    /**
     * Validates parsed particle data against Bukkit's expected particle data type.
     *
     * <p>Particles whose data type is {@link Void} must not receive data. All
     * other particles require non-null data matching {@link Particle#getDataType()}.</p>
     *
     * @param particle particle whose data should be validated
     * @param data     parsed particle data, or {@code null}
     * @param path     configuration path used for error reporting
     * @throws EffectConfigException if the particle data is missing, unsupported, or has the wrong type
     */
    private void validateParticleData(Particle particle, Object data, String path) {
        Class<?> expectedType = particle.getDataType();
        if (expectedType == Void.class) {
            if (data != null) {
                throw new EffectConfigException(path, String.format("Particle %s does not accept data, got %s", particle, data.getClass().getSimpleName()));
            }
            return;
        }
        if (data == null) {
            throw new EffectConfigException(path, String.format("Particle %s requires data of type %s", particle, expectedType.getSimpleName()));
        }
        if (!expectedType.isInstance(data)) {
            throw new EffectConfigException(path, String.format("Expected particle data type %s, got %s", expectedType.getSimpleName(), data.getClass().getSimpleName()));
        }
    }
}
