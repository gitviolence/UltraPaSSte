package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ShulkerPreview extends Module {

    public static ShulkerPreview INSTANCE = new ShulkerPreview();

    private Field focusedSlotField;

    private Field tooltipLinesField;

    private static ShulkerPreview instance;

    public ShulkerPreview() {
        super("ShulkerPreview", "Показывает содержимое шалкера при наводке на шалкер + CTRL в инвентаре", ModuleCategory.RENDER);
        this.updateEqualsWithDeclaredFields();
        instance = this;
    }

    public static ShulkerPreview getInstance() {
        return instance;
    }

    private void updateEqualsWithDeclaredFields() {
        try {
            for (Field focusedSlotField : HandledScreen.class.getDeclaredFields()) {
                if (focusedSlotField.getType() == int.class) {
                    focusedSlotField.setAccessible(true);
                    String name = focusedSlotField.getName();
                    if (name.equals("x") || name.equals("x") || name.contains("Left") || name.contains("guiLeft")) {
                        this.focusedSlotField = focusedSlotField;
                    } else if (name.equals("y") || name.equals("y") || name.contains("Top") || name.contains("guiTop")) {
                        this.tooltipLinesField = focusedSlotField;
                    }
                }
            }
        } catch (Exception failure) {
        }
    }

    private int calculateIntWithScaledWidth(HandledScreen<?> handledScreen) {
        try {
            if (this.focusedSlotField != null) {
                return this.focusedSlotField.getInt(handledScreen);
            }
        } catch (Exception failure) {
        }
        return (mc.getWindow().getScaledWidth() - 176) / 2;
    }

    private int calculateIntWithScaledHeight(HandledScreen<?> handledScreen) {
        try {
            if (this.tooltipLinesField != null) {
                return this.tooltipLinesField.getInt(handledScreen);
            }
        } catch (Exception failure) {
        }
        return (mc.getWindow().getScaledHeight() - 166) / 2;
    }

    public void renderFromMixin(DrawContext context, int intValue, int intValueValue) {
        if (this.isEnable() && mc != null && mc.player != null && mc.currentScreen != null && mc.currentScreen instanceof HandledScreen<?> handledScreen) {
            long handle = mc.getWindow().getHandle();
            boolean flag = GLFW.glfwGetKey(handle, 341) == 1;
            if (flag) {
                Slot slot = this.createSlotFromScreenHandlerWithScaledWidth(handledScreen);
                if (slot != null) {
                    ItemStack stack = slot.getStack();
                    if (this.isItem(stack)) {
                        ContainerComponent containerComponent = stack.get(DataComponentTypes.CONTAINER);
                        if (containerComponent != null) {
                            this.updateFillWithScaledWindowWidth(context, stack, containerComponent, intValue, intValueValue);
                        }
                    }
                }
            }
        }
    }

    private Slot createSlotFromScreenHandlerWithScaledWidth(HandledScreen<?> handledScreen) {
        try {
            ScreenHandler screenHandler = handledScreen.getScreenHandler();
            if (screenHandler == null || screenHandler.slots == null) {
                return null;
            }
            double doubleValue = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
            double doubleValueValue = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
            int intValue = this.calculateIntWithScaledWidth(handledScreen);
            int intValueValue = this.calculateIntWithScaledHeight(handledScreen);
            for (Slot slot : screenHandler.slots) {
                int intValueResult = intValue + slot.x;
                int intValueItem = intValueValue + slot.y;
                if (doubleValue >= intValueResult && doubleValue < intValueResult + 16 && doubleValueValue >= intValueItem && doubleValueValue < intValueItem + 16) {
                    return slot;
                }
            }
        } catch (Exception failure) {
        }
        return null;
    }

    private boolean isItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() ? stack.getItem() == Items.SHULKER_BOX || stack.getItem() == Items.WHITE_SHULKER_BOX || stack.getItem() == Items.ORANGE_SHULKER_BOX || stack.getItem() == Items.MAGENTA_SHULKER_BOX || stack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX || stack.getItem() == Items.YELLOW_SHULKER_BOX || stack.getItem() == Items.LIME_SHULKER_BOX || stack.getItem() == Items.PINK_SHULKER_BOX || stack.getItem() == Items.GRAY_SHULKER_BOX || stack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX || stack.getItem() == Items.CYAN_SHULKER_BOX || stack.getItem() == Items.PURPLE_SHULKER_BOX || stack.getItem() == Items.BLUE_SHULKER_BOX || stack.getItem() == Items.BROWN_SHULKER_BOX || stack.getItem() == Items.GREEN_SHULKER_BOX || stack.getItem() == Items.RED_SHULKER_BOX || stack.getItem() == Items.BLACK_SHULKER_BOX : false;
    }

    private int calculateItem(ItemStack stack) {
        if (stack.getItem() == Items.SHULKER_BOX) {
            return -6394435;
        } else if (stack.getItem() == Items.WHITE_SHULKER_BOX) {
            return -1;
        } else if (stack.getItem() == Items.ORANGE_SHULKER_BOX) {
            return -425955;
        } else if (stack.getItem() == Items.MAGENTA_SHULKER_BOX) {
            return -3715395;
        } else if (stack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX) {
            return -12930086;
        } else if (stack.getItem() == Items.YELLOW_SHULKER_BOX) {
            return -75715;
        } else if (stack.getItem() == Items.LIME_SHULKER_BOX) {
            return -8337633;
        } else if (stack.getItem() == Items.PINK_SHULKER_BOX) {
            return -816214;
        } else if (stack.getItem() == Items.GRAY_SHULKER_BOX) {
            return -12103854;
        } else if (stack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX) {
            return -6447721;
        } else if (stack.getItem() == Items.CYAN_SHULKER_BOX) {
            return -15295332;
        } else if (stack.getItem() == Items.PURPLE_SHULKER_BOX) {
            return -7785800;
        } else if (stack.getItem() == Items.BLUE_SHULKER_BOX) {
            return -12827478;
        } else if (stack.getItem() == Items.BROWN_SHULKER_BOX) {
            return -8170446;
        } else if (stack.getItem() == Items.GREEN_SHULKER_BOX) {
            return -10585066;
        } else if (stack.getItem() == Items.RED_SHULKER_BOX) {
            return -5231066;
        } else {
            return stack.getItem() == Items.BLACK_SHULKER_BOX ? -14869215 : -6394435;
        }
    }

    private void updateFillWithScaledWindowWidth(DrawContext context, ItemStack stack, ContainerComponent containerComponent, float floatValue, float floatValueValue) {
        MatrixStack matrices = context.getMatrices();
        int scaledWindowWidth = context.getScaledWindowWidth();
        int scaledWindowHeight = context.getScaledWindowHeight();
        float floatValueResult = 162.0F;
        float floatValueItem = 54.0F;
        float floatValueEntry = floatValueResult + 14.0F;
        float floatValueCopy = floatValueItem + 14.0F + 14.0F;
        float floatValue2 = floatValue + 12.0F;
        float floatValue3 = floatValueValue - 12.0F;
        if (floatValue2 + floatValueEntry > scaledWindowWidth) {
            floatValue2 = floatValue - floatValueEntry - 4.0F;
        }
        if (floatValue3 + floatValueCopy > scaledWindowHeight) {
            floatValue3 = scaledWindowHeight - floatValueCopy - 4.0F;
        }
        if (floatValue3 < 4.0F) {
            floatValue3 = 4.0F;
        }
        if (floatValue2 < 4.0F) {
            floatValue2 = 4.0F;
        }
        int intValue = this.calculateItem(stack);
        int intValueValue = ColorMathUtil.applyAlpha(intValue, 0.85F);
        int intValueResult = this.darkenColor(intValue, 0.6F);
        int intValueItem = this.brightenColor(intValue, 1.3F);
        matrices.push();
        GL11.glClear(256);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.translate(0.0F, 0.0F, 500.0F);
        Renderer2D.drawBlur(matrices, floatValue2 - 2.0F, floatValue3 - 2.0F, floatValueEntry + 4.0F, floatValueCopy + 4.0F, 7.0F, 8.0F, 0);
        context.fill((int) floatValue2, (int) floatValue3, (int) (floatValue2 + floatValueEntry), (int) (floatValue3 + floatValueCopy), intValueValue);
        context.fill((int) floatValue2, (int) floatValue3, (int) (floatValue2 + floatValueEntry), (int) (floatValue3 + 2.0F), intValueItem);
        context.fill((int) floatValue2, (int) (floatValue3 + floatValueCopy - 2.0F), (int) (floatValue2 + floatValueEntry), (int) (floatValue3 + floatValueCopy), intValueResult);
        context.fill((int) floatValue2, (int) floatValue3, (int) (floatValue2 + 2.0F), (int) (floatValue3 + floatValueCopy), intValueItem);
        context.fill((int) (floatValue2 + floatValueEntry - 2.0F), (int) floatValue3, (int) (floatValue2 + floatValueEntry), (int) (floatValue3 + floatValueCopy), intValueResult);
        TextRenderer font = FontManager.getFont("sf_regular", 12);
        if (font != null) {
            String string = stack.getName().getString();
            float floatValue4 = floatValue2 + 7.0F;
            float floatValue5 = floatValue3 + 7.0F - 1.0F;
            int intValueEntry = this.isInteger(intValue) ? -1 : -15066598;
            font.drawString(matrices, string, floatValue4, floatValue5, intValueEntry);
        }
        float floatValue6 = floatValue2 + 7.0F;
        float floatValue7 = floatValue3 + 7.0F + 14.0F - 2.0F;
        int intValueCopy = this.darkenColor(intValue, 0.5F);
        context.fill((int) (floatValue6 - 1.0F), (int) (floatValue7 - 1.0F), (int) (floatValue6 + floatValueResult + 1.0F), (int) (floatValue7 + floatValueItem + 1.0F), intValueCopy);
        ArrayList<net.minecraft.item.ItemStack> values = new ArrayList<>();
        containerComponent.stream().forEach(values::add);
        for (int index = 0; index < 27; index++) {
            int intValue2 = index / 9;
            int intValue3 = index % 9;
            int intValue4 = (int) (floatValue6 + intValue3 * 18);
            int intValue5 = (int) (floatValue7 + intValue2 * 18);
            context.fill(intValue4, intValue5, intValue4 + 18 - 2, intValue5 + 18 - 2, -7631989);
            context.fill(intValue4, intValue5, intValue4 + 18 - 2, intValue5 + 1, -11184811);
            context.fill(intValue4, intValue5, intValue4 + 1, intValue5 + 18 - 2, -11184811);
            context.fill(intValue4, intValue5 + 18 - 3, intValue4 + 18 - 2, intValue5 + 18 - 2, -1);
            context.fill(intValue4 + 18 - 3, intValue5, intValue4 + 18 - 2, intValue5 + 18 - 2, -1);
            if (index < values.size()) {
                ItemStack stackValue = values.get(index);
                if (!stackValue.isEmpty()) {
                    context.drawItem(stackValue, intValue4, intValue5);
                    context.drawStackOverlay(mc.textRenderer, stackValue, intValue4, intValue5);
                }
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private int darkenColor(int intValue, float floatValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = (int) ((intValue >> 16 & 0xFF) * floatValue);
        int intValueItem = (int) ((intValue >> 8 & 0xFF) * floatValue);
        int intValueEntry = (int) ((intValue & 0xFF) * floatValue);
        return intValueValue << 24 | Math.min(255, intValueResult) << 16 | Math.min(255, intValueItem) << 8 | Math.min(255, intValueEntry);
    }

    private int brightenColor(int intValue, float floatValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = (int) Math.min(255.0F, (intValue >> 16 & 0xFF) * floatValue);
        int intValueItem = (int) Math.min(255.0F, (intValue >> 8 & 0xFF) * floatValue);
        int intValueEntry = (int) Math.min(255.0F, (intValue & 0xFF) * floatValue);
        return intValueValue << 24 | intValueResult << 16 | intValueItem << 8 | intValueEntry;
    }

    private boolean isInteger(int intValue) {
        int intValueValue = intValue >> 16 & 0xFF;
        int intValueResult = intValue >> 8 & 0xFF;
        int intValueItem = intValue & 0xFF;
        double doubleValue = (0.299 * intValueValue + 0.587 * intValueResult + 0.114 * intValueItem) / 255.0;
        return doubleValue < 0.5;
    }
}
