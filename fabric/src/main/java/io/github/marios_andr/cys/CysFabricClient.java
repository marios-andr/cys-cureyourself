package io.github.marios_andr.cys;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderPipelines;

public class CysFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CysClient.init();

        RenderPipelines.register(CysClient.ZOMBIFY_ENTITY_CUTOUT_PIPELINE);
    }
}
