package net.soldplace.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.soldplace.config.VersionTooltipsConfig;

import java.util.Arrays;
import java.util.function.Consumer;

public class ModCommandsNeoForge {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("versiontooltips")
                        .executes(ctx -> {
                            var config = AutoConfig.getConfigHolder(VersionTooltipsConfig.class).getConfig();
                            ctx.getSource().sendSystemMessage(Component.literal(
                                    "Current Version Tooltips config:\n" +
                                            "  §6active:§r           " + config.modActive + "\n" +
                                            "  §6type:§r             " + config.type.name() + "\n" +
                                            "  §6style:§r            " + config.style.name() + "\n" +
                                            "  §6requirekeybind:§r " + config.requireKeybind + "\n" +
                                            "  §6advancedonly:§r " + config.onlyShowWithAdvancedTooltips
                            ));
                            return 1;
                        })

                        .then(Commands.literal("active")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean value = BoolArgumentType.getBool(ctx, "value");
                                            setConfig(c -> c.modActive = value);
                                            ctx.getSource().sendSystemMessage(Component.literal(
                                                    "§6[VersionTooltips]§r active set to " + value));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("type")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(VersionTooltipsConfig.TooltipType.values()).map(Enum::name),
                                                builder
                                        ))
                                        .executes(ctx -> {
                                            String value = StringArgumentType.getString(ctx, "value");
                                            try {
                                                var parsed = VersionTooltipsConfig.TooltipType.valueOf(value);
                                                setConfig(c -> c.type = parsed);
                                                ctx.getSource().sendSystemMessage(Component.literal(
                                                        "§6[VersionTooltips]§r type set to " + parsed.name()));
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendSystemMessage(Component.literal(
                                                        "§6[VersionTooltips]§r Unknown type '" + value + "'. Options: " +
                                                                Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                                                                        .map(Enum::name)
                                                                        .reduce((a, b) -> a + ", " + b).orElse("")));
                                            }
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("style")
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(VersionTooltipsConfig.TooltipStyle.values()).map(Enum::name),
                                                builder
                                        ))
                                        .executes(ctx -> {
                                            String value = StringArgumentType.getString(ctx, "value");
                                            try {
                                                var parsed = VersionTooltipsConfig.TooltipStyle.valueOf(value);
                                                setConfig(c -> c.style = parsed);
                                                ctx.getSource().sendSystemMessage(Component.literal(
                                                        "§6[VersionTooltips]§r style set to " + parsed.name()));
                                            } catch (IllegalArgumentException e) {
                                                ctx.getSource().sendSystemMessage(Component.literal(
                                                        "§6[VersionTooltips]§r Unknown style '" + value + "'. Options: Vanilla, Modded"));
                                            }
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("requirekeybind")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean value = BoolArgumentType.getBool(ctx, "value");
                                            setConfig(c -> c.requireKeybind = value);
                                            ctx.getSource().sendSystemMessage(Component.literal(
                                                    "§6[VersionTooltips]§r keybind set to " + value));
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("advancedtooltips")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            boolean value = BoolArgumentType.getBool(ctx, "value");
                                            setConfig(c -> c.onlyShowWithAdvancedTooltips = value);
                                            ctx.getSource().sendSystemMessage(Component.literal(
                                                    "§6[VersionTooltips]§r advancedonly set to " + value));
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static void setConfig(Consumer<VersionTooltipsConfig> mutator) {
        var holder = AutoConfig.getConfigHolder(VersionTooltipsConfig.class);
        mutator.accept(holder.getConfig());
        holder.save();
    }
}