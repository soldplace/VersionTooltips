
package net.soldplace.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.event.TickEvent;
import net.soldplace.config.VersionTooltipsConfig;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class ModCommandsForge {
    private static CommandDispatcher<SharedSuggestionProvider> registeredDispatcher;
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ModCommandsForge::onClientChat);
        MinecraftForge.EVENT_BUS.addListener(ModCommandsForge::onClientChat);
        MinecraftForge.EVENT_BUS.addListener(ModCommandsForge::onClientTick);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;

        var connection = Minecraft.getInstance().getConnection();

        if (connection == null) {
            registeredDispatcher = null;
            return;
        }

        var dispatcher = connection.getCommands();

        // Don't register the suggestions twice
        if (dispatcher == registeredDispatcher) return;

        dispatcher.register(
                LiteralArgumentBuilder.<SharedSuggestionProvider>literal("versiontooltips")

                        .executes(ctx -> 1)

                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("active")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, Boolean>argument(
                                                "value", BoolArgumentType.bool())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        new String[]{"true", "false"}, builder
                                                ))
                                        .executes(ctx -> 1)
                                )
                        )

                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("type")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, String>argument(
                                                "value", StringArgumentType.word())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                                                                .map(Enum::name),
                                                        builder
                                                ))
                                        .executes(ctx -> 1)
                                )
                        )

                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("style")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, String>argument(
                                                "value", StringArgumentType.word())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        Arrays.stream(VersionTooltipsConfig.TooltipStyle.values())
                                                                .map(Enum::name),
                                                        builder
                                                ))
                                        .executes(ctx -> 1)
                                )
                        )

                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("requirekeybind")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, Boolean>argument(
                                                "value", BoolArgumentType.bool())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        new String[]{"true", "false"}, builder
                                                ))
                                        .executes(ctx -> 1)
                                )
                        )

                        .then(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("advancedtooltips")
                                .then(RequiredArgumentBuilder.<SharedSuggestionProvider, Boolean>argument(
                                                "value", BoolArgumentType.bool())
                                        .suggests((ctx, builder) ->
                                                SharedSuggestionProvider.suggest(
                                                        new String[]{"true", "false"}, builder
                                                ))
                                        .executes(ctx -> 1)
                                )
                        )
        );

        registeredDispatcher = dispatcher;
    }

    private static void onClientChat(ClientChatEvent event) {

        String message = event.getMessage().trim();

        if (!message.startsWith("/")) return;

        String[] args = message.substring(1).split("\\s+");

        if (!args[0].equalsIgnoreCase("versiontooltips")) return;

        // Prevent the command from reaching the server
        event.setCanceled(true);

        // Remove the root command
        args = Arrays.copyOfRange(args, 1, args.length);

        execute(args);
    }

    private static void execute(String[] args) {

        if (args.length == 0) {
            showConfig();
            return;
        }

        if (args.length != 2) {
            send("§cUsage: /versiontooltips <option> <value>");
            return;
        }

        String option = args[0].toLowerCase(Locale.ROOT);
        String value = args[1];

        switch (option) {

            case "active" -> setBoolean(value,
                    config -> config.modActive = configValue(value),
                    "active");

            case "requirekeybind" -> setBoolean(value,
                    config -> config.requireKeybind = configValue(value),
                    "keybind");

            case "advancedtooltips" -> setBoolean(value,
                    config -> config.onlyShowWithAdvancedTooltips = configValue(value),
                    "advancedonly");

            case "type" -> setType(value);

            case "style" -> setStyle(value);

            default -> send("§cUnknown option '" + option + "'.");
        }
    }

    private static boolean configValue(String value) {
        return Boolean.parseBoolean(value);
    }

    private static void setBoolean(
            String value,
            Consumer<VersionTooltipsConfig> mutator,
            String name
    ) {

        if (!value.equalsIgnoreCase("true")
                && !value.equalsIgnoreCase("false")) {

            send("§cPlease specify true or false.");
            return;
        }

        setConfig(mutator);

        send("§a[VersionTooltips]§r " + name + " set to "
                + value.toLowerCase(Locale.ROOT));
    }

    private static void setType(String value) {

        try {
            var parsed = Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                    .filter(type -> type.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException());

            setConfig(config -> config.type = parsed);

            send("§a[VersionTooltips]§r type set to " + parsed.name());

        } catch (IllegalArgumentException e) {
            send("§cUnknown type '" + value + "'. Options: "
                    + Arrays.stream(VersionTooltipsConfig.TooltipType.values())
                    .map(Enum::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
    }

    private static void setStyle(String value) {

        try {
            var parsed = Arrays.stream(VersionTooltipsConfig.TooltipStyle.values())
                    .filter(style -> style.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException());

            setConfig(config -> config.style = parsed);

            send("§a[VersionTooltips]§r style set to " + parsed.name());

        } catch (IllegalArgumentException e) {
            send("§cUnknown style '" + value + "'. Options: Vanilla, Modded");
        }
    }

    private static void showConfig() {

        var config = AutoConfig.getConfigHolder(
                VersionTooltipsConfig.class
        ).getConfig();

        send(
                "Current Version Tooltips config:\n" +
                        "  §aactive:§r           " + config.modActive + "\n" +
                        "  §atype:§r             " + config.type.name() + "\n" +
                        "  §astyle:§r            " + config.style.name() + "\n" +
                        "  §arequirekeybind:§r " + config.requireKeybind + "\n" +
                        "  §aadvancedonly:§r " + config.onlyShowWithAdvancedTooltips
        );
    }

    private static void setConfig(Consumer<VersionTooltipsConfig> mutator) {

        var holder = AutoConfig.getConfigHolder(
                VersionTooltipsConfig.class
        );

        mutator.accept(holder.getConfig());

        holder.save();
    }

    private static void send(String message) {

        Minecraft.getInstance().gui.getChat().addMessage(
                new TextComponent(message)
        );
    }
}
