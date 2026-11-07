package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.storages.implement.FriendStorage;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public final class AutoTPAccept extends Module {

    public static AutoTPAccept INSTANCE = new AutoTPAccept();

    private final BooleanSetting friendsOnly = new BooleanSetting("Только друзья", false);

    public AutoTPAccept() {
        super("AutoTPAccept", "Автоматически принимает телепорт от игроков/друзей", ModuleCategory.MISC);
        this.addSettings(this.friendsOnly);
    }

    @EventHandler
    public void onEvent(EventPacket eventPacket) {
        if (mc.player != null && mc.world != null && eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
            String normalizedText = gameMessageS2CPacket.content().getString().toLowerCase(Locale.ROOT);
            if (normalizedText.contains("телепортироваться") || normalizedText.contains("запрашивает телепорт") || normalizedText.contains("просит к вам телепортироваться")) {
                if (this.friendsOnly.isState()) {
                    boolean flag = false;
                    if (brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.friendStorage) != null) {
                        for (String text : brova.toshiba.client.utils.runtime.AccessGuard.invoke(Toshiba.INSTANCE.friendStorage, new Object[] {}, (accessReceiver, accessArguments) -> ((FriendStorage) accessReceiver).getFriends())) {
                            if (normalizedText.contains(text.toLowerCase(Locale.ROOT))) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        return;
                    }
                }
                mc.player.networkHandler.sendChatCommand("tpaccept");
            }
        }
    }
}
