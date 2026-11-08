package brova.toshiba.client.utils.render.font;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public final class FontGlyph {

    private final int charCode;

    private final float uMin;

    private final float uMax;

    private final float vMin;

    private final float vMax;

    private final float advance;

    private final float bearingX;

    private final float bearingY;

    private final float glyphWidth;

    private final float glyphHeight;

    public FontGlyph(int charCode, float advance, float bearingX, float bearingY, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, float floatValueCopy, float floatValue2, float floatValue3) {
        this.charCode = charCode;
        this.advance = advance;
        if (floatValueResult == 0.0F && floatValueEntry == 0.0F && floatValueItem == 0.0F && floatValueCopy == 0.0F) {
            this.uMin = 0.0F;
            this.uMax = 0.0F;
            this.vMin = 0.0F;
            this.vMax = 0.0F;
        } else {
            this.uMin = floatValueResult / floatValue2;
            this.uMax = floatValueEntry / floatValue2;
            this.vMin = 1.0F - floatValueItem / floatValue3;
            this.vMax = 1.0F - floatValueCopy / floatValue3;
        }
        if (bearingX == 0.0F && floatValue == 0.0F && bearingY == 0.0F && floatValueValue == 0.0F) {
            this.glyphWidth = 0.0F;
            this.glyphHeight = 0.0F;
            this.bearingX = 0.0F;
            this.bearingY = 0.0F;
        } else {
            this.glyphWidth = floatValue - bearingX;
            this.glyphHeight = bearingY - floatValueValue;
            this.bearingX = bearingX;
            this.bearingY = bearingY;
        }
    }

    public float apply(Matrix4f matrix, VertexConsumer vertices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        floatValueValue += this.bearingX * floatValue;
        floatValueResult -= this.bearingY * floatValue;
        floatValueResult--;
        float floatValueEntry = this.glyphWidth * floatValue;
        float floatValueCopy = this.glyphHeight * floatValue;
        vertices.vertex(matrix, floatValueValue, floatValueResult, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMin, this.vMin);
        vertices.vertex(matrix, floatValueValue, floatValueResult + floatValueCopy, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMin, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult + floatValueCopy, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMax, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMax, this.vMin);
        return this.advance * floatValue;
    }

    public float applyVerticalGradient(Matrix4f matrix, VertexConsumer vertices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy, int intValue2, int intValue3) {
        floatValueValue += this.bearingX * floatValue;
        floatValueResult -= this.bearingY * floatValue;
        floatValueResult--;
        float floatValueEntry = this.glyphWidth * floatValue;
        float floatValueCopy = this.glyphHeight * floatValue;
        vertices.vertex(matrix, floatValueValue, floatValueResult, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMin, this.vMin);
        vertices.vertex(matrix, floatValueValue, floatValueResult + floatValueCopy, floatValueItem).color(intValueEntry, intValueCopy, intValue2, intValue3).texture(this.uMin, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult + floatValueCopy, floatValueItem).color(intValueEntry, intValueCopy, intValue2, intValue3).texture(this.uMax, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMax, this.vMin);
        return this.advance * floatValue;
    }

    public float applyHorizontalGradient(Matrix4f matrix, VertexConsumer vertices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy, int intValue2, int intValue3) {
        floatValueValue += this.bearingX * floatValue;
        floatValueResult -= this.bearingY * floatValue;
        floatValueResult--;
        float floatValueEntry = this.glyphWidth * floatValue;
        float floatValueCopy = this.glyphHeight * floatValue;
        vertices.vertex(matrix, floatValueValue, floatValueResult, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMin, this.vMin);
        vertices.vertex(matrix, floatValueValue, floatValueResult + floatValueCopy, floatValueItem).color(intValue, intValueValue, intValueResult, intValueItem).texture(this.uMin, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult + floatValueCopy, floatValueItem).color(intValueEntry, intValueCopy, intValue2, intValue3).texture(this.uMax, this.vMax);
        vertices.vertex(matrix, floatValueValue + floatValueEntry, floatValueResult, floatValueItem).color(intValueEntry, intValueCopy, intValue2, intValue3).texture(this.uMax, this.vMin);
        return this.advance * floatValue;
    }

    public float getWidth(float floatValue) {
        return this.advance * floatValue;
    }

    public int getCharCode() {
        return this.charCode;
    }
}
