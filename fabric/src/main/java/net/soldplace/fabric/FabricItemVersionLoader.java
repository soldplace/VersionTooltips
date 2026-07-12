package net.soldplace.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.soldplace.data.ItemVersionLoader;

public class FabricItemVersionLoader extends ItemVersionLoader
        implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(
                "versiontooltips",
                "item_versions");
    }
}
