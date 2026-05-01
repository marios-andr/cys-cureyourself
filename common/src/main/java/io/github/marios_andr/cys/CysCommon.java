package io.github.marios_andr.cys;

import io.github.marios_andr.cys.registration.RegistrationProvider;
import io.github.marios_andr.cys.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CysCommon {

    public static final RegistrationProvider<EntityType<?>> ENTITIES = RegistrationProvider.get(Registries.ENTITY_TYPE, Constants.MOD_ID);
    public static final RegistryObject<EntityType<?>, EntityType<ZombifiedPlayer>> ZOMBIFIED_PLAYER = ENTITIES.register("zombified_player", () -> EntityType.Builder
            .of(ZombifiedPlayer::create, MobCategory.MISC)
            .sized(0.6F, 1.95F)
            .eyeHeight(1.74F)
            .passengerAttachments(2.0125F)
            .ridingOffset(-0.7F)
            .clientTrackingRange(8)
            .build(register("zombified_player")));

    private static ResourceKey<EntityType<?>> register(String name) {
        return ResourceKey.create(ENTITIES.getRegistryKey(), Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }


    public static void init() {


//        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());


//        if (Services.PLATFORM.isModLoaded("examplemod")) {
//
//            Constants.LOG.info("Hello to examplemod");
//        }
    }
}