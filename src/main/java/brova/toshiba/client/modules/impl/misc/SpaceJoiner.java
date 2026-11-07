package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.SpaceJoiner.JoinState;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import brova.toshiba.client.utils.time.IntervalTimer;
import java.util.Locale;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public final class SpaceJoiner extends Module {

    private final FloatSetting anarchyNumber = new FloatSetting("Номер анархии", 1.0F, 1.0F, 6.0F, 1.0F);

    private final IntervalTimer intervalTimer = new IntervalTimer();

    private JoinState spaceJoinerJoinState;

    public SpaceJoiner() {
        super("SpaceJoiner", "Подключает к выбранной анархии через предмет хранилища", ModuleCategory.MISC);
        this.addSettings(this.anarchyNumber);
    }

    @Override
    public void onEnable() {
        this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
        this.intervalTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.spaceJoinerJoinState = null;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null && mc.interactionManager != null && this.spaceJoinerJoinState != null) {
            if (this.spaceJoinerJoinState != JoinState.USE_HONEYCOMB) {
                if (mc.currentScreen instanceof GenericContainerScreen genericContainerScreen) {
                    String normalizedText = genericContainerScreen.getTitle().getString().toLowerCase(Locale.ROOT);
                    if (normalizedText.contains("выберите режим")) {
                        if (this.spaceJoinerJoinState == JoinState.WAIT_SERVER_RESULT) {
                            this.spaceJoinerJoinState = JoinState.WAIT_MODE_MENU;
                            this.intervalTimer.reset();
                        }
                        if (this.spaceJoinerJoinState == JoinState.WAIT_MODE_MENU && this.intervalTimer.finished(250L)) {
                            this.selectAnarchyMode(genericContainerScreen);
                        } else if (this.intervalTimer.finished(2500L)) {
                            this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
                            this.intervalTimer.reset();
                        }
                    } else if (!normalizedText.contains("выберите сервер")) {
                        if (this.intervalTimer.finished(2500L)) {
                            mc.player.closeHandledScreen();
                            this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
                            this.intervalTimer.reset();
                        }
                    } else if (this.spaceJoinerJoinState == JoinState.WAIT_SERVER_MENU && this.intervalTimer.finished(250L)) {
                        this.selectAnarchyServer(genericContainerScreen);
                    } else if (this.intervalTimer.finished(2500L)) {
                        this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
                        this.intervalTimer.reset();
                    }
                } else if (this.intervalTimer.finished(2500L)) {
                    this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
                    this.intervalTimer.reset();
                }
            } else if (this.intervalTimer.finished(250L)) {
                int selectedSlot = this.calculateOfWithStack();
                if (selectedSlot == -1) {
                    this.setEnabled(false);
                } else {
                    mc.player.getInventory().selectedSlot = selectedSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    this.spaceJoinerJoinState = JoinState.WAIT_MODE_MENU;
                    this.intervalTimer.reset();
                }
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
            String normalizedText = gameMessageS2CPacket.content().getString().toLowerCase(Locale.ROOT);
            if (normalizedText.contains("заблокирован") || normalizedText.contains("забанен")) {
                ChatUtil.sendMessage("Забанен на сервере, отключаю модуль");
                this.setEnabled(false);
                return;
            }
            if (this.spaceJoinerJoinState == JoinState.WAIT_SERVER_RESULT && (normalizedText.contains("кикнут при подключении") || normalizedText.contains("сервер заполнен") || normalizedText.contains("ошибка") || normalizedText.contains("не удалось"))) {
                if (mc.player != null) {
                    mc.player.closeHandledScreen();
                }
                this.spaceJoinerJoinState = JoinState.USE_HONEYCOMB;
                this.intervalTimer.reset();
            }
        }
    }

    private void selectAnarchyMode(GenericContainerScreen genericContainerScreen) {
        ScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
        int intValue = this.calculateStackWithSlot(screenHandler, "анархия 1.21", true);
        if (intValue != -1) {
            mc.interactionManager.clickSlot(screenHandler.syncId, intValue, 0, SlotActionType.PICKUP, mc.player);
            this.spaceJoinerJoinState = JoinState.WAIT_SERVER_MENU;
            this.intervalTimer.reset();
        }
    }

    private void selectAnarchyServer(GenericContainerScreen genericContainerScreen) {
        ScreenHandler screenHandler = genericContainerScreen.getScreenHandler();
        int intValue = this.calculateStackWithSlot(screenHandler, "анархия #" + Math.round(this.anarchyNumber.get()), false);
        if (intValue != -1) {
            mc.interactionManager.clickSlot(screenHandler.syncId, intValue, 0, SlotActionType.PICKUP, mc.player);
            this.spaceJoinerJoinState = JoinState.WAIT_SERVER_RESULT;
            this.intervalTimer.reset();
        }
    }

    private int calculateOfWithStack() {
        for (int index = 0; index < 9; index++) {
            if (mc.player.getInventory().getStack(index).isOf(Items.HONEYCOMB)) {
                return index;
            }
        }
        return -1;
    }

    private int calculateStackWithSlot(ScreenHandler screenHandler, String text, boolean flag) {
        int intValue = screenHandler.slots.size() + -36;
        for (int index = 0; index < intValue; index++) {
            ItemStack stack = screenHandler.getSlot(index).getStack();
            if (!stack.isEmpty() && (!flag || stack.isOf(Items.PLAYER_HEAD)) && this.isLinesWithToLowerCase(stack, text)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isLinesWithToLowerCase(ItemStack stack, String text) {
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return false;
        } else {
            for (Text textValue : loreComponent.lines()) {
                if (textValue.getString().toLowerCase(Locale.ROOT).contains(text)) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum JoinState {

        USE_HONEYCOMB, WAIT_MODE_MENU, WAIT_SERVER_MENU, WAIT_SERVER_RESULT
    }
}
