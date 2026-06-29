package net.soldplace.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;


@Config(name = "versiontooltips")
public class VersionTooltipsConfig implements ConfigData {
    public boolean modActive = true;
    @ConfigEntry.Gui.Tooltip
    public boolean requireKeybind = false;
    public boolean onlyShowWithAdvancedTooltips = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TooltipStyle style = TooltipStyle.Vanilla;
    public enum TooltipStyle {
        Vanilla,
        Modded
    }
    public enum TooltipVersionType implements me.shedaniel.clothconfig2.gui.widget.SelectionListEntry.Translatable {
        SnapshotOnly("text.versiontooltips.type.snapshot_only"),
        VersionOnly("text.versiontooltips.type.version_only"),
        SnapshotVersion("text.versiontooltips.type.snapshot_v"),
        VersionSnapshot("text.versiontooltips.type.v_snapshot");

        private final String key;

        TooltipVersionType(String key) {
            this.key = key;
        }

        @Override
        public @org.jetbrains.annotations.NotNull String getKey() {
            return this.key;
        }
    }
}