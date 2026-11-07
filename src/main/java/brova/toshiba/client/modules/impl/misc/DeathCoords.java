package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class DeathCoords extends Module {

    public static DeathCoords INSTANCE = new DeathCoords();

    private final BooleanSetting copyToClipboard = new BooleanSetting("Копировать в буфер", true);

    private BlockPos blockPos = null;

    private boolean stateFlag = false;

    public DeathCoords() {
        super("DeathCoords", "Показывает координаты смерти", ModuleCategory.MISC);
        this.addSettings(this.copyToClipboard);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.stateFlag = false;
        this.blockPos = null;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (mc.player.getHealth() <= 0.0F && !this.stateFlag) {
                this.stateFlag = true;
                BlockPos blockPos = mc.player.getBlockPos();
                brova.toshiba.client.utils.runtime.AccessGuard.write(this, blockPos, (accessReceiver, accessValue) -> {
                    accessReceiver.blockPos = accessValue;
                });
                String text = "X: " + this.blockPos.getX() + " Y: " + this.blockPos.getY() + " Z: " + this.blockPos.getZ();
                String textValue = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this, new Object[] {}, (accessReceiver, accessArguments) -> ((DeathCoords) accessReceiver).createToStringWithRegistryKey());
                String textResult = "Кординаты смерти! " + text + " (" + textValue + ")";
                mc.player.sendMessage(Text.literal(textResult), false);
                if (this.copyToClipboard.isState()) {
                    mc.keyboard.setClipboard(this.blockPos.getX() + " " + this.blockPos.getY() + " " + this.blockPos.getZ());
                }
            }
            if (mc.player.getHealth() > 0.0F && this.stateFlag) {
                this.stateFlag = false;
            }
        }
    }

    private String createToStringWithRegistryKey() {
        if (mc.world == null) {
            return "Unknown";
        } else {
            String text = mc.world.getRegistryKey().getValue().toString();
            if (text.contains("overworld")) {
                return "Обычный мир";
            } else if (text.contains("nether")) {
                return "Незер мир";
            } else {
                return text.contains("end") ? "Энд мир" : text;
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.stateFlag = false;
        this.blockPos = null;
    }
}
