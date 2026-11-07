package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.MultiSetting;

public final class NoPush extends Module {

    public static NoPush INSTANCE = new NoPush();

    private MultiSetting collisionList = new MultiSetting("Отключать от", new BooleanSetting("Игроки", true), new BooleanSetting("Блоки", true), new BooleanSetting("Удочка", true), new BooleanSetting("Вода", true));

    public NoPush() {
        super("NoPush", "Отключает отталкивание от выбранных настроек", ModuleCategory.PLAYER);
        this.addSettings(this.collisionList);
    }

    public MultiSetting getCollisionList() {
        return this.collisionList;
    }

    public void setCollisionList(MultiSetting collisionList) {
        this.collisionList = collisionList;
    }
}
