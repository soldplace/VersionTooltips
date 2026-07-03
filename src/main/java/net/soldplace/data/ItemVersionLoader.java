package net.soldplace.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;


import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemVersionLoader implements SynchronousResourceReloader {

    public static Map<Identifier, String> BLOCK_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<Identifier, String> ITEM_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<Identifier, String> BLOCK_VERSIONS_VERSION = new HashMap<>();
    public static Map<Identifier, String> ITEM_VERSIONS_VERSION = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();

    public Identifier getFabricId() {
        return Identifier.of("versiontooltips", "item_versions");
    }

    public static Identifier getEffectiveId(ItemStack stack) {
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents != null && potionContents.potion().isPresent()) {
            var key = potionContents.potion().get().getKey();
            if (key.isPresent()) {
                return key.get().getValue();
            }
        }

        var storedEnchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (storedEnchants != null && !storedEnchants.isEmpty()) {
            var first = storedEnchants.getEnchantmentEntries().iterator().next();
            var key = first.getKey().getKey();
            if (key.isPresent()) {
                return key.get().getValue();
            }
        }

        return Registries.ITEM.getId(stack.getItem());
    }




    @Override
    public void reload(ResourceManager manager) {
        BLOCK_VERSIONS_SNAPSHOT.clear();
        ITEM_VERSIONS_SNAPSHOT.clear();
        BLOCK_VERSIONS_VERSION.clear();
        ITEM_VERSIONS_VERSION.clear();

        manager.getResource(Identifier.of("versiontooltips", "block_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.getInputStream())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_SNAPSHOT.put(Identifier.of(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.of("versiontooltips", "item_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.getInputStream())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_SNAPSHOT.put(Identifier.of(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.of("versiontooltips", "block_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.getInputStream())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_VERSION.put(Identifier.of(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.of("versiontooltips", "item_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.getInputStream())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_VERSION.put(Identifier.of(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        reportMissingEntries();
    }

    public static void reportMissingEntries() {
        List<String> missingItems = new ArrayList<>();
        List<String> missingPotions = new ArrayList<>();

        for (Identifier id : Registries.ITEM.getIds()) {
            String version = BLOCK_VERSIONS_VERSION.get(id);
            if (version == null) {
                version = ITEM_VERSIONS_VERSION.get(id);
            }

            String snapshot = BLOCK_VERSIONS_SNAPSHOT.get(id);
            if (snapshot == null) {
                snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);
            }

            if (version == null || snapshot == null) {
                missingItems.add(id.toString());
            }
        }
        // Potions logic
        for (Identifier id : Registries.POTION.getIds()) {
            if (id.getPath().equals("empty")) continue;

            String version = ITEM_VERSIONS_VERSION.get(id);
            String snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);

            if (version == null || snapshot == null) {
                missingPotions.add(id.toString());
            }
        }

        if (missingItems.isEmpty() && missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] No missing entries — every item has version data.");
            return;
        }

        if (!missingItems.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingItems.size() + " item(s) missing version data:\n" + String.join("\n", missingItems));
        }

        if (!missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingPotions.size() + " potion(s) missing version data:\n" + String.join("\n", missingPotions));
        }

//        String list = missingItems.stream().map(id -> "\"" + id + "\": \"\",").collect(java.util.stream.Collectors.joining("\n"));
//        System.out.println("[VersionTooltips] " + missing.size() + " item(s) missing version and/or snapshot data:\n" + list);
    }
}