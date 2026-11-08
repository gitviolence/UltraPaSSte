package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.modules.impl.misc.AutoWarden;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class WardenCommand extends CommandBase {

    public WardenCommand() {
        super("warden");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("an").then(this.literal("addmain").then(this.arg("Номер", IntegerArgumentType.integer(101, 999)).executes(input -> {
            int argument = input.getArgument("Номер", Integer.class);
            AutoWarden.INSTANCE.setMainAnarchy(argument);
            ChatUtil.sendMessage("Основная база установлена на анархии: " + argument);
            return 1;
        })))).then(this.literal("add").then(this.literal("anwarden").then(this.arg("Номер", IntegerArgumentType.integer(101, 999)).executes(inputValue -> {
            int argumentValue = inputValue.getArgument("Номер", Integer.class);
            AutoWarden.INSTANCE.addAnarchy(argumentValue);
            ChatUtil.sendMessage("Добавлена анархия Вардена: " + argumentValue);
            return 1;
        })))).then(this.literal("clear").executes(inputResult -> {
            AutoWarden.INSTANCE.clearAnarchies();
            ChatUtil.sendMessage("Список анархий очищен.");
            return 1;
        }));
    }
}
