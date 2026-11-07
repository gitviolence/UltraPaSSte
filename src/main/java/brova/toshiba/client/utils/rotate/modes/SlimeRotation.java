package brova.toshiba.client.utils.rotate.modes;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.CloseRangeRotationMultiplier;
import brova.toshiba.client.utils.rotate.MouseGcdUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationModeBase;
import brova.toshiba.client.utils.rotate.RotationPointUtil;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.world.EntityRaycastUtil;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SlimeRotation extends RotationModeBase implements MinecraftAccess {

    private final AttackAura attackAura;

    private final CloseRangeRotationMultiplier closeRangeRotationMultiplier = new CloseRangeRotationMultiplier();

    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    private LivingEntity livingEntity;

    private float yaw;

    private float pitch;

    private float x;

    private float y;

    private float yawMomentum;

    private float pitchMomentum;

    private float rotationStrength;

    private boolean decelerating;

    private boolean rotationDirection;

    private int cachedCount;

    private long lastUpdateTimestamp;

    private double sin;

    private float driftYaw;

    // Пришитая точка прицела: одна точка на теле цели едёт вместе с телом (по стоящей цели
    // голова стоит ровно в одной точке — без блуждания по телу), при потере луча пересобирается.
    private boolean rayOnTarget;

    private Vec3d cachedAimPoint;

    private Vec3d targetPrevPosition;

    private LivingEntity aimEntity;

    // Диапазон «силы» рывка рероллится каждый цикл наращивания/спада: без усреднённой
    // пилы 0→0.24→0 (детерминированный периодический сигнал) — вместо неё дрейфующий
    // потолок/пол, из которого ач периодичность не извлекут.
    private float strengthCeiling = 0.24F;

    private float strengthFloor = 0.14F;

    public SlimeRotation(AttackAura attackAura) {
        this.attackAura = attackAura;
    }

    public void reset() {
        this.livingEntity = null;
        this.rotationDirection = false;
        this.rotationStrength = 0.0F;
        this.decelerating = false;
        this.cachedCount = 0;
        this.lastUpdateTimestamp = 0L;
        this.yawMomentum = 0.0F;
        this.pitchMomentum = 0.0F;
        this.sin = this.threadLocalRandom.nextDouble(Math.PI * 2);
        this.driftYaw = 0.0F;
        this.rayOnTarget = false;
        this.cachedAimPoint = null;
        this.targetPrevPosition = null;
        this.aimEntity = null;
        this.strengthCeiling = 0.24F;
        this.strengthFloor = 0.14F;
        if (mc.player != null) {
            this.yaw = mc.player.getYaw();
            this.pitch = mc.player.getPitch();
        }
        this.rotate = new Vec2f(this.yaw, this.pitch);
    }

    public void onAttack() {
        long lastUpdateTimestamp = System.nanoTime();
        if (this.lastUpdateTimestamp == 0L || lastUpdateTimestamp - this.lastUpdateTimestamp >= 150000000L) {
            this.lastUpdateTimestamp = lastUpdateTimestamp;
            this.cachedCount = 1;
            this.decelerating = false;
            this.rotationStrength = Math.max(this.rotationStrength, 0.18F);
            this.yawMomentum *= 0.88F;
            this.pitchMomentum *= 0.86F;
        }
    }

    public void returnToCamera() {
        if (RotationStorage.instance != null && RotationStorage.instance.isRotating()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(RotationStorage.instance, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((RotationStorage) accessReceiver).stopRotation();
            });
        }
        this.reset();
    }

    @Override
    public void updateRotations(LivingEntity entity) {
        if (mc.player != null && entity != null) {
            Vec2f rotation = this.createVec2fFromResolveRotationPointWithMultipoint(entity);
            if (!this.rotationDirection || this.livingEntity != entity) {
                this.updateYawWithPitch(entity, rotation);
            }
            Box box = entity.getBoundingBox().expand(0.05);
            Vec3d eyePos = mc.player.getEyePos();
            boolean flag = mc.player.isGliding() && box.raycast(new Vec3d(mc.player.prevX, mc.player.prevY, mc.player.prevZ), mc.player.getPos()).isPresent();
            boolean flagValue = flag || box.contains(eyePos) || mc.player.getBoundingBox().intersects(box);
            float floatValue = this.calculateFloat(1.0F - MathHelper.clamp((float) (this.calculateSqrt(eyePos, box) / 0.9), 0.0F, 1.0F));
            if (flagValue) {
                floatValue = 1.0F;
            }
            rotation = this.createVec2fFromLerpWithSignum(rotation, floatValue);
            float errYaw = MathHelper.wrapDegrees(rotation.x - this.yaw);
            float errPitch = rotation.y - this.pitch;
            float magnitude = Math.abs(errYaw);
            float magnitudeValue = Math.abs(errPitch);
            this.updateFloat(magnitude);
            float strengthFactor = this.calculateFloat(MathHelper.clamp(Math.max(this.rotationStrength, 0.0F) / 0.03F, 0.0F, 1.0F));
            if (this.cachedCount > 0) {
                this.cachedCount--;
            }
            boolean flagItem = EntityRaycastUtil.rayTrace(mc.player.getRotationVector(this.pitch, this.yaw), 6.0, box);
            boolean attackWindow = mc.player.getAttackCooldownProgress(0.0F) > 0.9F;
            // Мощная доводка: без моторной инерции — шаг прямо пропорционален ошибке. Коэффициент
            // 0.85–1.0 (рероллится по циклу силы как «человеческая» вариация темпа; в окне готового
            // КД строго 1.0) → схождение за 1–3 тика, к каждому удару прицел уже на точке.
            // Капы 30–44°/тик: большие развороты закрываются за 3–5 тиков, в бою кэп почти не упирается.
            float yawStepFactor = attackWindow ? 1.0F : 0.85F + 0.15F * strengthFactor;
            float pitchStepFactor = attackWindow ? 1.0F : 0.83F + 0.17F * strengthFactor;
            float yawCap = 30.0F + 14.0F * strengthFactor;
            float pitchCap = yawCap * 0.35F;
            // Джиттер пропорционален дистанции до точки: далеко — лёгкое покачивание, в точке —
            // тишина. Промахов не вызывает, сигнатура движения — не «робот-шаг».
            float yawNoise = (float) this.threadLocalRandom.nextGaussian() * Math.min(magnitude, 1.5F) * 0.22F;
            float pitchNoise = (float) this.threadLocalRandom.nextGaussian() * Math.min(magnitudeValue, 1.0F) * 0.22F;
            float floatValueCopy = this.yaw + MathHelper.clamp(errYaw * yawStepFactor + yawNoise, -yawCap, yawCap);
            float floatValue2 = this.pitch + MathHelper.clamp(errPitch * pitchStepFactor + pitchNoise, -pitchCap, pitchCap);
            // Запоминаем: луч этого тика на хитбоксе или нет — точка прицела следует за телом,
            // а при потере луча пересобирается (не смотрим в пустоту, промахов нет).
            this.rayOnTarget = flagItem;
            this.apply(this.createVec2fFromGCDValueWithWrapDegrees(floatValueCopy, floatValue2), rotation);
        } else {
            this.reset();
        }
    }

    private Vec2f createVec2fFromResolveRotationPointWithMultipoint(LivingEntity entity) {
        Vec3d resolvedPoint = this.attackAura.resolveRotationPoint(null);
        Vec3d position;
        if (resolvedPoint != null) {
            this.aimEntity = entity;
            position = resolvedPoint;
        } else {
            if (this.aimEntity != entity || this.cachedAimPoint == null || !this.rayOnTarget) {
                // Новая цель, сброс или потерянный луч — точка собирается заново (с инсетом в тело).
                this.cachedAimPoint = this.createInsetAimPoint(entity);
            } else {
                // Точка едёт вместе с телом цели: сдвиг тика докидываем на место —
                // по стоящей цели крест стоит ровно в одной точке, по бегущей не отстаёт.
                this.cachedAimPoint = this.cachedAimPoint.add(entity.getPos().subtract(this.targetPrevPosition));
            }
            this.aimEntity = entity;
            position = this.cachedAimPoint;
        }
        this.targetPrevPosition = entity.getPos();
        if (this.shouldUseElytraPredict(entity)) {
            position = this.getPredictedPoint(entity, position);
        }
        return RotationUtil.getRotations(position);
    }

    private Vec3d createInsetAimPoint(LivingEntity entity) {
        // Свежая мультиточка затягивается на 25% к центру хитбокса: краевые точки держат луч
        // на самом берегу бокса — офсет GCD-квантования и лагов сервера сводит удар «в пустоту»
        // (свинг без урона). Внутри тела промахов нет, зрительно всё то же самое.
        Box box = entity.getBoundingBox();
        Vec3d position = RotationPointUtil.getMultipoint(entity, 128.0);
        Vec3d center = box.getCenter();
        return new Vec3d(MathHelper.lerp(0.25, position.x, center.x), MathHelper.lerp(0.25, position.y, center.y), MathHelper.lerp(0.25, position.z, center.z));
    }

    private Vec2f createVec2fFromLerpWithSignum(Vec2f rotation, float floatValue) {
        if (floatValue <= 0.0F) {
            this.x = rotation.x;
            this.y = rotation.y;
            return rotation;
        } else {
            float floatValueValue = MathHelper.wrapDegrees(rotation.x - this.x);
            float floatValueResult = rotation.y - this.y;
            float interpolatedValue = MathHelper.lerp(floatValue, 8.0F, 2.15F);
            float interpolatedValueValue = MathHelper.lerp(floatValue, 2.8F, 0.52F);
            float floatValueItem = floatValueValue * MathHelper.lerp(floatValue, 0.74F, 0.39F) + Math.signum(floatValueValue) * Math.min(floatValueValue * floatValueValue * 0.0025F, 0.45F);
            float floatValueEntry = floatValueResult * MathHelper.lerp(floatValue, 0.66F, 0.29F) + Math.signum(floatValueResult) * Math.min(floatValueResult * floatValueResult * 0.003F, 0.16F);
            this.x = this.x + MathHelper.clamp(floatValueItem, -interpolatedValue, interpolatedValue);
            this.y = this.y + MathHelper.clamp(floatValueEntry, -interpolatedValueValue, interpolatedValueValue);
            return new Vec2f(this.x, this.y);
        }
    }

    private void updateFloat(float floatValue) {
        if (this.decelerating) {
            this.rotationStrength -= floatValue > 30.0F ? 0.010F + this.threadLocalRandom.nextFloat() * 0.004F : 0.015F + this.threadLocalRandom.nextFloat() * 0.007F;
            if (this.rotationStrength <= this.strengthFloor) {
                this.rotationStrength = this.strengthFloor;
                this.decelerating = false;
            }
        } else {
            this.rotationStrength = Math.min(this.strengthCeiling, this.rotationStrength + 0.010F + this.threadLocalRandom.nextFloat() * 0.005F);
            if (this.rotationStrength >= this.strengthCeiling) {
                // Реролл диапазона при каждом развороте: потолок/пол дрейфуют,
                // спад идёт до пола (не до нуля) — пила исчезает из спектра.
                this.strengthCeiling = 0.21F + this.threadLocalRandom.nextFloat() * 0.05F;
                this.strengthFloor = Math.max(0.09F, this.strengthCeiling * (0.38F + this.threadLocalRandom.nextFloat() * 0.12F));
                this.decelerating = true;
            }
        }
    }

    private void apply(Vec2f rotation, Vec2f rotationValue) {
        RotationStorage.update(new Rotation(rotation.x, rotation.y), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, mc.player.isGliding());
        this.yaw = mc.player.getYaw();
        this.pitch = mc.player.getPitch();
        this.rotate = new Vec2f(this.yaw, this.pitch);
        this.attackAura.setCurrentRotations(this.rotate);
        this.attackAura.setTargetRotations(rotationValue);
    }

    private void updateYawWithPitch(LivingEntity livingEntity, Vec2f rotation) {
        this.livingEntity = livingEntity;
        this.yaw = mc.player.getYaw();
        this.pitch = mc.player.getPitch();
        this.x = rotation.x;
        this.y = rotation.y;
        this.yawMomentum = 0.0F;
        this.pitchMomentum = 0.0F;
        // Свежая наводка не стартует с нулевой «силы»: как и после удара, разгон сразу с 0.18 —
        // прицел ведёт цель с первого тика вместо 2–3 тиков полумёртвого ползания.
        this.rotationStrength = Math.max(this.rotationStrength, 0.18F);
        this.rotationDirection = true;
    }

    private Vec2f createVec2fFromGCDValueWithWrapDegrees(float floatValue, float floatValueValue) {
        float gCDValue = MouseGcdUtil.getGCDValue();
        if (gCDValue <= 0.0F) {
            return new Vec2f(floatValue, floatValueValue);
        } else {
            float floatValueResult = Math.round(MathHelper.wrapDegrees(floatValue - this.yaw) / gCDValue) * gCDValue;
            float floatValueItem = Math.round((floatValueValue - this.pitch) / gCDValue) * gCDValue;
            return new Vec2f(this.yaw + floatValueResult, MathHelper.clamp(this.pitch + floatValueItem, -90.0F, 90.0F));
        }
    }

    private double calculateSqrt(Vec3d position, Box box) {
        double doubleValue = position.x - MathHelper.clamp(position.x, box.minX, box.maxX);
        double doubleValueValue = position.y - MathHelper.clamp(position.y, box.minY, box.maxY);
        double doubleValueResult = position.z - MathHelper.clamp(position.z, box.minZ, box.maxZ);
        return Math.sqrt(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult);
    }

    private float calculateFloat(float floatValue) {
        float clampedValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
        return clampedValue * clampedValue * (3.0F - 2.0F * clampedValue);
    }
}
