package brova.toshiba.client.storages.implement;

import brova.toshiba.Toshiba;
import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.data.Macro;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;

public final class MacroStorage implements MinecraftAccess {

    private final List<Macro> macros = new ArrayList<>();

    private final List<String> names = new ArrayList<>();

    public MacroStorage() {
        EventBus.register(this);
    }

    public void add(Macro macro) {
        if (macro != null && macro.getName() != null && !macro.getName().isBlank() && this.getMacro(macro.getName()) == null) {
            this.macros.add(macro);
            this.names.add(macro.getName());
        }
    }

    public void remove(Macro macro) {
        if (macro != null) {
            this.macros.remove(macro);
            this.names.remove(macro.getName());
        }
    }

    public void clear() {
        if (!this.macros.isEmpty()) {
            this.macros.clear();
        }
        if (!this.names.isEmpty()) {
            this.names.clear();
        }
    }

    public boolean isEmpty() {
        return this.macros.isEmpty();
    }

    public Macro getMacro(String text) {
        for (Macro macro : this.macros) {
            if (macro.getName().equalsIgnoreCase(text)) {
                return macro;
            }
        }
        return null;
    }

    @EventHandler
    public void onKey(KeyBind keyBind) {
        if (mc.player != null && mc.world != null && mc.currentScreen == null && mc.player.networkHandler != null && !this.macros.isEmpty()) {
            for (Macro macro : this.macros) {
                if (macro != null && macro.getBind() != null && macro.getBind().getKey() == keyBind.getKey()) {
                    this.updateStartsWithWithSubstring(macro);
                }
            }
        }
    }

    private void updateStartsWithWithSubstring(Macro macro) {
        String command = macro.getCommand();
        if (command != null && !command.isBlank()) {
            if (command.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(command.substring(1));
            } else {
                String prefix = Toshiba.INSTANCE.commandStorage.getPrefix();
                if (prefix != null && !prefix.isEmpty() && command.startsWith(prefix)) {
                    try {
                        brova.toshiba.client.utils.runtime.AccessGuard.read(Toshiba.INSTANCE, accessReceiver -> accessReceiver.commandStorage).getDispatcher().execute(command.substring(prefix.length()), Toshiba.INSTANCE.commandStorage.getSource());
                    } catch (CommandSyntaxException failure) {
                        ChatUtil.sendMessage(Formatting.RED + "Ошибка в использовании макроса " + macro.getName() + "!");
                    }
                } else {
                    mc.player.networkHandler.sendChatMessage(command);
                }
            }
        }
    }

    public List<Macro> getMacros() {
        return this.macros;
    }

    public List<String> getNames() {
        return this.names;
    }
}
