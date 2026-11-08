package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.MovePositionEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class FreeCam extends Module {

    public static FreeCam INSTANCE = new FreeCam();

    public Vec3d pos;

    public FreeCam() {
        super("FreeCam", "Позволяет летать по карте, но на сервере вы стоите на месте", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            Vec3d pos = mc.player.getPos();
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, pos, (accessReceiver, accessValue) -> {
                accessReceiver.pos = accessValue;
            });
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null && this.pos != null) {
            mc.player.setPosition(this.pos);
        }
    }

    @EventHandler
    public void onEvent(EventPacket eventPacket) {
        var packet = eventPacket.getPacket();
        if (packet instanceof PlayerMoveC2SPacket) {
            eventPacket.cancel();
        } else if (packet instanceof PlayerRespawnS2CPacket || packet instanceof GameJoinS2CPacket) {
            this.toggle();
        }
    }

    @EventHandler
    public void onEvent(WorldRenderEvent event) {
        if (this.pos != null && mc.player != null) {
            float floatValue = mc.player.getWidth() / 2.0F;
            float height = mc.player.getHeight();
            Box box = new Box(this.pos.x - floatValue, this.pos.y, this.pos.z - floatValue, this.pos.x + floatValue, this.pos.y + height, this.pos.z + floatValue);
            this.renderColorWithVertex(event.getMatrices(), box, event.getCamera().getPos());
        }
    }

    private void renderColorWithVertex(MatrixStack matrices, Box box, Vec3d position) {
        double doubleValue = box.minX - position.x;
        double doubleValueValue = box.minY - position.y;
        double doubleValueResult = box.minZ - position.z;
        double doubleValueItem = box.maxX - position.x;
        double doubleValueEntry = box.maxY - position.y;
        double doubleValueCopy = box.maxZ - position.z;
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Tessellator instance = Tessellator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(1.5F);
        BufferBuilder buffer = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float floatValue = 1.0F;
        float floatValueValue = 1.0F;
        float floatValueResult = 1.0F;
        float floatValueItem = 1.0F;
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueResult).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValueItem, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueValue, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(positionMatrix, (float) doubleValue, (float) doubleValueEntry, (float) doubleValueCopy).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @EventHandler
    public void onEvent(MovePositionEvent event) {
        if (mc.player != null) {
            mc.player.noClip = true;
            double doubleValue = 1.0;
            double movementForward = mc.player.input.movementForward;
            double movementSideways = mc.player.input.movementSideways;
            double doubleValueValue = Math.toRadians(mc.player.getYaw());
            double doubleValueResult = 0.0;
            double doubleValueItem = 0.0;
            if (movementForward != 0.0 || movementSideways != 0.0) {
                double doubleValueEntry = doubleValueValue + Math.atan2(-movementSideways, movementForward);
                doubleValueResult = -Math.sin(doubleValueEntry) * doubleValue;
                doubleValueItem = Math.cos(doubleValueEntry) * doubleValue;
            }
            double doubleValueCopy = 0.0;
            if (mc.options.jumpKey.isPressed()) {
                doubleValueCopy = doubleValue;
            } else if (mc.options.sneakKey.isPressed()) {
                doubleValueCopy = -doubleValue;
            }
            event.setMovePos(new Vec3d(doubleValueResult, doubleValueCopy, doubleValueItem));
        }
    }

    @EventHandler
    public void onEvent(DirectionalInput directionalInput) {
        if (mc.player != null && (mc.player.getPose() == EntityPose.CROUCHING || mc.player.getPose() == EntityPose.SWIMMING)) {
            directionalInput.setStrafe(directionalInput.getStrafe() * 5.0F);
        }
    }
}
