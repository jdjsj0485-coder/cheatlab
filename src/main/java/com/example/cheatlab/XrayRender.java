package com.example.cheatlab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class XrayRender {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!Xray.enabled) return;
        List<Xray.Hit> list = Xray.hits();
        if (list.isEmpty()) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            PoseStack ps = e.getPoseStack();
            Vec3 cam = e.getCamera().getPosition();
            MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
            VertexConsumer vc = buf.getBuffer(XrayRenderType.LINES);
            ps.pushPose();
            ps.translate(-cam.x, -cam.y, -cam.z);
            for (Xray.Hit h : list) {
                float r = ((h.color() >> 16) & 255) / 255f;
                float g = ((h.color() >> 8) & 255) / 255f;
                float b = (h.color() & 255) / 255f;
                LevelRenderer.renderLineBox(ps, vc, new AABB(h.pos()).inflate(0.002), r, g, b, 1f);
            }
            ps.popPose();
            buf.endBatch(XrayRenderType.LINES);
        } catch (Throwable t) {
            Modules.error("xray-render", t);
        }
    }
}
