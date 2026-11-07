package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.SaturationEat.FoodInfo;
import brova.toshiba.mixin.DrawContextInvoker;
import brova.toshiba.mixin.HungerManagerAccessor;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;

public class SaturationEat extends Module {

    public static SaturationEat INSTANCE = new SaturationEat();

    private static final Identifier foodFullTexture = Identifier.of("minecraft", "textures/gui/sprites/hud/food_full.png");

    private static final Identifier foodHalfTexture = Identifier.of("minecraft", "textures/gui/sprites/hud/food_half.png");

    private static final Identifier fullTexture = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/full.png");

    private static final Identifier halfTexture = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/half.png");

    private static final Identifier iconsTexture = Identifier.of("toshiba", "textures/saturationeat/icons.png");

    public SaturationEat() {
        super("SaturationEat", "Показывает насыщение и эффект еды как AppleSkin", ModuleCategory.RENDER);
    }

    public void renderFoodUnderlay(DrawContext context, PlayerEntity player, int intValue, int intValueValue) {
        if (this.isEnable()) {
            float floatValue = this.calculateToshiba$getExhaustion(player.getHungerManager());
            int intValueResult = Math.round(MathHelper.clamp(floatValue / 4.0F, 0.0F, 1.0F) * 81.0F);
            if (intValueResult > 0) {
                this.updateToshiba$drawTexturedQuad(context, iconsTexture, intValueValue - intValueResult, intValue, intValueResult, 9, (81 - intValueResult) / 256.0F, 0.31640625F, 0.0703125F, 0.10546875F, this.calculateFloat(1.0F, 1.0F, 1.0F, 0.75F));
            }
        }
    }

    public void renderFoodOverlays(DrawContext context, PlayerEntity player, int intValue, int intValueValue, int intValueResult) {
        if (this.isEnable()) {
            HungerManager hungerManager = player.getHungerManager();
            float saturationLevel = hungerManager.getSaturationLevel();
            float floatValue = this.calculateInteger(intValueResult);
            this.updateDrawContext(context, saturationLevel, 0.0F, intValueValue, intValue, 1.0F, 0.25F, 1.0F);
            FoodInfo foodInfo = this.createSaturationEatFoodInfoFromMainHandStackWithOffHandStack(player);
            if (foodInfo != null) {
                FoodComponent foodComponent = foodInfo.food();
                this.updateDrawContext(context, hungerManager.getFoodLevel(), foodComponent.nutrition(), intValueValue, intValue, floatValue);
                int intValueItem = Math.min(20, hungerManager.getFoodLevel() + foodComponent.nutrition());
                float floatValueValue = Math.min(foodComponent.saturation(), intValueItem - saturationLevel);
                this.updateDrawContext(context, saturationLevel, Math.max(0.0F, floatValueValue), intValueValue, intValue, floatValue, 1.0F, 0.35F);
                float floatValueResult = this.calculateStatusEffectWithHealth(player, foodComponent);
                if (floatValueResult > 0.0F) {
                    this.updateHealthWithMaxHealth(context, player, floatValueResult, intValueValue - 182, intValue, floatValue);
                }
            }
        }
    }

    public void appendTooltip(ItemStack stack, List<Text> texts) {
        if (this.isEnable()) {
            FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
            if (foodComponent != null) {
                texts.add(Text.literal(""));
                texts.add(Text.literal("Питание: +" + foodComponent.nutrition()).formatted(Formatting.GOLD));
                texts.add(Text.literal("Насыщение: +" + this.formatTextFromFormat(foodComponent.saturation())).formatted(Formatting.YELLOW));
            }
        }
    }

    private FoodInfo createSaturationEatFoodInfoFromMainHandStackWithOffHandStack(PlayerEntity player) {
        FoodInfo foodInfo = this.createCanConsumeWithCanAlwaysEat(player.getMainHandStack(), player);
        return foodInfo != null ? foodInfo : this.createCanConsumeWithCanAlwaysEat(player.getOffHandStack(), player);
    }

    private FoodInfo createCanConsumeWithCanAlwaysEat(ItemStack stack, PlayerEntity player) {
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        return foodComponent != null && player.canConsume(foodComponent.canAlwaysEat()) ? new FoodInfo(stack, foodComponent) : null;
    }

    private void updateDrawContext(DrawContext context, int intValue, int intValueValue, int intValueResult, int intValueItem, float floatValue) {
        if (intValueValue > 0 && intValue < 20) {
            int intValueEntry = Math.min(20, intValue + intValueValue);
            int intValueCopy = intValue / 2;
            int roundedUp = MathHelper.ceil(intValueEntry / 2.0F);
            for (int index = intValueCopy; index < roundedUp; index++) {
                boolean flag = index * 2 + 1 == intValueEntry;
                this.updateToshiba$drawTexturedQuad(context, flag ? foodHalfTexture : foodFullTexture, intValueResult - index * 8 - 9, intValueItem, 9, 9, 0.0F, 1.0F, 0.0F, 1.0F, this.calculateFloat(1.0F, 0.9F, 0.25F, floatValue));
            }
        }
    }

    private void updateDrawContext(DrawContext context, float floatValue, float floatValueValue, int intValue, int intValueValue, float floatValueResult, float floatValueItem, float floatValueEntry) {
        float clampedValue = MathHelper.clamp(floatValue + floatValueValue, 0.0F, 20.0F);
        if (!(clampedValue <= 0.0F)) {
            int intValueResult = floatValueValue == 0.0F ? 0 : Math.max(0, (int) (floatValue / 2.0F));
            int roundedUp = MathHelper.ceil(clampedValue / 2.0F);
            for (int index = intValueResult; index < roundedUp; index++) {
                float floatValueCopy = clampedValue / 2.0F - index;
                int intValueItem = floatValueCopy >= 1.0F ? 27 : (floatValueCopy > 0.5F ? 18 : (floatValueCopy > 0.25F ? 9 : 0));
                this.updateToshiba$drawTexturedQuad(context, iconsTexture, intValue - index * 8 - 9, intValueValue, 9, 9, intValueItem / 256.0F, (intValueItem + 9.0F) / 256.0F, 0.0F, 0.03515625F, this.calculateFloat(floatValueItem, floatValueEntry, 0.25F, floatValueResult));
            }
        }
    }

    private void updateHealthWithMaxHealth(DrawContext context, PlayerEntity player, float floatValue, int intValue, int intValueValue, float floatValueValue) {
        int roundedUp = MathHelper.ceil(player.getHealth());
        int intValueResult = Math.min(MathHelper.ceil(player.getMaxHealth()), MathHelper.ceil(player.getHealth() + floatValue));
        if (intValueResult > roundedUp) {
            int roundedUpValue = MathHelper.ceil((player.getMaxHealth() + player.getAbsorptionAmount()) / 2.0F);
            if (roundedUpValue > 0 && roundedUpValue <= 1000) {
                int roundedUpResult = MathHelper.ceil(roundedUpValue / 10.0F);
                int intValueItem = Math.max(10 - (roundedUpResult - 2), 3);
                int intValueEntry = roundedUp / 2;
                int roundedUpItem = MathHelper.ceil(intValueResult / 2.0F);
                for (int index = intValueEntry; index < roundedUpItem; index++) {
                    int intValueCopy = MathHelper.ceil((index + 1) / 10.0F) - 1;
                    boolean flag = index * 2 + 1 == intValueResult;
                    this.updateToshiba$drawTexturedQuad(context, flag ? halfTexture : fullTexture, intValue + index % 10 * 8, intValueValue - intValueCopy * intValueItem, 9, 9, 0.0F, 1.0F, 0.0F, 1.0F, this.calculateFloat(0.35F, 1.0F, 0.35F, floatValueValue));
                }
            }
        }
    }

    private float calculateStatusEffectWithHealth(PlayerEntity player, FoodComponent foodComponent) {
        if (!(player.getHealth() >= player.getMaxHealth()) && player.getWorld().getDifficulty() != Difficulty.PEACEFUL && !player.hasStatusEffect(StatusEffects.POISON) && !player.hasStatusEffect(StatusEffects.WITHER) && !player.hasStatusEffect(StatusEffects.REGENERATION)) {
            HungerManager hungerManager = player.getHungerManager();
            if (hungerManager.getFoodLevel() >= 18) {
                return 0.0F;
            } else {
                int intValue = Math.min(20, hungerManager.getFoodLevel() + foodComponent.nutrition());
                if (intValue < 18) {
                    return 0.0F;
                } else {
                    float floatValue = Math.min((float) intValue, hungerManager.getSaturationLevel() + foodComponent.saturation());
                    float floatValueValue = this.calculateToshiba$getExhaustion(hungerManager);
                    float floatValueResult = 0.0F;
                    while (intValue >= 18) {
                        while (floatValueValue > 4.0F) {
                            floatValueValue -= 4.0F;
                            if (floatValue > 0.0F) {
                                floatValue = Math.max(0.0F, floatValue - 1.0F);
                            } else {
                                intValue--;
                            }
                        }
                        if (intValue >= 20 && Float.compare(floatValue, Float.MIN_NORMAL) > 0) {
                            float floatValueItem = Math.min(floatValue, 6.0F);
                            float floatValueEntry = Math.nextUp(4.0F) - floatValueValue;
                            int intValueValue = Math.max(1, MathHelper.ceil(floatValueEntry / floatValueItem));
                            floatValueResult += floatValueItem / 6.0F * intValueValue;
                            floatValueValue += floatValueItem * intValueValue;
                        } else if (intValue >= 18) {
                            floatValueResult++;
                            floatValueValue += 6.0F;
                        }
                    }
                    return Math.min(floatValueResult, player.getMaxHealth() - player.getHealth());
                }
            }
        } else {
            return 0.0F;
        }
    }

    private float calculateToshiba$getExhaustion(HungerManager hungerManager) {
        return ((HungerManagerAccessor) hungerManager).toshiba$getExhaustion();
    }

    private float calculateInteger(int intValue) {
        float floatValue = intValue % 32 / 32.0F;
        float floatValueValue = floatValue < 0.5F ? floatValue * 2.0F : (1.0F - floatValue) * 2.0F;
        return floatValueValue * 0.65F;
    }

    private String formatTextFromFormat(float floatValue) {
        return String.format(Locale.ROOT, "%.1f", floatValue);
    }

    private void updateToshiba$drawTexturedQuad(DrawContext context, Identifier texture, int intValue, int intValueValue, int intValueResult, int intValueItem, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValueEntry) {
        ((DrawContextInvoker) context).toshiba$drawTexturedQuad(RenderLayer::getGuiTextured, texture, intValue, intValue + intValueResult, intValueValue, intValueValue + intValueItem, floatValue, floatValueValue, floatValueResult, floatValueItem, intValueEntry);
    }

    private int calculateFloat(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        int intValue = Math.round(MathHelper.clamp(floatValueItem, 0.0F, 1.0F) * 255.0F);
        int intValueValue = Math.round(MathHelper.clamp(floatValue, 0.0F, 1.0F) * 255.0F);
        int intValueResult = Math.round(MathHelper.clamp(floatValueValue, 0.0F, 1.0F) * 255.0F);
        int intValueItem = Math.round(MathHelper.clamp(floatValueResult, 0.0F, 1.0F) * 255.0F);
        return intValue << 24 | intValueValue << 16 | intValueResult << 8 | intValueItem;
    }

    public static record FoodInfo(ItemStack stack, FoodComponent food) {
    }
}
