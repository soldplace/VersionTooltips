package net.soldplace;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.soldplace.config.VersionTooltipsConfig;
import net.soldplace.data.ItemVersionLoader;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;


public class VersionTooltipsClient implements ClientModInitializer {
	private static final java.util.Set<Identifier> MISSING_REPORTED = new java.util.HashSet<>();
	private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
			Identifier.of("versiontooltips", "main")
	);
	public static KeyBinding SHOW_KEY;
	@Override
	public void onInitializeClient() {
		AutoConfig.register(VersionTooltipsConfig.class, GsonConfigSerializer::new);

		ResourceLoader.get(ResourceType.CLIENT_RESOURCES)
				.registerReloader(Identifier.of("versiontooltips", "item_versions"), new ItemVersionLoader());

		SHOW_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.versiontooltips.show", // Translation key for the name
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT_SHIFT,
				CATEGORY
		));

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			var config = AutoConfig.getConfigHolder(VersionTooltipsConfig.class).getConfig();

			if (!config.modActive) {
				return;
			}

			if (config.onlyShowWithAdvancedTooltips) {
				if (!MinecraftClient.getInstance().options.advancedItemTooltips) {
					return;
				}
			}

			if (config.requireKeybind) {
				int boundKeyCode = KeyBindingHelper.getBoundKeyOf(SHOW_KEY).getCode();
				if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), boundKeyCode)) {
					return;
				}
			}

			var id = Registries.ITEM.getId(stack.getItem());



			if (config.type == VersionTooltipsConfig.TooltipType.SnapshotOnly) {
				String snapshot = ItemVersionLoader.BLOCK_VERSIONS_SNAPSHOT.get(id);

				if (snapshot == null) {
					snapshot = ItemVersionLoader.ITEM_VERSIONS_SNAPSHOT.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Text.literal("+ ").formatted(Formatting.RED)
							.append(Text.literal(snapshot).formatted(Formatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Text.literal("Added in " + snapshot).formatted(Formatting.DARK_GRAY));
					}
				}
			}

			if (config.type == VersionTooltipsConfig.TooltipType.VersionOnly) {
				String version = ItemVersionLoader.BLOCK_VERSIONS_VERSION.get(id);

				if (version == null) {
					version = ItemVersionLoader.ITEM_VERSIONS_VERSION.get(id);
				}

				if (config.style == VersionTooltipsConfig.TooltipStyle.Modded) {
					lines.add(Text.literal("+ ").formatted(Formatting.BLUE)
							.append(Text.literal(version).formatted(Formatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Text.literal("Added in " + version).formatted(Formatting.DARK_GRAY));
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
					lines.add(Text.literal("+ ").formatted(Formatting.GREEN)
							.append(Text.literal(version + " (" + snapshot + ")").formatted(Formatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Text.literal("Added in " + version + " (" + snapshot + ")").formatted(Formatting.DARK_GRAY));
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
					lines.add(Text.literal("+ ").formatted(Formatting.GREEN)
							.append(Text.literal(snapshot + " (" + version + ")").formatted(Formatting.WHITE)));
				} else {
					if (config.style == VersionTooltipsConfig.TooltipStyle.Vanilla) {
						lines.add(Text.literal("Added in " + snapshot + " (" + version + ")").formatted(Formatting.DARK_GRAY));
					}
				}
			}




		});
	}
}