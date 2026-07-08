package net.soldplace.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.Identifier;


import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemVersionLoader implements ResourceManagerReloadListener {

    public static Map<Identifier, String> BLOCK_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<Identifier, String> ITEM_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<Identifier, String> BLOCK_VERSIONS_VERSION = new HashMap<>();
    public static Map<Identifier, String> ITEM_VERSIONS_VERSION = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();

    public static Identifier getEffectiveId(ItemStack stack) {
        var potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null && potionContents.potion().isPresent()) {
            var key = potionContents.potion().get().unwrapKey();
            if (key.isPresent()) {
                Identifier effectId = key.get().identifier();
                String containerSuffix = switch (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) {
                    case "splash_potion"    -> "_splash";
                    case "lingering_potion" -> "_lingering";
                    case "tipped_arrow"     -> "_tipped";
                    default                 -> "_normal";
                };
                return Identifier.fromNamespaceAndPath(effectId.getNamespace(), effectId.getPath() + containerSuffix);
            }
        }

        var storedEnchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchants != null && !storedEnchants.isEmpty()) {
            var first = storedEnchants.entrySet().iterator().next().getKey();
            var key = first.unwrapKey();
            if (key.isPresent()) {
                System.out.println(key.get().identifier());
                return key.get().identifier();
            }
        }

        var paintings = stack.get(DataComponents.PAINTING_VARIANT);
        if (paintings != null) {
            var key = paintings.unwrapKey();
            if (key.isPresent()) {
                System.out.println(key.get().identifier());
                return key.get().identifier();
            }
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public void reload(ResourceManager manager) {
        BLOCK_VERSIONS_SNAPSHOT.clear();
        ITEM_VERSIONS_SNAPSHOT.clear();
        BLOCK_VERSIONS_VERSION.clear();
        ITEM_VERSIONS_VERSION.clear();

        manager.getResource(Identifier.fromNamespaceAndPath("versiontooltips", "block_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_SNAPSHOT.put(Identifier.parse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.fromNamespaceAndPath("versiontooltips", "item_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_SNAPSHOT.put(Identifier.parse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.fromNamespaceAndPath("versiontooltips", "block_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_VERSION.put(Identifier.parse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        manager.getResource(Identifier.fromNamespaceAndPath("versiontooltips", "item_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_VERSION.put(Identifier.parse(key), value)
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

        for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
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
        for (Identifier id : BuiltInRegistries.POTION.keySet()) {
            if (id.getPath().equals("empty")) continue;
            for (String suffix : new String[]{"_normal", "_splash", "_lingering", "_tipped"}) {
                Identifier compoundId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + suffix);
                String version = ITEM_VERSIONS_VERSION.get(compoundId);
                String snapshot = ITEM_VERSIONS_SNAPSHOT.get(compoundId);
                if (version == null || snapshot == null) {
                    missingPotions.add(compoundId.toString());
                }
            }
        }

        if (missingItems.isEmpty() && missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] No missing entries - every item has version data.");
            return;
        }

        if (!missingItems.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingItems.size() + " item(s) missing version data:\n" + String.join("\n", missingItems));
        }

        if (!missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingPotions.size() + " potion(s) missing version data:\n" + String.join("\n", missingPotions));
        }


    }
    public static void reportMissingEnchantments(net.minecraft.core.RegistryAccess registryManager) {
        var enchantmentRegistry = registryManager.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        List<String> missing = new ArrayList<>();

        for (Identifier id : enchantmentRegistry.keySet()) {
            String version = ITEM_VERSIONS_VERSION.get(id);
            String snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);

            if (version == null || snapshot == null) {
                missing.add(id.toString());
            }
        }

        if (missing.isEmpty()) {
            System.out.println("[VersionTooltips] No missing enchantments - every enchantment has version data.");
            return;
        }

        System.out.println("[VersionTooltips] " + missing.size() + " enchantment(s) missing version data:\n" + String.join("\n", missing));
    }

    public static void reportMissingPaintings(net.minecraft.core.RegistryAccess registryManager) {
        var enchantmentRegistry = registryManager.lookupOrThrow(Registries.PAINTING_VARIANT);
        List<String> missingA = new ArrayList<>();

        for (Identifier id : enchantmentRegistry.keySet()) {
            String version = ITEM_VERSIONS_VERSION.get(id);
            String snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);

            if (version == null || snapshot == null) {
                missingA.add(id.toString());
            }
        }

        if (missingA.isEmpty()) {
            System.out.println("[VersionTooltips] No missing painting variants - every painting variant has version data.");
            return;
        }

        System.out.println("[VersionTooltips] " + missingA.size() + " painting variant(s) missing version data:\n" + String.join("\n", missingA));
    }


    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reload(resourceManager);
    }
}
