package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

public final class ViewModel extends Module {

    public static ViewModel INSTANCE = new ViewModel();

    public final FloatSetting mainHandX = new FloatSetting("Правая рука X", 0.0F, -2.0F, 2.0F, 0.01F);

    public final FloatSetting mainHandY = new FloatSetting("Правая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);

    public final FloatSetting mainHandZ = new FloatSetting("Правая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);

    public final FloatSetting offHandX = new FloatSetting("Левая рука X", 0.0F, -2.0F, 2.0F, 0.01F);

    public final FloatSetting offHandY = new FloatSetting("Левая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);

    public final FloatSetting offHandZ = new FloatSetting("Левая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);

    public final BooleanSetting onlyAura = new BooleanSetting("Только с аурой", false);

    public ViewModel() {
        super("ViewModel", "Оффсеты рук от первого лица", ModuleCategory.RENDER);
        this.addSettings(this.mainHandX, this.mainHandY, this.mainHandZ, this.offHandX, this.offHandY, this.offHandZ, this.onlyAura);
    }

    public void applyHandPosition(MatrixStack matrices, Arm arm) {
        if (arm == Arm.RIGHT) {
            matrices.translate(this.mainHandX.get(), this.mainHandY.get(), this.mainHandZ.get());
        } else {
            matrices.translate(this.offHandX.get(), this.offHandY.get(), this.offHandZ.get());
        }
    }
}
