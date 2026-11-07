package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.ModeSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public final class AutoLeave extends Module {

    public static final AutoLeave INSTANCE = new AutoLeave();

    private static final Set<String> stringEntries = new HashSet<>(Arrays.asList("supp", "moder", "der", "adm", "wne", "curat", "dev", "модер", "помо", "адм", "владе", "курато", "сапп", "сотрудник"));

    private final FloatSetting detectionRadius = new FloatSetting("Радиус дистанции", 20.0F, 3.0F, 75.0F, 1.0F);

    private final MultiSetting leaveWhenDetected = new MultiSetting("Выходить если замечен", new BooleanSetting("Игрок", true), new BooleanSetting("Команда проекта", false));

    private final ModeSetting leaveDestination = new ModeSetting("Куда ливать", "/hub", "С Сервера", "/hub", "/spawn");

    private final BooleanSetting disableOnDisconnect = new BooleanSetting("Выключать после выхода", true);

    private int cachedCount;

    public AutoLeave() {
        super("AutoLeave", "Выходит с сервера, при указаной настройке", ModuleCategory.MISC);
        this.addSettings(this.detectionRadius, this.leaveWhenDetected, this.leaveDestination, this.disableOnDisconnect);
    }

    @Override
    public void onEnable() {
        this.cachedCount = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.cachedCount = 0;
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (this.cachedCount > 0) {
                this.cachedCount--;
            } else {
                float floatValue = this.detectionRadius.get();
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (player != null && player != mc.player && mc.player.distanceTo(player) <= floatValue && this.isPlayerEntity(player)) {
                        this.updateCurrent();
                        break;
                    }
                }
            }
        }
    }

    private boolean isPlayerEntity(PlayerEntity player) {
        return this.isNameWithStaff(player) ? this.leaveWhenDetected.is("Команда проекта") : this.leaveWhenDetected.is("Игрок");
    }

    private boolean isNameWithStaff(PlayerEntity player) {
        if (player == null) {
            return false;
        } else {
            String string = player.getName().getString();
            if (Toshiba.INSTANCE != null && Toshiba.INSTANCE.staffStorage != null && Toshiba.INSTANCE.staffStorage.isStaff(string)) {
                return true;
            } else {
                Team scoreboardTeam = player.getScoreboardTeam();
                if (scoreboardTeam == null) {
                    return false;
                } else {
                    String normalizedText = scoreboardTeam.getPrefix().getString().toLowerCase(Locale.ROOT);
                    for (String text : stringEntries) {
                        if (normalizedText.contains(text)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
    }

    private void updateCurrent() {
        String current = this.leaveDestination.getCurrent();
        switch(current) {
            case "С сервера":
                this.updateNetworkHandlerWithMessage();
                break;
            case "䈍纎퇬\ue113":
                this.updateNetworkHandlerWithMessage("hub");
                break;
            case "/home":
                this.updateNetworkHandlerWithMessage("home home");
                break;
            case "/spawn":
                this.updateNetworkHandlerWithMessage("spawn");
        }
    }

    private void updateNetworkHandlerWithMessage() {
        if (mc.getNetworkHandler() == null) {
            ChatUtil.sendMessage("false");
        } else {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLeave"));
            if (this.disableOnDisconnect.isState()) {
                this.toggle();
            }
        }
    }

    private void updateNetworkHandlerWithMessage(String text) {
        if (mc.getNetworkHandler() == null) {
            ChatUtil.sendMessage("false");
        } else {
            mc.getNetworkHandler().sendChatCommand(text);
            this.cachedCount = this.disableOnDisconnect.isState() ? 10 : 30;
            if (this.disableOnDisconnect.isState()) {
                this.toggle();
            }
        }
    }
}
