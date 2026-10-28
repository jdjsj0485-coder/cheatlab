package com.example.cheatlab;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
    public static final String CAT = "key.categories.cheatlab";
    public static final KeyMapping MENU = new KeyMapping("key.cheatlab.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, CAT);
    public static final KeyMapping FLY  = new KeyMapping("key.cheatlab.fly",  GLFW.GLFW_KEY_R, CAT);
    public static final KeyMapping ESP  = new KeyMapping("key.cheatlab.esp",  GLFW.GLFW_KEY_G, CAT);
    public static final KeyMapping BOAT = new KeyMapping("key.cheatlab.boat", GLFW.GLFW_KEY_B, CAT);

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent e) {
        e.register(MENU);
        e.register(FLY);
        e.register(ESP);
        e.register(BOAT);
    }
}
