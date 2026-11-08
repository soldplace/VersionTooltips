
package net.soldplace.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.soldplace.nicolesrt;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class ItemVersionLoader implements ResourceManagerReloadListener {

    public static Map<ResourceLocation, String> BLOCK_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<ResourceLocation, String> ITEM_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<ResourceLocation, String> BLOCK_VERSIONS_VERSION = new HashMap<>();
    public static Map<ResourceLocation, String> ITEM_VERSIONS_VERSION = new HashMap<>();

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private static ResourceLocation createResourceLocation(String namespace, String path) {
        return Objects.requireNonNull(
                ResourceLocation.tryBuild(namespace, path),
                "Invalid resource location: " + namespace + ":" + path
        );
    }

    public static ResourceLocation getEffectiveId(ItemStack stack) {

        // Potions
        if (stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.TIPPED_ARROW)) {

            PotionContents potionContents = stack.getOrDefault(
                    DataComponents.POTION_CONTENTS,
                    PotionContents.EMPTY
            );

            ResourceLocation effectId = potionContents.potion()
                    .map(potion -> BuiltInRegistries.POTION.getKey(potion.value()))
                    .orElseGet(() -> createResourceLocation("minecraft", "empty"));

            String containerSuffix = switch (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) {
                case "splash_potion"    -> "_splash";
                case "lingering_potion" -> "_lingering";
                case "tipped_arrow"     -> "_tipped";
                default                 -> "_normal";
            };

            return createResourceLocation(
                    effectId.getNamespace(),
                    effectId.getPath() + containerSuffix
            );
        }

        // Enchanted books
        if (stack.is(Items.ENCHANTED_BOOK)) {

            ItemEnchantments storedEnchants = stack.getOrDefault(
                    DataComponents.STORED_ENCHANTMENTS,
                    ItemEnchantments.EMPTY
            );

            if (!storedEnchants.isEmpty()) {
                var first = storedEnchants.keySet().iterator().next();

                return BuiltInRegistries.ENCHANTMENT.getKey(first.value());
            }
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public void reload(ResourceManager manager) {

        BLOCK_VERSIONS_SNAPSHOT.clear();
        ITEM_VERSIONS_SNAPSHOT.clear();
        BLOCK_VERSIONS_VERSION.clear();
        ITEM_VERSIONS_VERSION.clear();

        manager.getResource(createResourceLocation("versiontooltips", "block_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_SNAPSHOT.put(ResourceLocation.tryParse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        manager.getResource(createResourceLocation("versiontooltips", "item_snapshots.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_SNAPSHOT.put(ResourceLocation.tryParse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        manager.getResource(createResourceLocation("versiontooltips", "block_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                BLOCK_VERSIONS_VERSION.put(ResourceLocation.tryParse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        manager.getResource(createResourceLocation("versiontooltips", "item_versions.json"))
                .ifPresent(resource -> {
                    try (var reader = new InputStreamReader(resource.open())) {
                        Map<String, String> raw = GSON.fromJson(reader, TYPE);
                        raw.forEach((key, value) ->
                                ITEM_VERSIONS_VERSION.put(ResourceLocation.tryParse(key), value)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        reportMissingEntries();
    }

    public static void reportMissingEntries() {

        if (!nicolesrt.debug) return;

        List<String> missingItems = new ArrayList<>();
        List<String> missingPotions = new ArrayList<>();

        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {

            String version = BLOCK_VERSIONS_VERSION.get(id);
            if (version == null) {
                version = ITEM_VERSIONS_VERSION.get(id);
            }

            String snapshot = BLOCK_VERSIONS_SNAPSHOT.get(id);
            if (snapshot == null) {
                snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);
            }

            if (version == null || snapshot == null) {
                missingItems.add("\"" + id + "\": \"\",");
            }
        }

        // Potions logic
        for (ResourceLocation id : BuiltInRegistries.POTION.keySet()) {

            if (id.getPath().equals("empty")) continue;

            for (String suffix : new String[]{"_normal", "_splash", "_lingering", "_tipped"}) {

                ResourceLocation compoundId = createResourceLocation(
                        id.getNamespace(),
                        id.getPath() + suffix
                );

                String version = ITEM_VERSIONS_VERSION.get(compoundId);
                String snapshot = ITEM_VERSIONS_SNAPSHOT.get(compoundId);

                if (version == null || snapshot == null) {
                    missingPotions.add("\"" + compoundId + "\": \"\",");
                }
            }
        }

        if (missingItems.isEmpty() && missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] No missing entries - every item has version data.");
            return;
        }

        if (!missingItems.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingItems.size()
                    + " item(s) missing version data:\n" + String.join("\n", missingItems));
        }

        if (!missingPotions.isEmpty()) {
            System.out.println("[VersionTooltips] " + missingPotions.size()
                    + " potion(s) missing version data:\n" + String.join("\n", missingPotions));
        }
    }

    public static void reportMissingEnchantments(net.minecraft.core.RegistryAccess registryManager) {

        if (!nicolesrt.debug) return;

        var enchantmentRegistry = registryManager.lookupOrThrow(
                net.minecraft.core.registries.Registries.ENCHANTMENT
        );

        List<String> missing = new ArrayList<>();

        enchantmentRegistry.listElements().forEach(holder -> {

            ResourceLocation id = holder.key().location();

            String version = ITEM_VERSIONS_VERSION.get(id);
            String snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);

            if (version == null || snapshot == null) {
                missing.add("\"" + id + "\": \"\",");
            }
        });

        if (missing.isEmpty()) {
            System.out.println("[VersionTooltips] No missing enchantments - every enchantment has version data.");
            return;
        }

        System.out.println("[VersionTooltips] " + missing.size()
                + " enchantment(s) missing version data:\n" + String.join("\n", missing));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reload(resourceManager);
    }
}
