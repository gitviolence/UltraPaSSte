package brova.toshiba.client.core;

import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.gui.click.ClickGuiScreen;
import brova.toshiba.client.gui.hud.notification.NotificationManager;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.utils.discord.DiscordPresenceController;
import brova.toshiba.client.utils.neuro.NeuroManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;

public final class PanicController {

    private static final AtomicBoolean active = new AtomicBoolean();

    private PanicController() {
    }

    public static boolean isActive() {
        return active.get();
    }

    public static void activate() {
        if (active.compareAndSet(false, true)) {
            var iterator = new ArrayList<>(ModuleRegistry.INSTANCE.getObject()).iterator();
            while (true) {
                Module module;
                while (true) {
                    if (!iterator.hasNext()) {
                        NotificationManager.clear();
                        boolean flag = false;
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] { flag }, (accessReceiver, accessArguments) -> {
                            FreeLookStorage.setActive(((boolean) accessArguments[0]));
                        });
                        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> NeuroManager.getInstance()), new Object[] {}, (accessReceiver, accessArguments) -> {
                            ((NeuroManager) accessReceiver).shutdownForPanic();
                        });
                        DiscordPresenceController.shutdown();
                        MinecraftClient instance = MinecraftClient.getInstance();
                        if (instance.currentScreen instanceof ClickGuiScreen) {
                            instance.setScreen(null);
                        }
                        return;
                    }
                    module = iterator.next();
                    boolean flagValue = false;
                    try {
                        flagValue = true;
                        if (module.isEnable()) {
                            module.setEnabled(false);
                            flagValue = false;
                        } else {
                            flagValue = false;
                        }
                        break;
                    } catch (Throwable failure) {
                        flagValue = false;
                    } finally {
                        if (flagValue) {
                            boolean flagResult = false;
                            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(module, new Object[] { flagResult }, (accessReceiver, accessArguments) -> {
                                ((Module) accessReceiver).setEnable(((boolean) accessArguments[0]));
                            });
                            EventBus.unregister(module);
                        }
                    }
                    module.setEnable(false);
                    EventBus.unregister(module);
                }
                boolean flagItem = false;
                brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(module, new Object[] { flagItem }, (accessReceiver, accessArguments) -> {
                    ((Module) accessReceiver).setEnable(((boolean) accessArguments[0]));
                });
                EventBus.unregister(module);
            }
        }
    }
}
