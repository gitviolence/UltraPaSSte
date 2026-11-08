package brova.toshiba.client.gui.hud;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;

public class HudWidgetBase implements MinecraftAccess {

    public final Draggable draggable;

    private boolean unusualRectType = true;

    public HudWidgetBase(Draggable draggable) {
        this.draggable = draggable;
    }

    public boolean isUnusualRectType() {
        return this.unusualRectType;
    }

    public void setUnusualRectType(boolean unusualRectType) {
        this.unusualRectType = unusualRectType;
    }

    public void onUpdate(PlayerUpdateEvent event) {
    }

    public void onRender(HudRenderEvent event) {
    }
}
