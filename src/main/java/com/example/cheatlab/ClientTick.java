package com.example.cheatlab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTick {
    private static boolean fly, esp, boatFly;

    private static final float FLY_SPEED = 0.1f;     // ванильный = 0.05
    private static final double BOAT_VERT = 0.35;    // скорость вверх/вниз на лодке
    private static final double BOAT_HSPEED = 1.0;   // множитель горизонтальной скорости лодки

    private static void msg(LocalPlayer p, String name, boolean on) {
        p.displayClientMessage(Component.literal(name + ": " + (on ? "ON" : "OFF")), true);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        while (ModEvents.FLY.consumeClick())  { fly = !fly;         msg(p, "Fly", fly);
            if (!fly) restoreAbilities(p); }
        while (ModEvents.ESP.consumeClick())  { esp = !esp;         msg(p, "ESP", esp);
            if (!esp) clearGlow(mc); }
        while (ModEvents.BOAT.consumeClick()) { boatFly = !boatFly; msg(p, "BoatFly", boatFly); }

        // ---- Fly ----
        if (fly) {
            p.getAbilities().mayfly = true;
            p.getAbilities().flying = true;
            p.getAbilities().setFlyingSpeed(FLY_SPEED);
        }

        // ---- ESP (подсветка игроков через свечение) ----
        if (esp) {
            for (Player other : mc.level.players()) {
                if (other != p) other.setGlowingTag(true);
            }
        }

        // ---- Boat Fly ----
        if (boatFly && p.getVehicle() instanceof Boat boat) {
            Vec3 m = boat.getDeltaMovement();
            double y = 0;
            if (mc.options.keyJump.isDown()) y = BOAT_VERT;
            else if (mc.options.keyShift.isDown()) y = -BOAT_VERT;
            boat.setNoGravity(true);
            boat.setDeltaMovement(m.x * BOAT_HSPEED, y, m.z * BOAT_HSPEED);
        } else if (p.getVehicle() instanceof Boat boat && boat.isNoGravity()) {
            boat.setNoGravity(false);
        }
    }

    private static void restoreAbilities(LocalPlayer p) {
        boolean allowed = p.isCreative() || p.isSpectator();
        p.getAbilities().mayfly = allowed;
        if (!allowed) p.getAbilities().flying = false;
        p.getAbilities().setFlyingSpeed(0.05f);
    }

    private static void clearGlow(Minecraft mc) {
        if (mc.level == null) return;
        for (Entity en : mc.level.players()) en.setGlowingTag(false);
    }
}
