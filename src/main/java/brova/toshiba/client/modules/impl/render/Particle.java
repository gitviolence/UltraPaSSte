package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.Particle.ParticleInstance;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class Particle extends Module {

    public static Particle INSTANCE = new Particle();

    private static final Identifier bloomTexture = Identifier.of("toshiba", "featrues/particle/bloom.png");

    private static final Identifier starTexture = Identifier.of("toshiba", "featrues/particle/star.png");

    private static final Identifier dollarTexture = Identifier.of("toshiba", "featrues/particle/dollar.png");

    private final ModeSetting particleType = new ModeSetting("Тип частиц", "Звезда", "Звезда", "Доллар", "Свечение", "Кометы");

    private final MultiSetting spawnTrigger = new MultiSetting("Добавлять при", new BooleanSetting("Бездействии", false), new BooleanSetting("Беге", false), new BooleanSetting("Ударе", true), new BooleanSetting("Падении перла", false), new BooleanSetting("Падении трезубца", false), new BooleanSetting("Сносе тотема", true));

    private final FloatSetting count = new FloatSetting("Количество", 10.0F, 2.0F, 40.0F, 1.0F);

    private final BooleanSetting glow = new BooleanSetting("Свечение", true);

    private final ArrayList<ParticleInstance> removeIfParticleParticleInstanceEntries = new ArrayList<>();

    private final ArrayList<ParticleInstance> addAllParticleParticleInstanceEntries = new ArrayList<>();

    private final Random random = new Random();

    public Particle() {
        super("Particle", "Красивые частицы при различных действиях", ModuleCategory.RENDER);
        this.addSettings(this.particleType, this.spawnTrigger, this.count, this.glow);
    }

    @Override
    public void onDisable() {
        synchronized (this.removeIfParticleParticleInstanceEntries) {
            this.removeIfParticleParticleInstanceEntries.clear();
        }
        super.onDisable();
    }

    private Identifier createTextureFromCurrent() {
        String current = this.particleType.getCurrent();
        return switch(current) {
            case "Доллар" ->
                dollarTexture;
            case "Свечение" ->
                bloomTexture;
            default ->
                starTexture;
        };
    }

    private boolean isOfFlooredWithBlockState(Vec3d position) {
        if (mc.world == null) {
            return false;
        } else {
            BlockPos positionValue = BlockPos.ofFloored(position);
            BlockState blockState = mc.world.getBlockState(positionValue);
            return !blockState.isAir() && blockState.isSolidBlock(mc.world, positionValue);
        }
    }

    private float randomFloatBetween(float floatValue, float floatValueValue) {
        return floatValue + this.random.nextFloat() * (floatValueValue - floatValue);
    }

    private boolean isLocalState() {
        return mc.player != null && (mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F);
    }

    @EventHandler
    public void onAttack(TargetPlayerEvent event) {
        if (this.isEnable() && mc.player != null && this.spawnTrigger.is("Ударе")) {
            Entity target = event.getTarget();
            if (target != null) {
                int intValue = (int) this.count.get();
                for (int index = 0; index < intValue; index++) {
                    double doubleValue = target.getX() + this.randomFloatBetween(-0.5F, 0.5F);
                    double doubleValueValue = target.getY() + this.randomFloatBetween(0.2F, target.getHeight());
                    double doubleValueResult = target.getZ() + this.randomFloatBetween(-0.5F, 0.5F);
                    double doubleValueItem = this.randomFloatBetween(0.05F, 0.2F);
                    double doubleValueEntry = Math.toRadians(this.randomFloatBetween(0.0F, 360.0F));
                    Vec3d position = new Vec3d(Math.cos(doubleValueEntry) * doubleValueItem, this.randomFloatBetween(0.05F, 0.25F), Math.sin(doubleValueEntry) * doubleValueItem);
                    long longValue = (long) this.randomFloatBetween(1200.0F, 1800.0F);
                    int themeColor = ColorMathUtil.getThemeColor();
                    this.updateCheckCollision(doubleValue, doubleValueValue, doubleValueResult, position, themeColor, 0.35F, longValue, 2.5F, 0.001);
                }
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (this.isEnable() && mc.world != null && eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof EntityStatusS2CPacket entityStatusS2CPacket && entityStatusS2CPacket.getStatus() == 35 && this.spawnTrigger.is("Сносе тотема")) {
            Entity entity = entityStatusS2CPacket.getEntity(mc.world);
            if (entity != null) {
                int intValue = (int) (this.count.get() * 2.0F);
                for (int index = 0; index < intValue; index++) {
                    double doubleValue = entity.getX() + this.randomFloatBetween(-0.6F, 0.6F);
                    double doubleValueValue = entity.getY() + this.randomFloatBetween(0.2F, entity.getHeight());
                    double doubleValueResult = entity.getZ() + this.randomFloatBetween(-0.6F, 0.6F);
                    double doubleValueItem = this.randomFloatBetween(0.08F, 0.3F);
                    double doubleValueEntry = Math.toRadians(this.randomFloatBetween(0.0F, 360.0F));
                    Vec3d position = new Vec3d(Math.cos(doubleValueEntry) * doubleValueItem, this.randomFloatBetween(0.1F, 0.4F), Math.sin(doubleValueEntry) * doubleValueItem);
                    long longValue = (long) this.randomFloatBetween(1500.0F, 2500.0F);
                    int themeColor = ColorMathUtil.getThemeColor();
                    this.updateCheckCollision(doubleValue, doubleValueValue, doubleValueResult, position, themeColor, 0.4F, longValue, 2.0F, 0.0015);
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.isEnable() && mc.player != null && mc.world != null) {
            int intValue = (int) this.count.get();
            if (this.spawnTrigger.is("Бездействии")) {
                Vec3d position = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getHeight() / 2.0, mc.player.getZ());
                for (int index = 0; index < intValue; index++) {
                    double doubleValue = this.randomFloatBetween(7.0F, 35.0F);
                    double doubleValueValue = Math.toRadians(this.randomFloatBetween(0.0F, 360.0F));
                    double doubleValueResult = this.randomFloatBetween(-7.0F, 25.0F);
                    double doubleValueItem = position.x + Math.cos(doubleValueValue) * doubleValue;
                    double doubleValueEntry = position.y + doubleValueResult;
                    double doubleValueCopy = position.z + Math.sin(doubleValueValue) * doubleValue;
                    Vec3d positionValue = new Vec3d(doubleValueItem, doubleValueEntry, doubleValueCopy);
                    if (!this.isOfFlooredWithBlockState(positionValue)) {
                        long longValue = (long) this.randomFloatBetween(1500.0F, 2000.0F);
                        double doubleValue2 = this.random.nextDouble() < 0.8 ? this.randomFloatBetween(0.015F, 0.03F) : 0.125;
                        double doubleValue3 = Math.toRadians(this.randomFloatBetween(0.0F, 360.0F));
                        Vec3d positionResult = new Vec3d(Math.cos(doubleValue3) * doubleValue2, this.randomFloatBetween((float) (-doubleValue2 * 0.1), (float) (doubleValue2 * 0.1)), Math.sin(doubleValue3) * doubleValue2);
                        int themeColor = ColorMathUtil.getThemeColor();
                        this.updateCheckCollision(doubleValueItem, doubleValueEntry, doubleValueCopy, positionResult, themeColor, 0.3F, longValue, 3.0F, 5.0E-5F);
                    }
                }
            }
            if (this.spawnTrigger.is("Беге") && this.isLocalState()) {
                Vec3d velocity = mc.player.getVelocity();
                double squareRoot = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                Vec3d positionItem;
                if (squareRoot < 0.01) {
                    positionItem = mc.player.getRotationVector().multiply(-1.0);
                } else if (mc.player.isGliding()) {
                    positionItem = velocity.normalize().multiply(-1.0);
                } else {
                    positionItem = new Vec3d(-velocity.x / squareRoot, 0.0, -velocity.z / squareRoot);
                }
                double doubleValue4 = (mc.player.isGliding() ? 1.2 : 0.5) + (squareRoot > 0.1 ? squareRoot * 1.5 : 0.0);
                double doubleValue5 = this.randomFloatBetween(-0.35F, 0.35F);
                double doubleValue6 = this.randomFloatBetween(-0.35F, 0.35F);
                double doubleValue7 = mc.player.getX() + positionItem.x * doubleValue4 + doubleValue5;
                double doubleValue8 = mc.player.isGliding() ? mc.player.getY() + mc.player.getHeight() / 2.0 + positionItem.y * doubleValue4 + this.randomFloatBetween(-0.35F, 0.35F) : mc.player.getY() + this.randomFloatBetween(0.2F, mc.player.getHeight() + 0.1F);
                double doubleValue9 = mc.player.getZ() + positionItem.z * doubleValue4 + doubleValue6;
                if (!this.isOfFlooredWithBlockState(new Vec3d(doubleValue7, doubleValue8, doubleValue9))) {
                    double doubleValue10 = 0.075;
                    Vec3d positionEntry = positionItem.multiply(doubleValue10).add(this.randomFloatBetween(-0.01F, 0.01F), this.randomFloatBetween(-0.05F, 0.01F), this.randomFloatBetween(-0.01F, 0.01F)).multiply(0.1);
                    long longValueValue = (long) this.randomFloatBetween(1500.0F, 2000.0F);
                    int themeColorValue = ColorMathUtil.getThemeColor();
                    this.updateCheckCollision(doubleValue7, doubleValue8, doubleValue9, positionEntry, themeColorValue, 0.3F, longValueValue, 3.0F, 5.0E-5F);
                }
            }
            boolean flag = this.spawnTrigger.is("Падении перла");
            boolean flagValue = this.spawnTrigger.is("Падении трезубца");
            if (flag || flagValue) {
                Box box = mc.player.getBoundingBox().expand(100.0);
                for (Entity entity : mc.world.getOtherEntities(null, box, input -> true)) {
                    if (flag && entity instanceof EnderPearlEntity enderPearlEntity && !enderPearlEntity.isOnGround()) {
                        this.updateThemeColor(enderPearlEntity.getPos(), 1);
                    }
                    if (flagValue && entity instanceof TridentEntity tridentEntity && tridentEntity.getVelocity().lengthSquared() > 0.01) {
                        this.updateThemeColor(tridentEntity.getPos(), 1);
                    }
                }
            }
        }
    }

    private void updateThemeColor(Vec3d position, int intValue) {
        for (int index = 0; index < intValue; index++) {
            double doubleValue = position.x + this.randomFloatBetween(-0.2F, 0.2F);
            double doubleValueValue = position.y + this.randomFloatBetween(-0.2F, 0.2F);
            double doubleValueResult = position.z + this.randomFloatBetween(-0.2F, 0.2F);
            Vec3d positionValue = new Vec3d(this.randomFloatBetween(-0.02F, 0.02F), this.randomFloatBetween(-0.02F, 0.02F), this.randomFloatBetween(-0.02F, 0.02F));
            long longValue = (long) this.randomFloatBetween(800.0F, 1400.0F);
            int themeColor = ColorMathUtil.getThemeColor();
            this.updateCheckCollision(doubleValue, doubleValueValue, doubleValueResult, positionValue, themeColor, 0.25F, longValue, 3.0F, 5.0E-4);
        }
    }

    private void updateCheckCollision(double doubleValue, double doubleValueValue, double doubleValueResult, Vec3d position, int intValue, float floatValue, long longValue, float floatValueValue, double doubleValueItem) {
        if (ParticleInstance.checkCollision(doubleValue, doubleValueValue, doubleValueResult, floatValue, mc)) {
            synchronized (this.removeIfParticleParticleInstanceEntries) {
                this.removeIfParticleParticleInstanceEntries.add(new ParticleInstance(doubleValue, doubleValueValue, doubleValueResult, position, intValue, floatValue, longValue, floatValueValue, doubleValueItem));
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (this.isEnable() && mc.player != null && mc.world != null) {
            long currentTimeMillis = System.currentTimeMillis();
            synchronized (this.removeIfParticleParticleInstanceEntries) {
                this.removeIfParticleParticleInstanceEntries.removeIf(input -> input.isDead(currentTimeMillis));
            }
            if (!this.removeIfParticleParticleInstanceEntries.isEmpty()) {
                MatrixStack matrices = event.getMatrices();
                Vec3d pos = event.getCamera().getPos();
                boolean flag = this.particleType.is("Кометы");
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                if (!flag && !this.glow.isState()) {
                    RenderSystem.defaultBlendFunc();
                } else {
                    RenderSystem.blendFunc(770, 1);
                }
                synchronized (this.removeIfParticleParticleInstanceEntries) {
                    this.addAllParticleParticleInstanceEntries.clear();
                    this.addAllParticleParticleInstanceEntries.addAll(this.removeIfParticleParticleInstanceEntries);
                }
                Quaternionf quaternion = RotationAxis.POSITIVE_Y.rotationDegrees(-event.getCamera().getYaw());
                Quaternionf quaternionValue = RotationAxis.POSITIVE_X.rotationDegrees(event.getCamera().getPitch());
                long currentTimeNanos = System.nanoTime();
                for (ParticleInstance particleInstance : this.addAllParticleParticleInstanceEntries) {
                    particleInstance.update(mc, currentTimeMillis, currentTimeNanos, flag);
                }
                RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                if (flag) {
                    this.renderShaderWithShaderTexture(matrices, pos, quaternion, quaternionValue, currentTimeMillis);
                } else {
                    RenderSystem.setShaderTexture(0, this.createTextureFromCurrent());
                    BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                    boolean flagValue = false;
                    for (ParticleInstance particleInstanceValue : this.addAllParticleParticleInstanceEntries) {
                        flagValue |= isColorWithTexture(buffer, matrices, pos, quaternion, quaternionValue, new Vec3d(particleInstanceValue.positionX, particleInstanceValue.positionY, particleInstanceValue.positionZ), particleInstanceValue.value, particleInstanceValue.cachedCount, particleInstanceValue.cachedScalar);
                    }
                    if (flagValue) {
                        BufferRenderer.drawWithGlobalProgram(buffer.end());
                    }
                }
                RenderSystem.enableCull();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        }
    }

    private void renderShaderWithShaderTexture(MatrixStack matrices, Vec3d position, Quaternionf quaternion, Quaternionf quaternionValue, long longValue) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, bloomTexture);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        boolean flag = false;
        for (ParticleInstance particleInstance : this.addAllParticleParticleInstanceEntries) {
            Vec3d positionValue = new Vec3d(particleInstance.positionX, particleInstance.positionY, particleInstance.positionZ);
            float clampedValue = MathHelper.clamp(particleInstance.value * 0.82F, 0.14F, 0.38F);
            flag |= isColorWithTexture(buffer, matrices, position, quaternion, quaternionValue, positionValue, clampedValue * 2.75F, particleInstance.cachedCount, particleInstance.cachedScalar * 0.24F);
            flag |= isColorWithTexture(buffer, matrices, position, quaternion, quaternionValue, positionValue, clampedValue * 1.55F, particleInstance.cachedCount, particleInstance.cachedScalar * 0.58F);
            int size = particleInstance.vec3dEntries.size();
            if (size < 2) {
                Vec3d positionResult = particleInstance.createLengthSquaredWithSubtract(positionValue, clampedValue);
                flag |= isColorWithTexture(buffer, matrices, position, quaternion, quaternionValue, positionResult, clampedValue * 0.28F, particleInstance.cachedCount, particleInstance.cachedScalar * 0.22F);
            }
            for (int index = 1; index < size; index++) {
                float floatValue = (float) index / Math.max(size - 1, 1);
                float floatValueValue = 1.0F - floatValue;
                float floatValueResult = clampedValue * (0.25F + floatValueValue * 0.68F);
                float floatValueItem = particleInstance.cachedScalar * (float) Math.pow(floatValueValue, 1.45) * 0.48F;
                flag |= isColorWithTexture(buffer, matrices, position, quaternion, quaternionValue, particleInstance.vec3dEntries.get(index), floatValueResult, particleInstance.cachedCount, floatValueItem);
            }
        }
        if (flag) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, bloomTexture);
        BufferBuilder bufferValue = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        boolean flagValue = false;
        for (ParticleInstance particleInstanceValue : this.addAllParticleParticleInstanceEntries) {
            Vec3d positionItem = new Vec3d(particleInstanceValue.positionX, particleInstanceValue.positionY, particleInstanceValue.positionZ);
            float clampedValueValue = MathHelper.clamp(particleInstanceValue.value * 0.82F, 0.14F, 0.38F);
            int intValue = calculateInteger(particleInstanceValue.cachedCount, 0.72F);
            flagValue |= isColorWithTexture(bufferValue, matrices, position, quaternion, quaternionValue, positionItem, clampedValueValue * 0.92F, intValue, particleInstanceValue.cachedScalar * 0.92F);
            flagValue |= isColorWithTexture(bufferValue, matrices, position, quaternion, quaternionValue, positionItem, clampedValueValue * 0.38F, -1, particleInstanceValue.cachedScalar);
            int sizeValue = particleInstanceValue.vec3dEntries.size();
            if (sizeValue < 2) {
                Vec3d positionEntry = particleInstanceValue.createLengthSquaredWithSubtract(positionItem, clampedValueValue);
                flagValue |= isColorWithTexture(bufferValue, matrices, position, quaternion, quaternionValue, positionEntry, clampedValueValue * 0.14F, intValue, particleInstanceValue.cachedScalar * 0.32F);
            }
            for (byte indexValue = 4; indexValue < sizeValue; indexValue = (byte) (indexValue + 4)) {
                Vec3d positionCopy = particleInstanceValue.vec3dEntries.get(indexValue);
                double doubleValue = positiveFraction(longValue * 4.2E-4 + particleInstanceValue.cos + indexValue * 0.173);
                double doubleValueValue = Math.sin(longValue * 0.0021 + particleInstanceValue.cos * 11.0 + indexValue) * 0.09 * doubleValue;
                double doubleValueResult = Math.cos(longValue * 0.0017 + particleInstanceValue.cos * 7.0 + indexValue) * 0.07 * doubleValue;
                Vec3d position2 = positionCopy.add(doubleValueValue, -doubleValue * (0.38 + indexValue * 0.018), doubleValueResult);
                float floatValueEntry = 1.0F - (float) indexValue / Math.max(sizeValue, 1) * 0.45F;
                float floatValueCopy = particleInstanceValue.cachedScalar * (float) (1.0 - doubleValue) * floatValueEntry * 0.72F;
                float floatValue2 = clampedValueValue * (0.12F + (float) (1.0 - doubleValue) * 0.1F);
                flagValue |= isColorWithTexture(bufferValue, matrices, position, quaternion, quaternionValue, position2, floatValue2, intValue, floatValueCopy);
            }
        }
        if (flagValue) {
            BufferRenderer.drawWithGlobalProgram(bufferValue.end());
        }
    }

    private static boolean isColorWithTexture(BufferBuilder buffer, MatrixStack matrices, Vec3d position, Quaternionf quaternion, Quaternionf quaternionValue, Vec3d positionValue, float floatValue, int intValue, float floatValueValue) {
        if (!(floatValueValue <= 0.001F) && !(floatValue <= 0.001F)) {
            matrices.push();
            matrices.translate(positionValue.x - position.x, positionValue.y - position.y, positionValue.z - position.z);
            matrices.multiply(quaternion);
            matrices.multiply(quaternionValue);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            float floatValueResult = floatValue * 0.5F;
            int clampedValue = MathHelper.clamp((int) (floatValueValue * 255.0F), 0, 255);
            int intValueValue = intValue >> 16 & 0xFF;
            int intValueResult = intValue >> 8 & 0xFF;
            int intValueItem = intValue & 0xFF;
            buffer.vertex(positionMatrix, -floatValueResult, -floatValueResult, 0.0F).texture(0.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, clampedValue);
            buffer.vertex(positionMatrix, -floatValueResult, floatValueResult, 0.0F).texture(0.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, clampedValue);
            buffer.vertex(positionMatrix, floatValueResult, floatValueResult, 0.0F).texture(1.0F, 0.0F).color(intValueValue, intValueResult, intValueItem, clampedValue);
            buffer.vertex(positionMatrix, floatValueResult, -floatValueResult, 0.0F).texture(1.0F, 1.0F).color(intValueValue, intValueResult, intValueItem, clampedValue);
            matrices.pop();
            return true;
        } else {
            return false;
        }
    }

    private static int calculateInteger(int intValue, float floatValue) {
        int intValueValue = intValue >> 24 & 0xFF;
        int intValueResult = intValue >> 16 & 0xFF;
        int intValueItem = intValue >> 8 & 0xFF;
        int intValueEntry = intValue & 0xFF;
        intValueResult = Math.min(255, (int) (intValueResult + (255 - intValueResult) * floatValue));
        intValueItem = Math.min(255, (int) (intValueItem + (255 - intValueItem) * floatValue));
        intValueEntry = Math.min(255, (int) (intValueEntry + (255 - intValueEntry) * floatValue));
        return intValueValue << 24 | intValueResult << 16 | intValueItem << 8 | intValueEntry;
    }

    public static double positiveFraction(double doubleValue) {
        double doubleValueValue = doubleValue - Math.floor(doubleValue);
        return doubleValueValue < 0.0 ? doubleValueValue + 1.0 : doubleValueValue;
    }

    public static class ParticleInstance {

        public double positionX;

        public double positionY;

        public double positionZ;

        public double x;

        public double y;

        public double z;

        public int cachedCount;

        public float value;

        public long lastUpdateTimestampForDead;

        public long lastUpdateMillis;

        public float cachedScalar = 1.0F;

        public float pow;

        public long lastUpdateNanos;

        public double cachedCoordinate;

        public final ArrayList<Vec3d> vec3dEntries = new ArrayList<>(22);

        public final double cos;

        public long lastUpdateTimestampForUpdateSquaredDistanceTo;

        public boolean stateFlag;

        private final Mutable mutable = new Mutable();

        public ParticleInstance(double positionX, double positionY, double positionZ, Vec3d position, int cachedCount, float value, long lastUpdateTimestampForDead, float pow, double cachedCoordinate) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.positionZ = positionZ;
            this.x = position.x;
            this.y = position.y;
            this.z = position.z;
            this.cachedCount = cachedCount;
            this.value = value;
            this.lastUpdateTimestampForDead = lastUpdateTimestampForDead;
            this.lastUpdateMillis = System.currentTimeMillis();
            this.lastUpdateNanos = System.nanoTime();
            this.lastUpdateTimestampForUpdateSquaredDistanceTo = this.lastUpdateNanos;
            this.pow = pow;
            this.cachedCoordinate = cachedCoordinate;
            this.cos = Particle.positiveFraction(positionX * 0.173 + positionY * 0.317 + positionZ * 0.619 + this.lastUpdateMillis * 1.0E-4);
            this.vec3dEntries.add(new Vec3d(positionX, positionY, positionZ));
        }

        public boolean isDead(long longValue) {
            return longValue - this.lastUpdateMillis >= this.lastUpdateTimestampForDead;
        }

        public void update(MinecraftClient client, long longValue, long lastUpdateNanos, boolean flag) {
            double doubleValue = Math.min((lastUpdateNanos - this.lastUpdateNanos) / 1.0E9, 0.05);
            this.lastUpdateNanos = lastUpdateNanos;
            float floatValue = Math.min(1.0F, (float) (longValue - this.lastUpdateMillis) / (float) this.lastUpdateTimestampForDead);
            double doubleValueValue = Math.pow(1.0 - floatValue, this.pow);
            double x = this.x;
            double y = this.y;
            double z = this.z;
            double positionX = this.positionX;
            double positionY = this.positionY;
            double positionZ = this.positionZ;
            if (flag) {
                double doubleValueResult = (longValue - this.lastUpdateMillis) * 0.001;
                double doubleValueItem = doubleValue * 60.0;
                positionX += Math.sin(doubleValueResult * 1.35 + this.cos * (float) (Math.PI * 2)) * 7.0E-4 * doubleValueItem;
                positionY += Math.cos(doubleValueResult * 1.1 + this.cos * 5.0) * 4.5E-4 * doubleValueItem;
                positionZ += Math.cos(doubleValueResult * 1.25 + this.cos * (float) (Math.PI * 2)) * 7.0E-4 * doubleValueItem;
            }
            positionX += x * doubleValueValue * (doubleValue * 60.0);
            if (!isBlockStateWithAir(positionX, this.positionY, this.positionZ, this.value, client, this.mutable)) {
                x = -x * 0.8;
                positionX = this.positionX;
            }
            positionY += y * doubleValueValue * (doubleValue * 60.0);
            if (!isBlockStateWithAir(positionX, positionY, this.positionZ, this.value, client, this.mutable)) {
                y = -y * 1.5;
                positionY = this.positionY;
            }
            positionZ += z * doubleValueValue * (doubleValue * 60.0);
            if (!isBlockStateWithAir(positionX, positionY, positionZ, this.value, client, this.mutable)) {
                z = -z * 0.8;
                positionZ = this.positionZ;
            }
            this.positionX = positionX;
            this.positionY = positionY;
            this.positionZ = positionZ;
            this.x = x * 0.9999;
            this.y = y * 0.9999 - this.cachedCoordinate;
            this.z = z * 0.9999;
            this.cachedScalar = 1.0F - floatValue;
            this.updateSquaredDistanceTo(lastUpdateNanos, flag);
        }

        private void updateSquaredDistanceTo(long lastUpdateTimestampForUpdateSquaredDistanceTo, boolean flag) {
            if (!flag) {
                if (this.stateFlag) {
                    this.vec3dEntries.clear();
                    this.vec3dEntries.add(new Vec3d(this.positionX, this.positionY, this.positionZ));
                }
                this.stateFlag = false;
                this.lastUpdateTimestampForUpdateSquaredDistanceTo = lastUpdateTimestampForUpdateSquaredDistanceTo;
            } else if (!this.stateFlag) {
                this.vec3dEntries.clear();
                this.vec3dEntries.add(new Vec3d(this.positionX, this.positionY, this.positionZ));
                this.lastUpdateTimestampForUpdateSquaredDistanceTo = lastUpdateTimestampForUpdateSquaredDistanceTo;
                this.stateFlag = true;
            } else {
                Vec3d position = this.vec3dEntries.isEmpty() ? null : this.vec3dEntries.get(0);
                double doubleValue = position == null ? Double.MAX_VALUE : position.squaredDistanceTo(this.positionX, this.positionY, this.positionZ);
                if (lastUpdateTimestampForUpdateSquaredDistanceTo - this.lastUpdateTimestampForUpdateSquaredDistanceTo >= 22000000L || doubleValue >= 0.0036) {
                    this.vec3dEntries.add(0, new Vec3d(this.positionX, this.positionY, this.positionZ));
                    while (this.vec3dEntries.size() > 22) {
                        this.vec3dEntries.remove(this.vec3dEntries.size() - 1);
                    }
                    this.lastUpdateTimestampForUpdateSquaredDistanceTo = lastUpdateTimestampForUpdateSquaredDistanceTo;
                }
            }
        }

        Vec3d createLengthSquaredWithSubtract(Vec3d position, float floatValue) {
            Vec3d positionValue = new Vec3d(this.x, this.y, this.z);
            if (positionValue.lengthSquared() < 1.0E-6) {
                positionValue = new Vec3d(0.0, -0.1, 0.0);
            }
            double doubleValue = Math.max(0.08, floatValue * 1.35);
            return position.subtract(positionValue.normalize().multiply(doubleValue));
        }

        public static boolean checkCollision(double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, MinecraftClient client) {
            return isBlockStateWithAir(doubleValue, doubleValueValue, doubleValueResult, floatValue, client, new Mutable());
        }

        private static boolean isBlockStateWithAir(double doubleValue, double doubleValueValue, double doubleValueResult, float floatValue, MinecraftClient client, Mutable mutablePosition) {
            if (client.world == null) {
                return false;
            } else {
                double doubleValueItem = floatValue * 0.5;
                int roundedDown = MathHelper.floor(doubleValue - doubleValueItem);
                int roundedDownValue = MathHelper.floor(doubleValue + doubleValueItem);
                int roundedDownResult = MathHelper.floor(doubleValueValue - doubleValueItem);
                int roundedDownItem = MathHelper.floor(doubleValueValue + doubleValueItem);
                int roundedDownEntry = MathHelper.floor(doubleValueResult - doubleValueItem);
                int roundedDownCopy = MathHelper.floor(doubleValueResult + doubleValueItem);
                for (int index = roundedDown; index <= roundedDownValue; index++) {
                    for (int indexValue = roundedDownResult; indexValue <= roundedDownItem; indexValue++) {
                        for (int indexResult = roundedDownEntry; indexResult <= roundedDownCopy; indexResult++) {
                            mutablePosition.set(index, indexValue, indexResult);
                            BlockState blockState = client.world.getBlockState(mutablePosition);
                            if (!blockState.isAir() && blockState.isSolidBlock(client.world, mutablePosition)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
    }
}
