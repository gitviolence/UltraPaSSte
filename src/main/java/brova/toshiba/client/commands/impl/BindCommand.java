package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;

public class BindCommand extends CommandBase {

    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("module", StringArgumentType.word()).suggests((input, inputValue) -> {
            String normalizedText = inputValue.getRemaining().toLowerCase();
            ModuleRegistry.INSTANCE.getObject().stream().map(Module::getName).filter(inputResult -> inputResult.toLowerCase().startsWith(normalizedText)).forEach(inputValue::suggest);
            return inputValue.buildFuture();
        }).then(this.arg("key", StringArgumentType.word()).suggests((inputItem, inputEntry) -> {
            String text = inputEntry.getRemaining().toUpperCase();
            for (Field field : GLFW.class.getDeclaredFields()) {
                String name = field.getName();
                if (name.startsWith("GLFW_KEY_")) {
                    String textValue = name.replace("GLFW_KEY_", "");
                    if (textValue.startsWith(text)) {
                        inputEntry.suggest(textValue);
                    }
                }
            }
            if ("NONE".startsWith(text)) {
                inputEntry.suggest("NONE");
            }
            return inputEntry.buildFuture();
        }).executes(inputCopy -> {
            String argument = inputCopy.getArgument("module", String.class);
            var optional = this.findOptionalFromFirstWithFilter(argument);
            if (optional.isEmpty()) {
                ChatUtil.sendMessage("Модуль " + argument + " не найден");
                return 1;
            } else {
                Module module = optional.get();
                String textResult = inputCopy.getArgument("key", String.class).toUpperCase();
                int intValue = this.calculateEqualsIgnoreCaseWithInt(textResult);
                if (intValue == -1) {
                    ChatUtil.sendMessage("Клавиша " + textResult + " не найдена");
                } else {
                    module.setKey(intValue);
                    ChatUtil.sendMessage("Модуль " + module.getName() + " привязан к клавише " + textResult);
                }
                return 1;
            }
        }))));
        literalArgumentBuilder.then(this.literal("remove").then(this.arg("module", StringArgumentType.word()).executes(input2 -> {
            String argumentValue = input2.getArgument("module", String.class);
            var optionalValue = this.findOptionalFromFirstWithFilter(argumentValue);
            if (optionalValue.isEmpty()) {
                ChatUtil.sendMessage("Модуль " + argumentValue + " не найден");
                return 1;
            } else {
                Module moduleValue = optionalValue.get();
                moduleValue.setKey(-1);
                ChatUtil.sendMessage("Привязка клавиши для модуля " + moduleValue.getName() + " удалена");
                return 1;
            }
        })));
        literalArgumentBuilder.then(this.literal("clear").executes(input3 -> {
            ModuleRegistry.INSTANCE.getObject().forEach(input4 -> input4.setKey(-1));
            ChatUtil.sendMessage("Все привязки клавиш удалены");
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("list").executes(input5 -> {
            StringBuilder textBuilder = new StringBuilder("Список привязанных модулей: ");
            boolean present = ModuleRegistry.INSTANCE.getObject().stream().filter(input6 -> input6.getKey() != -1).peek(input7 -> textBuilder.append("Модуль: ").append(input7.getName()).append(" -> Клавиша: ").append(input7.getKey()).append("\n")).findAny().isPresent();
            if (!present) {
                ChatUtil.sendMessage("Нет привязанных модулей");
            } else {
                ChatUtil.sendMessage(textBuilder.toString());
            }
            return 1;
        }));
    }

    private Optional<Module> findOptionalFromFirstWithFilter(String text) {
        return ModuleRegistry.INSTANCE.getObject().stream().filter(input -> input.getName().equalsIgnoreCase(text)).findFirst();
    }

    private int calculateEqualsIgnoreCaseWithInt(String text) {
        if ("NONE".equalsIgnoreCase(text)) {
            return -1;
        } else {
            try {
                return GLFW.class.getField("GLFW_KEY_" + text).getInt(null);
            } catch (IllegalAccessException | NoSuchFieldException failure) {
                return -1;
            }
        }
    }
}
