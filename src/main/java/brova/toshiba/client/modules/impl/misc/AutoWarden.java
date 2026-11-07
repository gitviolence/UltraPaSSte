package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.AutoWarden.FarmState;
import brova.toshiba.client.modules.impl.render.WardenEsp;
import brova.toshiba.client.modules.impl.render.WardenEsp.WardenLocation;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.text.ChatUtil;
import brova.toshiba.mixin.BossBarHudAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class AutoWarden extends Module {

    public static final AutoWarden INSTANCE = new AutoWarden();

    public static final BooleanSetting clientLook = new BooleanSetting("Наводка от первого лица", false);

    private final BooleanSetting drinkSpeed = new BooleanSetting("Пить скорость", true);

    private final BooleanSetting drinkInvisibility = new BooleanSetting("Пить невидимость", true);

    private final ModeSetting junkItem = new ModeSetting("Приоритеты лута", "Средний", "Низкий", "Средний", "Высокий");

    private int mainAnarchy = 304;

    private final List<Integer> indexOfIntegerEntries = new ArrayList<>();

    private int lootPriorityIndex = 0;

    private int lastScreenSyncId = -1;

    private FarmState autoWardenFarmState = FarmState.IDLE;

    private long lastLootActionMillis = 0L;

    private BlockPos equalsBlockPos = null;

    private long lastRepairActionMillis = 0L;

    private long lastPotionUseMillis = 0L;

    private boolean waitingForInteraction = false;

    private boolean drinkingSpeedPotion = false;

    private int speedDrinkTicks = 0;

    private int slotBeforeSpeedPotion = -1;

    private boolean drinkingInvisibilityPotion = false;

    private int invisibilityDrinkTicks = 0;

    private int slotBeforeInvisibilityPotion = -1;

    private long lastSellActionMillis = 0L;

    private long lastTeleportActionMillis = 0L;

    private long lastSupplyCheckMillis = 0L;

    private boolean stateFlagForDisable = false;

    private final Map<BlockPos, Integer> orDefaultIntegerByBlockPos = new HashMap<>();

    private final Map<BlockPos, Integer> keySetIntegerByBlockPos = new HashMap<>();

    private final Set<BlockPos> removeIfBlockPosEntries = new HashSet<>();

    private BlockPos toCenterPosBlockPos = null;

    private BlockPos blockPos = null;

    public AutoWarden() {
        super("AutoWarden", "Автоматически лутает сундуки Вардена и меняет анархии", ModuleCategory.MISC);
        this.addSettings(clientLook, this.drinkSpeed, this.drinkInvisibility, this.junkItem);
    }

    @Override
    public void onEnable() {
        int lastScreenSyncId = this.calculateScoreboardWithObjectiveForSlot();
        if (lastScreenSyncId != this.lastScreenSyncId) {
            this.orDefaultIntegerByBlockPos.clear();
            this.keySetIntegerByBlockPos.clear();
            this.lastScreenSyncId = lastScreenSyncId;
        }
        this.removeIfBlockPosEntries.clear();
        this.blockPos = null;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.toCenterPosBlockPos = (BlockPos) accessValue;
        });
        if (this.indexOfIntegerEntries.contains(lastScreenSyncId)) {
            this.lootPriorityIndex = this.indexOfIntegerEntries.indexOf(lastScreenSyncId);
            this.autoWardenFarmState = FarmState.TELEPORT_HOME;
        } else if (lastScreenSyncId == this.mainAnarchy) {
            this.autoWardenFarmState = this.isWardenEventActive() ? FarmState.FARM_WARDEN : FarmState.BASE_RESTOCK;
        } else {
            this.autoWardenFarmState = FarmState.SWITCH_TO_BASE;
        }
        this.lastLootActionMillis = System.currentTimeMillis();
        value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.equalsBlockPos = (BlockPos) accessValue;
        });
        this.lastRepairActionMillis = 0L;
        this.waitingForInteraction = false;
        this.drinkingSpeedPotion = false;
        this.slotBeforeSpeedPotion = -1;
        this.drinkingInvisibilityPotion = false;
        this.slotBeforeInvisibilityPotion = -1;
        this.lastSellActionMillis = 0L;
        this.lastTeleportActionMillis = 0L;
        this.lastSupplyCheckMillis = 0L;
        WardenEsp wardenEsp = ModuleRegistry.wardenEsp;
        if (wardenEsp != null && !wardenEsp.isEnable()) {
            wardenEsp.setEnabled(true);
        }
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatMessage("#setting sprint true");
            mc.player.networkHandler.sendChatMessage("#setting assumeWalk false");
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.updateChatMessage();
        if (this.stateFlagForDisable && mc.options != null) {
            mc.options.sneakKey.setPressed(false);
            this.stateFlagForDisable = false;
        }
        WardenEsp wardenEsp = ModuleRegistry.wardenEsp;
        if (wardenEsp != null && wardenEsp.isEnable()) {
            wardenEsp.setEnabled(false);
        }
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.equalsBlockPos = (BlockPos) accessValue;
        });
        this.autoWardenFarmState = FarmState.IDLE;
        if (this.drinkingSpeedPotion && mc.options != null) {
            mc.options.useKey.setPressed(false);
            if (this.slotBeforeSpeedPotion != -1 && mc.player != null) {
                mc.player.getInventory().selectedSlot = this.slotBeforeSpeedPotion;
            }
        }
        if (this.drinkingInvisibilityPotion && mc.options != null) {
            mc.options.useKey.setPressed(false);
            if (this.slotBeforeInvisibilityPotion != -1 && mc.player != null) {
                mc.player.getInventory().selectedSlot = this.slotBeforeInvisibilityPotion;
            }
        }
        this.drinkingSpeedPotion = false;
        this.slotBeforeSpeedPotion = -1;
        this.drinkingInvisibilityPotion = false;
        this.slotBeforeInvisibilityPotion = -1;
        this.removeIfBlockPosEntries.clear();
        value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.blockPos = (BlockPos) accessValue;
        });
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime >= 5) {
                mc.player.requestRespawn();
                this.updateCurrentTimeMillis();
            } else {
                this.updateEntitiesByClassWithExpand();
                if (this.isEntitiesWithAlive()) {
                    if (!this.stateFlagForDisable) {
                        this.stateFlagForDisable = true;
                        this.updateChatMessage();
                    }
                    mc.options.sneakKey.setPressed(true);
                } else {
                    if (this.stateFlagForDisable) {
                        this.stateFlagForDisable = false;
                        mc.options.sneakKey.setPressed(false);
                    }
                    if (this.drinkingSpeedPotion) {
                        this.speedDrinkTicks++;
                        if (this.needsSpeedPotion() && this.speedDrinkTicks <= 40 && this.isItemWithEffects(mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot))) {
                            mc.options.useKey.setPressed(true);
                            if (!mc.player.isUsingItem()) {
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            }
                        } else {
                            this.drinkingSpeedPotion = false;
                            mc.options.useKey.setPressed(false);
                            if (this.slotBeforeSpeedPotion != -1) {
                                mc.player.getInventory().selectedSlot = this.slotBeforeSpeedPotion;
                                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.slotBeforeSpeedPotion));
                                this.slotBeforeSpeedPotion = -1;
                            }
                        }
                    } else if (this.drinkingInvisibilityPotion) {
                        this.invisibilityDrinkTicks++;
                        if (this.needsInvisibilityPotion() && this.invisibilityDrinkTicks <= 40 && this.isItemWithEquals(mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot))) {
                            mc.options.useKey.setPressed(true);
                            if (!mc.player.isUsingItem()) {
                                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            }
                        } else {
                            this.drinkingInvisibilityPotion = false;
                            mc.options.useKey.setPressed(false);
                            if (this.slotBeforeInvisibilityPotion != -1) {
                                mc.player.getInventory().selectedSlot = this.slotBeforeInvisibilityPotion;
                                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.slotBeforeInvisibilityPotion));
                                this.slotBeforeInvisibilityPotion = -1;
                            }
                        }
                    } else {
                        int intValue = this.calculateScoreboardWithObjectiveForSlot();
                        if (intValue != -1 && (intValue != this.mainAnarchy || this.isWardenEventActive())) {
                            if (this.drinkSpeed.isState() && this.needsSpeedPotion()) {
                                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                                    ((AutoWarden) accessReceiver).drinkSpeedPotion();
                                });
                            }
                            if (this.drinkInvisibility.isState() && this.needsInvisibilityPotion() && !this.drinkingSpeedPotion) {
                                this.drinkInvisibilityPotion();
                            }
                        }
                        switch(this.autoWardenFarmState) {
                            case SWITCH_TO_BASE:
                                if (intValue == this.mainAnarchy) {
                                    this.autoWardenFarmState = this.isWardenEventActive() ? FarmState.TELEPORT_HOME : FarmState.BASE_RESTOCK;
                                    this.lastLootActionMillis = System.currentTimeMillis();
                                } else {
                                    if (System.currentTimeMillis() - this.lastLootActionMillis > 1000L && this.lastLootActionMillis != 0L) {
                                        this.updateChatCommand("an" + this.mainAnarchy);
                                        this.lastTeleportActionMillis = System.currentTimeMillis();
                                        this.lastLootActionMillis = 0L;
                                    }
                                    if (this.lastLootActionMillis == 0L && System.currentTimeMillis() - this.lastTeleportActionMillis > 10000L) {
                                        this.updateChatCommand("an" + this.mainAnarchy);
                                        this.lastTeleportActionMillis = System.currentTimeMillis();
                                    }
                                }
                                break;
                            case BASE_RESTOCK:
                                this.updateCurrentTimeMillisWithStack();
                                break;
                            case SWITCH_TO_ANARCHY:
                                if (this.indexOfIntegerEntries.isEmpty()) {
                                    ChatUtil.sendMessage("Добавьте анархии с помощью команды .warden add anwarden <номер>");
                                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                                        ((AutoWarden) accessReceiver).toggle();
                                    });
                                    return;
                                }
                                int intValueValue = this.indexOfIntegerEntries.get(this.lootPriorityIndex);
                                if (this.isScoreboardWithObjectiveForSlot(intValueValue)) {
                                    FarmState TELEPORT_HOMEValue = FarmState.TELEPORT_HOME;
                                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, TELEPORT_HOMEValue, (accessReceiver, accessValue) -> {
                                        accessReceiver.autoWardenFarmState = accessValue;
                                    });
                                    this.lastLootActionMillis = System.currentTimeMillis();
                                } else {
                                    if (System.currentTimeMillis() - this.lastLootActionMillis > 1000L && this.lastLootActionMillis != 0L) {
                                        this.updateChatCommand("an" + intValueValue);
                                        this.lastTeleportActionMillis = System.currentTimeMillis();
                                        this.lastLootActionMillis = 0L;
                                    }
                                    if (this.lastLootActionMillis == 0L && System.currentTimeMillis() - this.lastTeleportActionMillis > 2000L) {
                                        this.updateChatCommand("an" + intValueValue);
                                        this.lastTeleportActionMillis = System.currentTimeMillis();
                                    }
                                }
                                break;
                            case TELEPORT_HOME:
                                if (this.lastLootActionMillis == 0L) {
                                    if (System.currentTimeMillis() - this.lastTeleportActionMillis > 3000L) {
                                        this.autoWardenFarmState = FarmState.WAIT_TELEPORT;
                                        this.lastLootActionMillis = System.currentTimeMillis();
                                        this.waitingForInteraction = false;
                                    }
                                } else if (System.currentTimeMillis() - this.lastLootActionMillis > 1500L) {
                                    this.updateChatCommand("home home");
                                    this.lastTeleportActionMillis = System.currentTimeMillis();
                                    this.lastLootActionMillis = 0L;
                                }
                                break;
                            case WAIT_TELEPORT:
                                boolean flag = false;
                                for (Object value : ((Map) this.readBossBars()).values()) {
                                    if (value instanceof BossBar bossBar) {
                                        String normalizedText = bossBar.getName().getString().toLowerCase();
                                        if (normalizedText.contains("телепорт") || normalizedText.contains("teleport") || normalizedText.contains("home") || normalizedText.contains("варден") || normalizedText.contains("warden")) {
                                            flag = true;
                                            this.waitingForInteraction = true;
                                        }
                                    }
                                }
                                if (this.waitingForInteraction && !flag || System.currentTimeMillis() - this.lastLootActionMillis > 8000L) {
                                    this.autoWardenFarmState = intValue == this.mainAnarchy ? FarmState.BASE_RESTOCK : FarmState.FARM_WARDEN;
                                    this.lastLootActionMillis = System.currentTimeMillis();
                                    Object valueValue = null;
                                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, valueValue, (accessReceiver, accessValue) -> {
                                        accessReceiver.equalsBlockPos = (BlockPos) accessValue;
                                    });
                                }
                                break;
                            case FARM_WARDEN:
                                if (this.isLocalState()) {
                                    this.autoWardenFarmState = this.isPvpBossbarActive() ? FarmState.ESCAPE : FarmState.SWITCH_TO_BASE;
                                    this.updateChatMessage();
                                    return;
                                }
                                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                                    ((AutoWarden) accessReceiver).updateCurrentTimeMillisWithSquaredDistanceTo();
                                });
                                break;
                            case ESCAPE:
                                if (intValue == this.mainAnarchy) {
                                    FarmState SWITCH_TO_BASEValue = FarmState.SWITCH_TO_BASE;
                                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, SWITCH_TO_BASEValue, (accessReceiver, accessValue) -> {
                                        accessReceiver.autoWardenFarmState = accessValue;
                                    });
                                    return;
                                }
                                if (!this.isPvpBossbarActive() && !this.isPlayersWithSquaredDistanceTo(mc.player.getBlockPos())) {
                                    FarmState SWITCH_TO_BASEResult = FarmState.SWITCH_TO_BASE;
                                    brova.toshiba.client.utils.runtime.AccessGuard.write(this, SWITCH_TO_BASEResult, (accessReceiver, accessValue) -> {
                                        accessReceiver.autoWardenFarmState = accessValue;
                                    });
                                    return;
                                }
                                this.updateToRadiansWithBlockPos();
                        }
                    }
                }
            }
        }
    }

    private void updateCurrentTimeMillisWithSquaredDistanceTo() {
        if (mc.currentScreen instanceof GenericContainerScreen genericContainerScreen) {
            this.updateJunkItem(genericContainerScreen);
        } else {
            BlockPos position = this.createBlockPosFromOfCenterWithSquaredDistanceTo();
            if (position == null) {
                position = this.createBlockPos();
            }
            boolean flag = position != null && this.equalsBlockPos != null && !position.equals(this.equalsBlockPos) && this.calculateCurrentTimeMillis(this.equalsBlockPos) > 25000L;
            if (!flag) {
                this.lastRepairActionMillis = System.currentTimeMillis();
            }
            if (!flag || System.currentTimeMillis() - this.lastRepairActionMillis > 1000L) {
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, position, (accessReceiver, accessValue) -> {
                    accessReceiver.equalsBlockPos = accessValue;
                });
            }
            BlockPos equalsBlockPos = this.equalsBlockPos;
            if (equalsBlockPos == null) {
                if (mc.player.age % 40 == 0) {
                    this.updateCurrentTimeMillis();
                }
            } else {
                long longValue = this.calculateCurrentTimeMillis(equalsBlockPos);
                if (longValue > 1000L && this.isPlayersWithSquaredDistanceTo(equalsBlockPos, 7.0)) {
                    BlockPos positionValue = this.createBlockPosFromUpWithAir(equalsBlockPos);
                    if (positionValue != null) {
                        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(positionValue)) > 2.0) {
                            this.updateChatMessage(positionValue);
                        } else {
                            this.updateChatMessage();
                        }
                    }
                } else if (longValue > 6000L) {
                    this.updateChatMessage(this.createBlockPosFromCosWithSin(equalsBlockPos));
                } else {
                    double doubleValue = mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(equalsBlockPos));
                    if (doubleValue <= 20.0) {
                        int intValue = longValue >= 0L ? 1 : 3;
                        if (this.orDefaultIntegerByBlockPos.getOrDefault(equalsBlockPos, 0) < intValue && this.canSinWithEyePos(equalsBlockPos, longValue >= 0L ? 6 : 1)) {
                            this.orDefaultIntegerByBlockPos.merge(equalsBlockPos, 1, Integer::sum);
                        }
                    } else {
                        if (doubleValue > 10.0) {
                            this.selectHotbarSlot();
                        }
                        BlockPos positionResult = this.createBlockPosFromUpWithAir(equalsBlockPos);
                        if (positionResult != null) {
                            this.updateChatMessage(positionResult);
                        } else {
                            this.updateChatMessage(equalsBlockPos);
                        }
                    }
                }
            }
        }
    }

    private void updateJunkItem(GenericContainerScreen genericContainerScreen) {
        if (this.isHorizontalLengthSquaredWithVelocity()) {
            this.updateChatMessage();
        } else if (mc.player.age % 2 == 0) {
            Slot slot = this.createSlotFromScreenHandlerWithInventory(genericContainerScreen, false, input -> !input.isEmpty() && !this.isJunkItem(input));
            if (slot == null) {
                this.isCloseHandledScreen();
            } else {
                this.updateClickSlotWithScreenHandler(genericContainerScreen, slot, 0, SlotActionType.QUICK_MOVE);
            }
        }
    }

    public boolean isJunkItem(ItemStack stack) {
        return this.junkItem.is("Низкий") ? false : this.isCommonJunkItem(stack) || this.junkItem.is("Высокий") && this.isSecondaryJunkItem(stack);
    }

    private boolean isCommonJunkItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ShovelItem || item instanceof AxeItem || item instanceof BannerItem || item instanceof SmithingTemplateItem && !stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE) || stack.isOf(Items.BLAZE_ROD) || stack.isOf(Items.ENCHANTED_BOOK) || stack.isOf(Items.TRIDENT) || stack.isOf(Items.NAME_TAG) || stack.isOf(Items.SCULK) || stack.isOf(Items.SCULK_SENSOR) || stack.isOf(Items.ENDER_CHEST) || stack.isOf(Items.REINFORCED_DEEPSLATE) || stack.isOf(Items.PUFFERFISH) || stack.isOf(Items.HONEY_BOTTLE) || stack.isOf(Items.FERMENTED_SPIDER_EYE) || stack.isOf(Items.ANVIL) || stack.isOf(Items.COOKED_PORKCHOP);
    }

    private boolean isSecondaryJunkItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArrowItem || item instanceof PickaxeItem || item instanceof AxeItem || stack.isOf(Items.CHORUS_FRUIT) || stack.isOf(Items.DISC_FRAGMENT_5) || stack.isOf(Items.NAUTILUS_SHELL) || stack.isOf(Items.BOOKSHELF) || stack.isOf(Items.COOKED_MUTTON) || stack.isOf(Items.SKELETON_SPAWN_EGG) || stack.isOf(Items.CREEPER_SPAWN_EGG) || stack.isOf(Items.ZOMBIE_SPAWN_EGG) || stack.isOf(Items.VINDICATOR_SPAWN_EGG) || stack.isOf(Items.PIGLIN_SPAWN_EGG) || stack.isOf(Items.FIRE_CHARGE) || stack.isOf(Items.LEATHER) || stack.isOf(Items.SHULKER_SHELL) || stack.isOf(Items.EXPERIENCE_BOTTLE) || stack.isOf(Items.WITHER_ROSE) || stack.isOf(Items.EMERALD) || stack.isOf(Items.SUGAR) || this.isEqualsWithCompound(stack, "potion-popper") || stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE) || stack.isOf(Items.GHAST_TEAR) || stack.isOf(Items.DRAGON_BREATH) || stack.isOf(Items.VEX_SPAWN_EGG) || stack.isOf(Items.ENDERMITE_SPAWN_EGG) || stack.isOf(Items.CAT_SPAWN_EGG) || stack.isOf(Items.ENCHANTING_TABLE) || stack.isOf(Items.DIAMOND_HELMET) || stack.isOf(Items.DIAMOND_CHESTPLATE) || stack.isOf(Items.DIAMOND_LEGGINGS) || stack.isOf(Items.DIAMOND_BOOTS);
    }

    private boolean isEqualsWithCompound(ItemStack stack, String text) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        return nbtComponent != null && text.equals(nbtComponent.copyNbt().getCompound("PublicBuk뜯itValues").getString("minecraft:ftid"));
    }

    private Slot createSlotFromScreenHandlerWithInventory(GenericContainerScreen genericContainerScreen, boolean flag, Predicate<ItemStack> predicate) {
        for (Slot slot : genericContainerScreen.getScreenHandler().slots) {
            if (slot.inventory == mc.player.getInventory() == flag && predicate.test(slot.getStack())) {
                return slot;
            }
        }
        return null;
    }

    private void updateClickSlotWithScreenHandler(GenericContainerScreen genericContainerScreen, Slot slot, int intValue, SlotActionType slotActionType) {
        if (slot != null) {
            mc.interactionManager.clickSlot(genericContainerScreen.getScreenHandler().syncId, slot.id, intValue, slotActionType, mc.player);
        }
    }

    private boolean isCloseHandledScreen() {
        if (mc.currentScreen instanceof GenericContainerScreen && mc.player.age % 2 == 0) {
            mc.player.closeHandledScreen();
        }
        return !(mc.currentScreen instanceof GenericContainerScreen);
    }

    private BlockPos createBlockPosFromOfCenterWithSquaredDistanceTo() {
        BlockPos position = null;
        byte byteValue = 99;
        double MAX_VALUE = Double.MAX_VALUE;
        for (BlockPos positionValue : this.createKeySetWithEmptySet()) {
            if (this.isBlockPos(positionValue) && !this.isPlayersWithSquaredDistanceTo(positionValue)) {
                double doubleValue = mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(positionValue));
                long longValue = this.calculateCurrentTimeMillis(positionValue);
                if ((!this.isKeySetWithSquaredDistance(positionValue) || longValue < 0L && doubleValue <= 16.0) && (longValue >= 0L || this.orDefaultIntegerByBlockPos.getOrDefault(positionValue, 0) < 3)) {
                    byte byteValueValue = -1;
                    if (longValue < 0L && doubleValue <= 25.0) {
                        byteValueValue = 0;
                    } else if (longValue >= 0L && longValue <= 5000L && doubleValue <= 144.0) {
                        byteValueValue = 1;
                    } else if (longValue < 0L && doubleValue <= 144.0) {
                        byteValueValue = 2;
                    } else if (longValue >= 0L && longValue <= 15000L && doubleValue <= 625.0) {
                        byteValueValue = 3;
                    } else if (longValue < 0L) {
                        byteValueValue = 4;
                    }
                    if (byteValueValue >= 0) {
                        double doubleValueValue = Vec3d.ofCenter(positionValue).y + -mc.player.getEyeY();
                        double doubleValueResult = positionValue.getX() + 0.5 + -mc.player.getX();
                        double doubleValueItem = positionValue.getZ() + 0.5 + -mc.player.getZ();
                        double doubleValueEntry = doubleValueResult * doubleValueResult + doubleValueItem * doubleValueItem + (doubleValueValue > 0.0 ? 2.0 : 1.0) * doubleValueValue * doubleValueValue;
                        if (byteValueValue < byteValue || byteValueValue == byteValue && doubleValueEntry < MAX_VALUE) {
                            byteValue = byteValueValue;
                            MAX_VALUE = doubleValueEntry;
                            position = positionValue;
                        }
                    }
                }
            }
        }
        return position;
    }

    private BlockPos createBlockPos() {
        BlockPos position = null;
        long longValue = 45000L;
        for (BlockPos positionValue : this.createKeySetWithEmptySet()) {
            long longValueValue = this.calculateCurrentTimeMillis(positionValue);
            if (longValueValue >= 0L && longValueValue < longValue && this.isBlockPos(positionValue) && !this.isKeySetWithSquaredDistance(positionValue) && !this.isPlayersWithSquaredDistanceTo(positionValue)) {
                longValue = longValueValue;
                position = positionValue;
            }
        }
        return position;
    }

    private Set<BlockPos> createKeySetWithEmptySet() {
        WardenEsp wardenEsp = ModuleRegistry.wardenEsp;
        if (wardenEsp == null) {
            return Collections.emptySet();
        } else {
            HashSet<net.minecraft.util.math.BlockPos> values = new HashSet<>(wardenEsp.foundBlocks.keySet());
            values.addAll(wardenEsp.chestTimers.keySet());
            return values;
        }
    }

    private long calculateCurrentTimeMillis(BlockPos position) {
        WardenEsp wardenEsp = ModuleRegistry.wardenEsp;
        if (wardenEsp == null) {
            return -1L;
        } else {
            WardenLocation wardenLocation = wardenEsp.chestTimers.get(position);
            if (wardenLocation == null) {
                return -1L;
            } else {
                long elapsedMillis = System.currentTimeMillis() - wardenLocation.lastUpdatedMs;
                return wardenLocation.timeRemainingSeconds * 1000L - elapsedMillis;
            }
        }
    }

    private boolean isKeySetWithSquaredDistance(BlockPos position) {
        for (BlockPos positionValue : this.keySetIntegerByBlockPos.keySet()) {
            if (positionValue.getSquaredDistance(position) < 25.0) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayersWithSquaredDistanceTo(BlockPos position) {
        if (mc.world == null) {
            return false;
        } else {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && player.getPos().squaredDistanceTo(Vec3d.ofCenter(position)) < 400.0 && Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).anyMatch(input -> player.getEquippedStack(input).getItem() instanceof ArmorItem)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isPlayersWithSquaredDistanceTo(BlockPos position, double doubleValue) {
        if (mc.world == null) {
            return false;
        } else {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player != mc.player && player.getPos().squaredDistanceTo(Vec3d.ofCenter(position)) < doubleValue * doubleValue) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isBlockPos(BlockPos position) {
        return this.createBlockPosFromUpWithAir(position) != null;
    }

    private BlockPos createBlockPosFromUpWithAir(BlockPos position) {
        for (int index = -1; index <= 1; index++) {
            for (int indexValue = -1; indexValue <= 1; indexValue++) {
                if (index != 0 || indexValue != 0) {
                    BlockPos positionValue = position.add(index, 0, indexValue);
                    if (mc.world.getBlockState(positionValue).isAir() && mc.world.getBlockState(positionValue.up()).isAir() && !mc.world.getBlockState(positionValue.down()).isAir() && this.isOfCenterWithEyeHeight(positionValue, position)) {
                        return positionValue;
                    }
                }
            }
        }
        return mc.world.getBlockState(position.up()).isAir() && mc.world.getBlockState(position.up().up()).isAir() && this.isOfCenterWithEyeHeight(position.up(), position) ? position.up() : null;
    }

    private boolean isOfCenterWithEyeHeight(BlockPos position, BlockPos positionValue) {
        Vec3d positionResult = Vec3d.ofCenter(position).add(0.0, mc.player.getEyeHeight(mc.player.getPose()) - 0.5, 0.0);
        return this.createOfCenterWithSquaredDistanceTo(positionResult, positionValue) != null;
    }

    private Vec3d createOfCenterWithSquaredDistanceTo(Vec3d position, BlockPos positionValue) {
        Vec3d positionResult = Vec3d.ofCenter(positionValue);
        Vec3d positionItem = null;
        double MAX_VALUE = Double.MAX_VALUE;
        for (double index = -0.4; index <= 0.41; index += 0.4) {
            for (double indexValue = -0.4; indexValue <= 0.41; indexValue += 0.4) {
                for (double indexResult = -0.4; indexResult <= 0.41; indexResult += 0.4) {
                    Vec3d positionEntry = positionResult.add(index, indexValue, indexResult);
                    double doubleValue = positionEntry.squaredDistanceTo(positionResult);
                    BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(position, positionEntry, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
                    if (doubleValue < MAX_VALUE && blockHitResult.getBlockPos().equals(positionValue)) {
                        MAX_VALUE = doubleValue;
                        positionItem = positionEntry;
                    }
                }
            }
        }
        return positionItem;
    }

    private boolean canSinWithEyePos(BlockPos position, int intValue) {
        if (position != null && !(mc.currentScreen instanceof GenericContainerScreen)) {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d positionValue = this.createOfCenterWithSquaredDistanceTo(eyePos, position);
            if (positionValue == null) {
                return false;
            } else {
                Rotation rotation = Rotation.from(eyePos, positionValue);
                float floatValue = mc.player.age + (mc.getRenderTickCounter() != null ? mc.getRenderTickCounter().getTickDelta(false) : 0.0F);
                float floatValueValue = (float) ((Math.sin(floatValue * 0.31F) * 0.5 + Math.sin(floatValue * 0.73F + 1.1F) * 0.3 + Math.sin(floatValue * 1.7F + 2.6F) * 0.2) * 8.0);
                RotationStorage.update(new Rotation(rotation.getYaw() + floatValueValue, MathHelper.clamp(rotation.getPitch() + floatValueValue / 4.0F, -90.0F, 90.0F)), 120.0F, 120.0F, 120.0F, 120.0F, 1, 1, clientLook.isState());
                if (mc.player.age % intValue == 0 && !(new Rotation(mc.player).getDelta(rotation) > 5.0F)) {
                    BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(eyePos, positionValue, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
                    if (!blockHitResult.getBlockPos().equals(position)) {
                        return false;
                    } else {
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private BlockPos createBlockPosFromCosWithSin(BlockPos position) {
        double doubleValue = mc.player.age / 40 * 2.4;
        return new BlockPos(position.getX() + (int) (Math.cos(doubleValue) * 10.0), position.getY(), position.getZ() + (int) (Math.sin(doubleValue) * 10.0));
    }

    private void updateEntitiesByClassWithExpand() {
        for (WardenEntity wardenEntity : mc.world.getEntitiesByClass(WardenEntity.class, mc.player.getBoundingBox().expand(256.0), input -> true)) {
            this.keySetIntegerByBlockPos.put(wardenEntity.getBlockPos(), mc.player.age + 100);
        }
        this.keySetIntegerByBlockPos.values().removeIf(inputValue -> mc.player.age > inputValue);
    }

    private boolean isEntitiesWithAlive() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity.isAlive() && entity instanceof WardenEntity && entity.squaredDistanceTo(mc.player) <= 1600.0) {
                return true;
            }
        }
        return false;
    }

    private boolean isHorizontalLengthSquaredWithVelocity() {
        return mc.player.getVelocity().horizontalLengthSquared() > 0.0025;
    }

    private void selectHotbarSlot() {
        if (!mc.player.getMainHandStack().isEmpty()) {
            for (int selectedSlot = 0; selectedSlot < 9; selectedSlot++) {
                if (mc.player.getInventory().getStack(selectedSlot).isEmpty()) {
                    mc.player.getInventory().selectedSlot = selectedSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                    return;
                }
            }
        }
    }

    private void updateToRadiansWithBlockPos() {
        int y = mc.player.getBlockPos().getY();
        BlockPos position = null;
        double doubleValue = -1.0;
        for (byte index = 0; index < 360; index = (byte) (index + 30)) {
            int intValue = (int) (mc.player.getX() + Math.cos(Math.toRadians(index)) * 25.0);
            int intValueValue = (int) (mc.player.getZ() + Math.sin(Math.toRadians(index)) * 25.0);
            double doubleValueValue = this.calculateEntitiesWithHypot(intValue, intValueValue);
            if (doubleValueValue > doubleValue) {
                doubleValue = doubleValueValue;
                position = new BlockPos(intValue, y, intValueValue);
            }
        }
        if (position != null && mc.player.age % 10 == 0) {
            this.updateChatMessage(position);
        }
    }

    private double calculateEntitiesWithHypot(int intValue, int intValueValue) {
        double MAX_VALUE = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity != mc.player && (entity instanceof PlayerEntity || entity instanceof WardenEntity)) {
                MAX_VALUE = Math.min(MAX_VALUE, Math.hypot(entity.getX() - intValue, entity.getZ() - intValueValue));
            }
        }
        return MAX_VALUE;
    }

    private void updateCurrentTimeMillisWithStack() {
        int intValue = this.calculateInventoryWithItem(Items.GOLDEN_CARROT) + this.calculateInventoryWithItem(Items.COOKED_BEEF) + this.calculateInventoryWithItem(Items.GOLDEN_APPLE);
        int intValueValue = this.countSpeedPotions();
        int intValueResult = this.countInvisibilityPotions();
        boolean flag = intValue < 10 || intValueValue < 2 || this.drinkInvisibility.isState() && intValueResult < 2;
        ArrayList<net.minecraft.util.math.BlockPos> values = new ArrayList<>();
        if (this.toCenterPosBlockPos != null) {
            double doubleValue = mc.player.getPos().distanceTo(this.toCenterPosBlockPos.toCenterPos());
            if (!(doubleValue <= 6.0)) {
                if (System.currentTimeMillis() - this.lastTeleportActionMillis > 10000L) {
                    this.updateChatCommand("home home");
                    this.lastTeleportActionMillis = System.currentTimeMillis();
                }
                return;
            }
            values.add(this.toCenterPosBlockPos);
        } else {
            for (int index = -4; index <= 4; index++) {
                for (int indexValue = -6; indexValue <= 6; indexValue++) {
                    for (int indexResult = -6; indexResult <= 6; indexResult++) {
                        BlockPos position = mc.player.getBlockPos().add(indexValue, index, indexResult);
                        BlockState blockState = mc.world.getBlockState(position);
                        if (blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST) || blockState.isOf(Blocks.BARREL)) {
                            values.add(position);
                        }
                    }
                }
            }
            if (values.isEmpty()) {
                if (System.currentTimeMillis() - this.lastTeleportActionMillis > 10000L) {
                    this.updateChatCommand("home home");
                    this.lastTeleportActionMillis = System.currentTimeMillis();
                }
                return;
            }
            values.removeIf(this.removeIfBlockPosEntries::contains);
        }
        ScreenHandler currentScreenHandler = mc.player.currentScreenHandler;
        if (currentScreenHandler instanceof GenericContainerScreenHandler || currentScreenHandler instanceof HopperScreenHandler) {
            int intValueItem = this.calculateInventory(currentScreenHandler);
            var slots = currentScreenHandler.slots;
            if (System.currentTimeMillis() - this.lastSupplyCheckMillis >= 100L) {
                boolean flagValue = false;
                for (int indexItem = intValueItem; indexItem < slots.size(); indexItem++) {
                    Slot slot = slots.get(indexItem);
                    if (slot.hasStack() && !slot.getStack().isEmpty()) {
                        ItemStack stack = slot.getStack();
                        if (!this.isItemWithEffects(stack) && !this.isItemWithEquals(stack) && stack.get(DataComponentTypes.FOOD) == null && this.isUsefulLoot(stack) && mc.player.currentScreenHandler == currentScreenHandler) {
                            mc.interactionManager.clickSlot(currentScreenHandler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                            this.lastSupplyCheckMillis = System.currentTimeMillis();
                            flagValue = true;
                            break;
                        }
                    }
                }
                if (!flagValue) {
                    boolean flagResult = false;
                    boolean flagItem = false;
                    if (flag) {
                        for (int indexEntry = 0; indexEntry < intValueItem; indexEntry++) {
                            Slot slotValue = slots.get(indexEntry);
                            if (slotValue.hasStack() && !slotValue.getStack().isEmpty()) {
                                ItemStack stackValue = slotValue.getStack();
                                boolean flagEntry = stackValue.get(DataComponentTypes.FOOD) != null;
                                boolean itemWithEffects = this.isItemWithEffects(stackValue);
                                boolean itemWithEquals = this.isItemWithEquals(stackValue);
                                if (flagEntry || itemWithEffects || itemWithEquals) {
                                    flagItem = true;
                                }
                                if ((flagEntry && intValue < 10 || itemWithEffects && intValueValue < 2 || itemWithEquals && this.drinkInvisibility.isState() && intValueResult < 2) && mc.player.currentScreenHandler == currentScreenHandler) {
                                    mc.interactionManager.clickSlot(currentScreenHandler.syncId, slotValue.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                    this.lastSupplyCheckMillis = System.currentTimeMillis();
                                    flagResult = true;
                                    if (this.toCenterPosBlockPos == null && this.blockPos != null) {
                                        BlockPos blockPos = this.blockPos;
                                        brova.toshiba.client.utils.runtime.AccessGuard.write(this, blockPos, (accessReceiver, accessValue) -> {
                                            accessReceiver.toCenterPosBlockPos = accessValue;
                                        });
                                        ChatUtil.sendMessage("§a[AutoWarden] Сундук пополнения сохранен: " + this.toCenterPosBlockPos.getX() + " " + this.toCenterPosBlockPos.getY() + " " + this.toCenterPosBlockPos.getZ());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (!flagResult) {
                        if (this.toCenterPosBlockPos != null && flag && !flagItem) {
                            ChatUtil.sendMessage("§c[AutoWarden] В сундуке пополнения закончились ресурсы! Выключаем модуль.");
                            mc.player.closeHandledScreen();
                            this.toggle();
                        } else {
                            mc.player.closeHandledScreen();
                            if (this.blockPos != null) {
                                this.removeIfBlockPosEntries.add(this.blockPos);
                                BlockPos positionValue = this.createBlockStateWithBlock(this.blockPos);
                                if (positionValue != null) {
                                    this.removeIfBlockPosEntries.add(positionValue);
                                }
                                this.blockPos = null;
                            }
                        }
                    }
                }
            }
        } else if (flag && !values.isEmpty()) {
            BlockPos blockPosValue = values.get(0);
            Vec3d positionResult = blockPosValue.toCenterPos();
            Vec2f rotations = RotationUtil.getRotations(positionResult);
            RotationStorage.update(new Rotation(rotations.x, rotations.y), 360.0F, 360.0F, 360.0F, 360.0F, 1, 10, clientLook.isState());
            if (System.currentTimeMillis() - this.lastSellActionMillis > 1000L) {
                BlockHitResult blockHitResult = new BlockHitResult(positionResult, Direction.UP, blockPosValue, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
                this.lastSellActionMillis = System.currentTimeMillis();
                this.blockPos = blockPosValue;
            }
        } else {
            this.removeIfBlockPosEntries.clear();
            this.autoWardenFarmState = FarmState.SWITCH_TO_ANARCHY;
            this.lastLootActionMillis = System.currentTimeMillis();
        }
    }

    private BlockPos createBlockStateWithBlock(BlockPos position) {
        if (mc.world == null) {
            return null;
        } else {
            BlockState blockState = mc.world.getBlockState(position);
            if (!(blockState.getBlock() instanceof ChestBlock)) {
                return null;
            } else if (!blockState.contains(ChestBlock.CHEST_TYPE)) {
                return null;
            } else {
                ChestType chestType = blockState.get(ChestBlock.CHEST_TYPE);
                if (chestType == ChestType.SINGLE) {
                    return null;
                } else {
                    Direction direction = blockState.get(ChestBlock.FACING);
                    for (Direction directionValue : Type.HORIZONTAL) {
                        BlockPos positionValue = position.offset(directionValue);
                        BlockState blockStateValue = mc.world.getBlockState(positionValue);
                        if (blockStateValue.isOf(blockState.getBlock()) && blockStateValue.get(ChestBlock.FACING) == direction) {
                            ChestType chestTypeValue = blockStateValue.get(ChestBlock.CHEST_TYPE);
                            if (chestTypeValue != ChestType.SINGLE && chestTypeValue != chestType) {
                                return positionValue;
                            }
                        }
                    }
                    return null;
                }
            }
        }
    }

    private void drinkSpeedPotion() {
        if (System.currentTimeMillis() - this.lastPotionUseMillis >= 2000L) {
            int selectedSlot = -1;
            for (int index = 0; index < 9; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (this.isItemWithEffects(stack)) {
                    selectedSlot = index;
                    break;
                }
            }
            if (selectedSlot != -1) {
                this.drinkingSpeedPotion = true;
                this.speedDrinkTicks = 0;
                this.slotBeforeSpeedPotion = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                mc.options.useKey.setPressed(true);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                this.lastPotionUseMillis = System.currentTimeMillis();
            }
        }
    }

    private void drinkInvisibilityPotion() {
        if (System.currentTimeMillis() - this.lastPotionUseMillis >= 2000L) {
            int selectedSlot = -1;
            for (int index = 0; index < 9; index++) {
                ItemStack stack = mc.player.getInventory().getStack(index);
                if (this.isItemWithEquals(stack)) {
                    selectedSlot = index;
                    break;
                }
            }
            if (selectedSlot != -1) {
                this.drinkingInvisibilityPotion = true;
                this.invisibilityDrinkTicks = 0;
                this.slotBeforeInvisibilityPotion = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                mc.options.useKey.setPressed(true);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                this.lastPotionUseMillis = System.currentTimeMillis();
            }
        }
    }

    private boolean isItemWithEffects(ItemStack stack) {
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        } else {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent != null) {
                for (StatusEffectInstance statusEffectInstance : potionContentsComponent.getEffects()) {
                    if (statusEffectInstance.getEffectType().equals(StatusEffects.SPEED)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean isItemWithEquals(ItemStack stack) {
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        } else {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent != null) {
                var registryEntry = potionContentsComponent.potion().orElse(null);
                if (registryEntry != null && (registryEntry.equals(Potions.INVISIBILITY) || registryEntry.equals(Potions.LONG_INVISIBILITY))) {
                    return true;
                }
                for (StatusEffectInstance statusEffectInstance : potionContentsComponent.getEffects()) {
                    if (statusEffectInstance.getEffectType().equals(StatusEffects.INVISIBILITY)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean needsSpeedPotion() {
        if (mc.player == null) {
            return false;
        } else {
            StatusEffectInstance statusEffect = mc.player.getStatusEffect(StatusEffects.SPEED);
            return statusEffect == null ? true : statusEffect.getDuration() <= 200;
        }
    }

    private boolean needsInvisibilityPotion() {
        if (mc.player == null) {
            return false;
        } else {
            StatusEffectInstance statusEffect = mc.player.getStatusEffect(StatusEffects.INVISIBILITY);
            return statusEffect == null ? true : statusEffect.getDuration() <= 200;
        }
    }

    private boolean isUsefulLoot(ItemStack stack) {
        return !stack.isEmpty() && !this.isJunkItem(stack);
    }

    private int calculateInventoryWithItem(Item item) {
        int intValue = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (stack.getItem() == item) {
                intValue += stack.getCount();
            }
        }
        return intValue;
    }

    private int countSpeedPotions() {
        int intValue = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (this.isItemWithEffects(stack)) {
                intValue += stack.getCount();
            }
        }
        return intValue;
    }

    private int countInvisibilityPotions() {
        int intValue = 0;
        for (ItemStack stack : mc.player.getInventory().main) {
            if (this.isItemWithEquals(stack)) {
                intValue += stack.getCount();
            }
        }
        return intValue;
    }

    private boolean isLocalState() {
        return this.countValuableLoot() >= 23;
    }

    private int countValuableLoot() {
        if (mc.player == null) {
            return 0;
        } else {
            int intValue = 0;
            for (ItemStack stack : mc.player.getInventory().main) {
                if (this.isUsefulLoot(stack) && !this.isItemWithEffects(stack) && !this.isItemWithEquals(stack) && stack.get(DataComponentTypes.FOOD) == null) {
                    intValue += stack.getCount();
                }
            }
            return intValue;
        }
    }

    private boolean isPvpBossbarActive() {
        var entries = this.readBossBars();
        if (entries != null && !entries.isEmpty()) {
            for (Object value : entries.values()) {
                if (value instanceof BossBar bossBar) {
                    String normalizedText = bossBar.getName().getString().toLowerCase();
                    if (normalizedText.contains("pvp") || normalizedText.contains("пвп") || normalizedText.contains("режим") || normalizedText.contains("секунд")) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private Map<?, ?> readBossBars() {
        try {
            return ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).toshiba$getBossBars();
        } catch (Exception failure) {
            return Collections.emptyMap();
        }
    }

    private int calculateInventory(ScreenHandler screenHandler) {
        if (screenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            return genericContainerScreenHandler.getInventory().size();
        } else {
            return screenHandler instanceof HopperScreenHandler ? 5 : 0;
        }
    }

    private void updateChatCommand(String text) {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand(text);
        }
    }

    private void updateChatMessage(BlockPos position) {
        if (mc.player != null && mc.player.networkHandler != null && position != null) {
            mc.player.networkHandler.sendChatMessage("#goto " + position.getX() + " " + position.getY() + " " + position.getZ());
        }
    }

    private void updateChatMessage() {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatMessage("#stop");
        }
    }

    private int calculateScoreboardWithObjectiveForSlot() {
        if (mc.world == null) {
            return -1;
        } else {
            try {
                Scoreboard scoreboard = mc.world.getScoreboard();
                ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                if (objectiveForSlot != null) {
                    String normalizedText = objectiveForSlot.getDisplayName().getString().toLowerCase();
                    int index = normalizedText.indexOf("анархия-");
                    if (index != -1) {
                        StringBuilder textBuilder = new StringBuilder();
                        for (int indexValue = index + 8; indexValue < normalizedText.length(); indexValue++) {
                            char character = normalizedText.charAt(indexValue);
                            if (Character.isDigit(character)) {
                                textBuilder.append(character);
                            } else if (textBuilder.length() > 0) {
                                break;
                            }
                        }
                        if (textBuilder.length() > 0) {
                            return Integer.parseInt(textBuilder.toString());
                        }
                    }
                }
            } catch (Exception failure) {
            }
            return -1;
        }
    }

    private boolean isScoreboardWithObjectiveForSlot(int intValue) {
        if (mc.world == null) {
            return false;
        } else {
            try {
                Scoreboard scoreboard = mc.world.getScoreboard();
                ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                if (objectiveForSlot == null) {
                    return false;
                } else {
                    String text = objectiveForSlot.getDisplayName().getString().toLowerCase(Locale.ROOT).replace(" ", "");
                    return text.contains("анархия-" + intValue) || text.contains("anarchy-" + intValue);
                }
            } catch (Exception failure) {
                return false;
            }
        }
    }

    private void updateCurrentTimeMillis() {
        this.updateChatMessage();
        this.lootPriorityIndex++;
        if (this.lootPriorityIndex >= this.indexOfIntegerEntries.size()) {
            this.lootPriorityIndex = 0;
            this.autoWardenFarmState = FarmState.SWITCH_TO_BASE;
        } else {
            this.autoWardenFarmState = FarmState.SWITCH_TO_ANARCHY;
        }
        this.lastLootActionMillis = System.currentTimeMillis();
        this.equalsBlockPos = null;
        this.orDefaultIntegerByBlockPos.clear();
    }

    public void setMainAnarchy(int mainAnarchy) {
        this.mainAnarchy = mainAnarchy;
    }

    public void addAnarchy(int intValue) {
        if (!this.indexOfIntegerEntries.contains(intValue)) {
            this.indexOfIntegerEntries.add(intValue);
        }
    }

    public void clearAnarchies() {
        this.indexOfIntegerEntries.clear();
    }

    private boolean isWardenEventActive() {
        for (Object value : this.readBossBars().values()) {
            if (value instanceof BossBar bossBar) {
                String normalizedText = bossBar.getName().getString().toLowerCase();
                if (normalizedText.contains("телепорт") || normalizedText.contains("teleport") || normalizedText.contains("варден") || normalizedText.contains("warden")) {
                    return true;
                }
            }
        }
        WardenEsp wardenEsp = ModuleRegistry.wardenEsp;
        return wardenEsp != null && !wardenEsp.foundBlocks.isEmpty();
    }

    public enum FarmState {

        IDLE,
        SWITCH_TO_BASE,
        BASE_RESTOCK,
        SWITCH_TO_ANARCHY,
        TELEPORT_HOME,
        WAIT_TELEPORT,
        FARM_WARDEN,
        ESCAPE
    }
}
