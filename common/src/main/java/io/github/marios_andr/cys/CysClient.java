package io.github.marios_andr.cys;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

public class CysClient {

    public static final RenderPipeline.Snippet ZOMBIFY_ENTITY_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
            .withVertexShader("core/entity")
            .withFragmentShader("cys/zombify_entity")
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();

    public static final RenderPipeline ZOMBIFY_ENTITY_CUTOUT_PIPELINE = RenderPipeline.builder(ZOMBIFY_ENTITY_SNIPPET)
            .withLocation("pipeline/zombify_entity_cutout")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withSampler("Sampler1")
            .withCull(false)
            .build();

    public static final BiFunction<Identifier, Boolean, RenderType> ZOMBIFY_ENTITY_CUTOUT = Util.memoize(
            (texture, affectsOutline) -> {
                RenderSetup state = RenderSetup.builder(ZOMBIFY_ENTITY_CUTOUT_PIPELINE)
                        .withTexture("Sampler0", texture)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                        .createRenderSetup();
                return RenderType.create("zombify_entity_cutout", state);
            }
    );

    public static void init() {

    }
}
