package brova.toshiba.client.utils.render.transform;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public final class TransformStack {

    private static final Deque<TransformState> pushTransformStateEntries = new ArrayDeque<>();

    private static TransformState scaleTransformState = new TransformState(1.0F, 0.0F, 0.0F);

    private TransformStack() {
    }

    public static void push(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult) {
        pushTransformStateEntries.push(scaleTransformState);
        scaleTransformState = new TransformState(scaleTransformState.scale() * floatValue, scaleTransformState.offsetX() + scaleTransformState.scale() * floatValueValue * (1.0F - floatValue), scaleTransformState.offsetY() + scaleTransformState.scale() * floatValueResult * (1.0F - floatValue));
        matrices.push();
        matrices.translate(floatValueValue, floatValueResult, 0.0F);
        matrices.scale(floatValue, floatValue, 1.0F);
        matrices.translate(-floatValueValue, -floatValueResult, 0.0F);
    }

    public static void pop(MatrixStack matrices) {
        matrices.pop();
        scaleTransformState = pushTransformStateEntries.pop();
    }

    public static float x(float floatValue) {
        return floatValue * scaleTransformState.scale() + scaleTransformState.offsetX();
    }

    public static float y(float floatValue) {
        return floatValue * scaleTransformState.scale() + scaleTransformState.offsetY();
    }

    public static double x(double doubleValue) {
        return doubleValue * scaleTransformState.scale() + scaleTransformState.offsetX();
    }

    public static double y(double doubleValue) {
        return doubleValue * scaleTransformState.scale() + scaleTransformState.offsetY();
    }

    public static double size(double doubleValue) {
        return doubleValue * scaleTransformState.scale();
    }

    public static Matrix4f matrix() {
        return new Matrix4f().translate(scaleTransformState.offsetX(), scaleTransformState.offsetY(), 0.0F).scale(scaleTransformState.scale(), scaleTransformState.scale(), 1.0F);
    }
}
