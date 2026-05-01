package io.github.marios_andr.cys;

import io.github.marios_andr.cys.config.BaseConfig;
import io.github.marios_andr.cys.config.ConfigProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class CysFabricMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        configSetup();
        CysCommon.init();

        FabricDefaultAttributeRegistry.register(CysCommon.ZOMBIFIED_PLAYER.get(), ZombifiedPlayer.createAttributes().build());
    }

    public static BaseConfig CONFIG;
    private static ConfigProvider provider;

    private void configSetup() {
        provider = new ConfigProvider();
        provider.addComment("Generic mod switch. Controls whether the zombie should spawn on player death.");
        Constants.isEnabled = provider.addEntry("isEnabled", true, "");
        provider.addComment("Whether the zombie should spawn in peaceful mode. If enabled, the zombie will not attack the player. Superseded by isEnabled.");
        Constants.isPeacefulEnabled = provider.addEntry("isPeacefulEnabled", false, "");
        provider.addComment("Whether the zombie can be cured to bring back a dead hardcore player.");
        Constants.isHardcoreCuringEnabled = provider.addEntry("canHardcoreCure", true, "");
        provider.addComment("Whether or not the zombie is immune to all non-player sources of damage, such as lava, drowning or other mobs.");
        Constants.zombieImmunity = provider.addEntry("zombieImmunity", true, "");
        provider.addComment("""
                    How the player's experience should be handled when they die.
                    # NONE: Player experience will be dropped as normal.
                    # Otherwise, will be stored in the zombie's inventory:
                    # STORE_PARTIAL: The amount of experience stored will be equal to the amount of experience what would have been dropped.
                    # STORE_FULL: The full amount of experience will be stored.""");
        Constants.zombieExperienceOptions = provider.addEntry("zombieExperienceOptions", Constants.ExperienceOptions.STORE_PARTIAL, "");


        CONFIG = BaseConfig.of(Constants.MOD_ID + "_config").provider(provider).request();
    }
}
