package ru.kelcuprum.waterplayer.gui.screens.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxSecretString;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;

public class SecretConfigsScreen extends Screen {
    private final Screen parent;
    private static final Component TITLE = Localization.getText("waterplayer.name");
    // CATEGORYES
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private Button MainConfigCategoryButton;
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private Button LocalizationConfigCategoryButton;
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private Button SecretConfigCategoryButton;
    // CATEGORY CONTENT
    private TextBox titleBox;

    private TextBox tokens;
    private Component tokensText = Localization.getText("waterplayer.secret.title.tokens");
    private EditBoxSecretString yandexMusic;
    private Component yandexMusicText = Localization.getText("waterplayer.config.yandex_music_token");
    private EditBoxSecretString deezer;
    private Component deezerText = Localization.getText("waterplayer.config.deezer_decryption_key");
    private EditBoxString floweryTTS;
    private Component floweryTTSText = Localization.getText("waterplayer.config.flowery_tts_voice");

    private TextBox spotify;
    private Component spotifyText = Localization.getText("waterplayer.secret.title.spotify");
    private EditBoxSecretString spotifyClientID;
    private Component spotifyClientIDText = Localization.getText("waterplayer.config.spotify_client_id");
    private EditBoxSecretString spotifyClientSecret;
    private Component spotifyClientSecretText = Localization.getText("waterplayer.config.spotify_client_secret");
    private EditBoxString spotifyCountryCode;
    private Component spotifyCountryCodeText = Localization.getText("waterplayer.config.spotify_country_code");

    private TextBox appleMusic;
    private Component appleMusicText = Localization.getText("waterplayer.secret.title.apple_music");
    private EditBoxSecretString appleMusicMediaAPIToken;
    private Component appleMusicMediaAPITokenText = Localization.getText("waterplayer.config.apple_music_media_api_token");
    private EditBoxString appleMusicCountryCode;
    private Component appleMusicCountryCodeText = Localization.getText("waterplayer.config.apple_music_country_code");
    //
    private static final Component EXIT = Localization.getText("waterplayer.screen.exit");
    private int scrolled = 0;

    public SecretConfigsScreen(Screen parent) {
        super(SecretConfigCategory);
        this.parent = parent;
    }

    public void tick() {
        this.titleBox.setYPos(15 - this.scrolled);
        //

        this.tokens.setYPos(40 - this.scrolled);
        this.yandexMusic.setYPos(65 - this.scrolled);
        this.deezer.setYPos(90 - this.scrolled);
        this.floweryTTS.setYPos(115 - this.scrolled);

        this.spotify.setYPos(140 - this.scrolled);
        this.spotifyClientID.setYPos(165 - this.scrolled);
        this.spotifyClientSecret.setYPos(190 - this.scrolled);
        this.spotifyCountryCode.setYPos(215 - this.scrolled);

        this.appleMusic.setYPos(240 - this.scrolled);
        this.appleMusicMediaAPIToken.setYPos(265 - this.scrolled);
        this.appleMusicCountryCode.setYPos(290 - this.scrolled);
        super.tick();
    }

    public void init() {
        this.scrolled = 0;
        this.initButton();
        this.initButtonsCategory();
    }

    private void initButtonsCategory() {
        int x = this.width - 150;
        this.titleBox = this.addRenderableWidget(new TextBox(140, 15, x, 9, this.title, true));
        //
        this.tokens = this.addRenderableWidget(new TextBox(140, 40, x, 20, tokensText, true));

        this.yandexMusic = new EditBoxSecretString(140, 65, x, 20, this.yandexMusicText);
        this.yandexMusic.setContent(WaterPlayer.config.getString("YANDEX_MUSIC_TOKEN", ""));
        this.yandexMusic.setResponse(s->{
            WaterPlayer.config.setString("YANDEX_MUSIC_TOKEN", s);
        });
        this.addRenderableWidget(yandexMusic);

        this.deezer = new EditBoxSecretString(140, 90, x, 20, this.deezerText);
        this.deezer.setContent(WaterPlayer.config.getString("DEEZER_DECRYPTION_KEY", ""));
        this.deezer.setResponse(s->{
            WaterPlayer.config.setString("DEEZER_DECRYPTION_KEY", s);
        });
        this.addRenderableWidget(deezer);

        this.floweryTTS = new EditBoxString(140, 115, x, 20, this.floweryTTSText);
        this.floweryTTS.setContent(WaterPlayer.config.getString("FLOWERY_TTS_VOICE", ""));
        this.floweryTTS.setResponse(s->{
            WaterPlayer.config.setString("FLOWERY_TTS_VOICE", s);
        });
        this.addRenderableWidget(floweryTTS);

        //
        this.spotify = this.addRenderableWidget(new TextBox(140, 140, x, 20, spotifyText, true));

        this.spotifyClientID = new EditBoxSecretString(140, 65, x, 20, this.spotifyClientIDText);
        this.spotifyClientID.setContent(WaterPlayer.config.getString("SPOTIFY_CLIENT_ID", ""));
        this.spotifyClientID.setResponse(s->{
            WaterPlayer.config.setString("SPOTIFY_CLIENT_ID", s);
        });
        this.addRenderableWidget(spotifyClientID);

        this.spotifyClientSecret = new EditBoxSecretString(140, 90, x, 20, this.spotifyClientSecretText);
        this.spotifyClientSecret.setContent(WaterPlayer.config.getString("SPOTIFY_CLIENT_SECRET", ""));
        this.spotifyClientSecret.setResponse(s->{
            WaterPlayer.config.setString("SPOTIFY_CLIENT_SECRET", s);
        });
        this.addRenderableWidget(spotifyClientSecret);

        this.spotifyCountryCode = new EditBoxString(140, 115, x, 20, this.spotifyCountryCodeText);
        this.spotifyCountryCode.setContent(WaterPlayer.config.getString("SPOTIFY_COUNTRY_CODE", "US"));
        this.spotifyCountryCode.setResponse(s->{
            WaterPlayer.config.setString("SPOTIFY_COUNTRY_CODE", s);
        });
        this.addRenderableWidget(spotifyCountryCode);

        //
        this.appleMusic = this.addRenderableWidget(new TextBox(140, 240, x, 20, appleMusicText, true));

        this.appleMusicMediaAPIToken = new EditBoxSecretString(140, 65, x, 20, this.appleMusicMediaAPITokenText);
        this.appleMusicMediaAPIToken.setContent(WaterPlayer.config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""));
        this.appleMusicMediaAPIToken.setResponse(s->{
            WaterPlayer.config.setString("APPLE_MUSIC_MEDIA_API_TOKEN", s);
        });
        this.addRenderableWidget(appleMusicMediaAPIToken);

        this.appleMusicCountryCode = new EditBoxString(140, 90, x, 20, this.appleMusicCountryCodeText);
        this.appleMusicCountryCode.setContent(WaterPlayer.config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"));
        this.appleMusicCountryCode.setResponse(s->{
            WaterPlayer.config.setString("APPLE_MUSIC_COUNTRY_CODE", s);
        });
        this.addRenderableWidget(appleMusicCountryCode);

    }

    private void initButton() {
        this.MainConfigCategoryButton = this.addRenderableWidget(new Button(10, 40, 110, 20, MainConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new MainConfigsScreen(this.parent));
        }));
        this.LocalizationConfigCategoryButton = this.addRenderableWidget(new Button(10, 65, 110, 20, LocalizationConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new LocalizationConfigsScreen(this.parent));
        }));
        this.SecretConfigCategoryButton = this.addRenderableWidget(new Button(10, 90, 110, 20, SecretConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new SecretConfigsScreen(this.parent));
        }));
        this.SecretConfigCategoryButton.setActive(false);
        //
        this.addRenderableWidget(new Button(10, this.height - 30, 110, 20, -1224789711, EXIT, (OnPress) -> {
            WaterPlayer.config.save();
            this.minecraft.setScreen(this.parent);
        }));
    }

    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.minecraft.level != null) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(guiGraphics);
        }

        InterfaceUtils.renderLeftPanel(guiGraphics, 130, this.height);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.minecraft.font, TITLE, 65, 15, -1);
    }

    public boolean mouseScrolled(double d, double e, double f, double g) {
        int scrolled = (int)((double)this.scrolled + g * 10.0 * -1.0);
        int size = 315;
        if (scrolled <= 0 || size <= this.height) {
            this.scrolled = 0;
        } else this.scrolled = Math.min(scrolled, size - this.height);

        return super.mouseScrolled(d, e, f, g);
    }
}
