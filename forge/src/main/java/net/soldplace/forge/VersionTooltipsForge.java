
package net.soldplace.forge;

import com.mojang.blaze3d.platform.InputConstants;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;

import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod("versiontooltips")
public class VersionTooltipsForge {

    private static VersionTooltipsConfig config;

    public static final Lazy<KeyMapping> SHOW_KEY = Lazy.of(() ->
            new KeyMapping(
                    "key.versiontooltips.show",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_LEFT_SHIFT,
                    "key.category.versiontooltips"
            )
    );

    public VersionTooltipsForge() {

        if (FMLEnvironment.dist.isClient()) {

            ModCommandsForge.register();

            ModLoadingContext.get().registerExtensionPoint(
                    ExtensionPoint.CONFIGGUIFACTORY,
                    () -> (minecraft, parent) ->
                            AutoConfig.getConfigScreen(
                                    VersionTooltipsConfig.class,
                                    parent
                            ).get()
            );
        }

        net.minecraftforge.eventbus.api.IEventBus modBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }

    private void clientSetup(FMLClientSetupEvent event) {

        AutoConfig.register(
                VersionTooltipsConfig.class,
                GsonConfigSerializer::new
        );

        config = AutoConfig.getConfigHolder(
                VersionTooltipsConfig.class
        ).getConfig();

        event.enqueueWork(() -> {

            ClientRegistry.registerKeyBinding(SHOW_KEY.get());

            ReloadableResourceManager resourceManager =
                    (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
            ItemVersionLoader loader = new ItemVersionLoader();
            resourceManager.registerReloadListener(loader);
            loader.reload(resourceManager);
        });
    }

    private void onPlayerJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {

        if (Minecraft.getInstance().getConnection() != null) {
            ItemVersionLoader.reportMissingEnchantments();
        }
    }

    private void onTooltip(ItemTooltipEvent event) {

        if (config == null) return;
        if (!config.modActive) return;

        if (config.onlyShowWithAdvancedTooltips
                && !Minecraft.getInstance().options.advancedItemTooltips) {
            return;
        }

        if (config.requireKeybind && !SHOW_KEY.get().isDown()) {
            return;
        }

        net.minecraft.resources.ResourceLocation id =
                ItemVersionLoader.getEffectiveId(event.getItemStack());

        if (id == null) return;

        List<Component> lines = event.getToolTip();

        String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);

        if (snapshot == null) {
            snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);
        }

        String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);

        if (version == null) {
            version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);
        }

        if (config.type == VersionTooltipsConfig.TooltipType.SnapshotOnly) {

            if (snapshot == null) return;

            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(new TextComponent("+ ").withStyle(ChatFormatting.RED)
                        .append(new TextComponent(snapshot).withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(new TextComponent("Added in " + snapshot)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.VersionOnly) {

            if (version == null) return;

            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(new TextComponent("+ ").withStyle(ChatFormatting.BLUE)
                        .append(new TextComponent(version).withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(new TextComponent("Added in " + version)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.VersionSnapshot) {

            if (version == null || snapshot == null) return;

            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(new TextComponent("+ ").withStyle(ChatFormatting.GREEN)
                        .append(new TextComponent(version + " (" + snapshot + ")")
                                .withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(new TextComponent("Added in " + version + " (" + snapshot + ")")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.SnapshotVersion) {

            if (version == null || snapshot == null) return;

            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(new TextComponent("+ ").withStyle(ChatFormatting.GREEN)
                        .append(new TextComponent(snapshot + " (" + version + ")")
                                .withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(new TextComponent("Added in " + snapshot + " (" + version + ")")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
