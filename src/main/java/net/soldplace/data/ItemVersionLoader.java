package net.soldplace.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;


import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
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
    }
}
