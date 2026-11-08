package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;

public class ScoreboardHP extends Module {

    public static ScoreboardHP INSTANCE = new ScoreboardHP();

    public ScoreboardHP() {
        super("ScoreboardHP", "Отображает и рассчитывает HP через скорборд сервера", ModuleCategory.PLAYER);
    }
}
