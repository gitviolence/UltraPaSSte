package brova.toshiba.client.event.impl;

import brova.toshiba.client.event.EventBase;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class WorldRenderEvent extends EventBase {

    private final MatrixStack matrices;

    private final Matrix4f positionMatrix;

    private final Matrix4f projectionMatrix;

    private final Camera camera;

    private final float tickDelta;

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public Matrix4f getPositionMatrix() {
        return this.positionMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public float getTickDelta() {
        return this.tickDelta;
    }

    public WorldRenderEvent(MatrixStack matrices, Matrix4f positionMatrix, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        this.matrices = matrices;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
        this.camera = camera;
        this.tickDelta = tickDelta;
    }
}
