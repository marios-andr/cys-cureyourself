package io.github.marios_andr.cys;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Constants.MOD_ID)
public class CysNeoforgeMod {

    private static IEventBus eventBus;

    public CysNeoforgeMod(IEventBus eventBus, ModContainer modContainer) {
        CysNeoforgeMod.eventBus = eventBus;
        CysCommon.init();

        eventBus.addListener(this::createDefaultAttributes);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void createDefaultAttributes(EntityAttributeCreationEvent e) {
        e.put(CysCommon.ZOMBIFIED_PLAYER.get(), ZombifiedPlayer.createAttributes().build());
    }
}