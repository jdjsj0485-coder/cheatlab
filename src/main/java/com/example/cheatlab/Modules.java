package com.example.cheatlab;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class Modules {
    public static boolean fly, esp, boatFly;
    public static boolean openMenu; // команда /cheatlab menu открывает меню на следующем тике
    public static double flySpeed = 0.6;   // блоков за тик
    public static double boatSpeed = 0.9;  // блоков за тик

    private static final Set<String> reported = new HashSet<>();

    private Modules() {}

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    public static void toggleFly()  { fly = !fly;         msg("Fly", fly); }
    public static void toggleBoat() { boatFly = !boatFly; msg("Boat Fly", boatFly); }
    public static void toggleEsp()  {
        esp = !esp;
        msg("ESP", esp);
        if (!esp) clearGlow();
    }
    public static void toggleXray() {
        Xray.enabled = !Xray.enabled;
        Xray.markDirty();
        if (!Xray.enabled) Xray.clear();
        msg("X-Ray", Xray.enabled);
    }

    public static void setFlySpeed(double v) {
        flySpeed = Mth.clamp(round1(v), 0.1, 3.0);
    }

    public static void setBoatSpeed(double v) {
        boatSpeed = Mth.clamp(round1(v), 0.1, 5.0);
        showBoatSpeed();
    }

    public static void adjustFly(double d) {
        flySpeed = Mth.clamp(round1(flySpeed + d), 0.1, 3.0);
    }

    public static void adjustBoat(double d) {
        boatSpeed = Mth.clamp(round1(boatSpeed + d), 0.1, 5.0);
        showBoatSpeed();
    }

    public static void showBoatSpeed() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) {
            p.displayClientMessage(Component.literal(String.format(Locale.ROOT,
                    "Boat speed: %.1f  (%.0f blocks/s)", boatSpeed, boatSpeed * 20)), true);
        }
    }

    private static void msg(String name, boolean on) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal(name + ": " + (on ? "ON" : "OFF")), true);
    }

    private static void clearGlow() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            for (Player pl : mc.level.players()) pl.setGlowingTag(false);
        } catch (Throwable t) {
            error("clearGlow", t);
        }
    }

    /** Ошибка в одной функции не должна ломать остальные: пишем в лог и один раз в чат. */
    public static void error(String where, Throwable t) {
        if (!reported.add(where)) return;
        LogUtils.getLogger().error("[CheatLab] error in {}", where, t);
        try {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p != null) {
                p.displayClientMessage(Component.literal("[CheatLab] error in " + where + ": " + t), false);
            }
        } catch (Throwable ignored) {
        }
    }
}
