package brova.toshiba.mixin;

import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.configs.AutoConfigLoader;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.BlockBreakingEvent;
import brova.toshiba.client.event.impl.ClientTickEndEvent;
import brova.toshiba.client.event.impl.ClientTickEvent;
import brova.toshiba.client.event.impl.GameUpdateEvent;
import brova.toshiba.client.event.impl.ScreenEvent;
import brova.toshiba.client.gui.click.ClickGuiScreen;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.ShaderESP;
import brova.toshiba.client.utils.render.WorldFramebufferRenderer;
import brova.toshiba.client.utils.time.FpsTracker;
import brova.toshiba.client.utils.world.BaritoneController;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Unique
    private boolean toshiba$inSetScreen = false;

    @Unique
    private long lastHookTime = Util.getMeasuringTimeNano();

    @Unique
    private int accumulatedCalls = 0;

    @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
    private void toshiba$redirectHudFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        Framebuffer framebuffer = WorldFramebufferRenderer.getInstance().getHudFramebuffer();
        if (framebuffer != null) {
            cir.setReturnValue(framebuffer);
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (PanicController.isActive() && screen instanceof ClickGuiScreen) {
            ci.cancel();
        } else if (!this.toshiba$inSetScreen) {
            try {
                if (EventBus.hasListeners(ScreenEvent.class)) {
                    ScreenEvent event = new ScreenEvent(screen);
                    this.toshiba$inSetScreen = true;
                    EventBus.invoke(event);
                    this.toshiba$inSetScreen = false;
                    if (event.isCancelled()) {
                        ci.cancel();
                    } else if (event.getScreen() != screen) {
                        ci.cancel();
                        ((MinecraftClient) (Object) this).setScreen(event.getScreen());
                    }
                }
            } catch (Exception failure) {
                this.toshiba$inSetScreen = false;
                System.err.println("[MinecraftClientMixin] " + failure.getMessage());
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (EventBus.hasListeners(ClientTickEvent.class)) {
            ClientTickEvent event = new ClientTickEvent();
            EventBus.invoke(event);
        }
        FpsTracker.updateFPS();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickEnd(CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (EventBus.hasListeners(ClientTickEndEvent.class)) {
            ClientTickEndEvent event = new ClientTickEndEvent();
            EventBus.invoke(event);
        }
        BaritoneController.tick();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(boolean tick, CallbackInfo ci) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (!EventBus.hasListeners(GameUpdateEvent.class)) {
            this.lastHookTime = Util.getMeasuringTimeNano();
            this.accumulatedCalls = 0;
        } else {
            long now = Util.getMeasuringTimeNano();
            long delta = now - this.lastHookTime;
            this.accumulatedCalls += (int) (delta / 4166666L);
            this.lastHookTime = this.lastHookTime + this.accumulatedCalls * 4166666L;
            for (this.accumulatedCalls = Math.min(this.accumulatedCalls, 240); this.accumulatedCalls > 0; this.accumulatedCalls--) {
                EventBus.invoke(new GameUpdateEvent());
            }
        }
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void toshiba$hasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleRegistry.INSTANCE != null) {
            ShaderESP shaderEsp = ModuleRegistry.shaderEsp;
            if (shaderEsp != null && shaderEsp.shouldOutline(entity)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (EventBus.hasListeners(BlockBreakingEvent.class)) {
            BlockBreakingEvent event = new BlockBreakingEvent();
            event.call();
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    /** При закрытии игры сохраняем текущее состояние, чтобы в следующий раз зайти с теми же настройками. */
    @Inject(method = "stop", at = @At("HEAD"))
    private void toshiba$onStop(CallbackInfo ci) {
        try {
            AutoConfigLoader.saveCurrentState();
        } catch (Throwable failure) {
            System.err.println("[MinecraftClientMixin] " + failure.getMessage());
        }
    }
}
