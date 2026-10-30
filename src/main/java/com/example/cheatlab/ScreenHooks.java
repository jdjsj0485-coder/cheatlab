package com.example.cheatlab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenHooks {
    // Кнопка "CheatLab" в меню паузы (Escape). Если другой мод подменил меню паузы,
    // меню всё равно открывается клавишей Right Shift.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInit(ScreenEvent.Init.Post e) {
        try {
            if (e.getScreen() instanceof PauseScreen) {
                e.addListener(Button.builder(Component.literal("CheatLab"),
                        b -> Minecraft.getInstance().setScreen(new CheatMenuScreen()))
                        .bounds(6, 6, 90, 20).build());
            }
        } catch (Throwable t) {
            Modules.error("pause-button", t);
        }
    }
}
