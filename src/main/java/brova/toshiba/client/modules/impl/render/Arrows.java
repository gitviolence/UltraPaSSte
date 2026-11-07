package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Arrows.ArrowPosition;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.utils.math.MathUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class Arrows extends Module {

    public static Arrows INSTANCE = new Arrows();

    private static final Identifier arrowTexture = Identifier.of("toshiba", "featrues/arrows/arrow.png");

    private static final Identifier arrTexture = Identifier.of("toshiba", "featrues/arrows/arr.png");

    private final ModeSetting style = new ModeSetting("Вид", "Первый", "Первый", "Второй");

    private final FloatSetting radius = new FloatSetting("Радиус", 28.0F, 0.0F, 50.0F, 1.0F);

    private final FloatSetting size = new FloatSetting("Размер", 13.0F, 8.0F, 35.0F, 0.5F);

    private final Map<UUID, ArrowPosition> entrySetArrowsArrowPositionByUUID = new HashMap<>();

    private final Set<UUID> uUIDEntries = new HashSet<>();

    public Arrows() {
        super("Arrows", "Рисует стрелки на игроков", ModuleCategory.RENDER);
        this.addSettings(this.style, this.radius, this.size);
    }

    @EventHandler
    public void onRender(HudRenderEvent event) {
        if (mc.player != null && mc.world != null && !mc.options.hudHidden) {
            if (mc.options.getPerspective() != Perspective.FIRST_PERSON) {
                this.updateRemoveIfWithEntrySet();
            } else {
                float partialTicks = event.getPartialTicks();
                float floatValue = mc.getWindow().getScaledWidth() * 0.5F;
                float floatValueValue = mc.getWindow().getScaledHeight() * 0.5F;
                float floatValueResult = this.size.get();
                float floatValueItem = floatValueValue - (this.radius.get() + 30.0F);
                float floatValueEntry = this.calculateActiveWithFreeYaw(partialTicks);
                Vec3d position = this.createPositionFromCameraWithActive(partialTicks);
                this.uUIDEntries.clear();
                HashSet<String> values = new HashSet<>();
                for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                    if (player != mc.player && player.isAlive() && !player.isSpectator() && !this.isCustomNameWithStartsWith(player)) {
                        String string = player.getName().getString();
                        if (values.add(string)) {
                            UUID uuid = player.getUuid();
                            ArrowPosition arrowPosition = this.entrySetArrowsArrowPositionByUUID.computeIfAbsent(uuid, input -> new ArrowPosition());
                            this.uUIDEntries.add(uuid);
                            int intValue = this.calculateNameWithFriend(player);
                            float floatValueCopy = this.calculateInterpolateWithToDegrees(player, partialTicks, floatValueEntry, position);
                            arrowPosition.cachedScalar = this.calculateWrapDegrees(arrowPosition.cachedScalar, floatValueCopy, 0.18F);
                            arrowPosition.value = this.calculateLerp(arrowPosition.value, 1.0F, 0.12F);
                            float clampedValue = MathHelper.clamp(arrowPosition.value, 0.0F, 1.0F);
                            if (!(clampedValue <= 0.01F)) {
                                int intValueValue = ColorMathUtil.applyAlpha(intValue, clampedValue);
                                this.updateTranslateWithIndex(event.getContext().getMatrices(), floatValue, floatValueValue, floatValueItem, floatValueResult, arrowPosition.cachedScalar, intValueValue);
                            }
                        }
                    }
                }
                this.entrySetArrowsArrowPositionByUUID.entrySet().removeIf(inputValue -> {
                    if (this.uUIDEntries.contains(inputValue.getKey())) {
                        return false;
                    } else {
                        ArrowPosition value = inputValue.getValue();
                        value.value = this.calculateLerp(value.value, 0.0F, 0.1F);
                        return value.value <= 0.02F;
                    }
                });
            }
        } else {
            this.entrySetArrowsArrowPositionByUUID.clear();
        }
    }

    private void updateTranslateWithIndex(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, float floatValueEntry, int intValue) {
        Identifier texture = this.style.getIndex() == 0 ? arrowTexture : arrTexture;
        matrices.push();
        matrices.translate(floatValue, floatValueValue, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(floatValueEntry));
        matrices.translate(-floatValue, -floatValueValue, 0.0F);
        float floatValueCopy = floatValue - floatValueItem * 0.5F;
        Renderer2D.drawImage(matrices, texture, floatValueCopy, floatValueResult, floatValueItem, floatValueItem, intValue);
        matrices.pop();
    }

    private void updateRemoveIfWithEntrySet() {
        this.entrySetArrowsArrowPositionByUUID.entrySet().removeIf(input -> {
            ArrowPosition value = input.getValue();
            value.value = this.calculateLerp(value.value, 0.0F, 0.1F);
            return value.value <= 0.02F;
        });
    }

    private float calculateLerp(float floatValue, float floatValueValue, float floatValueResult) {
        floatValueResult = MathHelper.clamp(floatValueResult, 0.0F, 1.0F);
        return MathHelper.lerp(floatValueResult, floatValue, floatValueValue);
    }

    private int calculateNameWithFriend(AbstractClientPlayerEntity player) {
        String string = player.getName().getString();
        boolean flag = Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(string);
        return flag ? ColorMathUtil.rgb(80, 170, 255) : ColorMathUtil.getThemeColor();
    }

    private float calculateInterpolateWithToDegrees(Entity entity, float floatValue, float floatValueValue, Vec3d position) {
        Vec3d positionValue = MathUtil.interpolate(entity, floatValue);
        double doubleValue = positionValue.x - position.x;
        double doubleValueValue = positionValue.z - position.z;
        float floatValueResult = (float) (-Math.toDegrees(Math.atan2(doubleValue, doubleValueValue)));
        return MathHelper.wrapDegrees(floatValueResult - floatValueValue);
    }

    private float calculateActiveWithFreeYaw(float floatValue) {
        return FreeLookStorage.isActive() ? FreeLookStorage.getFreeYaw() : MathHelper.lerp(floatValue, mc.player.prevYaw, mc.player.getYaw());
    }

    private Vec3d createPositionFromCameraWithActive(float floatValue) {
        return FreeLookStorage.isActive() && mc.gameRenderer != null && mc.gameRenderer.getCamera() != null ? mc.gameRenderer.getCamera().getPos() : MathUtil.interpolate(mc.player, floatValue);
    }

    private float calculateWrapDegrees(float floatValue, float floatValueValue, float floatValueResult) {
        float floatValueItem = MathHelper.wrapDegrees(floatValueValue - floatValue);
        return floatValue + floatValueItem * floatValueResult;
    }

    private boolean isCustomNameWithStartsWith(AbstractClientPlayerEntity player) {
        if (player.getCustomName() != null) {
            String string = player.getCustomName().getString();
            if (string != null && string.startsWith("Ghost_")) {
                return true;
            }
        }
        return player instanceof OtherClientPlayerEntity && player.getPitch() == -30.0F;
    }

    public final static class ArrowPosition {

        float value;

        float cachedScalar;

        ArrowPosition() {
        }
    }
}
