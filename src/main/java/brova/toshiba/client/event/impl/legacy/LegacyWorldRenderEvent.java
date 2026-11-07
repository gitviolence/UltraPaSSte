package brova.toshiba.client.event.impl.legacy;

import brova.toshiba.client.event.EventBase;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class LegacyWorldRenderEvent extends EventBase {

    private final WorldRenderer context;

    private final MatrixStack matrix;

    private final Matrix4f projectionMatrix;

    private final Camera camera;

    private final float partialTicks;

    private final long finishTimeNano;

    public WorldRenderer getContext() {
        return this.context;
    }

    public MatrixStack getMatrix() {
        return this.matrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public long getFinishTimeNano() {
        return this.finishTimeNano;
    }

    public LegacyWorldRenderEvent(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, Camera camera, float partialTicks, long finishTimeNano) {
        this.context = context;
        this.matrix = matrix;
        this.projectionMatrix = projectionMatrix;
        this.camera = camera;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
    }
}
