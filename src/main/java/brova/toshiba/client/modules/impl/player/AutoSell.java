package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.ScreenEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.player.AutoSell.ListingState;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public final class AutoSell extends Module {

    public static AutoSell INSTANCE = new AutoSell();

    private final BindSetting sellKeyBind = new BindSetting("Кнопка продажи", -1);

    private ListingState autoSellListingState = ListingState.IDLE;

    private int cachedCountForSetScreen = 0;

    private ItemStack empty = ItemStack.EMPTY;

    private String chatCommandText = "";

    private boolean stateFlag = false;

    private long lastUpdateTimestamp = -1L;

    private int sellPhase = 0;

    private int screenWaitTicks = 0;

    private long chatCommandTimestamp = -1L;

    private int sellDelayTicks = 0;

    private int confirmationTicks = 0;

    public AutoSell() {
        super("AutoSell", "Автоматически смотрит цены на аукционе FunTime и продает их по текущей цене", ModuleCategory.PLAYER);
        this.addSettings(this.sellKeyBind);
    }

    @Override
    public void onDisable() {
        ListingState IDLE = ListingState.IDLE;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, IDLE, (accessReceiver, accessValue) -> {
            accessReceiver.autoSellListingState = accessValue;
        });
        ItemStack EMPTY = ItemStack.EMPTY;
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, EMPTY, (accessReceiver, accessValue) -> {
            accessReceiver.empty = accessValue;
        });
        String text = "";
        brova.toshiba.client.utils.runtime.AccessGuard.write(this, text, (accessReceiver, accessValue) -> {
            accessReceiver.chatCommandText = accessValue;
        });
        this.stateFlag = false;
        this.lastUpdateTimestamp = -1L;
        this.sellPhase = 0;
        this.screenWaitTicks = 0;
        this.chatCommandTimestamp = -1L;
        this.sellDelayTicks = 0;
        this.confirmationTicks = 0;
        super.onDisable();
    }

    @EventHandler
    public void onSetScreen(ScreenEvent event) {
        Screen screen = event.getScreen();
        if (this.autoSellListingState == ListingState.AWAIT_SCREEN && screen != null && AhHelper.isAuction(screen)) {
            this.autoSellListingState = ListingState.SCANNING_DELAY;
            this.cachedCountForSetScreen = 3;
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket && this.autoSellListingState == ListingState.AWAIT_LISTING) {
            String normalizedText = gameMessageS2CPacket.content().getString().toLowerCase();
            if (normalizedText.contains("выставлен на продажу")) {
                ChatUtil.sendMessage("true");
                this.autoSellListingState = ListingState.IDLE;
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            boolean stateFlag = this.sellKeyBind.isPressed();
            if (stateFlag && !this.stateFlag && this.autoSellListingState == ListingState.IDLE) {
                this.updateMessageWithMainHandStack();
            }
            this.stateFlag = stateFlag;
            if ((this.autoSellListingState == ListingState.SCANNING_DELAY || this.autoSellListingState == ListingState.SCANNING) && !(mc.currentScreen instanceof GenericContainerScreen)) {
                this.autoSellListingState = ListingState.IDLE;
            } else {
                if (this.autoSellListingState == ListingState.SCANNING_DELAY) {
                    this.cachedCountForSetScreen--;
                    if (this.cachedCountForSetScreen <= 0) {
                        this.autoSellListingState = ListingState.SCANNING;
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                            ((AutoSell) accessReceiver).updateMessageWithCloseHandledScreen();
                        });
                    }
                }
                if (this.autoSellListingState == ListingState.AWAIT_LISTING) {
                    ItemStack mainHandStack = mc.player.getMainHandStack();
                    if (mainHandStack.isEmpty() || mainHandStack.getItem() != this.empty.getItem()) {
                        ChatUtil.sendMessage("true");
                        this.autoSellListingState = ListingState.IDLE;
                        return;
                    }
                    this.sellDelayTicks--;
                    if (this.sellDelayTicks <= 0) {
                        this.confirmationTicks++;
                        if (this.confirmationTicks >= 5) {
                            ChatUtil.sendMessage("false");
                            this.autoSellListingState = ListingState.IDLE;
                            return;
                        }
                        mc.player.networkHandler.sendChatCommand("ah sell " + this.chatCommandTimestamp);
                        this.sellDelayTicks = 15;
                    }
                }
            }
        } else {
            this.autoSellListingState = ListingState.IDLE;
        }
    }

    private void updateMessageWithMainHandStack() {
        ItemStack mainHandStack = mc.player.getMainHandStack();
        if (mainHandStack.isEmpty()) {
            ChatUtil.sendMessage("В руке нет предмета");
        } else if (this.isToLowerCaseWithName(mainHandStack)) {
            ChatUtil.sendMessage("Предмет является поддельным");
        } else {
            this.empty = mainHandStack.copy();
            this.chatCommandText = this.createTextFromNameWithToLowerCase(this.empty);
            this.lastUpdateTimestamp = -1L;
            this.sellPhase = 0;
            this.screenWaitTicks = 0;
            this.chatCommandTimestamp = -1L;
            this.sellDelayTicks = 0;
            this.confirmationTicks = 0;
            mc.player.networkHandler.sendChatCommand("ah search " + this.chatCommandText);
            this.autoSellListingState = ListingState.AWAIT_SCREEN;
        }
    }

    private void updateMessageWithCloseHandledScreen() {
        if (mc.currentScreen instanceof GenericContainerScreen genericContainerScreen) {
            GenericContainerScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
            int intValue = screenHandler.getRows() * 9;
            ArrayList<Long> timestamps = new ArrayList<>();
            int intValueValue = 0;
            for (int index = 0; index < Math.min(45, intValue); index++) {
                ItemStack stack = screenHandler.getSlot(index).getStack();
                if (!stack.isEmpty()) {
                    boolean itemWithName = this.isItemWithName(this.empty, stack);
                    long longValue = calculateLinesWithReplaceAll(stack);
                    if (itemWithName && longValue > 0L) {
                        intValueValue++;
                        long longValueValue = longValue / stack.getCount();
                        timestamps.add(longValueValue);
                    }
                }
            }
            boolean flag = this.empty.getItem() instanceof PotionItem && !isItemWithEffects(this.empty);
            int intValueResult = flag ? 5 : 4;
            if (intValueValue < intValueResult) {
                if (flag) {
                    ChatUtil.sendMessage("нету предмета - " + this.createTextFromReplaceAllWithTrim(this.empty.getName().getString()));
                } else {
                    ChatUtil.sendMessage("На аукционе мало предметов не могу высчитать цену");
                }
                mc.player.closeHandledScreen();
                this.autoSellListingState = ListingState.IDLE;
            } else {
                timestamps.sort(Long::compare);
                ArrayList<Long> timestampsValue = null;
                for (long longValueResult : timestamps) {
                    ArrayList<Long> timestampsResult = new ArrayList<>();
                    for (long longValueItem : timestamps) {
                        double doubleValue = (double) Math.abs(longValueResult - longValueItem) / longValueResult;
                        if (doubleValue <= 0.15) {
                            timestampsResult.add(longValueItem);
                        }
                    }
                    if (timestampsResult.size() >= intValueResult) {
                        timestampsValue = timestampsResult;
                        break;
                    }
                }
                if (timestampsValue == null) {
                    ChatUtil.sendMessage("Разброс цен слишком велик, не удалось определить стабильную стоимость");
                    mc.player.closeHandledScreen();
                    this.autoSellListingState = ListingState.IDLE;
                } else {
                    StringBuilder textBuilder = new StringBuilder("Цены: ");
                    for (int indexValue = 0; indexValue < timestampsValue.size(); indexValue++) {
                        textBuilder.append(timestampsValue.get(indexValue));
                        if (indexValue < timestampsValue.size() - 1) {
                            textBuilder.append(", ");
                        }
                    }
                    ChatUtil.sendMessage(textBuilder.toString());
                    long longValueEntry = 0L;
                    for (long longValueCopy : timestampsValue) {
                        longValueEntry += longValueCopy;
                    }
                    long lastUpdateTimestamp = longValueEntry / timestampsValue.size();
                    if (this.lastUpdateTimestamp == -1L) {
                        this.lastUpdateTimestamp = lastUpdateTimestamp;
                        this.sellPhase = 1;
                        this.updateClickSlotWithScreenHandler(genericContainerScreen);
                    } else {
                        double doubleValueValue = (double) Math.abs(lastUpdateTimestamp - this.lastUpdateTimestamp) / this.lastUpdateTimestamp;
                        if (doubleValueValue <= 0.15) {
                            this.sellPhase++;
                        } else {
                            this.sellPhase = 1;
                        }
                        this.lastUpdateTimestamp = lastUpdateTimestamp;
                        if (this.sellPhase >= 4) {
                            long longValue2 = (long) (lastUpdateTimestamp * 0.95);
                            long longValue3 = longValue2 * this.empty.getCount();
                            long chatCommandTimestamp = calculateTimestamp(longValue3);
                            this.chatCommandTimestamp = chatCommandTimestamp;
                            mc.player.networkHandler.sendChatCommand("ah sell " + chatCommandTimestamp);
                            mc.player.closeHandledScreen();
                            this.autoSellListingState = ListingState.AWAIT_LISTING;
                            this.sellDelayTicks = 15;
                            this.confirmationTicks = 0;
                        } else if (this.screenWaitTicks >= 15) {
                            ChatUtil.sendMessage("Не удалось стабилизировать цену после 15 попыток");
                            mc.player.closeHandledScreen();
                            ListingState IDLE = ListingState.IDLE;
                            brova.toshiba.client.utils.runtime.AccessGuard.write(this, IDLE, (accessReceiver, accessValue) -> {
                                accessReceiver.autoSellListingState = accessValue;
                            });
                        } else {
                            this.updateClickSlotWithScreenHandler(genericContainerScreen);
                        }
                    }
                }
            }
        } else {
            ListingState IDLEValue = ListingState.IDLE;
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, IDLEValue, (accessReceiver, accessValue) -> {
                accessReceiver.autoSellListingState = accessValue;
            });
        }
    }

    private void updateClickSlotWithScreenHandler(GenericContainerScreen genericContainerScreen) {
        int intValue = this.calculateStackWithSlot(genericContainerScreen);
        mc.interactionManager.clickSlot(genericContainerScreen.getScreenHandler().syncId, intValue, 0, SlotActionType.PICKUP, mc.player);
        this.autoSellListingState = ListingState.SCANNING_DELAY;
        this.cachedCountForSetScreen = 3;
        this.screenWaitTicks++;
    }

    private int calculateStackWithSlot(GenericContainerScreen genericContainerScreen) {
        GenericContainerScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
        int intValue = screenHandler.getRows() * 9;
        if (intValue > 49) {
            ItemStack stack = screenHandler.getSlot(49).getStack();
            if (!stack.isEmpty() && stack.getName().getString().toLowerCase().contains("обновить")) {
                return 49;
            }
        }
        if (intValue > 50) {
            ItemStack stackValue = screenHandler.getSlot(50).getStack();
            if (!stackValue.isEmpty() && stackValue.getName().getString().toLowerCase().contains("обновить")) {
                return 50;
            }
        }
        for (int index = Math.max(0, intValue - 9); index < intValue; index++) {
            ItemStack stackResult = screenHandler.getSlot(index).getStack();
            if (!stackResult.isEmpty() && stackResult.getName().getString().toLowerCase().contains("обновить")) {
                return index;
            }
        }
        return 50;
    }

    private static long calculateLinesWithReplaceAll(ItemStack stack) {
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return -1L;
        } else {
            for (Text text : loreComponent.lines()) {
                String string = text.getString();
                if (string.contains("Цен") || string.contains("$")) {
                    String textValue = string.replaceAll("[^0-9]", "");
                    if (!textValue.isEmpty()) {
                        try {
                            return Long.parseLong(textValue);
                        } catch (NumberFormatException failure) {
                        }
                    }
                }
            }
            return -1L;
        }
    }

    private String createTextFromNameWithToLowerCase(ItemStack stack) {
        String normalizedText = stack.getName().getString().toLowerCase();
        if (normalizedText.contains("black") || normalizedText.contains("блэк")) {
            return "блэк";
        } else if (normalizedText.contains("white") || normalizedText.contains("вайт")) {
            return "вайт";
        } else if (isItemWithEffects(stack)) {
            return this.createTextFromReplaceAllWithTrim(stack.getName().getString());
        } else if (stack.getItem() instanceof PotionItem) {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent != null) {
                var iterator = potionContentsComponent.getEffects().iterator();
                if (iterator.hasNext()) {
                    StatusEffectInstance statusEffectInstance = iterator.next();
                    return statusEffectInstance.getEffectType().value().getName().getString();
                }
            }
            return "Зелье";
        } else {
            return this.createTextFromReplaceAllWithTrim(stack.getName().getString());
        }
    }

    private boolean isItemWithName(ItemStack stack, ItemStack stackValue) {
        if (stackValue.isEmpty()) {
            return false;
        } else {
            boolean flag = stack.getItem() instanceof PotionItem;
            boolean flagValue = stackValue.getItem() instanceof PotionItem;
            if (flag != flagValue) {
                return false;
            } else if (flag) {
                PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
                PotionContentsComponent potionContentsComponentValue = stackValue.get(DataComponentTypes.POTION_CONTENTS);
                if (potionContentsComponent != null && potionContentsComponentValue != null) {
                    StatusEffectInstance statusEffectInstance = createIteratorWithEffects(potionContentsComponent);
                    StatusEffectInstance statusEffectInstanceValue = createIteratorWithEffects(potionContentsComponentValue);
                    return statusEffectInstance != null && statusEffectInstanceValue != null ? statusEffectInstance.getEffectType().equals(statusEffectInstanceValue.getEffectType()) && statusEffectInstance.getAmplifier() == statusEffectInstanceValue.getAmplifier() : false;
                } else {
                    return false;
                }
            } else {
                boolean itemWithEffects = isItemWithEffects(stack);
                boolean itemWithEffectsValue = isItemWithEffects(stackValue);
                if (itemWithEffects != itemWithEffectsValue) {
                    return false;
                } else if (itemWithEffects) {
                    if (stack.getItem() != stackValue.getItem()) {
                        return false;
                    } else {
                        String text = this.createTextFromReplaceAllWithTrim(stack.getName().getString());
                        String textValue = this.createTextFromReplaceAllWithTrim(stackValue.getName().getString());
                        return text.equalsIgnoreCase(textValue);
                    }
                } else if (stack.getItem() != stackValue.getItem()) {
                    return false;
                } else {
                    String normalizedText = stack.getName().getString().toLowerCase();
                    String normalizedTextValue = stackValue.getName().getString().toLowerCase();
                    if (!normalizedText.contains("black") && !normalizedText.contains("блэк")) {
                        return !normalizedText.contains("white") && !normalizedText.contains("вайт") ? !normalizedTextValue.contains("black") && !normalizedTextValue.contains("блэк") && !normalizedTextValue.contains("white") && !normalizedTextValue.contains("вайт") : normalizedTextValue.contains("white") || normalizedTextValue.contains("вайт");
                    } else {
                        return normalizedTextValue.contains("black") || normalizedTextValue.contains("блэк");
                    }
                }
            }
        }
    }

    private static StatusEffectInstance createIteratorWithEffects(PotionContentsComponent potionContentsComponent) {
        var iterator = potionContentsComponent.getEffects().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    private static boolean isItemWithEffects(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else if (stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION || stack.getItem() == Items.POTION) {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            return potionContentsComponent != null && potionContentsComponent.hasEffects() && isPotionContentsComponent(potionContentsComponent);
        } else if (stack.get(DataComponentTypes.CUSTOM_DATA) == null) {
            return false;
        } else {
            String normalizedText = stack.getName().getString().toLowerCase();
            Item item = stack.getItem();
            if (item == Items.ENDER_EYE && normalizedText.contains("дезориентация")) {
                return true;
            } else if (item == Items.SUGAR && normalizedText.contains("явная")) {
                return true;
            } else if (item == Items.PHANTOM_MEMBRANE && normalizedText.contains("божья аура")) {
                return true;
            } else if (item == Items.SNOWBALL && normalizedText.contains("снежок заморозка")) {
                return true;
            } else if (item == Items.DRIED_KELP && normalizedText.contains("пласт")) {
                return true;
            } else if (item == Items.NETHERITE_SCRAP && normalizedText.contains("трапка")) {
                return true;
            } else if (item != Items.FIRE_CHARGE || !normalizedText.contains("огненный смерч") && !normalizedText.contains("взрывная штучка")) {
                if (item == Items.PRISMARINE_SHARD && normalizedText.contains("взрывная трапка")) {
                    return true;
                } else if (item == Items.POPPED_CHORUS_FRUIT && normalizedText.contains("трапка")) {
                    return true;
                } else if (item == Items.NETHER_STAR && normalizedText.contains("стан")) {
                    return true;
                } else if (item == Items.JACK_O_LANTERN && normalizedText.contains("светильник джейка")) {
                    return true;
                } else {
                    return item == Items.EXPERIENCE_BOTTLE && normalizedText.contains("пузырь опыта") ? true : (item == Items.PINK_SHULKER_BOX || item == Items.BLUE_SHULKER_BOX || item == Items.RED_SHULKER_BOX) && normalizedText.contains("рюкзак");
                }
            } else {
                return true;
            }
        }
    }

    private static boolean isPotionContentsComponent(PotionContentsComponent potionContentsComponent) {
        if (isEffectsWithEquals(potionContentsComponent, StatusEffects.STRENGTH, 3) && isEffectsWithEquals(potionContentsComponent, StatusEffects.SPEED, 2)) {
            return true;
        } else if (isEffectsWithEquals(potionContentsComponent, StatusEffects.RESISTANCE, 0) && isEffectsWithEquals(potionContentsComponent, StatusEffects.FIRE_RESISTANCE, 0)) {
            return true;
        } else if (isEffectsWithEquals(potionContentsComponent, StatusEffects.POISON, 1) && isEffectsWithEquals(potionContentsComponent, StatusEffects.WITHER, 1)) {
            return true;
        } else if (isEffectsWithEquals(potionContentsComponent, StatusEffects.STRENGTH, 4) && isEffectsWithEquals(potionContentsComponent, StatusEffects.SLOWNESS, 3)) {
            return true;
        } else if (isEffectsWithEquals(potionContentsComponent, StatusEffects.REGENERATION, 1) && isEffectsWithEquals(potionContentsComponent, StatusEffects.INVISIBILITY, 1)) {
            return true;
        } else {
            return isEffectsWithEquals(potionContentsComponent, StatusEffects.SLOWNESS, 8) && isEffectsWithEquals(potionContentsComponent, StatusEffects.SPEED, 4) ? true : isEffectsWithEquals(potionContentsComponent, StatusEffects.WEAKNESS, 1) && isEffectsWithEquals(potionContentsComponent, StatusEffects.NAUSEA, 1);
        }
    }

    private static boolean isEffectsWithEquals(PotionContentsComponent potionContentsComponent, RegistryEntry<StatusEffect> registryEntry, int intValue) {
        for (StatusEffectInstance statusEffectInstance : potionContentsComponent.getEffects()) {
            if (statusEffectInstance.getEffectType().equals(registryEntry) && statusEffectInstance.getAmplifier() == intValue) {
                return true;
            }
        }
        return false;
    }

    private boolean isToLowerCaseWithName(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            String normalizedText = stack.getName().getString().toLowerCase();
            Item item = stack.getItem();
            boolean flag = item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.POTION;
            boolean flagValue = false;
            if (flag) {
                if (normalizedText.contains("ассасин") || normalizedText.contains("паладин") || normalizedText.contains("радиац") || normalizedText.contains("гнева") || normalizedText.contains("святая вода") || normalizedText.contains("хлопушка") || normalizedText.contains("снотворное")) {
                    flagValue = true;
                }
            } else if (normalizedText.contains("дезориентация") || normalizedText.contains("явная") || normalizedText.contains("божья аура") || normalizedText.contains("снежок заморозка") || normalizedText.contains("пласт") || normalizedText.contains("трапка") || normalizedText.contains("огненный смерч") || normalizedText.contains("взрывная трапка") || normalizedText.contains("стан") || normalizedText.contains("взрывная штучка") || normalizedText.contains("светильник джейка") || normalizedText.contains("пузырь опыта") || normalizedText.contains("рюкзак")) {
                flagValue = true;
            }
            return flagValue ? !isItemWithEffects(stack) : false;
        }
    }

    private String createTextFromReplaceAllWithTrim(String text) {
        return text.replaceAll("(?i)В§.", "").replaceAll("(?i)&.", "").replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9\\s-]", "").trim();
    }

    private static long calculateTimestamp(long longValue) {
        if (longValue >= 1000000L) {
            return (long) (Math.floor(longValue / 50000.0) * 50000.0);
        } else if (longValue >= 100000L) {
            return (long) (Math.floor(longValue / 10000.0) * 10000.0);
        } else if (longValue >= 10000L) {
            return (long) (Math.floor(longValue / 1000.0) * 1000.0);
        } else {
            return longValue >= 1000L ? (long) (Math.floor(longValue / 100.0) * 100.0) : (long) (Math.floor(longValue / 10.0) * 10.0);
        }
    }

    public enum ListingState {

        IDLE, AWAIT_SCREEN, SCANNING_DELAY, SCANNING, AWAIT_LISTING
    }
}
