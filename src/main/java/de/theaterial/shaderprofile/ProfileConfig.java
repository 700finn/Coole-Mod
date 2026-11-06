package de.theaterial.shaderprofile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProfileConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("shaderprofile.json");
    private static ProfileConfig instance;

    public int renderDistance = 16;
    public int simulationDistance = 8;
    /** Dateiname des Texturepacks im resourcepacks-Ordner (leer = keins) */
    public String packFile = "";

    public static ProfileConfig get() {
        if (instance == null) {
            try {
                if (Files.exists(PATH)) {
                    instance = GSON.fromJson(Files.readString(PATH), ProfileConfig.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (instance == null) instance = new ProfileConfig();
        }
        return instance;
    }

    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
