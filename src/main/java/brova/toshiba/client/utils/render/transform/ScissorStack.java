package brova.toshiba.client.utils.render.transform;

import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL30;

public class ScissorStack {

    private static ScissorRect glScissorScissorRect = new ScissorRect();

    private static final List<ScissorRect> scissorRectEntries = Lists.newArrayList();

    public static void push() {
        scissorRectEntries.add(glScissorScissorRect.clone());
    }

    public static void pop() {
        if (!scissorRectEntries.isEmpty()) {
            glScissorScissorRect = scissorRectEntries.remove(scissorRectEntries.size() - 1);
            if (glScissorScissorRect.enabled) {
                GL30.glEnable(3089);
                GL30.glScissor(glScissorScissorRect.x, glScissorScissorRect.y, glScissorScissorRect.width, glScissorScissorRect.height);
            } else {
                GL30.glDisable(3089);
            }
        }
    }

    public static void unset() {
        GL30.glDisable(3089);
        glScissorScissorRect.enabled = false;
    }

    private static Window createWindow() {
        MinecraftClient instance = MinecraftClient.getInstance();
        return instance == null ? null : instance.getWindow();
    }

    private static double calculateScaleFactor() {
        Window window = createWindow();
        return window == null ? 1.0 : window.getScaleFactor();
    }

    public static void setFromComponentCoordinates(int intValue, int intValueValue, int intValueResult, int intValueItem) {
        Window window = createWindow();
        if (window != null) {
            double doubleValue = calculateScaleFactor();
            int intValueEntry = (int) (TransformStack.x((float) intValue) * doubleValue);
            int intValueCopy = (int) (TransformStack.y((float) intValueValue) * doubleValue);
            int intValue2 = (int) (TransformStack.size(intValueResult) * doubleValue);
            int intValue3 = (int) (TransformStack.size(intValueItem) * doubleValue);
            intValueCopy = window.getHeight() - intValueCopy - intValue3;
            set(intValueEntry, intValueCopy, intValue2, intValue3);
        }
    }

    public static void setFromComponentCoordinates(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem) {
        Window window = createWindow();
        if (window != null) {
            double doubleValueEntry = calculateScaleFactor();
            int intValue = (int) (TransformStack.x(doubleValue) * doubleValueEntry);
            int intValueValue = (int) (TransformStack.y(doubleValueValue) * doubleValueEntry);
            int intValueResult = (int) (TransformStack.size(doubleValueResult) * doubleValueEntry);
            int intValueItem = (int) (TransformStack.size(doubleValueItem) * doubleValueEntry);
            intValueValue = window.getHeight() - intValueValue - intValueItem;
            set(intValue, intValueValue, intValueResult, intValueItem);
        }
    }

    public static void setFromComponentCoordinates(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, float floatValue) {
        Window window = createWindow();
        if (window != null) {
            double doubleValueEntry = calculateScaleFactor();
            float floatValueValue = (1.0F - floatValue) / 2.0F;
            double doubleValueCopy = doubleValue + doubleValueResult * floatValueValue;
            double doubleValue2 = doubleValueValue + doubleValueItem * floatValueValue;
            double doubleValue3 = doubleValueResult * floatValue;
            double doubleValue4 = doubleValueItem * floatValue;
            doubleValueCopy = doubleValueCopy * floatValue + (window.getScaledWidth() - doubleValue3) * floatValueValue;
            int intValue = (int) (TransformStack.x(doubleValueCopy) * doubleValueEntry);
            int intValueValue = (int) (TransformStack.y(doubleValue2) * doubleValueEntry);
            int intValueResult = (int) (TransformStack.size(doubleValue3) * doubleValueEntry);
            int intValueItem = (int) (TransformStack.size(doubleValue4) * doubleValueEntry);
            intValueValue = window.getHeight() - intValueValue - intValueItem;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { intValue, intValueValue, intValueResult, intValueItem }, (accessReceiver, accessArguments) -> {
                ScissorStack.set(((int) accessArguments[0]), ((int) accessArguments[1]), ((int) accessArguments[2]), ((int) accessArguments[3]));
            });
        }
    }

    public static void set(int intValue, int intValueValue, int intValueResult, int intValueItem) {
        Window window = createWindow();
        if (window != null) {
            Rectangle bounds = new Rectangle(0, 0, window.getWidth(), window.getHeight());
            Rectangle boundsValue;
            if (glScissorScissorRect.enabled) {
                boundsValue = new Rectangle(glScissorScissorRect.x, glScissorScissorRect.y, glScissorScissorRect.width, glScissorScissorRect.height);
            } else {
                boundsValue = bounds;
            }
            Rectangle boundsResult = new Rectangle(intValue + glScissorScissorRect.transX, intValueValue + glScissorScissorRect.transY, intValueResult, intValueItem);
            Rectangle boundsItem = boundsValue.intersection(boundsResult);
            boundsItem = boundsItem.intersection(bounds);
            if (boundsItem.width < 0) {
                boundsItem.width = 0;
            }
            if (boundsItem.height < 0) {
                boundsItem.height = 0;
            }
            glScissorScissorRect.enabled = true;
            glScissorScissorRect.x = boundsItem.x;
            glScissorScissorRect.y = boundsItem.y;
            glScissorScissorRect.width = boundsItem.width;
            glScissorScissorRect.height = boundsItem.height;
            GL30.glEnable(3089);
            GL30.glScissor(boundsItem.x, boundsItem.y, boundsItem.width, boundsItem.height);
        }
    }

    public static void translate(int transX, int transY) {
        glScissorScissorRect.transX = transX;
        glScissorScissorRect.transY = transY;
    }

    public static void translateFromComponentCoordinates(int intValue, int intValueValue) {
        Window window = createWindow();
        if (window != null) {
            int scaledHeight = window.getScaledHeight();
            double doubleValue = calculateScaleFactor();
            int intValueResult = (int) (TransformStack.x((float) intValue) * doubleValue);
            int intValueItem = (int) (TransformStack.y((float) intValueValue) * doubleValue);
            intValueItem = (int) (scaledHeight * doubleValue) - intValueItem;
            translate(intValueResult, intValueItem);
        }
    }
}
