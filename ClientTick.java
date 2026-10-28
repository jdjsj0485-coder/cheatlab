package com.example.cheatlab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTick {
    private static Boat lastBoat;

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        e.getPlayer().displayClientMessage(Component.literal(
                "[CheatLab] loaded. Right Shift = menu, R = fly, G = ESP, B = boat fly"), false);
    }

    // Пока включён Fly, обнуляем обычное управление, чтобы движение шло только от мода
    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (Modules.fly && !e.getEntity().isPassenger()) {
            Input in = e.getInput();
            in.forwardImpulse = 0;
            in.leftImpulse = 0;
            in.jumping = false;
            in.shiftKeyDown = false;
            in.up = false;
            in.down = false;
            in.left = false;
            in.right = false;
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        Options o = mc.options;

        while (ModEvents.MENU.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new CheatMenuScreen());
        }
        while (ModEvents.FLY.consumeClick())  Modules.toggleFly();
        while (ModEvents.ESP.consumeClick())  Modules.toggleEsp();
        while (ModEvents.BOAT.consumeClick()) Modules.toggleBoat();

        // ---- Fly: двигаем игрока напрямую ----
        if (Modules.fly && !p.isPassenger()) {
            double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
            double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
            double yaw = Math.toRadians(p.getYRot());
            double sin = Math.sin(yaw), cos = Math.cos(yaw);
            double mx = -sin * f + cos * s;
            double mz = cos * f + sin * s;
            double len = Math.sqrt(mx * mx + mz * mz);
            if (len > 0) {
                mx = mx / len * Modules.flySpeed;
                mz = mz / len * Modules.flySpeed;
            }
            double my = 0;
            if (o.keyJump.isDown()) my += Modules.flySpeed;
            if (o.keyShift.isDown()) my -= Modules.flySpeed;
            p.setDeltaMovement(mx, my, mz);
            p.fallDistance = 0;
        }

        // ---- ESP: подсветка игроков ----
        if (Modules.esp) {
            for (Player other : mc.level.players()) {
                if (other != p) other.setGlowingTag(true);
            }
        }

        // ---- Boat Fly ----
        if (Modules.boatFly && p.getVehicle() instanceof Boat boat) {
            lastBoat = boat;
            boat.setNoGravity(true);
            Vec3 m = boat.getDeltaMovement();
            double y = 0;
            if (o.keyJump.isDown()) y = 0.5;
            else if (o.keySprint.isDown()) y = -0.5; // Ctrl = вниз (Shift = выйти из лодки)
            double x = m.x * 0.85, z = m.z * 0.85;
            if (o.keyUp.isDown()) {
                double yaw = Math.toRadians(boat.getYRot());
                x = -Math.sin(yaw) * Modules.boatSpeed;
                z = Math.cos(yaw) * Modules.boatSpeed;
            }
            boat.setDeltaMovement(x, y, z);
        } else if (lastBoat != null) {
            lastBoat.setNoGravity(false);
            lastBoat = null;
        }
    }
}
