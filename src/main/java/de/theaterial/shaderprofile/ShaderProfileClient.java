package de.theaterial.shaderprofile;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ShaderProfileClient implements ClientModInitializer {
    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        ProfileConfig.get();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("shaderprofile", "main"));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.shaderprofile.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, category));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.shaderprofile.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) {
                ProfileManager.toggle(mc);
            }
            while (configKey.consumeClick()) {
                mc.setScreen(new ConfigScreen());
            }
        });
    }
}
