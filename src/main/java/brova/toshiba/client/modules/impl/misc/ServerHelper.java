package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.DirectionalInput;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.ScreenEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.gui.hud.notification.NotificationManager;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.ServerHelper.BoundItem;
import brova.toshiba.client.modules.impl.misc.ServerHelper.DroppedItem;
import brova.toshiba.client.modules.impl.misc.ServerHelper.InventoryAction;
import brova.toshiba.client.modules.impl.misc.ServerHelper.ItemAction;
import brova.toshiba.client.modules.impl.misc.ServerHelper.PotionEffect;
import brova.toshiba.client.modules.impl.misc.ServerHelper.PotionEffects;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.impl.player.PvPSave;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.settings.SettingBase;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.utils.player.InventoryUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.rotate.Rotation;
import brova.toshiba.client.utils.rotate.RotationUtil;
import brova.toshiba.client.utils.text.ChatUtil;
import brova.toshiba.client.utils.time.IntervalTimer;
import brova.toshiba.client.utils.time.TickTaskScheduler;
import brova.toshiba.client.utils.world.NearestPointUtil;
import brova.toshiba.mixin.IClientPlayerInteractionManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;

public final class ServerHelper extends Module {

    public static final ServerHelper INSTANCE = new ServerHelper();

    private final Map<BlockPos, BlockState> blockStateByBlockPos = new HashMap<>();

    private final List<DroppedItem> removeIfServerHelperDroppedItemEntries = new ArrayList<>();

    private final List<BoundItem> keyBindings = new ArrayList<>();

    private final IntervalTimer resetIntervalTimerForUpdate = new IntervalTimer();

    private final IntervalTimer resetIntervalTimerForPacket = new IntervalTimer();

    private final IntervalTimer finishedIntervalTimer = new IntervalTimer();

    private final TickTaskScheduler addTickStepTickTaskScheduler = new TickTaskScheduler();

    private final TickTaskScheduler cleanupTickTaskScheduler = new TickTaskScheduler();

    private UUID uuid;

    private final Map<Integer, Item> entrySetItemEntries = new HashMap<>();

    private final Map<String, Long> entrySetLongEntries = new HashMap<>();

    private final Map<ItemAction, ItemStack> itemStackByServerHelperItemAction = new IdentityHashMap<>();

    private final Map<BoundItem, ItemAction> serverHelperItemActionByServerHelperBoundItem = new IdentityHashMap<>();

    private ItemStack[] areEqualItemStackValues = new ItemStack[0];

    private Object object;

    private String current;

    private boolean stateFlagForRefreshScreenBindInventory;

    private final ModeSetting serverType = new ModeSetting("Тип сервера", "FunTime", "FunTime", "HolyWorld");

    private final BooleanSetting autoLoot = new BooleanSetting("Авто лут", true).visible(() -> this.serverType.is("HolyWorld"));

    private final BooleanSetting autoShulker = new BooleanSetting("Авто шалкер", true).visible(() -> this.serverType.is("HolyWorld"));

    private final BooleanSetting autoRepair = new BooleanSetting("Авто ремонт", true).visible(() -> this.serverType.is("HolyWorld"));

    private final BooleanSetting screenBindsSetting = new BooleanSetting("Бинды на экране", true);

    private final BooleanSetting ftCopiesSetting = new BooleanSetting("Копии FT", false).visible(() -> this.serverType.is("FunTime"));

    private final BindSetting leaveDuelArena = new BindSetting("Лив на /darena", -1).visible(() -> this.serverType.is("FunTime"));

    private final List<String> stringEntries = new ArrayList<>();

    private final IntervalTimer resetIntervalTimerForEnable = new IntervalTimer();

    private final Map<String, ItemAction> itemInfo = new HashMap<>();

    private final Map<String, Boolean> lootActionStates = new HashMap<>();

    private final Map<String, Boolean> repairActionStates = new HashMap<>();

    private final Map<String, Boolean> shulkerActionStates = new HashMap<>();

    private int previousSelectedSlot = -1;

    private int pendingInventorySlot = -1;

    private int clickSlot = -1;

    private boolean pvpState = false;

    private InventoryAction serverHelperInventoryAction = null;

    private boolean repairPending = false;

    private boolean repairItemSelected = false;

    private boolean useItemPending = false;

    private int cachedCountForEnable = 0;

    private int slotBeforeRepair = -1;

    private long lastUpdateMillis = 0L;

    private boolean forwardKeyWasPressed;

    private boolean backKeyWasPressed;

    private boolean leftKeyWasPressed;

    private boolean rightKeyWasPressed;

    public static boolean isPvp() {
        return MinecraftClient.getInstance().world != null;
    }

    public ServerHelper() {
        super("ServerHelper", "Вспомогающая утилита для FunTime", ModuleCategory.MISC);
        this.keyBindings.add(new BoundItem(Items.PRISMARINE_SHARD, new BindSetting("Взрывная трапка", -1).visible(() -> this.serverType.is("HolyWorld")), 5.0F));
        this.keyBindings.add(new BoundItem(Items.POPPED_CHORUS_FRUIT, new BindSetting("Обычная трапка", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.NETHER_STAR, new BindSetting("Стан", -1).visible(() -> this.serverType.is("HolyWorld")), 30.0F));
        this.keyBindings.add(new BoundItem(Items.FIRE_CHARGE, new BindSetting("Взрывная штучка", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SNOWBALL, new BindSetting("Снежок заморозка", -1).visible(() -> this.serverType.is("HolyWorld") || this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.PHANTOM_MEMBRANE, new BindSetting("Божья аура", -1), 0.0F));
        this.keyBindings.add(new BoundItem(Items.NETHERITE_SCRAP, new BindSetting("Трапка", -1), 0.0F));
        this.keyBindings.add(new BoundItem(Items.DRIED_KELP, new BindSetting("Пласт", -1), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SUGAR, new BindSetting("Явная пыль", -1), 10.0F));
        this.keyBindings.add(new BoundItem(Items.FIRE_CHARGE, new BindSetting("Огненный смерч", -1), 10.0F));
        this.keyBindings.add(new BoundItem(Items.ENDER_EYE, new BindSetting("Дезориентация", -1), 10.0F));
        this.keyBindings.add(new BoundItem(Items.JACK_O_LANTERN, new BindSetting("Светильник Джека", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.EXPERIENCE_BOTTLE, new BindSetting("Пузырь опыта", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.PINK_SHULKER_BOX, new BindSetting("Рюкзак 1 уровня", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.BLUE_SHULKER_BOX, new BindSetting("Рюкзак 2 уровня", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.RED_SHULKER_BOX, new BindSetting("Рюкзак 3 уровня", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.PINK_SHULKER_BOX, new BindSetting("Рюкзак 4 уровня", -1).visible(() -> this.serverType.is("HolyWorld")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Зелье Ассасина", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Зелье Паладина", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Зелье Радиации", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Зелье Гнева", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Святая вода", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Хлопушка", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        this.keyBindings.add(new BoundItem(Items.SPLASH_POTION, new BindSetting("Снотворное", -1).visible(() -> this.serverType.is("FunTime")), 0.0F));
        SettingBase[] settings = new SettingBase[7 + this.keyBindings.size()];
        settings[0] = this.serverType;
        settings[1] = this.autoLoot;
        settings[2] = this.autoShulker;
        settings[3] = this.autoRepair;
        settings[4] = this.screenBindsSetting;
        settings[5] = this.ftCopiesSetting;
        for (int index = 0; index < this.keyBindings.size(); index++) {
            settings[6 + index] = this.keyBindings.get(index).setting;
        }
        settings[settings.length - 1] = this.leaveDuelArena;
        this.addSettings(settings);
        this.itemInfo.put("disorientation", new ItemAction(null, Items.ENDER_EYE, "Дезориентация", true));
        this.itemInfo.put("sugar", new ItemAction("явная", Items.SUGAR, "Явная пыль", true));
        this.itemInfo.put("bojaura", new ItemAction(null, Items.PHANTOM_MEMBRANE, "Божья аура", true));
        this.itemInfo.put("snow", new ItemAction(null, Items.SNOWBALL, "Снежок заморозка", true));
        this.itemInfo.put("plast", new ItemAction(null, Items.DRIED_KELP, "Пласт", true));
        this.itemInfo.put("trap", new ItemAction(null, Items.NETHERITE_SCRAP, "Трапка", true));
        this.itemInfo.put("fireSwirl", new ItemAction(null, Items.FIRE_CHARGE, "Огненный смерч", true));
        this.itemInfo.put("assassin", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.STRENGTH, 3), new PotionEffect(StatusEffects.SPEED, 2), new PotionEffect(StatusEffects.HASTE, 0), new PotionEffect(StatusEffects.INSTANT_DAMAGE, 1)), "Зелье Ассасина"));
        this.itemInfo.put("paladin", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.RESISTANCE, 0), new PotionEffect(StatusEffects.FIRE_RESISTANCE, 0), new PotionEffect(StatusEffects.INSTANT_HEALTH, 2), new PotionEffect(StatusEffects.INVISIBILITY, 0)), "Зелье Паладина"));
        this.itemInfo.put("radiation", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.POISON, 1), new PotionEffect(StatusEffects.WITHER, 1), new PotionEffect(StatusEffects.SLOWNESS, 2), new PotionEffect(StatusEffects.HUNGER, 4), new PotionEffect(StatusEffects.GLOWING, 0)), "Зелье Радиации"));
        this.itemInfo.put("rage", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.STRENGTH, 4), new PotionEffect(StatusEffects.SLOWNESS, 3)), "Зелье Гнева"));
        this.itemInfo.put("holyWater", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.REGENERATION, 1), new PotionEffect(StatusEffects.INVISIBILITY, 1), new PotionEffect(StatusEffects.INSTANT_HEALTH, 1)), "Святая вода"));
        this.itemInfo.put("clapper", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.SLOWNESS, 8), new PotionEffect(StatusEffects.SPEED, 4), new PotionEffect(StatusEffects.BLINDNESS, 8), new PotionEffect(StatusEffects.GLOWING, 0)), "Хлопушка"));
        this.itemInfo.put("sleeping", new ItemAction(new PotionEffects(new PotionEffect(StatusEffects.WEAKNESS, 1), new PotionEffect(StatusEffects.NAUSEA, 1), new PotionEffect(StatusEffects.MINING_FATIGUE, 2), new PotionEffect(StatusEffects.BLINDNESS, 0)), "Снотворное"));
        this.itemInfo.put("dtrap", new ItemAction("взрывная трапка", Items.PRISMARINE_SHARD, "Взрывная трапка", false));
        this.itemInfo.put("trap_holy", new ItemAction("трапка", Items.POPPED_CHORUS_FRUIT, "Обычная трапка", false));
        this.itemInfo.put("stan", new ItemAction("стан", Items.NETHER_STAR, "Стан", false));
        this.itemInfo.put("ditem", new ItemAction("взрывная штучка", Items.FIRE_CHARGE, "Взрывная штучка", false));
        this.itemInfo.put("tikva", new ItemAction("светильник джейка", Items.JACK_O_LANTERN, "Светильник Джека", false));
        this.itemInfo.put("exp", new ItemAction("пузырь опыта", Items.EXPERIENCE_BOTTLE, "Пузырь опыта", false));
        this.itemInfo.put("shulker1", new ItemAction("рюкзак (i уровень)", Items.PINK_SHULKER_BOX, "Рюкзак 1 уровня", false));
        this.itemInfo.put("shulker2", new ItemAction("рюкзак (ii уровень)", Items.BLUE_SHULKER_BOX, "Рюкзак 2 уровня", false));
        this.itemInfo.put("shulker3", new ItemAction("рюкзак (iii уровень)", Items.RED_SHULKER_BOX, "Рюкзак 3 уровня", false));
        this.itemInfo.put("shulker4", new ItemAction("рюкзак (iv уровень)", Items.PINK_SHULKER_BOX, "Рюкзак 4 уровня", false));
        this.itemInfo.keySet().forEach(input -> {
            this.lootActionStates.put(input, false);
            this.repairActionStates.put(input, false);
            this.shulkerActionStates.put(input, false);
        });
    }

    @Override
    public void onEnable() {
        this.cleanupTickTaskScheduler.cleanup();
        this.entrySetItemEntries.clear();
        this.entrySetLongEntries.clear();
        this.stringEntries.clear();
        this.resetIntervalTimerForEnable.reset();
        this.lootActionStates.replaceAll((input, inputValue) -> false);
        this.repairActionStates.replaceAll((inputResult, inputItem) -> false);
        this.shulkerActionStates.replaceAll((inputEntry, inputCopy) -> false);
        this.previousSelectedSlot = -1;
        this.pendingInventorySlot = -1;
        this.clickSlot = -1;
        this.pvpState = false;
        Object value = null;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
            accessReceiver.serverHelperInventoryAction = (InventoryAction) accessValue;
        });
        this.repairPending = false;
        this.repairItemSelected = false;
        this.useItemPending = false;
        this.cachedCountForEnable = 0;
        this.slotBeforeRepair = -1;
        this.lastUpdateMillis = 0L;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.repairPending) {
            this.applyKeyPressedWithHandle();
        }
        this.cachedCountForEnable = 0;
        if (this.slotBeforeRepair != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = this.slotBeforeRepair;
        }
        this.slotBeforeRepair = -1;
        this.lootActionStates.replaceAll((input, inputValue) -> false);
        this.repairActionStates.replaceAll((inputResult, inputItem) -> false);
        this.shulkerActionStates.replaceAll((inputEntry, inputCopy) -> false);
        this.stringEntries.clear();
        this.previousSelectedSlot = -1;
        this.pendingInventorySlot = -1;
        this.clickSlot = -1;
        this.pvpState = false;
        this.serverHelperInventoryAction = null;
        this.repairPending = false;
        this.useItemPending = false;
        this.lastUpdateMillis = 0L;
        this.updatePopPause();
        super.onDisable();
    }

    @EventHandler
    public void onInput(DirectionalInput directionalInput) {
        if (mc.player != null && this.isLocalState()) {
            mc.player.setSprinting(false);
            directionalInput.setForward(0.0F);
            float floatValue = 0.0F;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(directionalInput, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((DirectionalInput) accessReceiver).setStrafe(((float) accessArguments[0]));
            });
            directionalInput.setJump(false);
            directionalInput.setSneak(false);
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && eventPacket.getType() == Type.RECEIVE) {
            var packet = eventPacket.getPacket();
            byte byteValue = 0;
            while (true) {
                switch(recoveredTypeSwitch(packet, byteValue)) {
                    case 0:
                        ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = (ItemPickupAnimationS2CPacket) packet;
                        if (this.autoShulker.isState() && itemPickupAnimationS2CPacket.getCollectorEntityId() == mc.player.getId() && mc.world.getEntityById(itemPickupAnimationS2CPacket.getEntityId()) instanceof ItemEntity itemEntity) {
                            ItemStack stack = itemEntity.getStack();
                            if (stack.get(DataComponentTypes.CONTAINER) == null) {
                                this.entrySetItemEntries.put(-ThreadLocalRandom.current().nextInt(1, 999999999), stack.getItem());
                                this.resetIntervalTimerForPacket.reset();
                            }
                            return;
                        }
                        byteValue = 1;
                        break;
                    case 1:
                        ScreenHandlerSlotUpdateS2CPacket screenHandlerSlotUpdateS2CPacket = (ScreenHandlerSlotUpdateS2CPacket) packet;
                        if (screenHandlerSlotUpdateS2CPacket.getSyncId() == 0) {
                            Item item = screenHandlerSlotUpdateS2CPacket.getStack().getItem();
                            this.entrySetItemEntries.entrySet().stream().filter(input -> input.getKey() < 0 && input.getValue().equals(item)).findFirst().ifPresent(inputValue -> {
                                this.entrySetItemEntries.put(screenHandlerSlotUpdateS2CPacket.getSlot() + 18, item);
                                this.entrySetItemEntries.remove(inputValue.getKey());
                            });
                        }
                        return;
                    case 2:
                        ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket = (ChunkDeltaUpdateS2CPacket) packet;
                        chunkDeltaUpdateS2CPacket.visitUpdates((inputResult, inputItem) -> this.blockStateByBlockPos.put(inputResult.toImmutable(), inputItem));
                        this.addTickStepTickTaskScheduler.addTickStep(0, () -> {
                            if (this.blockStateByBlockPos.size() >= 50 && this.blockStateByBlockPos.size() <= 600) {
                                chunkDeltaUpdateS2CPacket.visitUpdates((inputEntry, inputCopy) -> {
                                    Vec3d position = inputEntry.toCenterPos();
                                    if (!this.removeIfServerHelperDroppedItemEntries.stream().anyMatch(input2 -> input2.vec3d.squaredDistanceTo(position) < 1.0)) {
                                        if (this.isUpWithNorth(inputEntry.toImmutable().up(2))) {
                                            this.updateItem(Items.NETHERITE_SCRAP, position, System.currentTimeMillis() + 15000L);
                                        } else if (this.isAsItemWithBlock(inputEntry.toImmutable().up(3))) {
                                            this.updateItem(Items.NETHERITE_SCRAP, position, System.currentTimeMillis() + 30000L);
                                        }
                                    }
                                });
                            }
                        });
                        return;
                    case 3:
                        GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket) packet;
                        String string = gameMessageS2CPacket.content().getString();
                        if (string.contains("▶ Повторно активировать Пузырь опыта возможно через")) {
                            String text = StringUtils.substringBetween(string, "через ", " секунд");
                            if (text != null && !text.isEmpty()) {
                                int intValue = Integer.parseInt(text) * 20;
                                this.entrySetLongEntries.put("exp", System.currentTimeMillis() + intValue * 50L);
                                return;
                            }
                        }
                        return;
                    case 4:
                        OpenScreenS2CPacket openScreenS2CPacket = (OpenScreenS2CPacket) packet;
                        if (openScreenS2CPacket.getName().getString().contains("Рюкзак") && !this.entrySetItemEntries.isEmpty()) {
                            this.addTickStepTickTaskScheduler.cleanup().addTickStep(0, this.cleanupTickTaskScheduler::update);
                            return;
                        }
                        byteValue = 5;
                        break;
                    default:
                        return;
                }
            }
        }
    }

    @EventHandler
    public void onSetScreen(ScreenEvent event) {
        if (event.getScreen() instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().contains("Рюкзак") && !this.cleanupTickTaskScheduler.isFinished()) {
            event.setScreen(null);
        }
    }

    @EventHandler
    public void onBinding(KeyBind keyBind) {
        if (this.serverType.is("FunTime") && mc.currentScreen == null && mc.player != null && mc.world != null && mc.player.networkHandler != null && this.leaveDuelArena.getKey() != -1 && keyBind.getKey() == this.leaveDuelArena.getKey()) {
            if (PvPSave.isCombatActive()) {
                ChatUtil.sendMessage("Вы в режиме боя!");
            } else {
                this.useItemPending = true;
                mc.player.networkHandler.sendChatCommand("darena");
            }
        }
    }

    private void updateScreenHandlerWithTitle() {
        if (this.useItemPending && mc.currentScreen instanceof GenericContainerScreen genericContainerScreen && mc.interactionManager != null && genericContainerScreen.getTitle().getString().contains("Арена смерти") && genericContainerScreen.getScreenHandler().getRows() == 5) {
            byte byteValue = 24;
            if (genericContainerScreen.getScreenHandler().slots.size() > byteValue) {
                ItemStack stack = genericContainerScreen.getScreenHandler().slots.get(byteValue).getStack();
                if (stack.isOf(Items.PUFFERFISH) && stack.getName().getString().toLowerCase(Locale.ROOT).contains("смотров")) {
                    mc.interactionManager.clickSlot(genericContainerScreen.getScreenHandler().syncId, byteValue, 0, SlotActionType.PICKUP, mc.player);
                    this.useItemPending = false;
                }
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.slotBeforeRepair != -1) {
                mc.player.getInventory().selectedSlot = this.slotBeforeRepair;
                ((IClientPlayerInteractionManager) mc.interactionManager).invokeSyncSelectedSlot();
                this.slotBeforeRepair = -1;
            }
            this.updateScreenHandlerWithTitle();
            if (this.repairPending && System.currentTimeMillis() - this.lastUpdateMillis > 5000L) {
                this.applyKeyPressedWithHandle();
                if (this.serverHelperInventoryAction != null) {
                    this.serverHelperInventoryAction = null;
                    this.previousSelectedSlot = -1;
                    this.pendingInventorySlot = -1;
                    this.clickSlot = -1;
                    this.cachedCountForEnable = 0;
                    this.updatePopPause();
                }
            }
            this.pvpState = this.isLocalState();
            if (this.pvpState) {
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                    ((ServerHelper) accessReceiver).updateSprinting();
                });
            }
            if (this.serverHelperInventoryAction != null) {
                this.sendInventoryWithPacket();
            }
            if (this.cachedCountForEnable > 0) {
                this.cachedCountForEnable--;
                if (this.cachedCountForEnable == 0 && this.serverHelperInventoryAction == null) {
                    this.applyKeyPressedWithHandle();
                    this.updatePopPause();
                }
            }
            if (mc.currentScreen == null) {
                for (BoundItem boundItem : this.keyBindings) {
                    String itemKey = this.getItemKey(boundItem);
                    if (itemKey != null && boundItem.setting.visible()) {
                        boolean pressed = boundItem.setting.isPressed();
                        boolean orDefault = this.repairActionStates.getOrDefault(itemKey, false);
                        if (pressed && !orDefault) {
                            ItemAction itemAction = this.itemInfo.get(itemKey);
                            if (itemAction != null) {
                                Slot slot = this.createSlotFromStackWithMatchesItem(itemAction);
                                boolean addStarPrefix = itemAction.addStarPrefix;
                                if (slot != null) {
                                    ItemStack stack = slot.getStack();
                                    if (this.isOrDefaultWithCurrentTimeMillis(itemKey, stack)) {
                                        String text = brova.toshiba.client.utils.runtime.AccessGuard.read(itemAction, accessReceiver -> accessReceiver.displayName) + " еще перезаряжается";
                                        NotificationManager.pushCustom(text, "server");
                                    } else if (!this.stringEntries.contains(itemKey)) {
                                        this.stringEntries.add(itemKey);
                                    }
                                } else {
                                    String textValue = brova.toshiba.client.utils.runtime.AccessGuard.read(itemAction, accessReceiver -> accessReceiver.displayName) + " не найдено";
                                    NotificationManager.pushCustom(textValue, "server");
                                }
                            }
                        }
                        this.repairActionStates.put(itemKey, pressed);
                        this.shulkerActionStates.put(itemKey, pressed);
                    }
                }
            } else {
                for (BoundItem boundItemValue : this.keyBindings) {
                    String itemKeyValue = this.getItemKey(boundItemValue);
                    if (itemKeyValue != null) {
                        this.repairActionStates.put(itemKeyValue, false);
                        this.shulkerActionStates.put(itemKeyValue, false);
                    }
                }
            }
            if (this.serverHelperInventoryAction == null && this.slotBeforeRepair == -1 && !this.stringEntries.isEmpty()) {
                String textResult = this.stringEntries.remove(0);
                ItemAction itemActionValue = this.itemInfo.get(textResult);
                if (itemActionValue != null) {
                    Slot slotValue = this.createSlotFromStackWithMatchesItem(itemActionValue);
                    boolean addStarPrefixValue = itemActionValue.addStarPrefix;
                    if (slotValue != null) {
                        ItemStack stackValue = slotValue.getStack();
                        if (!this.isOrDefaultWithCurrentTimeMillis(textResult, stackValue)) {
                            this.updateKeyPressedWithHandle(textResult, slotValue, itemActionValue, addStarPrefixValue);
                        } else {
                            String textItem = itemActionValue.displayName + " еще перезаряжается";
                            NotificationManager.pushCustom(textItem, "server");
                        }
                    } else {
                        String textEntry = itemActionValue.displayName + " не найдено";
                        NotificationManager.pushCustom(textEntry, "server");
                    }
                }
            }
            if (this.autoRepair.isState() && StreamSupport.stream(mc.player.getArmorItems().spliterator(), false).anyMatch(input -> !input.isEmpty() && !((double) input.getDamage() / input.getMaxDamage() < 0.94) ? InventoryUtil.getEnchantmentLevel(input, Enchantments.MENDING) > 0 : false)) {
                Slot slotResult = this.createSlotFromStackWithItem();
                if (slotResult != null && this.finishedIntervalTimer.finished(5000L)) {
                    this.sendPacketWithInventory(slotResult);
                    this.finishedIntervalTimer.reset();
                }
            }
            if (this.autoShulker.isState() && !(mc.currentScreen instanceof GenericContainerScreen) && !this.entrySetItemEntries.isEmpty() && this.cleanupTickTaskScheduler.isFinished() && this.resetIntervalTimerForPacket.finished(300L)) {
                Slot slotItem = this.findSlotFromStackWithItem();
                if (slotItem != null) {
                    int id = slotItem.id;
                    int selectedSlot = mc.player.getInventory().selectedSlot;
                    if (id >= 36 && id <= 44) {
                        int selectedSlotValue = id - 36;
                        mc.player.getInventory().selectedSlot = selectedSlotValue;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlotValue));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    } else {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, selectedSlot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    this.cleanupTickTaskScheduler.cleanup().addTickStep(0, () -> {
                        if (mc.player != null && mc.interactionManager != null) {
                            ArrayList<Integer> values = new ArrayList<>();
                            for (Slot slotEntry : mc.player.currentScreenHandler.slots) {
                                if (slotEntry.inventory.equals(mc.player.getInventory())) {
                                    Item item = slotEntry.getStack().getItem();
                                    for (var entry : this.entrySetItemEntries.entrySet()) {
                                        if (entry.getValue().equals(item) && entry.getKey() == slotEntry.id) {
                                            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotEntry.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                                            values.add(slotEntry.id);
                                            break;
                                        }
                                    }
                                }
                            }
                            values.forEach(this.entrySetItemEntries::remove);
                            mc.player.closeHandledScreen();
                            if (id < 36 || id > 44) {
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, selectedSlot, SlotActionType.SWAP, mc.player);
                                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                            }
                            this.resetIntervalTimerForPacket.reset();
                        }
                    });
                }
            }
            if (this.autoLoot.isState()) {
                Box box = mc.player.getBoundingBox().expand(6.0);
                for (MerchantEntity merchantEntity : mc.world.getEntitiesByClass(MerchantEntity.class, box, Entity::isAlive)) {
                    if (merchantEntity.hasStackEquipped(EquipmentSlot.MAINHAND) || merchantEntity.hasStackEquipped(EquipmentSlot.OFFHAND)) {
                        Vec3d closestPoint = NearestPointUtil.getClosestPoint(merchantEntity);
                        if (closestPoint == null) {
                            closestPoint = merchantEntity.getBoundingBox().getCenter();
                        }
                        Vec2f rotations = RotationUtil.getRotations(closestPoint);
                        this.resetIntervalTimerForUpdate.reset();
                        this.uuid = merchantEntity.getUuid();
                        if (mc.player.getEyePos().distanceTo(merchantEntity.getBoundingBox().getCenter()) <= 6.0) {
                            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(merchantEntity, false, Hand.MAIN_HAND, merchantEntity.getBoundingBox().getCenter()));
                            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(merchantEntity, false, Hand.MAIN_HAND));
                            RotationStorage.update(new Rotation(rotations.x, rotations.y), 180.0F, 180.0F, 10, 10);
                        }
                        break;
                    }
                }
            }
            this.addTickStepTickTaskScheduler.cleanupIfFinished().update();
            this.blockStateByBlockPos.clear();
            this.removeIfServerHelperDroppedItemEntries.removeIf(inputValue -> inputValue.lastUpdateTimestamp - System.currentTimeMillis() <= 0L);
            this.entrySetLongEntries.entrySet().removeIf(inputResult -> inputResult.getValue() <= System.currentTimeMillis());
        } else {
            Object value = null;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, value, (accessReceiver, accessValue) -> {
                accessReceiver.serverHelperInventoryAction = (InventoryAction) accessValue;
            });
            this.useItemPending = false;
            this.cachedCountForEnable = 0;
            this.slotBeforeRepair = -1;
            this.updatePopPause();
        }
    }

    public String getItemKey(BoundItem boundItem) {
        String text = boundItem.setting.name();
        return switch(text) {
            case "Взрывная трапка" ->
                "dtrap";
            case "Обычная трапка" ->
                "trap_holy";
            case "Стан" ->
                "stan";
            case "Взрывная штучка" ->
                "ditem";
            case "Снежок заморозка" ->
                "snow";
            case "Божья аура" ->
                "bojaura";
            case "Трапка" ->
                "trap";
            case "Пласт" ->
                "plast";
            case "Явная пыль" ->
                "sugar";
            case "Огненный смерч" ->
                "fireSwirl";
            case "Дезориентация" ->
                "disorientation";
            case "Светильник Джека" ->
                "tikva";
            case "Пузырь опыта" ->
                "exp";
            case "Рюкзак 1 уровня" ->
                "shulker1";
            case "Рюкзак 2 уровня" ->
                "shulker2";
            case "Рюкзак 3 уровня" ->
                "shulker3";
            case "Рюкзак 4 уровня" ->
                "shulker4";
            case "Зелье Ассасина" ->
                "assassin";
            case "Зелье Паладина" ->
                "paladin";
            case "Зелье Радиации" ->
                "radiation";
            case "Зелье Гнева" ->
                "rage";
            case "Святая вода" ->
                "holyWater";
            case "Хлопушка" ->
                "clapper";
            case "Снотворное" ->
                "sleeping";
            default ->
                null;
        };
    }

    private void updateKeyPressedWithHandle(String text, Slot slot, ItemAction itemAction, boolean flag) {
        int intValue = this.calculateInventoryWithIndex(slot);
        if (intValue != -1) {
            this.updateInventoryWithInvokeSyncSelectedSlot(intValue);
        } else {
            this.previousSelectedSlot = mc.player.getInventory().selectedSlot;
            this.clickSlot = slot.id;
            this.pendingInventorySlot = slot.id;
            this.forwardKeyWasPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
            this.backKeyWasPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
            this.leftKeyWasPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
            this.rightKeyWasPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
            this.serverHelperInventoryAction = new InventoryAction(text, slot, itemAction, flag);
            this.updatePushPause();
            this.cachedCountForEnable = 5;
            this.applyPressedWithCurrentTimeMillis();
        }
    }

    private int calculateInventoryWithIndex(Slot slot) {
        if (slot.inventory != mc.player.getInventory()) {
            return -1;
        } else {
            int index = slot.getIndex();
            return index >= 0 && index < 9 ? index : -1;
        }
    }

    private void updateInventoryWithInvokeSyncSelectedSlot(int selectedSlot) {
        if (mc.player != null && mc.interactionManager != null && mc.player.networkHandler != null) {
            int slotBeforeRepair = mc.player.getInventory().selectedSlot;
            if (slotBeforeRepair != selectedSlot) {
                mc.player.getInventory().selectedSlot = selectedSlot;
                ((IClientPlayerInteractionManager) mc.interactionManager).invokeSyncSelectedSlot();
            }
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (slotBeforeRepair != selectedSlot) {
                this.slotBeforeRepair = slotBeforeRepair;
            }
        }
    }

    private void sendInventoryWithPacket() {
        if (this.serverHelperInventoryAction != null) {
            switch(this.serverHelperInventoryAction.cachedCount) {
                case 0:
                    this.updateSprinting();
                    this.serverHelperInventoryAction.cachedCount = 1;
                    break;
                case 1:
                    if (this.pendingInventorySlot >= 0 && this.pendingInventorySlot < 9) {
                        mc.player.getInventory().selectedSlot = this.pendingInventorySlot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.pendingInventorySlot));
                    } else if (this.pendingInventorySlot >= 36 && this.pendingInventorySlot < 45) {
                        int selectedSlot = this.pendingInventorySlot + -36;
                        mc.player.getInventory().selectedSlot = selectedSlot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                        this.pendingInventorySlot = selectedSlot;
                    } else {
                        int pendingInventorySlot = mc.player.getInventory().selectedSlot;
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.pendingInventorySlot, pendingInventorySlot, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                        this.pendingInventorySlot = pendingInventorySlot;
                    }
                    this.serverHelperInventoryAction.cachedCount = 2;
                    break;
                case 2:
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.serverHelperInventoryAction.cachedCount = 3;
                    break;
                case 3:
                    boolean flag = (this.clickSlot < 0 || this.clickSlot >= 9) && (this.clickSlot < 36 || this.clickSlot >= 45);
                    if (flag) {
                        int selectedSlotValue = mc.player.getInventory().selectedSlot;
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.clickSlot, selectedSlotValue, SlotActionType.SWAP, mc.player);
                        mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    }
                    if (mc.player.getInventory().selectedSlot != this.previousSelectedSlot) {
                        mc.player.getInventory().selectedSlot = this.previousSelectedSlot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.previousSelectedSlot));
                    }
                    this.serverHelperInventoryAction = null;
                    this.previousSelectedSlot = -1;
                    this.pendingInventorySlot = -1;
                    this.clickSlot = -1;
            }
        }
    }

    private boolean isLocalState() {
        return this.cachedCountForEnable > 0 || this.serverHelperInventoryAction != null && this.serverHelperInventoryAction.cachedCount < 3;
    }

    private void updateSprinting() {
        this.applyPressedWithCurrentTimeMillis();
        if (mc.player.input != null) {
            mc.player.input.movementForward = 0.0F;
            mc.player.input.movementSideways = 0.0F;
        }
        mc.player.setSprinting(false);
    }

    private void applyPressedWithCurrentTimeMillis() {
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
            if (!this.repairPending) {
                this.repairPending = true;
                this.lastUpdateMillis = System.currentTimeMillis();
            }
        }
    }

    private void applyKeyPressedWithHandle() {
        if (this.repairPending) {
            boolean keyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
            boolean keyPressedValue = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
            boolean keyPressedResult = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
            boolean keyPressedItem = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
            boolean keyPressedEntry = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());
            mc.options.forwardKey.setPressed(keyPressed);
            mc.options.backKey.setPressed(keyPressedValue);
            mc.options.leftKey.setPressed(keyPressedResult);
            mc.options.rightKey.setPressed(keyPressedItem);
            mc.options.jumpKey.setPressed(keyPressedEntry);
            this.repairPending = false;
        }
    }

    private void updatePushPause() {
        if (!this.repairItemSelected) {
            Sprint.pushPause(1000L);
            this.repairItemSelected = true;
        }
    }

    private void updatePopPause() {
        if (this.repairItemSelected && this.cachedCountForEnable <= 0 && (this.serverHelperInventoryAction == null || this.serverHelperInventoryAction.cachedCount >= 3)) {
            this.repairItemSelected = false;
            Sprint.popPause();
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
    }

    private void updateBlockPos(BlockPos position, Vec3d positionValue, float floatValue, int intValue) {
        double doubleValue = floatValue / 2.0;
        double doubleValueValue = position.getX() + 0.5 - doubleValue + positionValue.x;
        double doubleValueResult = position.getY() + positionValue.y;
        double doubleValueItem = position.getZ() + 0.5 - doubleValue + positionValue.z;
        double doubleValueEntry = position.getX() + 0.5 + doubleValue + positionValue.x;
        double doubleValueCopy = position.getY() + floatValue + positionValue.y;
        double doubleValue2 = position.getZ() + 0.5 + doubleValue + positionValue.z;
        this.renderDepthMaskWithBegin(new Box(doubleValueValue, doubleValueResult, doubleValueItem, doubleValueEntry, doubleValueCopy, doubleValue2), intValue, 2.0F);
    }

    private void renderDepthMaskWithBegin(Box box, int intValue, float floatValue) {
        if (mc.world != null && mc.player != null) {
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Tessellator instance = Tessellator.getInstance();
            float floatValueValue = ColorMathUtil.redf(intValue);
            float floatValueResult = ColorMathUtil.greenf(intValue);
            float floatValueItem = ColorMathUtil.bluef(intValue);
            float floatValueEntry = 0.18F;
            BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            this.appendFilledMarkerBox(buffer, box, pos, floatValueValue, floatValueResult, floatValueItem, floatValueEntry);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.lineWidth(floatValue);
            BufferBuilder bufferValue = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            this.appendMarkerBoxOutline(bufferValue, box, pos, floatValueValue, floatValueResult, floatValueItem, 1.0F);
            BufferRenderer.drawWithGlobalProgram(bufferValue.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

    private void appendFilledMarkerBox(BufferBuilder buffer, Box box, Vec3d position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float floatValueEntry = (float) (box.minX - position.x);
        float floatValueCopy = (float) (box.minY - position.y);
        float floatValue2 = (float) (box.minZ - position.z);
        float floatValue3 = (float) (box.maxX - position.x);
        float floatValue4 = (float) (box.maxY - position.y);
        float floatValue5 = (float) (box.maxZ - position.z);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    private void appendMarkerBoxOutline(BufferBuilder buffer, Box box, Vec3d position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float floatValueEntry = (float) (box.minX - position.x);
        float floatValueCopy = (float) (box.minY - position.y);
        float floatValue2 = (float) (box.minZ - position.z);
        float floatValue3 = (float) (box.maxX - position.x);
        float floatValue4 = (float) (box.maxY - position.y);
        float floatValue5 = (float) (box.maxZ - position.z);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValue3, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValueCopy, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(floatValueEntry, floatValue4, floatValue5).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    private void renderDepthMaskWithColor(MatrixStack matrices, float floatValue, int intValue) {
        if (mc.world != null && mc.player != null) {
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            double doubleValue = mc.player.getX() - pos.x;
            double doubleValueValue = mc.player.getY() - pos.y;
            double doubleValueResult = mc.player.getZ() - pos.z;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Tessellator instance = Tessellator.getInstance();
            BufferBuilder buffer = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            float floatValueValue = ColorMathUtil.redf(intValue);
            float floatValueResult = ColorMathUtil.greenf(intValue);
            float floatValueItem = ColorMathUtil.bluef(intValue);
            float floatValueEntry = 1.0F;
            byte byteValue = 60;
            for (int index = 0; index < byteValue; index++) {
                double doubleValueItem = index * 2 * Math.PI / byteValue;
                double doubleValueEntry = (index + 1) * 2 * Math.PI / byteValue;
                buffer.vertex((float) (doubleValue + Math.cos(doubleValueItem) * floatValue), (float) doubleValueValue, (float) (doubleValueResult + Math.sin(doubleValueItem) * floatValue)).color(floatValueValue, floatValueResult, floatValueItem, floatValueEntry);
                buffer.vertex((float) (doubleValue + Math.cos(doubleValueEntry) * floatValue), (float) doubleValueValue, (float) (doubleValueResult + Math.sin(doubleValueEntry) * floatValue)).color(floatValueValue, floatValueResult, floatValueItem, floatValueEntry);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
    }

    private void updateEastWithDown(BlockPos position, Vec3d positionValue, int intValue, int intValueValue, boolean flag) {
        Vec3d positionResult;
        Vec3d positionItem;
        if (flag) {
            positionResult = Vec3d.of(position.down(2).east(3 * intValueValue)).add(positionValue);
            positionItem = Vec3d.of(position.up(3).west(2 * intValueValue).south(2)).add(positionValue);
        } else {
            positionResult = Vec3d.of(position.down(2).south(3 * intValueValue)).add(positionValue);
            positionItem = Vec3d.of(position.up(3).north(2 * intValueValue).east(2)).add(positionValue);
        }
        this.renderDepthMaskWithBegin(new Box(positionResult, positionItem), intValue, 3.0F);
    }

    private boolean isUpWithNorth(BlockPos position) {
        if (mc.world == null) {
            return false;
        } else {
            BlockPos[] positions = new BlockPos[] { position.north(), position.south(), position.east(), position.west(), position.up().north(), position.up().south(), position.up().east(), position.up().west() };
            for (BlockPos positionValue : positions) {
                Item item = mc.world.getBlockState(positionValue).getBlock().asItem();
                if (item != Items.OBSIDIAN && item != Items.CRYING_OBSIDIAN && item != Items.BEDROCK) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isAsItemWithBlock(BlockPos position) {
        if (mc.world == null) {
            return false;
        } else {
            int intValue = 0;
            for (int index = -1; index <= 1; index++) {
                for (int indexValue = -1; indexValue <= 1; indexValue++) {
                    if (index != 0 || indexValue != 0) {
                        BlockPos positionValue = position.add(index, 0, indexValue);
                        Item item = mc.world.getBlockState(positionValue).getBlock().asItem();
                        if (item == Items.OBSIDIAN || item == Items.CRYING_OBSIDIAN || item == Items.BEDROCK) {
                            intValue++;
                        }
                    }
                }
            }
            return intValue >= 6;
        }
    }

    private void updateItem(Item item, Vec3d position, long longValue) {
        this.removeIfServerHelperDroppedItemEntries.add(new DroppedItem(item, position, longValue));
    }

    private Slot createSlotFromStackWithMatchesItem(ItemAction itemAction) {
        if (mc.player == null) {
            return null;
        } else {
            for (Slot slot : mc.player.currentScreenHandler.slots) {
                ItemStack stack = slot.getStack();
                if (this.matchesItem(stack, itemAction)) {
                    return slot;
                }
            }
            return null;
        }
    }

    public boolean matchesItem(ItemStack stack, ItemAction itemAction) {
        if (stack.isEmpty()) {
            return false;
        } else {
            boolean flag = this.serverType.is("FunTime") && this.ftCopiesSetting.isState();
            if (itemAction.potionSignature != null) {
                if (stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION && stack.getItem() != Items.POTION) {
                    return false;
                } else if (flag) {
                    return this.isToLowerCase(stack.getName().getString().toLowerCase(), itemAction);
                } else {
                    PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
                    if (potionContentsComponent != null && potionContentsComponent.hasEffects()) {
                        for (PotionEffect potionEffect : itemAction.potionSignature.effects) {
                            boolean flagValue = false;
                            for (StatusEffectInstance statusEffectInstance : potionContentsComponent.getEffects()) {
                                if (statusEffectInstance.getEffectType().equals(potionEffect.effect()) && statusEffectInstance.getAmplifier() == potionEffect.amplifier()) {
                                    flagValue = true;
                                    break;
                                }
                            }
                            if (!flagValue) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            } else if (stack.getItem() != itemAction.item) {
                return false;
            } else {
                String normalizedText = stack.getName().getString().toLowerCase();
                if (itemAction.item == Items.NETHERITE_SCRAP) {
                    boolean flagResult = normalizedText.contains("трап") || normalizedText.contains("лом") || normalizedText.contains("незер") || normalizedText.contains("scrap");
                    return flag ? flagResult : stack.getComponents().size() == 11 && flagResult;
                } else if (itemAction.item == Items.SNOWBALL) {
                    boolean flagItem = normalizedText.contains("снеж") || normalizedText.contains("замороз") || normalizedText.contains("snow");
                    return flag ? flagItem : stack.getComponents().size() == 10 && flagItem;
                } else if (itemAction.item == Items.DRIED_KELP) {
                    boolean flagEntry = normalizedText.contains("пласт") || normalizedText.contains("ламин") || normalizedText.contains("kelp");
                    return flag ? flagEntry : stack.getComponents().size() == 12 && flagEntry;
                } else if (itemAction.item == Items.SUGAR) {
                    boolean flagCopy = normalizedText.contains("явн") || normalizedText.contains("пыль") || normalizedText.contains("sugar") || normalizedText.contains("сахар");
                    return flag ? flagCopy : stack.getComponents().size() == 10 && flagCopy;
                } else if (itemAction.item == Items.PHANTOM_MEMBRANE) {
                    boolean flag2 = normalizedText.contains("бож") || normalizedText.contains("аур") || normalizedText.contains("мембр") || normalizedText.contains("phant");
                    return flag ? flag2 : stack.getComponents().size() == 10 && flag2;
                } else if (itemAction.item == Items.FIRE_CHARGE) {
                    boolean flag3 = normalizedText.contains("смерч") || normalizedText.contains("огнен") || normalizedText.contains("заряд") || normalizedText.contains("файр") || normalizedText.contains("fire");
                    return flag ? flag3 : stack.getComponents().size() == 10 && flag3;
                } else if (itemAction.item == Items.ENDER_EYE && flag) {
                    return normalizedText.contains("дезориент") || normalizedText.contains("disorient") || itemAction.displayName != null && normalizedText.contains(itemAction.displayName.toLowerCase());
                } else {
                    return itemAction.searchName != null ? normalizedText.contains(itemAction.searchName.toLowerCase()) : true;
                }
            }
        }
    }

    private boolean isToLowerCase(String text, ItemAction itemAction) {
        if (itemAction.displayName == null) {
            return false;
        } else {
            String normalizedText = itemAction.displayName.toLowerCase();
            if (text.contains(normalizedText)) {
                return true;
            } else {
                return switch(normalizedText) {
                    case "зелье ассасина" ->
                        text.contains("ассасин");
                    case "зелье паладина" ->
                        text.contains("паладин");
                    case "зелье радиации" ->
                        text.contains("радиаци");
                    case "зелье гнева" ->
                        text.contains("гнев");
                    case "святая вода" ->
                        text.contains("святая");
                    case "хлопушка" ->
                        text.contains("хлопушк");
                    case "снотворное" ->
                        text.contains("снотворн");
                    default ->
                        false;
                };
            }
        }
    }

    private Slot createSlotFromStackWithItem() {
        if (mc.player == null) {
            return null;
        } else {
            for (Slot slot : mc.player.currentScreenHandler.slots) {
                ItemStack stack = slot.getStack();
                if (stack.getItem() == Items.EXPERIENCE_BOTTLE) {
                    NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
                    if (nbtComponent != null && nbtComponent.toString().contains("при нажатие ПКМ, полностью ремонтирует") && !mc.player.getItemCooldownManager().isCoolingDown(stack)) {
                        return slot;
                    }
                }
            }
            return null;
        }
    }

    private Slot findSlotFromStackWithItem() {
        if (mc.player == null) {
            return null;
        } else {
            Slot slot = null;
            int intValue = -1;
            for (Slot slotValue : mc.player.currentScreenHandler.slots) {
                ItemStack stack = slotValue.getStack();
                if (this.isPathWithId(stack.getItem())) {
                    ContainerComponent containerComponent = stack.get(DataComponentTypes.CONTAINER);
                    int intValueValue = 0;
                    if (containerComponent != null) {
                        intValueValue = (int) containerComponent.stream().filter(input -> !input.isEmpty()).count();
                    }
                    if (intValueValue > intValue) {
                        intValue = intValueValue;
                        slot = slotValue;
                    }
                }
            }
            return slot;
        }
    }

    private boolean isPathWithId(Item item) {
        String path = Registries.ITEM.getId(item).getPath();
        return path.contains("shulker_box");
    }

    private void sendPacketWithInventory(Slot slot) {
        if (mc.player != null && mc.interactionManager != null) {
            int id = slot.id;
            if (id >= 36 && id <= 44) {
                int intValue = id + -36;
                int selectedSlot = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(intValue));
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
            } else {
                int selectedSlotValue = mc.player.getInventory().selectedSlot;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, selectedSlotValue, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, selectedSlotValue, SlotActionType.SWAP, mc.player);
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }

    private boolean isOrDefaultWithCurrentTimeMillis(String text, ItemStack stack) {
        if (mc.player == null) {
            return false;
        } else {
            return this.entrySetLongEntries.getOrDefault(text, 0L) > System.currentTimeMillis() ? true : mc.player.getItemCooldownManager().isCoolingDown(stack);
        }
    }

    public List<BoundItem> getKeyBindings() {
        return this.keyBindings;
    }

    public BooleanSetting getScreenBindsSetting() {
        return this.screenBindsSetting;
    }

    public BooleanSetting getFtCopiesSetting() {
        return this.ftCopiesSetting;
    }

    public int getItemCount(ItemAction itemAction) {
        if (mc.player == null) {
            return 0;
        } else {
            int intValue = 0;
            for (ItemStack stack : mc.player.getInventory().main) {
                if (this.matchesItem(stack, itemAction)) {
                    intValue += stack.getCount();
                }
            }
            ItemStack offHandStack = mc.player.getOffHandStack();
            if (this.matchesItem(offHandStack, itemAction)) {
                intValue += offHandStack.getCount();
            }
            return intValue;
        }
    }

    public ItemAction getItemInfo(String text) {
        return this.itemInfo.get(text);
    }

    public void refreshScreenBindInventory() {
        if (mc.player == null) {
            this.itemStackByServerHelperItemAction.clear();
            this.areEqualItemStackValues = new ItemStack[0];
            this.object = null;
        } else {
            var main = mc.player.getInventory().main;
            int intValue = main.size() + 1;
            boolean stateFlagForRefreshScreenBindInventory = this.ftCopiesSetting.isState();
            boolean flag = this.object != mc.player || !Objects.equals(this.current, this.serverType.getCurrent()) || this.stateFlagForRefreshScreenBindInventory != stateFlagForRefreshScreenBindInventory;
            if (this.areEqualItemStackValues.length != intValue) {
                this.areEqualItemStackValues = new ItemStack[intValue];
                flag = true;
            }
            for (int index = 0; index < intValue; index++) {
                ItemStack stack = index < main.size() ? main.get(index) : mc.player.getOffHandStack();
                if (this.areEqualItemStackValues[index] == null || !ItemStack.areEqual(this.areEqualItemStackValues[index], stack)) {
                    this.areEqualItemStackValues[index] = stack.copy();
                    flag = true;
                }
            }
            if (flag) {
                this.itemStackByServerHelperItemAction.clear();
            }
            this.object = mc.player;
            this.current = this.serverType.getCurrent();
            this.stateFlagForRefreshScreenBindInventory = stateFlagForRefreshScreenBindInventory;
        }
    }

    public ItemAction getScreenBindInfo(BoundItem boundItem) {
        ItemAction itemAction = this.serverHelperItemActionByServerHelperBoundItem.get(boundItem);
        if (itemAction == null) {
            String itemKey = this.getItemKey(boundItem);
            itemAction = itemKey == null ? null : this.itemInfo.get(itemKey);
            if (itemAction != null) {
                this.serverHelperItemActionByServerHelperBoundItem.put(boundItem, itemAction);
            }
        }
        return itemAction;
    }

    public ItemStack getScreenBindStack(ItemAction itemAction) {
        ItemStack stack = this.itemStackByServerHelperItemAction.get(itemAction);
        if (stack != null) {
            return stack;
        } else {
            for (ItemStack stackValue : this.areEqualItemStackValues) {
                if (!stackValue.isEmpty()) {
                    Item item = stackValue.getItem();
                    if ((itemAction.potionSignature == null ? item == itemAction.item : item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.POTION) && this.matchesItem(stackValue, itemAction)) {
                        this.itemStackByServerHelperItemAction.put(itemAction, stackValue);
                        return stackValue;
                    }
                }
            }
            ItemStack stackResult = new ItemStack(itemAction.item);
            this.itemStackByServerHelperItemAction.put(itemAction, stackResult);
            return stackResult;
        }
    }

    private static int recoveredTypeSwitch(Object value, int intValue) {
        if (value == null) {
            return -1;
        } else if (intValue <= 0 && value instanceof ItemPickupAnimationS2CPacket) {
            return 0;
        } else if (intValue <= 1 && value instanceof ScreenHandlerSlotUpdateS2CPacket) {
            return 1;
        } else if (intValue <= 2 && value instanceof ChunkDeltaUpdateS2CPacket) {
            return 2;
        } else if (intValue <= 3 && value instanceof GameMessageS2CPacket) {
            return 3;
        } else {
            return intValue <= 4 && value instanceof OpenScreenS2CPacket ? 4 : 5;
        }
    }

    public static class InventoryAction {

        public String cachedText;

        public Slot slot;

        public ItemAction serverHelperItemAction;

        public boolean stateFlag;

        public int cachedCount;

        public InventoryAction(String cachedText, Slot slot, ItemAction serverHelperItemAction, boolean stateFlag) {
            this.cachedText = cachedText;
            this.slot = slot;
            this.serverHelperItemAction = serverHelperItemAction;
            this.stateFlag = stateFlag;
            this.cachedCount = 0;
        }
    }

    public static class PotionEffects {

        public final List<PotionEffect> effects;

        public PotionEffects(PotionEffect... potionEffect) {
            this.effects = List.of(potionEffect);
        }
    }

    public static record PotionEffect(RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int amplifier) {
    }

    public static class DroppedItem {

        public Item item;

        public Vec3d vec3d;

        public long lastUpdateTimestamp;

        public DroppedItem(Item item, Vec3d vec3d, long lastUpdateTimestamp) {
            this.item = item;
            this.vec3d = vec3d;
            this.lastUpdateTimestamp = lastUpdateTimestamp;
        }
    }

    public static class ItemAction {

        public final String searchName;

        public final Item item;

        public final String displayName;

        public final PotionEffects potionSignature;

        public final boolean addStarPrefix;

        public ItemAction(String searchName, Item item, String displayName, boolean addStarPrefix) {
            this.searchName = searchName;
            this.item = item;
            this.displayName = displayName;
            this.potionSignature = null;
            this.addStarPrefix = addStarPrefix;
        }

        public ItemAction(PotionEffects potionSignature, String displayName) {
            this.searchName = null;
            this.item = Items.SPLASH_POTION;
            this.displayName = displayName;
            this.potionSignature = potionSignature;
            this.addStarPrefix = true;
        }
    }

    public static class BoundItem {

        public final Item item;

        public final BindSetting setting;

        public final float cooldownSeconds;

        public BoundItem(Item item, BindSetting setting, float cooldownSeconds) {
            this.item = item;
            this.setting = setting;
            this.cooldownSeconds = cooldownSeconds;
        }
    }
}
