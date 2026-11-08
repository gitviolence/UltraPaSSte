package brova.toshiba.client.storages.implement;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.data.Waypoint;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.font.StyledFontRenderer;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class WaypointStorage implements MinecraftAccess {

    private final Matrix4f matrix4f = new Matrix4f();

    private final Quaternionf quaternionf = new Quaternionf();

    private Vec3d pos = Vec3d.ZERO;

    private Waypoint activeWaypoint = null;

    public WaypointStorage() {
        EventBus.register(this);
    }

    public void set(Waypoint waypoint) {
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, waypoint, (accessReceiver, accessValue) -> {
            accessReceiver.activeWaypoint = accessValue;
        });
    }

    public void remove(Waypoint waypoint) {
        if (this.activeWaypoint != null && this.activeWaypoint.equals(waypoint)) {
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.activeWaypoint = (Waypoint) accessValue;
            });
        }
    }

    public void clear() {
        this.activeWaypoint = null;
    }

    public boolean isEmpty() {
        return this.activeWaypoint == null;
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        this.matrix4f.set(event.getProjectionMatrix());
        this.pos = event.getCamera().getPos();
        this.quaternionf.set(event.getCamera().getRotation());
    }

    @EventHandler
    public void onRender2D(HudRenderEvent event) {
        if (mc.player != null && mc.world != null && this.activeWaypoint != null) {
            double doubleValue = this.activeWaypoint.getX() + 0.5 - mc.player.getX();
            double doubleValueValue = this.activeWaypoint.getY() + 0.5 - mc.player.getY();
            double doubleValueResult = this.activeWaypoint.getZ() + 0.5 - mc.player.getZ();
            double squareRoot = Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult);
            float floatValue;
            if (squareRoot <= 1.0) {
                floatValue = 0.0F;
            } else if (squareRoot < 5.0) {
                floatValue = (float) ((squareRoot - 1.0) / 4.0);
            } else {
                floatValue = 1.0F;
            }
            if (!(floatValue <= 0.01F)) {
                Vec3d position = new Vec3d(this.activeWaypoint.getX() + 0.5, this.activeWaypoint.getY() + 0.5, this.activeWaypoint.getZ() + 0.5);
                Vec3d positionValue = this.createPositionFromNaNWithInfinite(position);
                if (positionValue != null) {
                    float x = (float) positionValue.x;
                    float y = (float) positionValue.y;
                    StyledFontRenderer font = SystemFontManager.getFont("Manrope-Bold.ttf", 15.0F);
                    if (font != null) {
                        String text = this.activeWaypoint.getName() + " x" + (int) this.activeWaypoint.getX() + " y" + (int) this.activeWaypoint.getY() + " z" + (int) this.activeWaypoint.getZ();
                        String textValue = "[" + Math.max(0L, Math.round(squareRoot)) + "m]";
                        float floatValueValue = 2.0F;
                        float floatValueResult = 1.0F;
                        float floatValueItem = 1.0F;
                        float floatValueEntry = 1.0F;
                        float stringWidth = font.getStringWidth(text);
                        float stringWidthValue = font.getStringWidth(textValue);
                        float floatValueCopy = stringWidth + floatValueValue + stringWidthValue;
                        float floatValue2 = font.getStringHeight(text) + 2.0F;
                        float floatValue3 = floatValue2 + floatValueItem + floatValueEntry;
                        float floatValue4 = Math.round(x);
                        float floatValue5 = Math.round(floatValue4 - (floatValueCopy + floatValueResult * 2.0F) / 2.0F);
                        float floatValue6 = Math.round(y - floatValue3 - 3.0F);
                        float floatValue7 = floatValue5 + floatValueResult;
                        float floatValue8 = floatValue6 + floatValueItem + 2.0F;
                        MatrixStack matrices = event.getContext().getMatrices();
                        this.renderColorWithVertex(matrices, floatValue5, floatValue6, floatValueCopy + floatValueResult * 2.0F, floatValue3, floatValue);
                        int intValue = this.calculateInteger(-1, floatValue);
                        int intValueValue = this.calculateInteger(-11141291, floatValue);
                        font.drawString(text, Math.round(floatValue7), Math.round(floatValue8), intValue);
                        font.drawString(textValue, Math.round(floatValue7 + stringWidth + floatValueValue), Math.round(floatValue8), intValueValue);
                    }
                }
            }
        }
    }

    private void renderColorWithVertex(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry) {
        int intValue = (int) (80.0F * floatValueEntry);
        if (intValue > 0) {
            int intValueValue = ColorMathUtil.rgba(0, 0, 0, intValue);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(intValueValue);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(intValueValue);
            buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(intValueValue);
            buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(intValueValue);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();
        }
    }

    private int calculateInteger(int intValue, float floatValue) {
        int intValueValue = intValue >>> 24 & 0xFF;
        intValueValue = (int) (intValueValue * floatValue);
        return intValue & 16777215 | intValueValue << 24;
    }

    private Vec3d createPositionFromNaNWithInfinite(Vec3d position) {
        Vector3f vector3f = new Vector3f((float) (position.x - this.pos.x), (float) (position.y - this.pos.y), (float) (position.z - this.pos.z));
        vector3f.rotate(new Quaternionf(this.quaternionf).conjugate());
        Vector4f vector4f = new Vector4f(vector3f.x, vector3f.y, vector3f.z, 1.0F);
        this.matrix4f.transform(vector4f);
        float w = vector4f.w;
        if (w <= 1.0E-5F) {
            return null;
        } else {
            float floatValue = vector4f.x / w;
            float floatValueValue = vector4f.y / w;
            float floatValueResult = (floatValue * 0.5F + 0.5F) * mc.getWindow().getScaledWidth();
            float floatValueItem = (1.0F - (floatValueValue * 0.5F + 0.5F)) * mc.getWindow().getScaledHeight();
            return !Float.isNaN(floatValueResult) && !Float.isNaN(floatValueItem) && !Float.isInfinite(floatValueResult) && !Float.isInfinite(floatValueItem) ? new Vec3d(floatValueResult, floatValueItem, 0.0) : null;
        }
    }

    public Waypoint getActiveWaypoint() {
        return this.activeWaypoint;
    }
}
