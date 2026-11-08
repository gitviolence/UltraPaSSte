package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.utils.network.BotManager;
import brova.toshiba.client.utils.network.bot.BotConnection;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

public class BotCommand extends CommandBase {

    public BotCommand() {
        super("bot");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("connect").then(this.arg("server", StringArgumentType.string()).then(this.arg("nickname", StringArgumentType.string()).executes(input -> {
            MinecraftClient instance = MinecraftClient.getInstance();
            if (instance.player == null) {
                return 0;
            } else {
                String string = StringArgumentType.getString(input, "server");
                String stringValue = StringArgumentType.getString(input, "nickname");
                BotManager.connect(stringValue, string);
                ChatUtil.sendMessage("§4[Bot] §fПодключение успешное: " + stringValue + " → " + string);
                return 1;
            }
        })))).then(this.literal("remove").then(this.arg("name", StringArgumentType.string()).suggests((inputValue, inputResult) -> {
            BotManager.getSessionNames(false).forEach(inputResult::suggest);
            return inputResult.buildFuture();
        }).executes(inputItem -> {
            MinecraftClient instanceValue = MinecraftClient.getInstance();
            if (instanceValue.player == null) {
                return 0;
            } else {
                String stringResult = StringArgumentType.getString(inputItem, "name");
                if (BotManager.remove(stringResult)) {
                    ChatUtil.sendMessage("§f[Bot] §fБот кикнут и удален: " + stringResult);
                } else {
                    ChatUtil.sendMessage("§f[Bot] §fБот не найден: " + stringResult);
                }
                return 1;
            }
        }))).then(this.literal("return").then(this.arg("name", StringArgumentType.string()).suggests((inputEntry, inputCopy) -> {
            BotManager.getSessionNames(false).forEach(inputCopy::suggest);
            return inputCopy.buildFuture();
        }).executes(input2 -> {
            MinecraftClient instanceResult = MinecraftClient.getInstance();
            if (instanceResult.player == null) {
                return 0;
            } else {
                String stringItem = StringArgumentType.getString(input2, "name");
                if (stringItem.equalsIgnoreCase(BotManager.getCurrentSessionName())) {
                    ChatUtil.sendMessage("§f[Bot] §fТы уже подключен к этому боту: " + stringItem);
                    return 1;
                } else {
                    if (BotManager.control(stringItem)) {
                        ChatUtil.sendMessage("§f[Bot] §fПереключение на бота: " + stringItem);
                    } else {
                        ChatUtil.sendMessage("§f[Bot] §fБот не найден: " + stringItem);
                    }
                    return 1;
                }
            }
        }))).then(this.literal("text").then(this.arg("name", StringArgumentType.string()).suggests((input3, input4) -> {
            BotManager.getSessionNames(false).forEach(input4::suggest);
            return input4.buildFuture();
        }).then(this.arg("message", StringArgumentType.greedyString()).executes(input5 -> {
            MinecraftClient instanceItem = MinecraftClient.getInstance();
            if (instanceItem.player == null) {
                return 0;
            } else {
                String stringEntry = StringArgumentType.getString(input5, "name");
                String stringCopy = StringArgumentType.getString(input5, "message");
                if (BotManager.say(stringEntry, stringCopy)) {
                    ChatUtil.sendMessage("§f[Bot] §fСообщение отправлено от бота " + stringEntry);
                } else {
                    ChatUtil.sendMessage("§f[Bot] §fСессия не найдена: " + stringEntry);
                }
                return 1;
            }
        })))).then(this.literal("textall").then(this.arg("message", StringArgumentType.greedyString()).executes(input6 -> {
            MinecraftClient instanceEntry = MinecraftClient.getInstance();
            if (instanceEntry.player == null) {
                return 0;
            } else {
                String string2 = StringArgumentType.getString(input6, "message");
                BotManager.sayAll(string2);
                ChatUtil.sendMessage("§f[Bot] §fСообщение отправлено со всех ботов.");
                return 1;
            }
        }))).then(this.literal("list").executes(input7 -> {
            MinecraftClient instanceCopy = MinecraftClient.getInstance();
            if (instanceCopy.player == null) {
                return 0;
            } else {
                var connections = BotManager.getConnections();
                if (connections.isEmpty()) {
                    ChatUtil.sendMessage("§f[Bot] §fБотов сейчас нету");
                } else {
                    ChatUtil.sendMessage("§f[Bot] §fБоты:");
                    for (BotConnection botConnection : connections) {
                        ChatUtil.sendMessage("§7- §f" + botConnection.name() + " (§f" + botConnection.address() + "§f)");
                    }
                }
                return 1;
            }
        }));
    }
}
