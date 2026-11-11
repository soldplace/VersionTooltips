
package net.soldplace.forge;

import me.shedaniel.autoconfig.AutoConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;

import net.soldplace.config.VersionTooltipsConfig;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class ModCommandsForge {

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ModCommandsForge::onClientChat);
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
            var parsed = VersionTooltipsConfig.TooltipType.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );

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
            var parsed = VersionTooltipsConfig.TooltipStyle.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );

            setConfig(config -> config.style = parsed);

            send("§a[VersionTooltips]§r style set to " + parsed.name());

        } catch (IllegalArgumentException e) {

            send("§cUnknown style '" + value + "'. Options: "
                    + Arrays.stream(VersionTooltipsConfig.TooltipStyle.values())
                    .map(Enum::name)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
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
