package brova.toshiba.client.gui.swap;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.impl.combat.AutoSwap;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.render.BlurFramebufferManager;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class SwapScreen extends Screen implements MinecraftAccess {

    private final List<SwapItem> swapItemEntries = new ArrayList<>();

    private final Animation animation = new Animation(0.0F, 16.0F, PowerEasing.CUBIC_OUT);

    private final float buttonHeight = 38.0F;

    private final float buttonWidth = 94.0F;

    private final float buttonSpacing = 3.0F;

    private int update = -1;

    public SwapScreen() {
        super(Text.literal("SwapScreen"));
        this.swapItemEntries.add(new SwapItem(new ItemStack(Items.TOTEM_OF_UNDYING), "Тотем"));
        this.swapItemEntries.add(new SwapItem(new ItemStack(Items.PLAYER_HEAD), "Шар"));
        this.swapItemEntries.add(new SwapItem(new ItemStack(Items.GOLDEN_APPLE), "Гепл"));
    }

    @Override
    public void init() {
        this.animation.setValue(0.0F);
        this.animation.update(1.0F);
        this.update = -1;
        for (SwapItem swapItem : this.swapItemEntries) {
            swapItem.anim.setValue(0.0F);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        BlurFramebufferManager.getInstance().request();
        this.animation.update(1.0F);
        float value = this.animation.getValue();
        if (!(value <= 0.01F)) {
            MatrixStack matrices = context.getMatrices();
            float floatValue = this.width / 2.0F;
            float floatValueValue = this.height / 2.0F;
            int themeColor = ColorMathUtil.getThemeColor();
            int intValue = (int) (125.0F * value);
            context.fill(0, 0, this.width, this.height, ColorMathUtil.rgba(0, 0, 0, intValue));
            int size = this.swapItemEntries.size();
            double doubleValue = mouseX - floatValue;
            double doubleValueValue = mouseY - floatValueValue;
            double doubleValueResult = Math.hypot(doubleValue, doubleValueValue);
            this.update = -1;
            if (size > 0 && doubleValueResult >= 38.0 && doubleValueResult <= 124.0) {
                double doubleValueItem = Math.atan2(doubleValueValue, doubleValue) + (Math.PI / 2);
                while (doubleValueItem < 0.0) {
                    doubleValueItem += Math.PI * 2;
                }
                while (doubleValueItem >= Math.PI * 2) {
                    doubleValueItem -= Math.PI * 2;
                }
                double doubleValueEntry = (Math.PI * 2) / size;
                this.update = Math.min((int) (doubleValueItem / doubleValueEntry), size - 1);
            }
            matrices.push();
            matrices.translate(floatValue, floatValueValue, 0.0F);
            matrices.scale(value, value, 1.0F);
            matrices.translate(-floatValue, -floatValueValue, 0.0F);
            double doubleValueCopy = (Math.PI * 2) / size;
            float[] values = new float[size];
            float[] valuesValue = new float[size];
            float[] valuesResult = new float[size];
            for (int index = 0; index < size; index++) {
                SwapItem swapItem = this.swapItemEntries.get(index);
                swapItem.anim.update(index == this.update ? 1.0F : 0.0F);
                valuesResult[index] = swapItem.anim.getValue();
            }
            for (int indexValue = 0; indexValue < size; indexValue++) {
                float floatValueResult = valuesResult[indexValue];
                double doubleValue2 = (-Math.PI / 2) + doubleValueCopy * indexValue;
                double doubleValue3 = doubleValue2 + doubleValueCopy;
                double doubleValue4 = (doubleValue2 + doubleValue3) * 0.5;
                float floatValueItem = 94.0F + floatValueResult * 4.0F;
                float floatValueEntry = 38.0F;
                float floatValueCopy = (floatValueEntry + floatValueItem) * 0.5F;
                values[indexValue] = floatValue + (float) Math.cos(doubleValue4) * floatValueCopy;
                valuesValue[indexValue] = floatValueValue + (float) Math.sin(doubleValue4) * floatValueCopy;
                int interpolatedValue = MathHelper.lerp(floatValueResult, 22, 34);
                int interpolatedValueValue = MathHelper.lerp(floatValueResult, 22, 36);
                int interpolatedValueResult = MathHelper.lerp(floatValueResult, 28, 48);
                int intValueValue = ColorMathUtil.rgba(interpolatedValue, interpolatedValueValue, interpolatedValueResult, (int) (185.0F * value));
                int intValueResult = ColorMathUtil.red(themeColor);
                int intValueItem = ColorMathUtil.green(themeColor);
                int intValueEntry = ColorMathUtil.blue(themeColor);
                int interpolatedValueItem = MathHelper.lerp(floatValueResult, 55, intValueResult);
                int interpolatedValueEntry = MathHelper.lerp(floatValueResult, 55, intValueItem);
                int interpolatedValueCopy = MathHelper.lerp(floatValueResult, 68, intValueEntry);
                int intValueCopy = (int) (MathHelper.lerp(floatValueResult, 50, 230) * value);
                int intValue2 = ColorMathUtil.rgba(interpolatedValueItem, interpolatedValueEntry, interpolatedValueCopy, intValueCopy);
                this.renderColorWithVertex(matrices.peek().getPositionMatrix(), floatValue, floatValueValue, floatValueEntry, floatValueItem, doubleValue2, doubleValue3, 3.0F, intValueValue, intValue2, 0.6F + floatValueResult * 0.2F, value);
            }
            TextRenderer font = FontManager.getFont("manrope", 12);
            for (int indexResult = 0; indexResult < size; indexResult++) {
                SwapItem swapItemValue = this.swapItemEntries.get(indexResult);
                float floatValue2 = valuesResult[indexResult];
                float floatValue3 = values[indexResult];
                float floatValue4 = valuesValue[indexResult];
                if (!swapItemValue.stack.isEmpty()) {
                    matrices.push();
                    float floatValue5 = 1.15F + floatValue2 * 0.18F;
                    matrices.translate(floatValue3, floatValue4, 0.0F);
                    matrices.scale(floatValue5, floatValue5, 1.0F);
                    context.drawItem(swapItemValue.stack, -8, -8);
                    matrices.pop();
                    int itemCount = InventoryUtil.getItemCount(swapItemValue.stack.getItem());
                    if (itemCount > 1 && font != null) {
                        String text = String.valueOf(itemCount);
                        float stringWidth = font.getStringWidth(text);
                        float floatValue6 = stringWidth + 5.0F;
                        float floatValue7 = 9.5F;
                        float floatValue8 = floatValue3 + 3.0F;
                        float floatValue9 = floatValue4 + 1.0F;
                        Renderer2D.drawRoundedRect(matrices, floatValue8, floatValue9, floatValue6, floatValue7, 2.5F, ColorMathUtil.rgba(12, 12, 16, (int) (210.0F * value)));
                        font.drawString(matrices, text, floatValue8 + (floatValue6 - stringWidth) * 0.5F, floatValue9 + 3.0F, ColorMathUtil.rgba(255, 255, 255, (int) (240.0F * value)));
                    }
                }
            }
            TextRenderer fontValue = FontManager.getFont("manrope", 15);
            TextRenderer fontResult = FontManager.getFont("manrope", 12);
            float floatValue10 = floatValueValue + 94.0F + 20.0F;
            if (this.update >= 0 && this.update < size && fontValue != null) {
                SwapItem swapItemResult = this.swapItemEntries.get(this.update);
                String textValue = swapItemResult.customName != null ? swapItemResult.customName : swapItemResult.stack.getName().getString();
                fontValue.drawString(matrices, textValue, floatValue - fontValue.getStringWidth(textValue) / 2.0F + 1.0F, floatValue10, ColorMathUtil.rgba(255, 255, 255, (int) (255.0F * value)));
                if (fontResult != null) {
                    String textResult = "ПКМ - назначить предмет из руки";
                    fontResult.drawString(matrices, textResult, floatValue - fontResult.getStringWidth(textResult) / 2.0F + 1.5F, floatValue10 + 13.0F, ColorMathUtil.rgba(175, 175, 190, (int) (180.0F * value)));
                }
            } else if (fontResult != null) {
                String textItem = "Наведите на предмет и отпустите клавишу";
                fontResult.drawString(matrices, textItem, floatValue - fontResult.getStringWidth(textItem) / 2.0F + 12.0F, floatValue10 + 6.0F, ColorMathUtil.rgba(155, 155, 170, (int) (160.0F * value)));
            }
            matrices.pop();
        }
    }

    private void renderColorWithVertex(Matrix4f matrix, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, double doubleValue, double doubleValueValue, float floatValueEntry, int intValue, int intValueValue, float floatValueCopy, float floatValue2) {
        float floatValue3 = floatValueEntry * 0.5F;
        double doubleValueResult = Math.asin(Math.min(0.99, (double) (floatValue3 / floatValueResult)));
        double doubleValueItem = Math.asin(Math.min(0.99, (double) (floatValue3 / floatValueItem)));
        double doubleValueEntry = doubleValue + doubleValueResult;
        double doubleValueCopy = doubleValue + doubleValueItem;
        double doubleValue2 = doubleValueValue - doubleValueResult;
        double doubleValue3 = doubleValueValue - doubleValueItem;
        int intValueResult = Math.max(64, (int) (Math.toDegrees(doubleValueValue - doubleValue) * 2.0));
        byte byteValue = 4;
        int intValueItem = intValueResult + 1 + byteValue + intValueResult + 1 + byteValue;
        float[] values = new float[intValueItem];
        float[] valuesValue = new float[intValueItem];
        int intValueEntry = 0;
        for (int index = 0; index <= intValueResult; index++) {
            double doubleValue4 = doubleValueCopy + (doubleValue3 - doubleValueCopy) * ((double) index / intValueResult);
            values[intValueEntry] = floatValue + (float) (Math.cos(doubleValue4) * floatValueItem);
            valuesValue[intValueEntry] = floatValueValue + (float) (Math.sin(doubleValue4) * floatValueItem);
            intValueEntry++;
        }
        for (int indexValue = 1; indexValue <= byteValue; indexValue++) {
            double doubleValue5 = (double) indexValue / (byteValue + 1);
            double doubleValue6 = floatValueItem + (floatValueResult - floatValueItem) * doubleValue5;
            double doubleValue7 = doubleValue3 + (doubleValue2 - doubleValue3) * doubleValue5;
            values[intValueEntry] = floatValue + (float) (Math.cos(doubleValue7) * doubleValue6);
            valuesValue[intValueEntry] = floatValueValue + (float) (Math.sin(doubleValue7) * doubleValue6);
            intValueEntry++;
        }
        for (int indexResult = 0; indexResult <= intValueResult; indexResult++) {
            double doubleValue8 = doubleValue2 - (doubleValue2 - doubleValueEntry) * ((double) indexResult / intValueResult);
            values[intValueEntry] = floatValue + (float) (Math.cos(doubleValue8) * floatValueResult);
            valuesValue[intValueEntry] = floatValueValue + (float) (Math.sin(doubleValue8) * floatValueResult);
            intValueEntry++;
        }
        for (int indexItem = 1; indexItem <= byteValue; indexItem++) {
            double doubleValue9 = (double) indexItem / (byteValue + 1);
            double doubleValue10 = floatValueResult + (floatValueItem - floatValueResult) * doubleValue9;
            double doubleValue11 = doubleValueEntry + (doubleValueCopy - doubleValueEntry) * doubleValue9;
            values[intValueEntry] = floatValue + (float) (Math.cos(doubleValue11) * doubleValue10);
            valuesValue[intValueEntry] = floatValueValue + (float) (Math.sin(doubleValue11) * doubleValue10);
            intValueEntry++;
        }
        float[] valuesResult = new float[intValueEntry];
        float[] valuesItem = new float[intValueEntry];
        for (int indexEntry = 0; indexEntry < intValueEntry; indexEntry++) {
            int intValueCopy = (indexEntry - 1 + intValueEntry) % intValueEntry;
            int intValue2 = (indexEntry + 1) % intValueEntry;
            float floatValue4 = values[intValue2] - values[intValueCopy];
            float floatValue5 = valuesValue[intValue2] - valuesValue[intValueCopy];
            float floatValue6 = (float) Math.hypot(floatValue4, floatValue5);
            if (floatValue6 > 1.0E-4F) {
                valuesResult[indexEntry] = floatValue5 / floatValue6;
                valuesItem[indexEntry] = -floatValue4 / floatValue6;
            } else {
                valuesResult[indexEntry] = 0.0F;
                valuesItem[indexEntry] = 0.0F;
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        float scaledWidth = mc.getWindow().getScaledWidth();
        float scaledHeight = mc.getWindow().getScaledHeight();
        if (BlurFramebufferManager.getBuffer2() != null) {
            int texture = BlurFramebufferManager.getTexture();
            if (texture != 0) {
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE_COLOR);
                for (int indexCopy = 0; indexCopy <= intValueResult; indexCopy++) {
                    double doubleValue12 = (double) indexCopy / intValueResult;
                    double doubleValue13 = doubleValueCopy + (doubleValue3 - doubleValueCopy) * doubleValue12;
                    double doubleValue14 = doubleValueEntry + (doubleValue2 - doubleValueEntry) * doubleValue12;
                    float floatValue7 = floatValue + (float) (Math.cos(doubleValue13) * floatValueItem);
                    float floatValue8 = floatValueValue + (float) (Math.sin(doubleValue13) * floatValueItem);
                    float floatValue9 = floatValue7 / scaledWidth;
                    float floatValue10 = (scaledHeight - floatValue8) / scaledHeight;
                    float floatValue11 = floatValue + (float) (Math.cos(doubleValue14) * floatValueResult);
                    float floatValue12 = floatValueValue + (float) (Math.sin(doubleValue14) * floatValueResult);
                    float floatValue13 = floatValue11 / scaledWidth;
                    float floatValue14 = (scaledHeight - floatValue12) / scaledHeight;
                    buffer.vertex(matrix, floatValue7, floatValue8, 0.0F).texture(floatValue9, floatValue10).color(1.0F, 1.0F, 1.0F, floatValue2);
                    buffer.vertex(matrix, floatValue11, floatValue12, 0.0F).texture(floatValue13, floatValue14).color(1.0F, 1.0F, 1.0F, floatValue2);
                }
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE_COLOR);
                for (int index2 = 0; index2 <= intValueEntry; index2++) {
                    int intValue3 = index2 % intValueEntry;
                    float floatValue15 = values[intValue3];
                    float floatValue16 = valuesValue[intValue3];
                    float floatValue17 = valuesResult[intValue3];
                    float floatValue18 = valuesItem[intValue3];
                    float floatValue19 = floatValue15 / scaledWidth;
                    float floatValue20 = (scaledHeight - floatValue16) / scaledHeight;
                    float floatValue21 = floatValue15 + floatValue17 * 0.85F;
                    float floatValue22 = floatValue16 + floatValue18 * 0.85F;
                    float floatValue23 = floatValue21 / scaledWidth;
                    float floatValue24 = (scaledHeight - floatValue22) / scaledHeight;
                    bufferValue.vertex(matrix, floatValue15, floatValue16, 0.0F).texture(floatValue19, floatValue20).color(1.0F, 1.0F, 1.0F, floatValue2);
                    bufferValue.vertex(matrix, floatValue21, floatValue22, 0.0F).texture(floatValue23, floatValue24).color(1.0F, 1.0F, 1.0F, 0.0F);
                }
                BufferRenderer.drawWithGlobalProgram(bufferValue.end());
                RenderSystem.setShaderTexture(0, 0);
            }
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float floatValue25 = ColorMathUtil.redf(intValue);
        float floatValue26 = ColorMathUtil.greenf(intValue);
        float floatValue27 = ColorMathUtil.bluef(intValue);
        float floatValue28 = ColorMathUtil.alphaf(intValue);
        BufferBuilder bufferResult = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int index3 = 0; index3 <= intValueResult; index3++) {
            double doubleValue15 = (double) index3 / intValueResult;
            double doubleValue16 = doubleValueCopy + (doubleValue3 - doubleValueCopy) * doubleValue15;
            double doubleValue17 = doubleValueEntry + (doubleValue2 - doubleValueEntry) * doubleValue15;
            float floatValue29 = floatValue + (float) (Math.cos(doubleValue16) * floatValueItem);
            float floatValue30 = floatValueValue + (float) (Math.sin(doubleValue16) * floatValueItem);
            float floatValue31 = floatValue + (float) (Math.cos(doubleValue17) * floatValueResult);
            float floatValue32 = floatValueValue + (float) (Math.sin(doubleValue17) * floatValueResult);
            bufferResult.vertex(matrix, floatValue29, floatValue30, 0.0F).color(floatValue25, floatValue26, floatValue27, floatValue28);
            bufferResult.vertex(matrix, floatValue31, floatValue32, 0.0F).color(floatValue25, floatValue26, floatValue27, floatValue28);
        }
        BufferRenderer.drawWithGlobalProgram(bufferResult.end());
        BufferBuilder bufferItem = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int index4 = 0; index4 <= intValueEntry; index4++) {
            int intValue4 = index4 % intValueEntry;
            float floatValue33 = values[intValue4];
            float floatValue34 = valuesValue[intValue4];
            float floatValue35 = valuesResult[intValue4];
            float floatValue36 = valuesItem[intValue4];
            bufferItem.vertex(matrix, floatValue33, floatValue34, 0.0F).color(floatValue25, floatValue26, floatValue27, floatValue28);
            bufferItem.vertex(matrix, floatValue33 + floatValue35 * 0.85F, floatValue34 + floatValue36 * 0.85F, 0.0F).color(floatValue25, floatValue26, floatValue27, 0.0F);
        }
        BufferRenderer.drawWithGlobalProgram(bufferItem.end());
        float floatValue37 = ColorMathUtil.redf(intValueValue);
        float floatValue38 = ColorMathUtil.greenf(intValueValue);
        float floatValue39 = ColorMathUtil.bluef(intValueValue);
        float floatValue40 = ColorMathUtil.alphaf(intValueValue);
        if (floatValue40 > 0.01F && floatValueCopy > 0.05F) {
            float floatValue41 = floatValueCopy * 0.5F;
            float floatValue42 = 0.85F;
            BufferBuilder bufferEntry = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int index5 = 0; index5 <= intValueEntry; index5++) {
                int intValue5 = index5 % intValueEntry;
                float floatValue43 = values[intValue5];
                float floatValue44 = valuesValue[intValue5];
                float floatValue45 = valuesResult[intValue5];
                float floatValue46 = valuesItem[intValue5];
                bufferEntry.vertex(matrix, floatValue43 + floatValue45 * floatValue41, floatValue44 + floatValue46 * floatValue41, 0.0F).color(floatValue37, floatValue38, floatValue39, floatValue40);
                bufferEntry.vertex(matrix, floatValue43 - floatValue45 * floatValue41, floatValue44 - floatValue46 * floatValue41, 0.0F).color(floatValue37, floatValue38, floatValue39, floatValue40);
            }
            BufferRenderer.drawWithGlobalProgram(bufferEntry.end());
            BufferBuilder bufferCopy = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int index6 = 0; index6 <= intValueEntry; index6++) {
                int intValue6 = index6 % intValueEntry;
                float floatValue47 = values[intValue6];
                float floatValue48 = valuesValue[intValue6];
                float floatValue49 = valuesResult[intValue6];
                float floatValue50 = valuesItem[intValue6];
                bufferCopy.vertex(matrix, floatValue47 + floatValue49 * (floatValue41 + floatValue42), floatValue48 + floatValue50 * (floatValue41 + floatValue42), 0.0F).color(floatValue37, floatValue38, floatValue39, 0.0F);
                bufferCopy.vertex(matrix, floatValue47 + floatValue49 * floatValue41, floatValue48 + floatValue50 * floatValue41, 0.0F).color(floatValue37, floatValue38, floatValue39, floatValue40);
            }
            BufferRenderer.drawWithGlobalProgram(bufferCopy.end());
            BufferBuilder buffer2 = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int index7 = 0; index7 <= intValueEntry; index7++) {
                int intValue7 = index7 % intValueEntry;
                float floatValue51 = values[intValue7];
                float floatValue52 = valuesValue[intValue7];
                float floatValue53 = valuesResult[intValue7];
                float floatValue54 = valuesItem[intValue7];
                buffer2.vertex(matrix, floatValue51 - floatValue53 * floatValue41, floatValue52 - floatValue54 * floatValue41, 0.0F).color(floatValue37, floatValue38, floatValue39, floatValue40);
                buffer2.vertex(matrix, floatValue51 - floatValue53 * (floatValue41 + floatValue42), floatValue52 - floatValue54 * (floatValue41 + floatValue42), 0.0F).color(floatValue37, floatValue38, floatValue39, 0.0F);
            }
            BufferRenderer.drawWithGlobalProgram(buffer2.end());
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.update >= 0 && this.update < this.swapItemEntries.size()) {
            SwapItem swapItem = this.swapItemEntries.get(this.update);
            if (button == 0) {
                this.updateSwapToItemWithClose(brova.toshiba.client.utils.runtime.AccessGuard.read(swapItem, accessReceiver -> accessReceiver.stack));
                return true;
            }
            if (button == 1) {
                this.updateMainHandStackWithOffHandStack(swapItem);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == AutoSwap.INSTANCE.getSwapKey().getKey()) {
            this.updateClose();
            return true;
        } else {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int key = brova.toshiba.client.utils.runtime.AccessGuard.invoke(AutoSwap.INSTANCE, new Object[] {}, (accessReceiver, accessArguments) -> ((AutoSwap) accessReceiver).getSwapKey()).getKey();
        if (key >= 1000 && button == key - 1000) {
            this.updateClose();
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    private void updateClose() {
        if (this.update >= 0 && this.update < this.swapItemEntries.size()) {
            this.updateSwapToItemWithClose(brova.toshiba.client.utils.runtime.AccessGuard.read(this.swapItemEntries.get(this.update), accessReceiver -> accessReceiver.stack));
        } else {
            this.close();
        }
    }

    private void updateSwapToItemWithClose(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            AutoSwap.INSTANCE.swapToItem(stack);
        }
        this.close();
    }

    private void updateMainHandStackWithOffHandStack(SwapItem swapItem) {
        if (mc.player != null) {
            ItemStack mainHandStack = mc.player.getMainHandStack();
            if (mainHandStack.isEmpty()) {
                mainHandStack = mc.player.getOffHandStack();
            }
            if (!mainHandStack.isEmpty()) {
                swapItem.setStack(mainHandStack.copy());
                swapItem.setCustomName(mainHandStack.getName().getString());
            }
        }
    }
}
