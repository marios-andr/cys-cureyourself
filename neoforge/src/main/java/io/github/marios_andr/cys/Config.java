package io.github.marios_andr.cys;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static io.github.marios_andr.cys.Constants.*;

@EventBusSubscriber(modid = MOD_ID)
public class Config { // Spawn in creative option
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue IS_ENABLED = BUILDER.comment("Generic mod switch. Controls whether the zombie should spawn on player death.").define("isEnabled", true);
    private static final ModConfigSpec.BooleanValue IS_PEACEFUL_ENABLED = BUILDER.comment("Whether the zombie should spawn in peaceful mode. If enabled, the zombie will not attack the player. Superseded by isEnabled.").define("isPeacefulEnabled", false);
    private static final ModConfigSpec.BooleanValue IS_HARDCORE_CURING_ENABLED = BUILDER.comment("Whether the zombie can be cured to bring back a dead hardcore player.").define("canHardcoreCure", true);

    private static final ModConfigSpec.BooleanValue ZOMBIE_IMMUNITY = BUILDER.comment("Whether or not the zombie is immune to all non-player sources of damage, such as lava, drowning or other mobs.").define("zombieImmunity", true);

    private static final ModConfigSpec.ConfigValue<Constants.ExperienceOptions> ZOMBIE_EXPERIENCE_OPTIONS = BUILDER.comment("""
                    How the player's experience should be handled when they die.
                    NONE: Player experience will be dropped as normal.
                    Otherwise, will be stored in the zombie's inventory:
                    STORE_PARTIAL: The amount of experience stored will be equal to the amount of experience what would have been dropped.
                    STORE_FULL: The full amount of experience will be stored.""")
            .defineEnum("zombieExperienceOptions", Constants.ExperienceOptions.STORE_PARTIAL);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        isEnabled = IS_ENABLED.get();
        isPeacefulEnabled = IS_PEACEFUL_ENABLED.get();
        isHardcoreCuringEnabled = IS_HARDCORE_CURING_ENABLED.get();
        zombieImmunity = ZOMBIE_IMMUNITY.get();
        zombieExperienceOptions = ZOMBIE_EXPERIENCE_OPTIONS.get();
    }
}