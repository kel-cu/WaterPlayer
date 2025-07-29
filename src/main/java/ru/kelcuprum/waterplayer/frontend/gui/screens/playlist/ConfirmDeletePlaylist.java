package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.RECYCLE_BIN;

public class ConfirmDeletePlaylist extends Screen {
    public final Screen parent;
    public final BooleanConsumer consumer;
    public final WebPlaylist webPlaylist;
    public ConfirmDeletePlaylist(Screen parent, WebPlaylist webPlaylist, BooleanConsumer consumer) {
        super(Component.translatable("waterplayer.playlist.web.delete.title"));
        this.parent = parent;
        this.webPlaylist = webPlaylist;
        this.consumer = consumer;
    }

    @Override
    protected void init() {
        int x = (width / 2) - 102;
        int y = Math.max((168+this.font.lineHeight), (height/2) - 10);
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_YES, (s) -> {
            if(webPlaylist.delete()){
                WaterPlayer.getToast().setIcon(RECYCLE_BIN).setMessage(Component.translatable("waterplayer.playlist.web.deleted", webPlaylist.playlist.title, webPlaylist.url))
                        .setType(ToastBuilder.Type.ERROR).buildAndShow();
                consumer.accept(true);
            } else {
                WaterPlayer.getToast().setIcon(RECYCLE_BIN).setMessage(Component.translatable("waterplayer.playlist.web.not_deleted"))
                        .setType(ToastBuilder.Type.ERROR).buildAndShow();
                onClose();
            }
        })
                .setPosition(x, y).setSize(100, 20).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_NO,
                (s) -> onClose())
                .setPosition(x+104, y).setSize(100, 20).build());
    }

    //#if MC >= 12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif

        //#if MC >= 12100
        renderBlurredBackground(
                //#if MC >= 12106
                guiGraphics
                //#elseif MC >= 12102
                //$$
                //#elseif MC == 12101
                //$$f
                //#endif
        );
        //#endif
        int bottom = 0x7FA14343;
        int top = 0x7F000000;
        guiGraphics.fillGradient(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), top, bottom);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        //#if MC < 12002
        //$$     renderBackground(guiGraphics);
        //#endif
        super.render(guiGraphics, i, j, f);

        int y = 60;

        guiGraphics.blit(
                //#if MC >= 12106
                RenderPipelines.GUI_TEXTURED,
                //#elseif MC >= 12102
                //$$ RenderType::guiTextured,
                //#endif
                RECYCLE_BIN, (width/2)-25, y, 0,0, 50,50, 50, 50);
        y+=50;
        //#if MC >= 12106
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(2.0F, 2.0F);
        //#elseif MC >= 12102
        //$$ guiGraphics.pose().pushPose();
        //$$ guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        //#endif
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, y/2, 16777215);
        //#if MC >= 12106
        guiGraphics.pose().popMatrix();
        //#elseif MC >= 12102
        //$$ guiGraphics.pose().popPose();
        //#endif
        y+=25;
        guiGraphics.drawCenteredString(this.font, Component.translatable("waterplayer.playlist.web.delete.description"), this.width / 2, y, 16777215);
        y+=(this.font.lineHeight+3);
        guiGraphics.drawCenteredString(this.font, Component.translatable("waterplayer.playlist.web.delete.description.details", webPlaylist.playlist.title, webPlaylist.url), this.width / 2, y, 16777215);
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
