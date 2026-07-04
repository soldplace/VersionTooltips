package net.soldplace;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.soldplace.config.VersionTooltipsConfig;

import java.util.Arrays;

public class ModCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("versiontooltips")
                            .executes(ctx -> {

                                var config = AutoConfig.getConfigHolder(VersionTooltipsConfig.class).getConfig();
                                ctx.getSource().sendFeedback(Component.literal(
                                        "Current Version Tooltips config:\n" +
                                                "  §aactive:§r           " + config.modActive + "\n" +
                                                "  §atype:§r             " + config.type.name() + "\n" +
                                                "  §astyle:§r            " + config.style.name() + "\n" +
                                                "  §arequirekeybind:§r " + config.requireKeybind + "\n" +
                                                "  §aadvancedonly:§r " + config.onlyShowWithAdvancedTooltips
                                ));
                                return 1;
                            })

                            .then(ClientCommandManager.literal("active")
                                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                            .executes(ctx -> {
                                                boolean value = BoolArgumentType.getBool(ctx, "value");
                                                setConfig(config -> config.modActive = value);
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "§a[VersionTooltips]§r active set to " + value
                                                ));
                                                return 1;
                                            })
                                    )
                            )

                            .then(ClientCommandManager.literal("type")
                                    .then(ClientCommandManager.argument("value", StringArgumentType.word())
                                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                    Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                                                            .map(Enum::name),
                                                    builder
                                            ))
                                            .executes(ctx -> {
                                                String value = StringArgumentType.getString(ctx, "value");
                                                try {
                                                    var parsed = VersionTooltipsConfig.TooltipType.valueOf(value);
                                                    setConfig(config -> config.type = parsed);
                                                    ctx.getSource().sendFeedback(Component.literal(
                                                            "§a[VersionTooltips]§r type set to " + parsed.name()
                                                    ));
                                                } catch (IllegalArgumentException e) {
                                                    ctx.getSource().sendFeedback(Component.literal(
                                                            "§a[VersionTooltips]§r Unknown type '" + value + "'. Options: " +
                                                                    Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                                                                            .map(Enum::name)
                                                                            .reduce((a, b) -> a + ", " + b).orElse("")
                                                    ));
                                                }
                                                return 1;
                                            })
                                    )
                            )

                            .then(ClientCommandManager.literal("style")
                                    .then(ClientCommandManager.argument("value", StringArgumentType.word())
                                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                    Arrays.stream(VersionTooltipsConfig.TooltipStyle.values())
                                                            .map(Enum::name),
                                                    builder
                                            ))
                                            .executes(ctx -> {
                                                String value = StringArgumentType.getString(ctx, "value");
                                                try {
                                                    var parsed = VersionTooltipsConfig.TooltipStyle.valueOf(value);
                                                    setConfig(config -> config.style = parsed);
                                                    ctx.getSource().sendFeedback(Component.literal(
                                                            "§a[VersionTooltips]§r style set to " + parsed.name()
                                                    ));
                                                } catch (IllegalArgumentException e) {
                                                    ctx.getSource().sendFeedback(Component.literal(
                                                            "§a[VersionTooltips]§r Unknown style '" + value + "'. Options: Vanilla, Modded"
                                                    ));
                                                }
                                                return 1;
                                            })
                                    )
                            )

                            .then(ClientCommandManager.literal("requirekeybind")
                                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                            .executes(ctx -> {
                                                boolean value = BoolArgumentType.getBool(ctx, "value");
                                                setConfig(config -> config.requireKeybind = value);
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "§a[VersionTooltips]§r keybind set to " + value
                                                ));
                                                return 1;
                                            })
                                    )
                            )

                            .then(ClientCommandManager.literal("advancedonly")
                                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                            .executes(ctx -> {
                                                boolean value = BoolArgumentType.getBool(ctx, "value");
                                                setConfig(config -> config.onlyShowWithAdvancedTooltips = value);
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "§a[VersionTooltips]§r advancedonly set to " + value
                                                ));
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }

    // Helper to mutate and immediately save the config in one call
    private static void setConfig(java.util.function.Consumer<VersionTooltipsConfig> mutator) {
        var holder = AutoConfig.getConfigHolder(VersionTooltipsConfig.class);
        mutator.accept(holder.getConfig());
        holder.save();
    }
}
