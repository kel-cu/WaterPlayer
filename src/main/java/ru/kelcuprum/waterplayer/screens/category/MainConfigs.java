package ru.kelcuprum.waterplayer.screens.category;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import ru.kelcuprum.waterplayer.config.Localization;
import ru.kelcuprum.waterplayer.config.UserConfig;

public class MainConfigs {
    public ConfigCategory getCategory(ConfigBuilder builder){
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.config"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_overlay"),
                        UserConfig.ENABLE_OVERLAY)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> UserConfig.ENABLE_OVERLAY = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_notice"),
                        UserConfig.ENABLE_NOTICE)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> UserConfig.ENABLE_NOTICE = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startBooleanToggle(
                        Localization.getText("waterplayer.config.enable_change_title"),
                        UserConfig.ENABLE_CHANGE_TITLE)
                .setDefaultValue(true)
                .setTooltip(Localization.getText("waterplayer.config.enable_change_title.tooltip"))
                .setSaveConsumer(newValue -> UserConfig.ENABLE_CHANGE_TITLE = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.config.localization.title.overlay")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.overlay"),
                        Localization.getLocalization("overlay", false))
                .setDefaultValue(Localization.getLcnDefault("overlay"))
                .setSaveConsumer(newValue -> Localization.setLocalization("overlay", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.overlay.live"),
                        Localization.getLocalization("overlay.live", false))
                .setDefaultValue(Localization.getLcnDefault("overlay.live"))
                .setSaveConsumer(newValue -> Localization.setLocalization("overlay.live", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.localization.overlay.pause"),
                        Localization.getLocalization("overlay.pause", false))
                .setDefaultValue(Localization.getLcnDefault("overlay.pause"))
                .setSaveConsumer(newValue -> Localization.setLocalization("overlay.pause", newValue))
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
