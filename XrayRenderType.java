package com.example.cheatlab;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

/** Линии без проверки глубины: видны сквозь блоки. */
public class XrayRenderType extends RenderType {
    private XrayRenderType(String n, VertexFormat f, VertexFormat.Mode m, int b, boolean a, boolean s,
                           Runnable setup, Runnable clear) {
        super(n, f, m, b, a, s, setup, clear);
    }

    public static final RenderType LINES = RenderType.create(
            "cheatlab_xray_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.5)))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false));
}
