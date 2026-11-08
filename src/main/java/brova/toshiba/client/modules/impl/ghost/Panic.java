package brova.toshiba.client.modules.impl.ghost;

import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;

public final class Panic extends Module {

    public Panic() {
        super("Panic", "Полностью скрывает и отключает клиент до перезапуска", ModuleCategory.GHOST);
    }

    @Override
    public void onEnable() {
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
            PanicController.activate();
        });
        boolean flag = false;
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { flag }, (accessReceiver, accessArguments) -> {
            ((Panic) accessReceiver).setEnable(((boolean) accessArguments[0]));
        });
    }
}
