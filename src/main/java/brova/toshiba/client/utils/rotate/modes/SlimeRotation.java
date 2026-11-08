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

    // Офсет точки прицела от ног цели: точка жёстко привязана к телу (никакой интеграции
    // дрейфов) и чуть опережает тело по скорости — стрейфы ведём с полутиковым упреждением.
    private Vec3d aimOffset;

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
        this.aimOffset = null;
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
            boolean attackWindow = mc.player.getAttackCooldownProgress(0.0F) > 0.85F;
            // Мощная доводка: без моторной инерции — шаг прямо пропорционален ошибке. Коэффициент
            // 0.92–1.0 (рероллится по циклу силы как «человеческая» вариация темпа; в окне готового
            // КД строго 1.0) → схождение за 1–2 тика, к каждому удару прицел уже на точке.
            // Капы 46–64°/тик yaw и до ~28°/тик pitch: разворот на 180° закрывается за 3 тика,
            // стрейфы цели держится напрямую, в бою кэп фактически не упирается.
            float yawStepFactor = attackWindow ? 1.0F : 0.92F + 0.08F * strengthFactor;
            float pitchStepFactor = attackWindow ? 1.0F : 0.90F + 0.10F * strengthFactor;
            float yawCap = 46.0F + 18.0F * strengthFactor;
            float pitchCap = yawCap * 0.44F;
            // Джиттер пропорционален дистанции до точки: далеко — лёгкое покачивание, в точке —
            // тишина. Промахов не вызывает, сигнатура движения — не «робот-шаг».
            float yawNoise = (float) this.threadLocalRandom.nextGaussian() * Math.min(magnitude, 1.5F) * 0.12F;
            float pitchNoise = (float) this.threadLocalRandom.nextGaussian() * Math.min(magnitudeValue, 1.0F) * 0.12F;
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
            if (this.aimEntity != entity || this.aimOffset == null || !this.rayOnTarget) {
                // Новая цель, сброс или потерянный луч — точка собирается заново (с инсетом в тело):
                // запоминаем офсет от ног цели — дальше точка жёстко едет вместе с телом.
                this.aimOffset = this.createInsetAimPoint(entity).subtract(entity.getPos());
                this.aimEntity = entity;
            }
            // Точка привязана к телу (офсет от ног) + лёгкое упреждение по скорости (0.3 тика):
            // по стоящей цели крест мёртв в одной точке, по стрейфящейся ведётся с опережением.
            this.cachedAimPoint = entity.getPos().add(this.aimOffset).add(entity.getVelocity().multiply(0.3));
            position = this.cachedAimPoint;
        }
        if (this.shouldUseElytraPredict(entity)) {
            position = this.getPredictedPoint(entity, position);
        }
        return RotationUtil.getRotations(position);
    }

    private Vec3d createInsetAimPoint(LivingEntity entity) {
        // Мультиточка затягивается на 50% к центру хитбокса: точка сидит глубоко в торсе с
        // максимальным запасом луча до краёв — GCD-уровни и лаг-комп не сводят свинг «в пустоту»,
        // зрительно прицел всё равно на теле цели.
        Box box = entity.getBoundingBox();
        Vec3d position = RotationPointUtil.getMultipoint(entity, 128.0);
        Vec3d center = box.getCenter();
        return new Vec3d(MathHelper.lerp(0.5, position.x, center.x), MathHelper.lerp(0.5, position.y, center.y), MathHelper.lerp(0.5, position.z, center.z));
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
