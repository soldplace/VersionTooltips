package net.soldplace.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@Mod("versiontooltips")
public class VersionTooltipsNeoForge {
    private static VersionTooltipsConfig config;
    public static final Lazy<KeyMapping> SHOW_KEY = Lazy.of(() ->
            new KeyMapping(
                    "key.versiontooltips.show",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_LEFT_SHIFT,
                    "key.category.versiontooltips"
            ));
    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SHOW_KEY.get());
    }

    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(ResourceLocation.fromNamespaceAndPath("versiontooltips", "item_versions"), new ItemVersionLoader());
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        ModCommandsNeoForge.register(event.getDispatcher());
    }

    private void clientSetup() {
        AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(VersionTooltipsConfig.class).get();

        NeoForge.EVENT_BUS.addListener(this::onTooltip);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
    }
    public VersionTooltipsNeoForge(IEventBus modEventBus, ModContainer modContainer)
    {
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.registerConfigScreen(modContainer);
        }

        modEventBus.addListener((FMLClientSetupEvent event) -> clientSetup());
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerReloadListeners);
    }
    private static class ClientSetup {

        private static void registerConfigScreen(ModContainer modContainer) {
            modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (mc, parent) -> AutoConfig.getConfigScreen(VersionTooltipsConfig.class, parent).get()
            );
        }

    }


    private void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (Minecraft.getInstance().getConnection() != null) {
            ItemVersionLoader.reportMissingEnchantments(Minecraft.getInstance().getConnection().registryAccess());
            ItemVersionLoader.reportMissingPaintings(Minecraft.getInstance().getConnection().registryAccess());
        }
    }


    private void onTooltip(ItemTooltipEvent event) {
        if (config == null) return;
        if (!config.modActive) return;
        if (config.onlyShowWithAdvancedTooltips && !Minecraft.getInstance().options.advancedItemTooltips) return;
        if (config.requireKeybind && SHOW_KEY != null) {
            int boundKeyCode = SHOW_KEY.get().getKey().getValue();
            if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKeyCode)) return;
        }

        var id = ItemVersionLoader.getEffectiveId(event.getItemStack());
        if (id == null) return;

        var lines = event.getToolTip();
        String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);
        if (snapshot == null) snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);

        String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);
        if (version == null) version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);

        if (config.type == VersionTooltipsConfig.TooltipType.SnapshotOnly) {
            if (snapshot == null) return;
            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(Component.literal("+ ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(snapshot).withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(Component.literal("Added in " + snapshot).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.VersionOnly) {
            if (version == null) return;
            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(Component.literal("+ ").withStyle(ChatFormatting.BLUE)
                        .append(Component.literal(version).withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(Component.literal("Added in " + version).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.VersionSnapshot) {
            if (version == null || snapshot == null) return;
            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(Component.literal("+ ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(version + " (" + snapshot + ")").withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(Component.literal("Added in " + version + " (" + snapshot + ")").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (config.type == VersionTooltipsConfig.TooltipType.SnapshotVersion) {
            if (version == null || snapshot == null) return;
            if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
                lines.add(Component.literal("+ ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(snapshot + " (" + version + ")").withStyle(ChatFormatting.WHITE)));
            } else {
                lines.add(Component.literal("Added in " + snapshot + " (" + version + ")").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}