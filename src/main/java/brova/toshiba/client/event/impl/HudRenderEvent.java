package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.client.gui.DrawContext;

public class HudRenderEvent extends EventBase {

    private final DrawContext context;

    private final float partialTicks;

    public DrawContext getContext() {
        return this.context;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public HudRenderEvent(DrawContext context, float partialTicks) {
        this.context = context;
        this.partialTicks = partialTicks;
    }
}
