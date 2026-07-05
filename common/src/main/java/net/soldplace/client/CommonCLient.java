package net.soldplace;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackType;
import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CommonClient {

    public static KeyMapping SHOW_KEY;

    public static void init() {
        AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);

        SHOW_KEY = new KeyMapping(
                "key.versiontooltips.show",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                "key.categories.versiontooltips"
        );
        KeyMappingRegistry.register(SHOW_KEY);

        ReloadListenerRegistry.register(
                PackType.CLIENT_RESOURCES,
                new ItemVersionLoader(),
                ResourceLocation.fromNamespaceAndPath("versiontooltips", "item_versions")
        );
    }
}