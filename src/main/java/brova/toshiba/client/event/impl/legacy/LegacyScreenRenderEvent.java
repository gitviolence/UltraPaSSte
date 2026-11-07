package brova.toshiba.client.event.impl.legacy;

import brova.toshiba.client.event.EventBase;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class LegacyScreenRenderEvent extends EventBase {

    private final Window scaledResolution;

    private final float partialTicks;

    private final Matrix4f matrix;

    private final MatrixStack matrixStack;

    public Window getScaledResolution() {
        return this.scaledResolution;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public Matrix4f getMatrix() {
        return this.matrix;
    }

    public MatrixStack getMatrixStack() {
        return this.matrixStack;
    }

    public LegacyScreenRenderEvent(Window scaledResolution, float partialTicks, Matrix4f matrix, MatrixStack matrixStack) {
        this.scaledResolution = scaledResolution;
        this.partialTicks = partialTicks;
        this.matrix = matrix;
        this.matrixStack = matrixStack;
    }
}
