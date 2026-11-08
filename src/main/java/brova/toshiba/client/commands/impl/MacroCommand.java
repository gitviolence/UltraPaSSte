package brova.toshiba.client.commands.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.data.Macro;
import brova.toshiba.client.modules.settings.implement.BindSetting;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.reflect.Field;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;

public class MacroCommand extends CommandBase {

    public MacroCommand() {
        super("macro");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("name", StringArgumentType.word()).then(this.arg("bind", StringArgumentType.word()).suggests((input, inputValue) -> {
            for (Field field : GLFW.class.getDeclaredFields()) {
                String name = field.getName();
                if (name.startsWith("GLFW_KEY_")) {
                    String text = name.replace("GLFW_KEY_", "");
                    if (text.startsWith(inputValue.getRemaining())) {
                        inputValue.suggest(text);
                    }
                }
            }
            if ("NONE".startsWith(inputValue.getRemaining().toUpperCase())) {
                inputValue.suggest("NONE");
            }
            return inputValue.buildFuture();
        }).then(this.arg("command", StringArgumentType.greedyString()).executes(inputResult -> {
            String argument = inputResult.getArgument("name", String.class);
            String textValue = inputResult.getArgument("bind", String.class).toUpperCase();
            String argumentValue = inputResult.getArgument("command", String.class);
            if (Toshiba.INSTANCE.macroStorage.getMacro(argument) != null) {
                ChatUtil.sendMessage("Макрос " + argument + " уже существует!");
                return 1;
            } else {
                try {
                    int intValue = "NONE".equals(textValue) ? -1 : GLFW.class.getField("GLFW_KEY_" + textValue).getInt(null);
                    Toshiba.INSTANCE.macroStorage.add(new Macro(argument, argumentValue, new BindSetting("bind", intValue)));
                    ChatUtil.sendMessage("Макрос " + argument + " был добавлен!");
                } catch (Exception failure) {
                    ChatUtil.sendMessage("Неверный бинд: " + textValue);
                }
                return 1;
            }
        }))))).then(this.literal("remove").then(this.arg("name", StringArgumentType.word()).suggests((inputItem, inputEntry) -> {
            Toshiba.INSTANCE.macroStorage.getNames().stream().filter(inputCopy -> inputCopy.startsWith(inputEntry.getRemaining())).forEach(inputEntry::suggest);
            return inputEntry.buildFuture();
        }).executes(input2 -> {
            String argumentResult = input2.getArgument("name", String.class);
            if (Toshiba.INSTANCE.macroStorage.isEmpty()) {
                ChatUtil.sendMessage("Список макросов пуст!");
                return 1;
            } else {
                Macro macro = Toshiba.INSTANCE.macroStorage.getMacro(argumentResult);
                if (macro == null) {
                    ChatUtil.sendMessage("Макрос " + argumentResult + " не найден!");
                    return 1;
                } else {
                    Toshiba.INSTANCE.macroStorage.remove(macro);
                    ChatUtil.sendMessage("Макрос " + argumentResult + " был удалён!");
                    return 1;
                }
            }
        }))).then(this.literal("list").executes(input3 -> {
            StringBuilder textBuilder = new StringBuilder();
            if (Toshiba.INSTANCE.macroStorage.getNames().isEmpty()) {
                ChatUtil.sendMessage("Список макросов пуст!");
            } else {
                for (int index = 0; index < Toshiba.INSTANCE.macroStorage.getNames().size(); index++) {
                    textBuilder.append(Toshiba.INSTANCE.macroStorage.getNames().get(index));
                    if (index < Toshiba.INSTANCE.macroStorage.getNames().size() - 1) {
                        textBuilder.append(", ");
                    }
                }
                textBuilder.append(".");
                ChatUtil.sendMessage("Макросы: " + textBuilder);
            }
            return 1;
        })).then(this.literal("clear").executes(input4 -> {
            if (!Toshiba.INSTANCE.macroStorage.isEmpty()) {
                Toshiba.INSTANCE.macroStorage.clear();
                ChatUtil.sendMessage("Все макросы были удалены!");
            } else {
                ChatUtil.sendMessage("Список макросов пуст!");
            }
            return 1;
        }));
    }
}
