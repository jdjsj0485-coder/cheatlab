package com.example.cheatlab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class Modules {
    public static boolean fly, esp, boatFly;
    public static double flySpeed = 0.6;   // блоков за тик
    public static double boatSpeed = 0.9;  // блоков за тик

    private Modules() {}

    public static void toggleFly()  { fly = !fly;         msg("Fly", fly); }
    public static void toggleBoat() { boatFly = !boatFly; msg("Boat Fly", boatFly); }
    public static void toggleEsp()  {
        esp = !esp;
        msg("ESP", esp);
        if (!esp) clearGlow();
    }

    private static void msg(String name, boolean on) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal(name + ": " + (on ? "ON" : "OFF")), true);
    }

    private static void clearGlow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Player pl : mc.level.players()) pl.setGlowingTag(false);
    }
}
