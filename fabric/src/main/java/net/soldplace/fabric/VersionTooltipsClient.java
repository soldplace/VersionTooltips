package net.soldplace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;
import net.soldplace.ModCommands;

public class VersionTooltipsClient implements ClientModInitializer {
	private static final java.util.Set<Identifier> MISSING_REPORTED = new java.util.HashSet<>();
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("versiontooltips", "main")
	);
	public static KeyMapping SHOW_KEY;
	@Override
	public void onInitializeClient() {
		AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);
		ModCommands.register();
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
				.registerReloader(Identifier.fromNamespaceAndPath("versiontooltips", "item_versions"), new ItemVersionLoader());

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ItemVersionLoader.reportMissingEnchantments(handler.registryAccess());
		});

		SHOW_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.versiontooltips.show", // Translation key for the name
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT_SHIFT,
				CATEGORY
		));

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
				if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), boundKeyCode)) {
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