package brova.toshiba.client.modules.impl.movement;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.CloseHandledScreenEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.utils.movement.StrafeUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public final class GuiMove extends Module {

    public static GuiMove INSTANCE = new GuiMove();

    public ModeSetting mode = new ModeSetting("Обход", "Обычный", "Обычный", "Легит");

    public int tick = 0;

    private final List<ClickSlotC2SPacket> clickSlotC2SPacketEntries = new ArrayList<>();

    private CloseHandledScreenC2SPacket closeHandledScreenC2SPacket = null;

    private boolean movementRestorePending = false;

    private boolean movementPaused = false;

    private int cachedCount = -1;

    private boolean stateFlagForSendNetworkHandlerWithPacket = false;

    public GuiMove() {
        super("GuiMove", "Ходьба с открытым инвентарём", ModuleCategory.MOVEMENT);
        this.addSettings(this.mode);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null) {
            KeyBinding[] keyBindings = new KeyBinding[] { mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey };
            if (this.mode.is("Легит") && this.cachedCount >= 0) {
                if (this.cachedCount == 0) {
                    this.sendNetworkHandlerWithPacket(true);
                    this.cachedCount = -1;
                    this.tick = 1;
                } else {
                    this.cachedCount--;
                }
            }
            if (this.tick == 0 && !this.clickSlotC2SPacketEntries.isEmpty() && mc.currentScreen == null && !this.movementPaused) {
                this.sendNetworkHandlerWithPacket();
            }
            if (this.tick != 0) {
                for (KeyBinding keyBinding : keyBindings) {
                    keyBinding.setPressed(false);
                }
                this.tick--;
                if (this.tick == 0 && this.movementRestorePending) {
                    this.movementRestorePending = false;
                    Sprint.popPause();
                }
            } else if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof SignEditScreen) && (!this.mode.is("Легит") || !(mc.currentScreen instanceof HandledScreen) || mc.currentScreen instanceof InventoryScreen)) {
                if (this.movementPaused) {
                    for (KeyBinding keyBindingValue : keyBindings) {
                        keyBindingValue.setPressed(false);
                    }
                } else {
                    for (KeyBinding keyBindingResult : keyBindings) {
                        boolean keyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBindingResult.getDefaultKey().getCode());
                        keyBindingResult.setPressed(keyPressed);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == Type.SEND && !this.stateFlagForSendNetworkHandlerWithPacket) {
            var packet = eventPacket.getPacket();
            if (this.mode.is("Легит") && StrafeUtil.isMoving() && mc.currentScreen instanceof InventoryScreen) {
                if (packet instanceof ClickSlotC2SPacket clickSlotC2SPacket) {
                    this.clickSlotC2SPacketEntries.add(clickSlotC2SPacket);
                    eventPacket.cancel();
                } else if (packet instanceof CloseHandledScreenC2SPacket closeHandledScreenC2SPacket) {
                    this.closeHandledScreenC2SPacket = closeHandledScreenC2SPacket;
                    this.cachedCount = 1;
                    this.movementPaused = false;
                    this.updatePushPause();
                    eventPacket.cancel();
                }
            }
        }
    }

    @EventHandler
    public void onCloseInv(CloseHandledScreenEvent event) {
        if (this.mode.is("Легит") && StrafeUtil.isMoving() && mc.currentScreen instanceof InventoryScreen) {
            CloseHandledScreenC2SPacket closeHandledScreenC2SPacket = new CloseHandledScreenC2SPacket(event.windowId);
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, closeHandledScreenC2SPacket, (accessReceiver, accessValue) -> {
                accessReceiver.closeHandledScreenC2SPacket = accessValue;
            });
            this.cachedCount = 1;
            this.updatePushPause();
            this.tick = 1;
            event.cancel();
        } else if (this.mode.is("Легит") && !this.movementPaused) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((GuiMove) accessReceiver).updatePushPause();
            });
            this.tick = 1;
        }
    }

    private void updatePushPause() {
        if (!this.movementRestorePending) {
            Sprint.pushPause(0L);
            this.movementRestorePending = true;
        }
    }

    private void sendNetworkHandlerWithPacket() {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            this.stateFlagForSendNetworkHandlerWithPacket = true;
            try {
                for (ClickSlotC2SPacket clickSlotC2SPacket : this.clickSlotC2SPacketEntries) {
                    mc.getNetworkHandler().sendPacket(clickSlotC2SPacket);
                }
            } finally {
                this.stateFlagForSendNetworkHandlerWithPacket = false;
            }
            this.clickSlotC2SPacketEntries.clear();
        } else {
            this.clickSlotC2SPacketEntries.clear();
        }
    }

    private void sendNetworkHandlerWithPacket(boolean flag) {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            this.sendNetworkHandlerWithPacket();
            if (flag && this.closeHandledScreenC2SPacket != null) {
                this.stateFlagForSendNetworkHandlerWithPacket = true;
                try {
                    mc.getNetworkHandler().sendPacket(this.closeHandledScreenC2SPacket);
                } finally {
                    this.stateFlagForSendNetworkHandlerWithPacket = false;
                }
                this.closeHandledScreenC2SPacket = null;
            }
        } else {
            this.clickSlotC2SPacketEntries.clear();
            this.closeHandledScreenC2SPacket = null;
        }
    }

    public static void stopTick(int intValue) {
        GuiMove guiMove = ModuleRegistry.guiMove;
        if (guiMove != null && guiMove.isEnable()) {
            guiMove.tick = Math.max(guiMove.tick, intValue);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        boolean flag = true;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { flag }, (accessReceiver, accessArguments) -> {
            ((GuiMove) accessReceiver).sendNetworkHandlerWithPacket(((boolean) accessArguments[0]));
        });
        if (this.movementRestorePending) {
            this.movementRestorePending = false;
            Sprint.popPause();
        }
        this.movementPaused = false;
        this.cachedCount = -1;
        this.stateFlagForSendNetworkHandlerWithPacket = false;
        this.tick = 0;
    }
}
