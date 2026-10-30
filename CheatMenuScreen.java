package com.example.cheatlab;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheatMenuScreen extends Screen {
    private static final int W = 190, H = 312;
    private static int px = -1, py = -1; // позиция окна запоминается

    private static final String[] KEY_LABELS = {
            "Open menu", "Fly", "Player ESP", "Boat Fly", "X-Ray", "Boat faster", "Boat slower"
    };

    private record Entry(AbstractWidget w, int dx, int dy, int page) {}

    private final List<Entry> entries = new ArrayList<>();
    private boolean dragging;
    private int dragDX, dragDY;
    private KeyMapping listening;
    private int page = 0; // 0 = функции, 1 = клавиши
    private Button flyBtn, espBtn, boatBtn, xrayBtn, flyKey, espKey, boatKey, xrayKey;
    private final Button[] oreBtns = new Button[Xray.ORES.length];
    private final Button[] keyBtns = new Button[KEY_LABELS.length];

    public CheatMenuScreen() {
        super(Component.literal("CheatLab"));
    }

    private static KeyMapping[] keyMaps() {
        return new KeyMapping[]{
                ModEvents.MENU, ModEvents.FLY, ModEvents.ESP, ModEvents.BOAT,
                ModEvents.XRAY, ModEvents.BOAT_FASTER, ModEvents.BOAT_SLOWER
        };
    }

    private Button btn(int w, int h, String text, Button.OnPress press) {
        return Button.builder(Component.literal(text), press).bounds(0, 0, w, h).build();
    }

    private <T extends AbstractWidget> T put(T w, int dx, int dy, int pg) {
        entries.add(new Entry(w, dx, dy, pg));
        w.setX(px + dx);
        w.setY(py + dy);
        return addRenderableWidget(w);
    }

    @Override
    protected void init() {
        entries.clear();
        if (px < 0) {
            px = (width - W) / 2;
            py = (height - H) / 2;
        }
        clampPos();

        // ===== Страница 0: функции =====
        flyBtn = put(btn(112, 20, "", b -> { Modules.toggleFly(); refresh(); }), 6, 22, 0);
        flyKey = put(btn(60, 20, "", b -> listen(ModEvents.FLY)), 124, 22, 0);
        put(btn(20, 20, "-", b -> Modules.adjustFly(-0.1)), 6, 46, 0);
        put(btn(20, 20, "+", b -> Modules.adjustFly(0.1)), 164, 46, 0);

        espBtn = put(btn(112, 20, "", b -> { Modules.toggleEsp(); refresh(); }), 6, 70, 0);
        espKey = put(btn(60, 20, "", b -> listen(ModEvents.ESP)), 124, 70, 0);

        boatBtn = put(btn(112, 20, "", b -> { Modules.toggleBoat(); refresh(); }), 6, 94, 0);
        boatKey = put(btn(60, 20, "", b -> listen(ModEvents.BOAT)), 124, 94, 0);
        put(btn(20, 20, "-", b -> Modules.adjustBoat(-0.1)), 6, 118, 0);
        put(btn(20, 20, "+", b -> Modules.adjustBoat(0.1)), 164, 118, 0);

        xrayBtn = put(btn(112, 20, "", b -> { Modules.toggleXray(); refresh(); }), 6, 142, 0);
        xrayKey = put(btn(60, 20, "", b -> listen(ModEvents.XRAY)), 124, 142, 0);
        put(btn(20, 20, "-", b -> Xray.adjustRange(-4)), 6, 166, 0);
        put(btn(20, 20, "+", b -> Xray.adjustRange(4)), 164, 166, 0);

        for (int i = 0; i < Xray.ORES.length; i++) {
            final Xray.Ore ore = Xray.ORES[i];
            int col = i % 3, row = i / 3;
            oreBtns[i] = put(btn(58, 16, "", b -> {
                ore.enabled = !ore.enabled;
                Xray.markDirty();
                refresh();
            }), 6 + col * 60, 202 + row * 18, 0);
        }

        put(btn(178, 16, "Keys / rebind  >", b -> {
            page = 1;
            listening = null;
            applyPage();
            refresh();
        }), 6, 268, 0);

        // ===== Страница 1: все клавиши =====
        KeyMapping[] maps = keyMaps();
        for (int i = 0; i < maps.length; i++) {
            final KeyMapping m = maps[i];
            keyBtns[i] = put(btn(74, 18, "", b -> listen(m)), 110, 24 + i * 22, 1);
        }
        put(btn(178, 18, "<  Back", b -> {
            page = 0;
            listening = null;
            applyPage();
            refresh();
        }), 6, 184, 1);

        applyPage();
        refresh();
    }

    private void applyPage() {
        for (Entry e : entries) {
            boolean show = e.page() == page;
            e.w().visible = show;
            e.w().active = show;
        }
    }

    private void listen(KeyMapping m) {
        listening = m;
        refresh();
    }

    private Component keyLabel(KeyMapping m) {
        return listening == m ? Component.literal("> ? <") : m.getTranslatedKeyMessage();
    }

    private void refresh() {
        flyBtn.setMessage(Component.literal("Fly: " + (Modules.fly ? "ON" : "OFF")));
        espBtn.setMessage(Component.literal("Player ESP: " + (Modules.esp ? "ON" : "OFF")));
        boatBtn.setMessage(Component.literal("Boat Fly: " + (Modules.boatFly ? "ON" : "OFF")));
        xrayBtn.setMessage(Component.literal("X-Ray: " + (Xray.enabled ? "ON" : "OFF")));
        flyKey.setMessage(keyLabel(ModEvents.FLY));
        espKey.setMessage(keyLabel(ModEvents.ESP));
        boatKey.setMessage(keyLabel(ModEvents.BOAT));
        xrayKey.setMessage(keyLabel(ModEvents.XRAY));
        for (int i = 0; i < Xray.ORES.length; i++) {
            Xray.Ore o = Xray.ORES[i];
            oreBtns[i].setMessage(Component.literal(o.name)
                    .withStyle(o.enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        KeyMapping[] maps = keyMaps();
        for (int i = 0; i < maps.length; i++) {
            keyBtns[i].setMessage(keyLabel(maps[i]));
        }
    }

    private void clampPos() {
        px = Mth.clamp(px, 0, Math.max(0, width - W));
        py = Mth.clamp(py, 0, Math.max(0, height - H));
    }

    private void reposition() {
        for (Entry e : entries) {
            e.w().setX(px + e.dx());
            e.w().setY(py + e.dy());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        g.fill(px, py, px + W, py + H, 0xE0101018);
        g.fill(px, py, px + W, py + 16, 0xFF2D5BFF);
        g.renderOutline(px, py, W, H, 0xFF2D5BFF);
        g.drawString(font, page == 0 ? "CheatLab  (drag title)" : "CheatLab - keys  (drag title)", px + 6, py + 4, 0xFFFFFF);

        if (page == 0) {
            g.drawCenteredString(font, String.format(Locale.ROOT, "Fly speed: %.1f", Modules.flySpeed), px + W / 2, py + 52, 0xFFFFFF);
            g.drawCenteredString(font, String.format(Locale.ROOT, "Boat speed: %.1f", Modules.boatSpeed), px + W / 2, py + 124, 0xFFFFFF);
            g.drawCenteredString(font, "X-Ray range: " + Xray.range, px + W / 2, py + 172, 0xFFFFFF);
            g.drawString(font, "Ores to find (click):", px + 6, py + 190, 0xFFFFFF);

            String faster = ModEvents.BOAT_FASTER.getTranslatedKeyMessage().getString();
            String slower = ModEvents.BOAT_SLOWER.getTranslatedKeyMessage().getString();
            g.drawString(font, "Boat: " + faster + " faster, " + slower + " slower", px + 6, py + 290, 0xAAAAAA);
            g.drawString(font, "Ctrl = down in boat. /cheatlab = commands", px + 6, py + 300, 0xAAAAAA);
        } else {
            for (int i = 0; i < KEY_LABELS.length; i++) {
                g.drawString(font, KEY_LABELS[i], px + 8, py + 24 + i * 22 + 5, 0xFFFFFF);
            }
            g.drawString(font, "Click a key box, then press a key", px + 6, py + 212, 0xAAAAAA);
            g.drawString(font, "ESC = cancel", px + 6, py + 222, 0xAAAAAA);
        }

        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (listening != null) {
            listening = null;
            refresh();
            return true;
        }
        if (button == 0 && mx >= px && mx <= px + W && my >= py && my <= py + 16) {
            dragging = true;
            dragDX = (int) mx - px;
            dragDY = (int) my - py;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            px = Mth.clamp((int) mx - dragDX, 0, Math.max(0, width - W));
            py = Mth.clamp((int) my - dragDY, 0, Math.max(0, height - H));
            reposition();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != null) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                listening.setKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
                KeyMapping.resetMapping();
                minecraft.options.save();
            }
            listening = null;
            refresh();
            return true;
        }
        if (ModEvents.MENU.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
