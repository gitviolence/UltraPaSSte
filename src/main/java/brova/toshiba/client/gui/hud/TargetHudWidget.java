package brova.toshiba.client.gui.hud;

import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.TargetHudWidget.HealthAnimation;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.combat.AimAssist;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.modules.impl.combat.MaceAura;
import brova.toshiba.client.modules.impl.render.Hud;
import brova.toshiba.client.utils.player.HealthUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.ColorUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class TargetHudWidget extends HudWidgetBase {

    private final Animation visibilityAnimation = new Animation(0.0F, 9.0F, PowerEasing.QUAD_OUT);

    private final Animation healthRatioAnimation = new Animation(1.0F, 9.2F, PowerEasing.QUAD_OUT);

    private final Animation healthTrailAnimation = new Animation(1.0F, 7.4F, PowerEasing.QUAD_OUT);

    private final Animation maximumHealthAnimation = new Animation(20.0F, 7.0F, PowerEasing.QUAD_OUT);

    private final Animation absorptionAnimation = new Animation(0.0F, 7.0F, PowerEasing.QUAD_OUT);

    private final Animation absorptionRatioAnimation = new Animation(0.0F, 9.2F, PowerEasing.QUAD_OUT);

    private final Animation absorptionTrailAnimation = new Animation(0.0F, 7.4F, PowerEasing.QUAD_OUT);

    private final Animation absorptionVisibilityAnimation = new Animation(0.0F, 9.0F, PowerEasing.QUAD_OUT);

    private final List<HealthAnimation> targetHudWidgetHealthAnimationEntries = new ObjectArrayList<>();

    private LivingEntity targetForDefaultStyle;

    private float cachedScalar = 20.0F;

    private boolean healthBarStyleEnabled = false;

    private long valueTimestamp = System.nanoTime();

    private LivingEntity targetForRenderNextFloatWithColor;

    private int cachedCountForRenderNextFloatWithColor = 0;

    private int accentColor = ColorMathUtil.rgba(124, 91, 242, 255);

    private int secondaryAccentColor = ColorMathUtil.rgba(93, 67, 175, 255);

    private Framebuffer framebuffer;

    private int cachedPrimaryColor = -1;

    private int cachedSecondaryColor = -1;

    private final ItemStack[] equipmentSlots = new ItemStack[6];

    private final ItemStack[] armorSlots = new ItemStack[4];

    private TextRenderer manropeFont12;

    private TextRenderer manropeFont13;

    private TextRenderer manropeFont14;

    private TextRenderer robotoFlexSemiboldFont15;

    private TextRenderer manropeFont26;

    private TextRenderer rainFont23;

    private int lastHealthTenths = Integer.MIN_VALUE;

    private int lastAbsorptionTenths = Integer.MIN_VALUE;

    private int unusedHealthCache = Integer.MIN_VALUE;

    private int unusedAbsorptionCache = Integer.MIN_VALUE;

    private String healthLabel = "";

    private String absorptionLabel = "";

    private String cachedText = "";

    public TargetHudWidget(Draggable draggable) {
        super(draggable);
    }

    private TextRenderer createTextRendererFromInteger(int intValue) {
        if (intValue == 12) {
            if (this.manropeFont12 == null) {
                this.manropeFont12 = FontManager.getFont("manrope", 12);
            }
            return this.manropeFont12;
        } else if (intValue == 13) {
            if (this.manropeFont13 == null) {
                this.manropeFont13 = FontManager.getFont("manrope", 13);
            }
            return this.manropeFont13;
        } else if (intValue == 14) {
            if (this.manropeFont14 == null) {
                this.manropeFont14 = FontManager.getFont("manrope", 14);
            }
            return this.manropeFont14;
        } else if (intValue == 15) {
            if (this.robotoFlexSemiboldFont15 == null) {
                this.robotoFlexSemiboldFont15 = FontManager.getFont("roboto_flex_semibold", 15);
            }
            return this.robotoFlexSemiboldFont15;
        } else if (intValue == 26) {
            if (this.manropeFont26 == null) {
                this.manropeFont26 = FontManager.getFont("manrope", 26);
            }
            return this.manropeFont26;
        } else {
            return FontManager.getFont("manrope", intValue);
        }
    }

    private TextRenderer createTextRenderer() {
        if (this.rainFont23 == null) {
            this.rainFont23 = FontManager.getFont("rain", 23);
        }
        return this.rainFont23;
    }

    public boolean isHealthBarStyleEnabled() {
        return this.healthBarStyleEnabled;
    }

    public void setHealthBarStyleEnabled(boolean healthBarStyleEnabled) {
        this.healthBarStyleEnabled = healthBarStyleEnabled;
    }

    private int interpolateRgb(int intValue, int intValueValue, float floatValue) {
        floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        int intValueCopy = intValue >> 24 & 0xFF;
        int intValue2 = intValueValue >> 16 & 0xFF;
        int intValue3 = intValueValue >> 8 & 0xFF;
        int intValue4 = intValueValue & 0xFF;
        int intValue5 = intValueValue >> 24 & 0xFF;
        int intValue6 = (int) (intValueResult + (intValue2 - intValueResult) * floatValue);
        int intValue7 = (int) (intValueItem + (intValue3 - intValueItem) * floatValue);
        int intValue8 = (int) (intValueEntry + (intValue4 - intValueEntry) * floatValue);
        int intValue9 = (int) (intValueCopy + (intValue5 - intValueCopy) * floatValue);
        return intValue9 << 24 | intValue6 << 16 | intValue7 << 8 | intValue8;
    }

    private void updateFramebufferWidthWithFramebufferHeight() {
        if (mc != null && mc.getWindow() != null) {
            int cachedPrimaryColor = mc.getWindow().getFramebufferWidth();
            int cachedSecondaryColor = mc.getWindow().getFramebufferHeight();
            if (this.framebuffer == null || this.cachedPrimaryColor != cachedPrimaryColor || this.cachedSecondaryColor != cachedSecondaryColor) {
                if (this.framebuffer != null) {
                    this.framebuffer.delete();
                }
                this.framebuffer = new SimpleFramebuffer(cachedPrimaryColor, cachedSecondaryColor, true);
                this.cachedPrimaryColor = cachedPrimaryColor;
                this.cachedSecondaryColor = cachedSecondaryColor;
            }
        }
    }

    private int calculateArmorItemsWithMainHandStack(LivingEntity entity) {
        int intValue = 0;
        for (ItemStack stack : entity.getArmorItems()) {
            if (intValue < this.armorSlots.length) {
                this.armorSlots[intValue++] = stack;
            }
        }
        int intValueValue = 0;
        for (int index = intValue - 1; index >= 0; index--) {
            this.equipmentSlots[intValueValue++] = this.armorSlots[index];
            this.armorSlots[index] = ItemStack.EMPTY;
        }
        ItemStack mainHandStack = entity.getMainHandStack();
        if (!mainHandStack.isEmpty()) {
            this.equipmentSlots[intValueValue++] = mainHandStack;
        }
        ItemStack offHandStack = entity.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            this.equipmentSlots[intValueValue++] = offHandStack;
        }
        return intValueValue;
    }

    private boolean isClearColorWithBeginWrite(boolean flag) {
        if (!flag) {
            return false;
        } else {
            this.updateFramebufferWidthWithFramebufferHeight();
            if (this.framebuffer == null) {
                return false;
            } else {
                this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                this.framebuffer.clear();
                this.framebuffer.beginWrite(false);
                return true;
            }
        }
    }

    private void updateTranslateWithScale(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy) {
        float floatValue2 = floatValue + floatValueResult * 0.5F;
        float floatValue3 = floatValueValue + floatValueItem * 0.5F;
        matrices.translate(floatValue2, floatValue3, 0.0F);
        matrices.scale(floatValueEntry, floatValueCopy, 1.0F);
        matrices.translate(-floatValue2, -floatValue3, 0.0F);
    }

    private void renderNextFloatWithColor(MatrixStack matrices, LivingEntity targetForRenderNextFloatWithColor, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue) {
        if (targetForRenderNextFloatWithColor != null && !(floatValueItem <= 0.02F)) {
            long valueTimestamp = System.nanoTime();
            float age = MathHelper.clamp((float) (valueTimestamp - this.valueTimestamp) / 1.0E9F * 60.0F, 0.2F, 3.0F);
            this.valueTimestamp = valueTimestamp;
            if (this.targetForRenderNextFloatWithColor != targetForRenderNextFloatWithColor) {
                this.targetHudWidgetHealthAnimationEntries.clear();
                this.targetForRenderNextFloatWithColor = targetForRenderNextFloatWithColor;
                this.cachedCountForRenderNextFloatWithColor = Math.max(0, targetForRenderNextFloatWithColor.hurtTime);
            }
            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            float floatValueEntry = floatValue + floatValueResult * 0.5F;
            float floatValueCopy = floatValueValue + floatValueResult * 0.5F;
            int cachedCountForRenderNextFloatWithColor = Math.max(0, targetForRenderNextFloatWithColor.hurtTime);
            boolean flag = cachedCountForRenderNextFloatWithColor > 0 && (cachedCountForRenderNextFloatWithColor > this.cachedCountForRenderNextFloatWithColor || cachedCountForRenderNextFloatWithColor % 3 == 0);
            this.cachedCountForRenderNextFloatWithColor = cachedCountForRenderNextFloatWithColor;
            if (flag) {
                int intValueValue = 1 + threadLocalRandom.nextInt(2);
                for (int index = 0; index < intValueValue && this.targetHudWidgetHealthAnimationEntries.size() < 14; index++) {
                    float floatValue2 = (float) (threadLocalRandom.nextDouble() * Math.PI * 2.0);
                    float floatValue3 = threadLocalRandom.nextFloat() * floatValueResult * 0.24F;
                    float floatValue4 = (float) (threadLocalRandom.nextDouble() * Math.PI * 2.0);
                    float floatValue5 = 0.58F + threadLocalRandom.nextFloat() * 0.9F;
                    HealthAnimation healthAnimation = new HealthAnimation();
                    healthAnimation.positionX = floatValueEntry + MathHelper.cos(floatValue2) * floatValue3;
                    healthAnimation.positionY = floatValueCopy + MathHelper.sin(floatValue2) * floatValue3;
                    healthAnimation.velocityX = MathHelper.cos(floatValue4) * floatValue5 + (healthAnimation.positionX - floatValueEntry) * 0.025F;
                    healthAnimation.velocityY = MathHelper.sin(floatValue4) * floatValue5 + (healthAnimation.positionY - floatValueCopy) * 0.025F;
                    healthAnimation.positionZ = 3.8F + threadLocalRandom.nextFloat() * 1.4F;
                    healthAnimation.age = 0.0F;
                    healthAnimation.lifetime = 74.0F + threadLocalRandom.nextFloat() * 42.0F;
                    this.targetHudWidgetHealthAnimationEntries.add(healthAnimation);
                }
            }
            if (!this.targetHudWidgetHealthAnimationEntries.isEmpty() && floatValueItem > 0.02F) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                boolean flagValue = false;
                for (int indexValue = this.targetHudWidgetHealthAnimationEntries.size() - 1; indexValue >= 0; indexValue--) {
                    HealthAnimation healthAnimationValue = this.targetHudWidgetHealthAnimationEntries.get(indexValue);
                    healthAnimationValue.age += age;
                    if (healthAnimationValue.age >= healthAnimationValue.lifetime) {
                        this.targetHudWidgetHealthAnimationEntries.remove(indexValue);
                    } else {
                        healthAnimationValue.positionX = healthAnimationValue.positionX + healthAnimationValue.velocityX * age;
                        healthAnimationValue.positionY = healthAnimationValue.positionY + healthAnimationValue.velocityY * age;
                        healthAnimationValue.velocityX = healthAnimationValue.velocityX * (float) Math.pow(0.975F, age);
                        healthAnimationValue.velocityY = healthAnimationValue.velocityY * (float) Math.pow(0.975F, age);
                        healthAnimationValue.velocityY += 0.0012F * age;
                        float floatValue6 = 1.0F - healthAnimationValue.age / healthAnimationValue.lifetime;
                        float floatValue7 = floatValue6 * floatValue6 * (3.0F - 2.0F * floatValue6);
                        float floatValue8 = floatValueItem * floatValue7;
                        if (!(floatValue8 <= 0.02F)) {
                            float floatValue9 = healthAnimationValue.positionX - healthAnimationValue.positionZ * 0.5F;
                            float floatValue10 = healthAnimationValue.positionY - healthAnimationValue.positionZ * 0.5F;
                            int intValueResult = ColorMathUtil.applyAlpha(intValue, floatValue8 * 0.58F);
                            int intValueItem = intValueResult >> 16 & 0xFF;
                            int intValueEntry = intValueResult >> 8 & 0xFF;
                            int intValueCopy = intValueResult & 0xFF;
                            int intValue2 = intValueResult >> 24 & 0xFF;
                            buffer.vertex(positionMatrix, floatValue9, floatValue10 + healthAnimationValue.positionZ, 0.0F).color(intValueItem, intValueEntry, intValueCopy, intValue2);
                            buffer.vertex(positionMatrix, floatValue9 + healthAnimationValue.positionZ, floatValue10 + healthAnimationValue.positionZ, 0.0F).color(intValueItem, intValueEntry, intValueCopy, intValue2);
                            buffer.vertex(positionMatrix, floatValue9 + healthAnimationValue.positionZ, floatValue10, 0.0F).color(intValueItem, intValueEntry, intValueCopy, intValue2);
                            buffer.vertex(positionMatrix, floatValue9, floatValue10, 0.0F).color(intValueItem, intValueEntry, intValueCopy, intValue2);
                            flagValue = true;
                        }
                    }
                }
                if (flagValue) {
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                }
                RenderSystem.disableBlend();
            }
        } else {
            this.targetHudWidgetHealthAnimationEntries.clear();
            this.targetForRenderNextFloatWithColor = targetForRenderNextFloatWithColor;
            this.cachedCountForRenderNextFloatWithColor = 0;
        }
    }

    private void onHudRender(HudRenderEvent event, MatrixStack matrices, ItemStack stack, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        if (!stack.isEmpty()) {
            matrices.push();
            matrices.translate(floatValue + 4.0F, floatValueValue + 4.0F, 0.0F);
            matrices.scale(floatValueResult, floatValueResult, 1.0F);
            matrices.translate(-4.0F, -4.0F, 0.0F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, floatValueItem);
            event.getContext().drawItem(stack, 0, 0);
            if (stack.isDamageable()) {
                event.getContext().drawStackOverlay(mc.textRenderer, stack, 0, 0);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            matrices.pop();
        }
    }

    @Override
    public void onRender(HudRenderEvent event) {
        {
            this.DefaultStyle(event);
            super.onRender(event);
        }
    }

    public void DefaultStyle(HudRenderEvent event) {
        if (mc.player == null) {
            this.targetHudWidgetHealthAnimationEntries.clear();
            this.cachedCountForRenderNextFloatWithColor = 0;
            this.draggable.setWidth(0.0F);
            this.draggable.setHeight(0.0F);
        } else {
            AttackAura aura = ModuleRegistry.aura;
            MaceAura maceAura = ModuleRegistry.maceAura;
            AimAssist aimAssist = ModuleRegistry.aimAssist;
            boolean flag = mc.currentScreen instanceof ChatScreen;
            LivingEntity entity = aura != null && aura.isEnable() ? aura.getTarget() : null;
            LivingEntity entityValue = maceAura != null && maceAura.isEnable() ? maceAura.getTarget() : null;
            LivingEntity entityResult = aimAssist != null && aimAssist.isEnable() ? aimAssist.getTarget() : null;
            LivingEntity entityItem = null;
            if (Hud.INSTANCE.showOnHover.isState()) {
                if (mc.targetedEntity instanceof LivingEntity entity2 && entity2 != mc.player && entity2.isAlive()) {
                    entityItem = entity2;
                } else if (mc.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity entity3 && entity3 != mc.player && entity3.isAlive()) {
                    entityItem = entity3;
                }
            }
            LivingEntity entityEntry = entity != null ? entity : (entityValue != null ? entityValue : (entityResult != null ? entityResult : entityItem));
            boolean flagValue = flag || entityEntry != null;
            this.visibilityAnimation.setSpeed(5.0F);
            this.visibilityAnimation.update(flagValue ? 1.0F : 0.0F);
            float clampedValue = MathHelper.clamp(this.visibilityAnimation.getValue(), 0.0F, 1.0F);
            if (flagValue) {
                this.targetForDefaultStyle = (flag ? mc.player : entityEntry);
            }
            LivingEntity entityCopy = flagValue ? (flag ? mc.player : entityEntry) : this.targetForDefaultStyle;
            if (entityCopy != null && !(clampedValue <= 0.01F)) {
                float absorptionAmount = entityCopy.getAbsorptionAmount();
                boolean flagResult = absorptionAmount > 0.0F;
                this.absorptionVisibilityAnimation.setSpeed(flagResult ? 9.0F : 5.0F);
                this.absorptionVisibilityAnimation.update(flagResult ? 1.0F : 0.0F);
                float clampedValueValue = MathHelper.clamp(this.absorptionVisibilityAnimation.getValue(), 0.0F, 1.0F);
                float x = this.draggable.getX();
                float y = this.draggable.getY();
                int accentColor = ColorMathUtil.getThemeColor();
                int secondaryAccentColor = ColorMathUtil.darken(accentColor, 0.4F);
                if (flagValue) {
                    this.accentColor = accentColor;
                    this.secondaryAccentColor = secondaryAccentColor;
                }
                if (!flagValue) {
                }
                if (!flagValue) {
                }
                float floatValue = Math.max(1.0F, entityCopy.getMaxHealth());
                float floatValueValue = Math.max(1.0F, this.cachedScalar);
                float health = HealthUtil.getHealth(entityCopy);
                this.maximumHealthAnimation.update(health);
                this.absorptionAnimation.update(absorptionAmount);
                float clampedValueResult = MathHelper.clamp(this.maximumHealthAnimation.getValue(), 0.0F, floatValue);
                float clampedValueItem = MathHelper.clamp(this.absorptionAnimation.getValue(), 0.0F, floatValueValue);
                float clampedValueEntry = MathHelper.clamp(health / floatValue, 0.0F, 1.0F);
                this.healthRatioAnimation.update(clampedValueEntry);
                float clampedValueCopy = MathHelper.clamp(this.healthRatioAnimation.getValue(), 0.0F, 1.0F);
                if (clampedValueCopy > this.healthTrailAnimation.getValue()) {
                    this.healthTrailAnimation.setValue(MathHelper.lerp(0.78F, this.healthTrailAnimation.getValue(), clampedValueCopy));
                } else {
                    this.healthTrailAnimation.update(clampedValueCopy);
                }
                float clampedValue2 = MathHelper.clamp(this.healthTrailAnimation.getValue(), 0.0F, 1.0F);
                boolean flagItem = !flagValue;
                if (flagItem) {
                }
                float clampedValue3 = MathHelper.clamp(absorptionAmount / floatValueValue, 0.0F, 1.0F);
                this.absorptionRatioAnimation.update(clampedValue3);
                float clampedValue4 = MathHelper.clamp(this.absorptionRatioAnimation.getValue(), 0.0F, 1.0F);
                if (clampedValue4 > this.absorptionTrailAnimation.getValue()) {
                    this.absorptionTrailAnimation.setValue(MathHelper.lerp(0.78F, this.absorptionTrailAnimation.getValue(), clampedValue4));
                } else {
                    this.absorptionTrailAnimation.update(clampedValue4);
                }
                float clampedValue5 = MathHelper.clamp(this.absorptionTrailAnimation.getValue(), 0.0F, 1.0F);
                if (flagItem || !flagResult) {
                    clampedValue5 = clampedValue4;
                }
                String text = entityCopy.getName() != null ? entityCopy.getName().getString() : "Unknown";
                if (text == null || text.isEmpty()) {
                    text = "Unknown";
                }
                int lastHealthTenths = Math.round(clampedValueResult * 10.0F);
                if (lastHealthTenths != this.lastHealthTenths) {
                    this.lastHealthTenths = lastHealthTenths;
                    this.healthLabel = "HP: " + String.format("%.1f", lastHealthTenths / 10.0F);
                }
                int lastAbsorptionTenths = Math.round(clampedValueItem * 10.0F);
                if (lastAbsorptionTenths != this.lastAbsorptionTenths) {
                    this.lastAbsorptionTenths = lastAbsorptionTenths;
                    this.absorptionLabel = " (" + String.format("%.1f", lastAbsorptionTenths / 10.0F) + ")";
                }
                float floatValueResult = 163.0F;
                float floatValueItem = 53.0F;
                float floatValueEntry = 14.0F;
                float scaleFactor = (float) mc.getWindow().getScaleFactor();
                float floatValueCopy = 1.4F / Math.max(1.0F, scaleFactor);
                float floatValue2 = 53.0F;
                float floatValue3 = 35.0F;
                float floatValue4 = x + (floatValue2 - floatValue3) - 9.3F;
                float floatValue5 = y + (floatValueItem - floatValue3) / 2.0F;
                float floatValue6 = x + floatValue2;
                float floatValue7 = floatValue6 + 10.0F;
                float floatValue8 = y + 9.5F;
                float floatValue9 = floatValue6 + 9.0F;
                float floatValue10 = y + 25.0F;
                float floatValue11 = 16.3F;
                float floatValue12 = floatValue6 + 9.0F;
                float floatValue13 = y + 42.0F;
                float floatValue14 = 90.0F;
                float floatValue15 = 3.0F;
                int intValue = Math.max(0, Math.round(clampedValueResult + clampedValueItem));
                String textValue = intValue + "hp";
                MatrixStack matrices = event.getContext().getMatrices();
                float floatValue16 = clampedValue;
                int intValueValue = (int) (255.0F * clampedValue);
                int intValueResult = BackgroundSettings.backgroundColor(18, 18, 18, clampedValue);
                int intValueItem = ColorMathUtil.applyAlpha(ColorMathUtil.rgba(0, 0, 0, 80), clampedValue);
                int intValueEntry = BackgroundSettings.overlayColor(0, 0, 0, 38, clampedValue);
                int intValueCopy = ColorUtil.rgba(255, 255, 255, intValueValue);
                int intValue2 = ColorUtil.rgba(255, 255, 255, (int) (35.0F * clampedValue));
                int intValue3 = ColorMathUtil.applyAlpha(ColorMathUtil.getThemeStaticColor(), clampedValue);
                int intValue4 = ColorMathUtil.applyAlpha(ColorMathUtil.darken(ColorMathUtil.getThemeStaticColor(), 0.55F), clampedValue);
                matrices.push();
                this.updateTranslateWithScale(matrices, x, y, floatValueResult, floatValueItem, floatValueCopy, floatValueCopy);
                if (flagValue) {
                    Renderer2D.drawHudShadow(matrices, x, y, floatValueResult, floatValueItem, floatValueEntry, 2.5F, intValueItem);
                    Renderer2D.drawHudBlur(matrices, x + 0.5F, y + 0.5F, floatValueResult - 1.0F, floatValueItem - 1.0F, floatValueEntry - 0.5F, clampedValue);
                }
                Renderer2D.drawRoundedRect(matrices, x, y, floatValueResult, floatValueItem, floatValueEntry, intValueResult);
                Renderer2D.drawRoundedRect(matrices, x, y, floatValue2, floatValueItem, floatValueEntry, floatValueEntry, 0.0F, 0.0F, intValueEntry);
                if (flagValue) {
                    Renderer2D.drawHudShadow(matrices, floatValue4, floatValue5, floatValue3, floatValue3, 4.0F, 2.0F, ColorMathUtil.applyAlpha(ColorMathUtil.rgba(0, 0, 0, 72), clampedValue));
                }
                if (entityCopy instanceof PlayerEntity player) {
                    Renderer2D.drawPlayerHead(matrices, player, floatValue4, floatValue5, floatValue3, 4.0F, clampedValue, MathHelper.clamp(entityCopy.hurtTime / 10.0F, 0.0F, 1.0F));
                } else {
                    this.createTextRendererFromInteger(26).drawCenteredString(matrices, "?", floatValue4 + floatValue3 / 2.0F, floatValue5 + floatValue3 / 1.5F - this.createTextRendererFromInteger(26).getHeight() / 2.0F, ColorUtil.rgba(220, 220, 220, intValueValue));
                }
                TextRenderer textRenderer = this.createTextRendererFromInteger(21);
                float stringWidth = textRenderer.getStringWidth(textValue);
                float floatValue17 = x + floatValueResult - 10.0F - stringWidth;
                float floatValue18 = Math.max(8.0F, floatValue17 - floatValue7 - 8.0F);
                textRenderer.drawStringWithFade(matrices, text, floatValue7, floatValue8, floatValue18, intValueCopy);
                float stringWidthValue = textRenderer.getStringWidth(String.valueOf(intValue));
                textRenderer.drawString(matrices, String.valueOf(intValue), floatValue17, floatValue8, intValueCopy);
                textRenderer.drawGradientStringHorizontal(matrices, "hp", floatValue17 + stringWidthValue, floatValue8, intValue3, intValue4);
                ItemStack[] stacks = new ItemStack[] { entityCopy.getMainHandStack(), entityCopy.getOffHandStack(), entityCopy.getEquippedStack(EquipmentSlot.HEAD), entityCopy.getEquippedStack(EquipmentSlot.CHEST), entityCopy.getEquippedStack(EquipmentSlot.LEGS), entityCopy.getEquippedStack(EquipmentSlot.FEET) };
                for (int index = 0; index < stacks.length; index++) {
                    ItemStack stack = stacks[index];
                    float floatValue19 = floatValue9 + index * floatValue11;
                    if (stack.isEmpty()) {
                        this.createTextRenderer().drawCenteredString(matrices, "i", floatValue19 + 4.5F, floatValue10 + 2.0F, intValue2);
                    } else if (flagValue) {
                        this.onHudRender(event, matrices, stack, floatValue19 - 2.3F, floatValue10 - 2.3F, 0.8F, floatValue16);
                    }
                }
                Renderer2D.drawRoundedRect(matrices, floatValue12, floatValue13 - 2.5F, floatValue14, floatValue15 + 1.5F, 1.5F, BackgroundSettings.overlayColor(255, 255, 255, 8, floatValue16));
                float floatValue20 = floatValue14 * clampedValueCopy;
                if (floatValue20 > 0.5F) {
                    Renderer2D.drawGradientRect(matrices, floatValue12, floatValue13 - 2.5F, floatValue20, floatValue15 + 1.5F, 1.5F, intValue3, intValue4, intValue3, intValue4);
                }
                if (clampedValueValue > 0.01F) {
                    float floatValue21 = floatValue14 * clampedValue5;
                    if (floatValue21 > 0.5F) {
                        int intValue5 = ColorMathUtil.applyAlpha(ColorMathUtil.rgba(112, 82, 18, 255), floatValue16 * clampedValueValue * 0.8F);
                        Renderer2D.drawRoundedRect(matrices, floatValue12, floatValue13 - 2.5F, floatValue21, floatValue15 + 1.5F, 1.5F, intValue5);
                    }
                    float floatValue22 = floatValue14 * clampedValue4;
                    if (floatValue22 > 0.5F) {
                        int intValue6 = ColorMathUtil.applyAlpha(ColorMathUtil.rgba(151, 108, 20, 255), floatValue16 * clampedValueValue);
                        int intValue7 = ColorMathUtil.applyAlpha(ColorMathUtil.rgba(218, 169, 48, 255), floatValue16 * clampedValueValue);
                        Renderer2D.drawGradientRect(matrices, floatValue12, floatValue13 - 2.5F, floatValue22, floatValue15 + 1.5F, 1.5F, intValue6, intValue7, intValue6, intValue7);
                    }
                }
                matrices.pop();
                this.draggable.setWidth(floatValueResult * floatValueCopy);
                this.draggable.setHeight(floatValueItem * floatValueCopy);
            } else {
                this.targetHudWidgetHealthAnimationEntries.clear();
                this.cachedCountForRenderNextFloatWithColor = 0;
                this.draggable.setWidth(0.0F);
                this.draggable.setHeight(0.0F);
                this.absorptionVisibilityAnimation.setValue(0.0F);
                this.absorptionAnimation.setValue(0.0F);
                this.absorptionRatioAnimation.setValue(0.0F);
                this.absorptionTrailAnimation.setValue(0.0F);
            }
        }
    }

    public final static class HealthAnimation {

        public float positionX;

        public float positionY;

        public float velocityX;

        public float velocityY;

        public float positionZ;

        public float age;

        public float lifetime;

        HealthAnimation() {
        }
    }
}
