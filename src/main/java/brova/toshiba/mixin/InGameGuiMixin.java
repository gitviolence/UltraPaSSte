package brova.toshiba.mixin;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.utils.render.BlurFramebufferManager;
import brova.toshiba.client.utils.render.WorldFramebufferRenderer;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameGuiMixin implements MinecraftAccess {

    private static final int DOMAIN_COLOR = 15557921;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        BlurFramebufferManager.getInstance().beginFrame();
        BlurFramebufferManager.getHudInstance().beginFrame();
        if (EventBus.hasListeners(HudRenderEvent.class)) {
            new HudRenderEvent(context, tickCounter.getTickDelta(true)).call();
        }
    }

    @Inject(method = "renderMainHud", at = @At("HEAD"))
    private void toshiba$captureHudColorSaturation(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        WorldFramebufferRenderer.getInstance().beginHud(context);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void toshiba$renderHudColorSaturation(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        WorldFramebufferRenderer.getInstance().endHud(context);
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void toshiba$hideVanillaPotionEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null && ModuleRegistry.interfaceModule != null) {
            if (ModuleRegistry.interfaceModule.isEnable() && ModuleRegistry.interfaceModule.hudModules.is("Зелья")) {
                ci.cancel();
            }
        }
    }

    @Shadow
    private PlayerEntity getCameraPlayer() {
        return null;
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void toshiba$renderPatchedScoreboard(DrawContext drawContext, ScoreboardObjective objective, CallbackInfo ci) {
        if (ModuleRegistry.INSTANCE != null && ModuleRegistry.noRender != null && ModuleRegistry.noRender.isEnabled("СкорБорд")) {
            ci.cancel();
        } else if (this.toshiba$shouldPatchScoreboard()) {
            Scoreboard scoreboard = objective.getScoreboard();
            NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
            List<brova.toshiba.client.data.ScoreboardEntry> lines = scoreboard.getScoreboardEntries(objective).stream().filter(entry -> !entry.hidden()).sorted(Comparator.comparing(net.minecraft.scoreboard.ScoreboardEntry::value).reversed().thenComparing(net.minecraft.scoreboard.ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER)).limit(15L).map(entry -> {
                Team team = scoreboard.getScoreHolderTeam(entry.owner());
                Text name = this.toshiba$patchText(Team.decorateName(team, entry.name()));
                Text score = entry.formatted(numberFormat);
                int scoreWidth = this.client.textRenderer.getWidth(score);
                return new brova.toshiba.client.data.ScoreboardEntry(name, score, scoreWidth);
            }).toList();
            Text title = this.toshiba$patchText(objective.getDisplayName());
            int titleWidth = this.client.textRenderer.getWidth(title);
            int maxWidth = titleWidth;
            int separatorWidth = this.client.textRenderer.getWidth(": ");
            for (brova.toshiba.client.data.ScoreboardEntry line : lines) {
                maxWidth = Math.max(maxWidth, this.client.textRenderer.getWidth(line.name) + (line.scoreWidth > 0 ? separatorWidth + line.scoreWidth : 0));
            }
            int lineCount = lines.size();
            int totalHeight = lineCount * 9;
            int bottom = drawContext.getScaledWindowHeight() / 2 + totalHeight / 3;
            int left = drawContext.getScaledWindowWidth() - maxWidth - 3;
            int right = drawContext.getScaledWindowWidth() - 1;
            int bodyColor = this.client.options.getTextBackgroundColor(0.3F);
            int headerColor = this.client.options.getTextBackgroundColor(0.4F);
            int top = bottom - lineCount * 9;
            drawContext.fill(left - 2, top - 10, right, top - 1, headerColor);
            drawContext.fill(left - 2, top - 1, right, bottom, bodyColor);
            drawContext.drawText(this.client.textRenderer, title, left + maxWidth / 2 - titleWidth / 2, top - 9, -1, false);
            for (int index = 0; index < lineCount; index++) {
                brova.toshiba.client.data.ScoreboardEntry line = lines.get(index);
                int y = bottom - (lineCount - index) * 9;
                drawContext.drawText(this.client.textRenderer, line.name, left, y, -1, false);
                drawContext.drawText(this.client.textRenderer, line.score, right - line.scoreWidth, y, -1, false);
            }
            ci.cancel();
        }
    }

    private boolean toshiba$shouldPatchScoreboard() {
        return ModuleRegistry.INSTANCE != null && ModuleRegistry.nameProtect != null && ModuleRegistry.nameProtect.isEnable();
    }

    private Text toshiba$patchText(Text text) {
        NameProtect nameProtect = ModuleRegistry.nameProtect;
        Text patched = nameProtect.patchText(text);
        String patchedString = patched.getString();
        return (patchedString.equals(patched.getString()) ? patched : Text.literal(patchedString).setStyle(patched.getStyle().withColor(TextColor.fromRgb(15557921))));
    }
}
