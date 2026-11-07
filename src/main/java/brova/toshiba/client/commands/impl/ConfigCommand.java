package brova.toshiba.client.commands.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.configs.ConfigManager;
import brova.toshiba.client.configs.ConfigResult;
import brova.toshiba.client.configs.LocalConfigStorage;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;

/**
 * Локальные конфиги клиента: .config add/save/delete/load/list.
 * Полностью независимы от облачных конфигов.
 */
public final class ConfigCommand extends CommandBase {

    public ConfigCommand() {
        super("config");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("name", StringArgumentType.word()).suggests(this::suggestExisting).executes(input -> {
            this.save(input.getArgument("name", String.class), false);
            return 1;
        })));
        literalArgumentBuilder.then(this.literal("save").then(this.arg("name", StringArgumentType.word()).suggests(this::suggestExisting).executes(input -> {
            this.save(input.getArgument("name", String.class), true);
            return 1;
        })));
        literalArgumentBuilder.then(this.literal("delete").then(this.arg("name", StringArgumentType.word()).suggests(this::suggestExisting).executes(input -> {
            this.delete(input.getArgument("name", String.class));
            return 1;
        })));
        literalArgumentBuilder.then(this.literal("load").then(this.arg("name", StringArgumentType.word()).suggests(this::suggestExisting).executes(input -> {
            this.load(input.getArgument("name", String.class));
            return 1;
        })));
        literalArgumentBuilder.then(this.literal("list").executes(input -> {
            this.list();
            return 1;
        }));
    }

    private void save(String name, boolean overwrite) {
        LocalConfigStorage storage = this.getStorage();
        if (storage == null) {
            ChatUtil.sendMessage("Хранилище конфигов недоступно.");
            return;
        }
        if (!LocalConfigStorage.isValidName(name)) {
            ChatUtil.sendMessage("Имя конфига: до 64 букв, цифр, _ или -.");
            return;
        }
        if (!overwrite && storage.exists(name)) {
            ChatUtil.sendMessage("Конфиг " + name + " уже есть. Перезаписать: .config save " + name);
            return;
        }
        String json;
        try {
            json = ConfigManager.toJson();
        } catch (Throwable failure) {
            ChatUtil.sendMessage("Не удалось прочитать настройки клиента.");
            return;
        }
        if (storage.save(name, json)) {
            ChatUtil.sendMessage("Конфиг " + name + " сохранён локально: " + storage.getDirectory());
        } else {
            ChatUtil.sendMessage("Не удалось сохранить конфиг " + name + ".");
        }
    }

    private void delete(String name) {
        LocalConfigStorage storage = this.getStorage();
        if (storage == null) {
            ChatUtil.sendMessage("Хранилище конфигов недоступно.");
            return;
        }
        if (!LocalConfigStorage.isValidName(name)) {
            ChatUtil.sendMessage("Имя конфига: до 64 букв, цифр, _ или -.");
            return;
        }
        if (!storage.exists(name)) {
            ChatUtil.sendMessage("Конфиг " + name + " не найден.");
            return;
        }
        ChatUtil.sendMessage(storage.delete(name) ? "Конфиг " + name + " удалён." : "Не удалось удалить конфиг " + name + ".");
    }

    private void load(String name) {
        LocalConfigStorage storage = this.getStorage();
        if (storage == null) {
            ChatUtil.sendMessage("Хранилище конфигов недоступно.");
            return;
        }
        String json = storage.read(name);
        if (json == null) {
            ChatUtil.sendMessage("Конфиг " + name + " не найден.");
            return;
        }
        ConfigResult result = ConfigManager.apply(json);
        ChatUtil.sendMessage((result.successful() ? "Конфиг " + name + " применён." : "Не удалось применить конфиг " + name + ": ") + result.message());
    }

    private void list() {
        LocalConfigStorage storage = this.getStorage();
        if (storage == null) {
            ChatUtil.sendMessage("Хранилище конфигов недоступно.");
            return;
        }
        List<String> names = storage.list();
        if (names.isEmpty()) {
            ChatUtil.sendMessage("Локальных конфигов нет. Создать: .config add <имя>");
            return;
        }
        ChatUtil.sendMessage("Локальные конфиги: " + String.join(", ", names));
    }

    private CompletableFuture<Suggestions> suggestExisting(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        LocalConfigStorage storage = this.getStorage();
        if (storage != null) {
            String remaining = builder.getRemaining().toLowerCase();
            for (String name : storage.list()) {
                if (name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }
        return builder.buildFuture();
    }

    private LocalConfigStorage getStorage() {
        return Toshiba.INSTANCE.localConfigStorage;
    }
}
