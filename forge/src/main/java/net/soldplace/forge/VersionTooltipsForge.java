package net.soldplace.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;

import org.lwjgl.glfw.GLFW;

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
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) ->
                                    AutoConfig.getConfigScreen(
                                            VersionTooltipsConfig.class,
                                            parent
                                    ).get()
                    )
            );
        }

        var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
                .get()
                .getModEventBus();

        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerKeyMappings);
        modBus.addListener(this::registerReloadListeners);

        MinecraftForge.EVENT_BUS.addListener(this::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerJoin);
        MinecraftForge.EVENT_BUS.addListener(this::registerClientCommands);
    }


    private void clientSetup(FMLClientSetupEvent event) {
        AutoConfig.register(
                VersionTooltipsConfig.class,
                GsonConfigSerializer::new
        );

        config = AutoConfig.getConfigHolder(
                VersionTooltipsConfig.class
        ).get();
    }


    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SHOW_KEY.get());
    }


    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ItemVersionLoader());
    }


    private void registerClientCommands(RegisterClientCommandsEvent event) {
        net.soldplace.forge.ModCommandsForge.register(
                event.getDispatcher()
        );
    }


    private void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {

        if (Minecraft.getInstance().getConnection() != null) {
            ItemVersionLoader.reportMissingEnchantments(
                    Minecraft.getInstance()
                            .getConnection()
                            .registryAccess()
            );
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