package com.example.cheatlab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** "X-Ray": подсвечивает выбранные руды рамками сквозь стены. */
public final class Xray {
    public static final class Ore {
        public final String name;
        public final int color;
        public final Block[] blocks;
        public boolean enabled;

        Ore(String name, int color, boolean enabled, Block... blocks) {
            this.name = name;
            this.color = color;
            this.enabled = enabled;
            this.blocks = blocks;
        }
    }

    public record Hit(BlockPos pos, int color) {}

    public static final Ore[] ORES = {
            new Ore("Coal",     0x777777, false, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
            new Ore("Iron",     0xD8AF93, false, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
            new Ore("Copper",   0xE77C56, false, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
            new Ore("Gold",     0xFFD700, false, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE),
            new Ore("Redstone", 0xFF2020, false, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
            new Ore("Lapis",    0x3050FF, false, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
            new Ore("Diamond",  0x00FFFF, true,  Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
            new Ore("Emerald",  0x00FF40, false, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
            new Ore("Quartz",   0xFFFFFF, false, Blocks.NETHER_QUARTZ_ORE),
            new Ore("Debris",   0xC040FF, true,  Blocks.ANCIENT_DEBRIS)
    };

    private static final Map<Block, Ore> LOOKUP = new HashMap<>();
    static {
        for (Ore o : ORES) for (Block b : o.blocks) LOOKUP.put(b, o);
    }

    private static final int MAX_HITS = 3000;

    public static boolean enabled;
    public static int range = 16; // 8..24
    private static boolean dirty = true;
    private static int counter;
    private static volatile List<Hit> hits = List.of();

    private Xray() {}

    public static List<Hit> hits() { return hits; }

    public static void markDirty() { dirty = true; }

    public static void clear() { hits = List.of(); }

    public static void setRange(int v) {
        range = Math.max(8, Math.min(24, v));
        markDirty();
    }

    public static void adjustRange(int delta) {
        range = Math.max(8, Math.min(24, range + delta));
        markDirty();
    }

    public static void tick(Minecraft mc, LocalPlayer p) {
        if (!enabled) return;
        if (dirty || ++counter >= 20) {
            counter = 0;
            dirty = false;
            scan(mc, p);
        }
    }

    private static void scan(Minecraft mc, LocalPlayer p) {
        if (mc.level == null) return;
        BlockPos c = p.blockPosition();
        int r = range;
        int minY = Math.max(mc.level.getMinBuildHeight(), c.getY() - r);
        int maxY = Math.min(mc.level.getMaxBuildHeight() - 1, c.getY() + r);
        List<Hit> list = new ArrayList<>();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        outer:
        for (int x = c.getX() - r; x <= c.getX() + r; x++) {
            for (int z = c.getZ() - r; z <= c.getZ() + r; z++) {
                for (int y = minY; y <= maxY; y++) {
                    m.set(x, y, z);
                    Ore o = LOOKUP.get(mc.level.getBlockState(m).getBlock());
                    if (o != null && o.enabled) {
                        list.add(new Hit(m.immutable(), o.color));
                        if (list.size() >= MAX_HITS) break outer;
                    }
                }
            }
        }
        hits = list;
    }
}
