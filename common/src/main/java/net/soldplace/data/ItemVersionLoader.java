
package net.soldplace.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.soldplace.nicolesrt;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemVersionLoader implements ResourceManagerReloadListener {

    public static Map<ResourceLocation, String> BLOCK_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<ResourceLocation, String> ITEM_VERSIONS_SNAPSHOT = new HashMap<>();
    public static Map<ResourceLocation, String> BLOCK_VERSIONS_VERSION = new HashMap<>();
    public static Map<ResourceLocation, String> ITEM_VERSIONS_VERSION = new HashMap<>();

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();

    public static ResourceLocation getEffectiveId(ItemStack stack) {

        if (stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.TIPPED_ARROW)) {

            Potion potion = PotionUtils.getPotion(stack);

            ResourceLocation effectId = Registry.POTION.getKey(potion);

            String containerSuffix = switch (Registry.ITEM.getKey(stack.getItem()).getPath()) {
                case "splash_potion"    -> "_splash";
                case "lingering_potion" -> "_lingering";
                case "tipped_arrow"     -> "_tipped";
                default                 -> "_normal";
            };

            return new ResourceLocation(
                    effectId.getNamespace(),
                    effectId.getPath() + containerSuffix
            );
        }

        if (stack.is(Items.ENCHANTED_BOOK)) {

            Map<Enchantment, Integer> storedEnchants =
                    EnchantmentHelper.getEnchantments(stack);

            if (!storedEnchants.isEmpty()) {
                Enchantment first = storedEnchants.keySet().iterator().next();
                return Registry.ENCHANTMENT.getKey(first);
            }
        }

        return Registry.ITEM.getKey(stack.getItem());
    }

    public void reload(ResourceManager manager) {

        BLOCK_VERSIONS_SNAPSHOT.clear();
        ITEM_VERSIONS_SNAPSHOT.clear();
        BLOCK_VERSIONS_VERSION.clear();
        ITEM_VERSIONS_VERSION.clear();

        loadJson(manager, "block_snapshots.json", BLOCK_VERSIONS_SNAPSHOT);
        loadJson(manager, "item_snapshots.json", ITEM_VERSIONS_SNAPSHOT);
        loadJson(manager, "block_versions.json", BLOCK_VERSIONS_VERSION);
        loadJson(manager, "item_versions.json", ITEM_VERSIONS_VERSION);

        reportMissingEntries();
    }

    private static void loadJson(
            ResourceManager manager,
            String fileName,
            Map<ResourceLocation, String> destination
    ) {

        ResourceLocation location = new ResourceLocation(
                "versiontooltips",
                fileName
        );

        if (!manager.hasResource(location)) {
            return;
        }

        try (var reader = new InputStreamReader(
                manager.getResource(location).getInputStream(),
                StandardCharsets.UTF_8
        )) {

            Map<String, String> raw = GSON.fromJson(reader, TYPE);

            if (raw == null) return;

            raw.forEach((key, value) -> {
                ResourceLocation id = ResourceLocation.tryParse(key);

                if (id != null) {
                    destination.put(id, value);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportMissingEntries() {

        if (!nicolesrt.debug) return;

        List<String> missingItems = new ArrayList<>();
        List<String> missingPotions = new ArrayList<>();

        for (ResourceLocation id : Registry.ITEM.keySet()) {

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
        for (ResourceLocation id : Registry.POTION.keySet()) {

            if (id.getPath().equals("empty")) continue;

            for (String suffix : new String[]{
                    "_normal",
                    "_splash",
                    "_lingering",
                    "_tipped"
            }) {

                ResourceLocation compoundId = new ResourceLocation(
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
            System.out.println(
                    "[VersionTooltips] " + missingItems.size()
                            + " item(s) missing version data:\n"
                            + String.join("\n", missingItems)
            );
        }

        if (!missingPotions.isEmpty()) {
            System.out.println(
                    "[VersionTooltips] " + missingPotions.size()
                            + " potion(s) missing version data:\n"
                            + String.join("\n", missingPotions)
            );
        }
    }

    public static void reportMissingEnchantments() {

        if (!nicolesrt.debug) return;

        List<String> missing = new ArrayList<>();

        for (ResourceLocation id : Registry.ENCHANTMENT.keySet()) {

            String version = ITEM_VERSIONS_VERSION.get(id);
            String snapshot = ITEM_VERSIONS_SNAPSHOT.get(id);

            if (version == null || snapshot == null) {
                missing.add("\"" + id + "\": \"\",");
            }
        }

        if (missing.isEmpty()) {
            System.out.println(
                    "[VersionTooltips] No missing enchantments - every enchantment has version data."
            );
            return;
        }

        System.out.println(
                "[VersionTooltips] " + missing.size()
                        + " enchantment(s) missing version data:\n"
                        + String.join("\n", missing)
        );
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        reload(resourceManager);
    }
}
