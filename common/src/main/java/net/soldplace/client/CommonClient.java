package net.soldplace.client;

import com.mojang.blaze3d.platform.InputConstants;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;

import org.lwjgl.glfw.GLFW;

public class CommonClient {
    public static KeyMapping SHOW_KEY;
    public static void init() {
        AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);

        SHOW_KEY = new KeyMapping(
                "key.versiontooltips.show",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                new KeyMapping.Category(Identifier.fromNamespaceAndPath("versiontooltips", "main"))
        );

        KeyMappingRegistry.register(SHOW_KEY);
        ReloadListenerRegistry.register(
                PackType.CLIENT_RESOURCES,
                new ItemVersionLoader(),
                Identifier.fromNamespaceAndPath("versiontooltips", "item_versions")
        );
    }
}