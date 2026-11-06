package de.theaterial.shaderprofile;

import net.minecraft.util.Util;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.util.function.IntConsumer;

public class ConfigScreen extends Screen {
    private StringWidget packLabel;

    public ConfigScreen() {
        super(Component.literal("Shader Profile"));
    }

    @Override
    protected void init() {
        ProfileConfig cfg = ProfileConfig.get();
        int w = 220;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 70;

        addRenderableWidget(new StringWidget(x, y - 20, w, 12, Component.literal("Shader Profile"), this.font));

        addRenderableWidget(new IntSlider(x, y, w, 20, "Render Distance", 2, 64, cfg.renderDistance,
                v -> cfg.renderDistance = v));
        addRenderableWidget(new IntSlider(x, y + 24, w, 20, "Simulation Distance", 5, 32, cfg.simulationDistance,
                v -> cfg.simulationDistance = v));

        packLabel = new StringWidget(x, y + 54, w, 12, packText(cfg), this.font);
        addRenderableWidget(packLabel);

        addRenderableWidget(Button.builder(Component.literal("Texturepack hochladen (.zip)"), b -> pickPack())
                .bounds(x, y + 70, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Texturepack entfernen"), b -> {
            cfg.packFile = "";
            packLabel.setMessage(packText(cfg));
        }).bounds(x, y + 94, w, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(x, y + 124, w, 20).build());
    }

    private Component packText(ProfileConfig cfg) {
        return Component.literal("Pack: " + (cfg.packFile.isBlank() ? "keins" : cfg.packFile));
    }

    private void pickPack() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.zip")).flip();
            String result = TinyFileDialogs.tinyfd_openFileDialog(
                    "Texturepack auswählen", null, filters, "ZIP-Dateien", false);
            if (result != null) {
                ProfileManager.importPack(this.minecraft, new File(result));
                packLabel.setMessage(packText(ProfileConfig.get()));
            }
        } catch (Throwable t) {
            // Fallback: resourcepacks-Ordner öffnen, Datei manuell reinziehen
            t.printStackTrace();
            Util.getPlatform().openPath(this.minecraft.getResourcePackDirectory());
        }
    }

    @Override
    public void onClose() {
        ProfileConfig.get().save();
        this.minecraft.setScreen(null);
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String label;
        private final int min, max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, int h, String label, int min, int max, int value, IntConsumer onChange) {
            super(x, y, w, h, Component.empty(), (value - min) / (double) (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + current()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(current());
        }
    }
}
