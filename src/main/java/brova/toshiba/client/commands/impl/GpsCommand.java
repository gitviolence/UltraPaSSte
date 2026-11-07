package brova.toshiba.client.commands.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.data.Waypoint;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class GpsCommand extends CommandBase {

    public GpsCommand() {
        super("gps");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("Название метки", StringArgumentType.word()).suggests((input, inputValue) -> {
            if (inputValue.getRemaining().isEmpty()) {
                inputValue.suggest("Название");
            }
            return inputValue.buildFuture();
        }).then(this.arg("X:", IntegerArgumentType.integer()).suggests((inputResult, inputItem) -> {
            if (inputItem.getRemaining().isEmpty()) {
                inputItem.suggest("X");
            }
            return inputItem.buildFuture();
        }).then(this.arg("Y:", IntegerArgumentType.integer()).suggests((inputEntry, inputCopy) -> {
            if (inputCopy.getRemaining().isEmpty()) {
                inputCopy.suggest("Y");
            }
            return inputCopy.buildFuture();
        }).then(this.arg("Z:", IntegerArgumentType.integer()).suggests((input2, input3) -> {
            if (input3.getRemaining().isEmpty()) {
                input3.suggest("Z");
            }
            return input3.buildFuture();
        }).executes(input4 -> {
            String argument = input4.getArgument("Название метки", String.class);
            int argumentValue = input4.getArgument("X:", Integer.class);
            int argumentResult = input4.getArgument("Y:", Integer.class);
            int argumentItem = input4.getArgument("Z:", Integer.class);
            Waypoint waypoint = new Waypoint(argument, argumentValue, argumentResult, argumentItem);
            Toshiba.INSTANCE.waypointStorage.set(waypoint);
            ChatUtil.sendMessage("Метка '" + argument + "' поставлена: " + argumentValue + " " + argumentResult + " " + argumentItem);
            return 1;
        })))))).then(this.literal("remove").executes(input5 -> {
            if (!Toshiba.INSTANCE.waypointStorage.isEmpty()) {
                Toshiba.INSTANCE.waypointStorage.clear();
                ChatUtil.sendMessage("Метка удалена!");
            } else {
                ChatUtil.sendMessage("Метки не было");
            }
            return 1;
        }));
    }
}
