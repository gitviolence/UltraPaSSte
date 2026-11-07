package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.TargetPlayerEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.utils.player.InventorySearchUtil;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.ExplosionImpl;

public final class FakePlayer extends Module {

    public static final FakePlayer INSTANCE = new FakePlayer();

    private final BooleanSetting copyInventory = new BooleanSetting("Копировать инвентарь", false);

    private final TextSetting playerName = new TextSetting("Имя", "Рэйни");

    public static OtherClientPlayerEntity fakePlayer;

    private int cachedCount;

    public FakePlayer() {
        super("FakePlayer", "Спавнит фейкового игрока для тестирования", ModuleCategory.MISC);
        this.addSettings(this.copyInventory, this.playerName);
    }

    @Override
    public void onEnable() {
        if (mc.world != null && mc.player != null) {
            fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), brova.toshiba.client.utils.runtime.AccessGuard.invoke(this.playerName, new Object[] {}, (accessReceiver, accessArguments) -> ((TextSetting) accessReceiver).get())));
            fakePlayer.copyPositionAndRotation(mc.player);
            if (this.copyInventory.isState()) {
                fakePlayer.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack().copy());
                fakePlayer.setStackInHand(Hand.OFF_HAND, mc.player.getOffHandStack().copy());
                fakePlayer.getInventory().setStack(36, mc.player.getInventory().getStack(36).copy());
                fakePlayer.getInventory().setStack(37, mc.player.getInventory().getStack(37).copy());
                fakePlayer.getInventory().setStack(38, mc.player.getInventory().getStack(38).copy());
                fakePlayer.getInventory().setStack(39, mc.player.getInventory().getStack(39).copy());
            }
            mc.world.addEntity(fakePlayer);
            fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
            fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
            fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
            super.onEnable();
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((FakePlayer) accessReceiver).toggle();
            });
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof ExplosionS2CPacket explosionS2CPacket && fakePlayer != null && fakePlayer.hurtTime == 0) {
            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            float floatValue = this.calculateDistanceToWithReceivedDamage(explosionS2CPacket.center(), fakePlayer);
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - floatValue);
            if (fakePlayer.isDead()) {
                this.isAddStatusEffectWithOf(fakePlayer);
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && fakePlayer != null) {
            if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.isDead()) {
                this.cachedCount++;
                if (this.cachedCount > 10) {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((FakePlayer) accessReceiver).toggle();
                    });
                }
            }
        }
    }

    @EventHandler
    public void onAttack(TargetPlayerEvent event) {
        if (fakePlayer != null && event.getTarget() == fakePlayer && fakePlayer.hurtTime == 0) {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            if (mc.player.fallDistance > 0.0F || ModuleRegistry.packetCriticals.isEnable()) {
                mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            float floatValue;
            if (ModuleRegistry.aura.isEnable() && ModuleRegistry.aura.getAttackCooldown() >= 0.85) {
                floatValue = InventorySearchUtil.getHitDamage(mc.player.getMainHandStack(), fakePlayer);
            } else {
                floatValue = 1.0F;
            }
            fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - floatValue);
            if (fakePlayer.isDead()) {
                this.isAddStatusEffectWithOf(fakePlayer);
            }
        }
    }

    private boolean isAddStatusEffectWithOf(OtherClientPlayerEntity otherClientPlayerEntity) {
        ItemStack mainHandStack = otherClientPlayerEntity.getMainHandStack();
        ItemStack offHandStack = otherClientPlayerEntity.getOffHandStack();
        boolean flag = false;
        if (mainHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
            mainHandStack.decrement(1);
            flag = true;
        } else if (offHandStack.isOf(Items.TOTEM_OF_UNDYING)) {
            offHandStack.decrement(1);
            flag = true;
        }
        if (flag) {
            otherClientPlayerEntity.setHealth(10.0F);
            otherClientPlayerEntity.clearStatusEffects();
            otherClientPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
            otherClientPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            otherClientPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
            new EntityStatusS2CPacket(otherClientPlayerEntity, (byte) 35).apply((ClientPlayPacketListener) mc.player.networkHandler);
        }
        return flag;
    }

    private float calculateDistanceToWithReceivedDamage(Vec3d position, PlayerEntity player) {
        if (mc.world == null) {
            return 0.0F;
        } else {
            double doubleValue = player.getPos().distanceTo(position);
            if (doubleValue > 12.0) {
                return 0.0F;
            } else {
                double doubleValueValue = ExplosionImpl.calculateReceivedDamage(position, player);
                double doubleValueResult = (1.0 - doubleValue / 12.0) * doubleValueValue;
                double doubleValueItem = (doubleValueResult * doubleValueResult + doubleValueResult) / 2.0 * 7.0 * 12.0 + 1.0;
                Difficulty difficulty = mc.world.getDifficulty();
                if (difficulty == Difficulty.PEACEFUL) {
                    return 0.0F;
                } else {
                    if (difficulty == Difficulty.EASY) {
                        doubleValueItem = Math.min(doubleValueItem / 2.0 + 1.0, doubleValueItem);
                    } else if (difficulty == Difficulty.HARD) {
                        doubleValueItem *= 1.5;
                    }
                    doubleValueItem = DamageUtil.getDamageLeft(player, (float) doubleValueItem, mc.world.getDamageSources().generic(), player.getArmor(), (float) player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
                    if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
                        int intValue = player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1;
                        int intValueValue = 25 - intValue * 5;
                        double doubleValueEntry = doubleValueItem * intValueValue;
                        doubleValueItem = Math.max(doubleValueEntry / 25.0, 0.0);
                    }
                    return (float) doubleValueItem;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null && mc.world != null) {
            mc.world.removeEntity(fakePlayer.getId(), RemovalReason.KILLED);
        }
        fakePlayer = null;
        this.cachedCount = 0;
        super.onDisable();
    }
}
