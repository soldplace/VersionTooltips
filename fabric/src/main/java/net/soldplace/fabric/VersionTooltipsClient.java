package net.soldplace.fabric;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;
import net.soldplace.ModCommands;

public class VersionTooltipsClient implements ClientModInitializer {
	public static KeyMapping SHOW_KEY;
	@Override
	public void onInitializeClient() {

		AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);
		SHOW_KEY = new KeyMapping(
				"key.versiontooltips.show",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT_SHIFT,
				"key.category.versiontooltips"
		);

		KeyMappingRegistry.register(SHOW_KEY);
		ReloadListenerRegistry.register(
				PackType.CLIENT_RESOURCES,
				new ItemVersionLoader(),
				ResourceLocation.fromNamespaceAndPath("versiontooltips", "item_versions")
		);
		ModCommands.register();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ItemVersionLoader.reportMissingEnchantments(handler.registryAccess());
		});

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			var config = AutoConfig.getConfigHolder(VersionTooltipsConfig.class).getConfig();

			if (!config.modActive) {
				return;
			}

			if (config.onlyShowWithAdvancedTooltips) {
				if (!Minecraft.getInstance().options.advancedItemTooltips) {
					return;
				}
			}

			if (config.requireKeybind) {
				int boundKeyCode = KeyBindingHelper.getBoundKeyOf(SHOW_KEY).getValue();
				if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKeyCode)) {
					return;
				}
			}

			var id = ItemVersionLoader.getEffectiveId(stack);



			if (config.type == VersionTooltipsConfig.TooltipType.SnapshotOnly) {
				String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);

				if (snapshot == null) {
					snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Component.literal("+ ").withStyle(ChatFormatting.RED)
							.append(Component.literal(snapshot).withStyle(ChatFormatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Component.literal("Added in " + snapshot).withStyle(ChatFormatting.DARK_GRAY));
					}
				}
			}

			if (config.type == VersionTooltipsConfig.TooltipType.VersionOnly) {
				String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);

				if (version == null) {
					version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Component.literal("+ ").withStyle(ChatFormatting.BLUE)
							.append(Component.literal(version).withStyle(ChatFormatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Component.literal("Added in " + version).withStyle(ChatFormatting.DARK_GRAY));
					}
				}
			}

			if (config.type == VersionTooltipsConfig.TooltipType.VersionSnapshot) {
				String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);
				String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);

				if (version == null) {
					version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);
				}

				if (snapshot == null) {
					snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Component.literal("+ ").withStyle(ChatFormatting.GREEN)
							.append(Component.literal(version + " (" + snapshot + ")").withStyle(ChatFormatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Component.literal("Added in " + version + " (" + snapshot + ")").withStyle(ChatFormatting.DARK_GRAY));
					}
				}
			}

			if (config.type == VersionTooltipsConfig.TooltipType.SnapshotVersion) {
				String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);
				String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);

				if (version == null) {
					version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);
				}

				if (snapshot == null) {
					snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Component.literal("+ ").withStyle(ChatFormatting.GREEN)
							.append(Component.literal(snapshot + " (" + version + ")").withStyle(ChatFormatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Component.literal("Added in " + snapshot + " (" + version + ")").withStyle(ChatFormatting.DARK_GRAY));
					}
				}
			}




		});
	}
}