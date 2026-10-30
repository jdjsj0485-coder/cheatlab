package com.example.cheatlab;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Клиентские команды: /cheatlab (или /cl). Работают только у вас, на сервер не отправляются. */
@Mod.EventBusSubscriber(modid = CheatLab.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCommands {

    private static final String B = "\u00a7b";
    private static final String G = "\u00a77";
    private static final String R = "\u00a7r";

    private static final String[] HELP = {
            B + "=== CheatLab commands (/cheatlab or /cl) ===" + R,
            B + "/cheatlab help" + G + " - show this list" + R,
            B + "/cheatlab status" + G + " - what is ON/OFF, speeds" + R,
            B + "/cheatlab menu" + G + " - open the draggable menu (or Right Shift)" + R,
            B + "/cheatlab fly" + G + " - toggle Fly (key R)" + R,
            B + "/cheatlab esp" + G + " - toggle player ESP (key G)" + R,
            B + "/cheatlab boat" + G + " - toggle Boat Fly (key B)" + R,
            B + "/cheatlab xray" + G + " - toggle X-Ray (key X)" + R,
            B + "/cheatlab flyspeed <0.1-3.0>" + G + " - set fly speed" + R,
            B + "/cheatlab boatspeed <0.1-5.0>" + G + " - set boat speed (] faster, [ slower)" + R,
            B + "/cheatlab range <8-24>" + G + " - X-Ray search radius" + R,
            B + "/cheatlab ore <name>" + G + " - toggle ore: coal iron copper gold redstone lapis diamond emerald quartz debris" + R,
    };

    private static void say(String text) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.displayClientMessage(Component.literal(text), false);
    }

    private static int help() {
        for (String line : HELP) say(line);
        return 1;
    }

    private static String onOff(boolean v) {
        return v ? "\u00a7aON" + R : "\u00a7cOFF" + R;
    }

    private static int status() {
        say(B + "CheatLab status" + R);
        say("Fly: " + onOff(Modules.fly) + "  speed " + String.format(Locale.ROOT, "%.1f", Modules.flySpeed));
        say("Player ESP: " + onOff(Modules.esp));
        say("Boat Fly: " + onOff(Modules.boatFly) + "  speed " + String.format(Locale.ROOT, "%.1f", Modules.boatSpeed));
        say("X-Ray: " + onOff(Xray.enabled) + "  range " + Xray.range);
        StringBuilder sb = new StringBuilder("Ores: ");
        for (Xray.Ore o : Xray.ORES) if (o.enabled) sb.append(o.name).append(' ');
        say(sb.toString());
        return 1;
    }

    private static List<String> oreNames() {
        List<String> list = new ArrayList<>();
        for (Xray.Ore o : Xray.ORES) list.add(o.name.toLowerCase(Locale.ROOT));
        return list;
    }

    private static int toggleOre(String name) {
        for (Xray.Ore o : Xray.ORES) {
            if (o.name.equalsIgnoreCase(name)) {
                o.enabled = !o.enabled;
                Xray.markDirty();
                say("Ore " + o.name + ": " + onOff(o.enabled));
                return 1;
            }
        }
        say("Unknown ore. Use: " + String.join(", ", oreNames()));
        return 0;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
        return Commands.literal(name)
                .executes(c -> help())
                .then(Commands.literal("help").executes(c -> help()))
                .then(Commands.literal("status").executes(c -> status()))
                .then(Commands.literal("menu").executes(c -> { Modules.openMenu = true; return 1; }))
                .then(Commands.literal("fly").executes(c -> { Modules.toggleFly(); return 1; }))
                .then(Commands.literal("esp").executes(c -> { Modules.toggleEsp(); return 1; }))
                .then(Commands.literal("boat").executes(c -> { Modules.toggleBoat(); return 1; }))
                .then(Commands.literal("xray").executes(c -> { Modules.toggleXray(); return 1; }))
                .then(Commands.literal("flyspeed")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 3.0))
                                .executes(c -> {
                                    Modules.setFlySpeed(DoubleArgumentType.getDouble(c, "value"));
                                    say("Fly speed: " + String.format(Locale.ROOT, "%.1f", Modules.flySpeed));
                                    return 1;
                                })))
                .then(Commands.literal("boatspeed")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1, 5.0))
                                .executes(c -> {
                                    Modules.setBoatSpeed(DoubleArgumentType.getDouble(c, "value"));
                                    return 1;
                                })))
                .then(Commands.literal("range")
                        .then(Commands.argument("value", IntegerArgumentType.integer(8, 24))
                                .executes(c -> {
                                    Xray.setRange(IntegerArgumentType.getInteger(c, "value"));
                                    say("X-Ray range: " + Xray.range);
                                    return 1;
                                })))
                .then(Commands.literal("ore")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(oreNames(), b))
                                .executes(c -> toggleOre(StringArgumentType.getString(c, "name")))));
    }

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent e) {
        e.getDispatcher().register(build("cheatlab"));
        e.getDispatcher().register(build("cl"));
    }
}
