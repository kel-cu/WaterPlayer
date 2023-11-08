package ru.kelcuprum.waterplayer.screens.config_old.category;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;

public class MainConfigs {
    public ConfigCategory getCategory(ConfigBuilder builder){
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.config"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_bossbar"),
                        WaterPlayer.config.getBoolean("ENABLE_BOSS_BAR", false))
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> WaterPlayer.config.setBoolean("ENABLE_BOSS_BAR", newValue))
                .build());
        //
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_overlay"),
                        WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true))
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> WaterPlayer.config.setBoolean("ENABLE_OVERLAY", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_notice"),
                        WaterPlayer.config.getBoolean("ENABLE_NOTICE", true))
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> WaterPlayer.config.setBoolean("ENABLE_NOTICE", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_change_title"),
                        WaterPlayer.config.getBoolean("ENABLE_CHANGE_TITLE", true))
                .setDefaultValue(true)
                .setTooltip(Localization.getText("waterplayer.config.enable_change_title.tooltip"))
                .setSaveConsumer(newValue -> WaterPlayer.config.setBoolean("ENABLE_CHANGE_TITLE", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.config.localization.title.bossbar")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.bossbar"),
                        Localization.getLocalization("bossbar", false))
                .setDefaultValue(Localization.getLcnDefault("bossbar"))
                .setSaveConsumer(newValue -> Localization.setLocalization("bossbar", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.bossbar.live"),
                        Localization.getLocalization("bossbar.live", false))
                .setDefaultValue(Localization.getLcnDefault("bossbar.live"))
                .setSaveConsumer(newValue -> Localization.setLocalization("bossbar.live", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.bossbar.pause"),
                        Localization.getLocalization("bossbar.pause", false))
                .setDefaultValue(Localization.getLcnDefault("bossbar.pause"))
                .setSaveConsumer(newValue -> Localization.setLocalization("bossbar.pause", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.config.localization.title.title")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.title"),
                        Localization.getLocalization("title", false))
                .setDefaultValue(Localization.getLcnDefault("title"))
                .setSaveConsumer(newValue -> Localization.setLocalization("title", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.title.live"),
                        Localization.getLocalization("title.live", false))
                .setDefaultValue(Localization.getLcnDefault("title.live"))
                .setSaveConsumer(newValue -> Localization.setLocalization("title.live", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.title.pause"),
                        Localization.getLocalization("title.pause", false))
                .setDefaultValue(Localization.getLcnDefault("title.pause"))
                .setSaveConsumer(newValue -> Localization.setLocalization("title.pause", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.config.localization.title.formats")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.format.author"),
                        Localization.getLocalization("format.author", false))
                .setDefaultValue(Localization.getLcnDefault("format.author"))
                .setSaveConsumer(newValue -> Localization.setLocalization("format.author", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.format.title"),
                        Localization.getLocalization("format.title", false))
                .setDefaultValue(Localization.getLcnDefault("format.title"))
                .setSaveConsumer(newValue -> Localization.setLocalization("format.title", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.format.time"),
                        Localization.getLocalization("format.time", false))
                .setDefaultValue(Localization.getLcnDefault("format.time"))
                .setSaveConsumer(newValue -> Localization.setLocalization("format.time", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.format.live"),
                        Localization.getLocalization("format.live", false))
                .setDefaultValue(Localization.getLcnDefault("format.live"))
                .setSaveConsumer(newValue -> Localization.setLocalization("format.live", newValue))
                .build());
        return category;
    }
}
