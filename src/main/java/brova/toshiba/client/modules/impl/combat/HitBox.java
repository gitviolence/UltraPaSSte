package brova.toshiba.client.modules.impl.combat;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;

public final class HitBox extends Module {

    public static HitBox INSTANCE;

    private final FloatSetting verticalExpansion = new FloatSetting("Вертикально", 0.05F, 0.05F, 3.0F, 0.05F);

    private final FloatSetting horizontalExpansion = new FloatSetting("Горизонтально", 0.05F, 0.05F, 3.0F, 0.05F);

    public HitBox() {
        super("HitBox", "Расширяет хитбоксы живых существ", ModuleCategory.COMBAT);
        this.addSettings(this.verticalExpansion, this.horizontalExpansion);
        HitBox.INSTANCE = this;
    }

    public Box expand(Entity entity, Box box) {
        if (this.isEnable() && entity != mc.player && entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity)) {
            double doubleValue = this.horizontalExpansion.get() * 0.5;
            double doubleValueValue = this.verticalExpansion.get();
            return new Box(box.minX - doubleValue, box.minY - doubleValueValue, box.minZ - doubleValue, box.maxX + doubleValue, box.maxY + doubleValueValue, box.maxZ + doubleValue);
        } else {
            return box;
        }
    }
}
