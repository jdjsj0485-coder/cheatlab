package com.example.cheatlab;

import com.mojang.blaze3d.platform.InputConstants;
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
    private static final int W = 190, H = 186;
    private static int px = -1, py = -1; // позиция окна запоминается

    private record Entry(AbstractWidget w, int dx, int dy) {}

    private final List<Entry> entries = new ArrayList<>();
    private boolean dragging;
    private int dragDX, dragDY;
    private KeyMapping listening;
    private Button flyBtn, espBtn, boatBtn, flyKey, espKey, boatKey, menuKey;

    public CheatMenuScreen() {
        super(Component.literal("CheatLab"));
    }

    private Button btn(int w, String text, Button.OnPress press) {
        return Button.builder(Component.literal(text), press).bounds(0, 0, w, 20).build();
    }

    private <T extends AbstractWidget> T put(T w, int dx, int dy) {
        entries.add(new Entry(w, dx, dy));
        w.setX(px + dx);
        w.setY(py + dy);
        return addRenderableWidget(w);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    @Override
    protected void init() {
        entries.clear();
        if (px < 0) {
            px = (width - W) / 2;
            py = (height - H) / 2;
        }
        clampPos();

        flyBtn  = put(btn(112, "", b -> { Modules.toggleFly();  refresh(); }), 6, 22);
        flyKey  = put(btn(60, "", b -> listen(ModEvents.FLY)), 124, 22);

        put(btn(20, "-", b -> Modules.flySpeed = Mth.clamp(round1(Modules.flySpeed - 0.1), 0.1, 3.0)), 6, 46);
        put(btn(20, "+", b -> Modules.flySpeed = Mth.clamp(round1(Modules.flySpeed + 0.1), 0.1, 3.0)), 164, 46);

        espBtn  = put(btn(112, "", b -> { Modules.toggleEsp();  refresh(); }), 6, 70);
        espKey  = put(btn(60, "", b -> listen(ModEvents.ESP)), 124, 70);

        boatBtn = put(btn(112, "", b -> { Modules.toggleBoat(); refresh(); }), 6, 94);
        boatKey = put(btn(60, "", b -> listen(ModEvents.BOAT)), 124, 94);

        put(btn(20, "-", b -> Modules.boatSpeed = Mth.clamp(round1(Modules.boatSpeed - 0.1), 0.1, 3.0)), 6, 118);
        put(btn(20, "+", b -> Modules.boatSpeed = Mth.clamp(round1(Modules.boatSpeed + 0.1), 0.1, 3.0)), 164, 118);

        menuKey = put(btn(60, "", b -> listen(ModEvents.MENU)), 124, 142);

        refresh();
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
        flyKey.setMessage(keyLabel(ModEvents.FLY));
        espKey.setMessage(keyLabel(ModEvents.ESP));
        boatKey.setMessage(keyLabel(ModEvents.BOAT));
        menuKey.setMessage(keyLabel(ModEvents.MENU));
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
        g.drawString(font, "CheatLab  (drag title)", px + 6, py + 4, 0xFFFFFF);

        g.drawCenteredString(font, String.format(Locale.ROOT, "Fly speed: %.1f", Modules.flySpeed), px + W / 2, py + 52, 0xFFFFFF);
        g.drawCenteredString(font, String.format(Locale.ROOT, "Boat speed: %.1f", Modules.boatSpeed), px + W / 2, py + 124, 0xFFFFFF);
        g.drawString(font, "Menu key", px + 8, py + 148, 0xFFFFFF);
        g.drawString(font, "Click a key box, then press a key", px + 6, py + 168, 0xAAAAAA);
        g.drawString(font, "ESC = cancel   Ctrl = down in boat", px + 6, py + 176, 0xAAAAAA);

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
