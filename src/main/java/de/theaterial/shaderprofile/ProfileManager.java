package de.theaterial.shaderprofile;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ProfileManager {
    private ProfileManager() {}

    private static boolean active = false;
    private static int prevRender, prevSim;
    private static boolean prevShaders;
    private static List<String> prevPacks = new ArrayList<>();

    public static boolean isActive() { return active; }

    public static void toggle(Minecraft mc) {
        if (active) deactivate(mc); else activate(mc);
    }

    private static void activate(Minecraft mc) {
        ProfileConfig cfg = ProfileConfig.get();
        PackRepository repo = mc.getResourcePackRepository();

        // alten Zustand merken
        prevRender = mc.options.renderDistance().get();
        prevSim = mc.options.simulationDistance().get();
        prevShaders = IrisBridge.areShadersEnabled();
        prevPacks = new ArrayList<>(repo.getSelectedIds());

        // Distanzen setzen
        mc.options.renderDistance().set(cfg.renderDistance);
        mc.options.simulationDistance().set(cfg.simulationDistance);
        mc.options.save();

        // Texturepack aktivieren
        if (cfg.packFile != null && !cfg.packFile.isBlank()) {
            repo.reload();
            String id = "file/" + cfg.packFile;
            if (repo.getAvailableIds().contains(id)) {
                List<String> ids = new ArrayList<>(repo.getSelectedIds());
                if (!ids.contains(id)) ids.add(id); // am Ende = höchste Priorität
                repo.setSelected(ids);
                mc.options.updateResourcePacks(repo); // speichert + lädt neu bei Änderung
            } else {
                message(mc, "Texturepack nicht gefunden: " + cfg.packFile);
            }
        }

        // Shader an
        if (!IrisBridge.setShadersEnabled(true)) {
            message(mc, "Iris nicht gefunden – Shader konnten nicht aktiviert werden");
        }

        active = true;
        message(mc, "Shader-Profil: AN (" + cfg.renderDistance + " / " + cfg.simulationDistance + ")");
    }

    private static void deactivate(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();

        mc.options.renderDistance().set(prevRender);
        mc.options.simulationDistance().set(prevSim);
        mc.options.save();

        repo.reload();
        repo.setSelected(prevPacks);
        mc.options.updateResourcePacks(repo);

        IrisBridge.setShadersEnabled(prevShaders);

        active = false;
        message(mc, "Shader-Profil: AUS");
    }

    /** Kopiert die gewählte Datei in den resourcepacks-Ordner und speichert den Namen. */
    public static void importPack(Minecraft mc, File source) throws Exception {
        Path dir = mc.getResourcePackDirectory();
        Files.createDirectories(dir);
        Path target = dir.resolve(source.getName());
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        ProfileConfig cfg = ProfileConfig.get();
        cfg.packFile = source.getName();
        cfg.save();
    }

    private static void message(Minecraft mc, String text) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(text), true);
    }
}
