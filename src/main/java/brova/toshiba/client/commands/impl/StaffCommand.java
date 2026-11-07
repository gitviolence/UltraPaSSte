package brova.toshiba.client.commands.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

public class StaffCommand extends CommandBase {

    public StaffCommand() {
        super("staff");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("player", StringArgumentType.word()).suggests((input, inputValue) -> {
            for (PlayerListEntry playerListEntry : mc.getNetworkHandler().getPlayerList()) {
                String name = playerListEntry.getProfile().getName();
                if (name.toLowerCase().startsWith(inputValue.getRemaining().toLowerCase())) {
                    inputValue.suggest(name);
                }
            }
            return inputValue.buildFuture();
        }).executes(inputResult -> {
            String argument = inputResult.getArgument("player", String.class);
            if (!Toshiba.INSTANCE.staffStorage.isStaff(argument)) {
                Toshiba.INSTANCE.staffStorage.add(argument);
                ChatUtil.sendMessage("Игрок " + argument + " добавлен в список стаффов!");
            } else {
                ChatUtil.sendMessage("Игрок " + argument + " уже в списке стаффов!");
            }
            return 1;
        }))).then(this.literal("remove").then(this.arg("player", StringArgumentType.word()).suggests((inputItem, inputEntry) -> {
            Toshiba.INSTANCE.staffStorage.getStaffs().stream().sorted(String::compareTo).filter(inputCopy -> inputCopy.startsWith(inputEntry.getRemaining())).forEach(inputEntry::suggest);
            return inputEntry.buildFuture();
        }).executes(input2 -> {
            String argumentValue = input2.getArgument("player", String.class);
            if (Toshiba.INSTANCE.staffStorage.isStaff(argumentValue)) {
                Toshiba.INSTANCE.staffStorage.remove(argumentValue);
                ChatUtil.sendMessage("Игрок " + argumentValue + " удалён из списка стаффов!");
            } else {
                ChatUtil.sendMessage("Игрок " + argumentValue + " не найден в списке стаффов!");
            }
            return 1;
        }))).then(this.literal("list").executes(input3 -> {
            StringBuilder textBuilder = new StringBuilder();
            if (Toshiba.INSTANCE.staffStorage.getStaffs().isEmpty()) {
                ChatUtil.sendMessage("Список стаффов пуст!");
            } else {
                for (int index = 0; index < Toshiba.INSTANCE.staffStorage.getStaffs().size(); index++) {
                    textBuilder.append(Toshiba.INSTANCE.staffStorage.getStaffs().get(index));
                    if (index < Toshiba.INSTANCE.staffStorage.getStaffs().size() - 1) {
                        textBuilder.append(", ");
                    }
                }
                textBuilder.append(".");
                ChatUtil.sendMessage("Стаффы: " + textBuilder);
            }
            return 1;
        })).then(this.literal("clear").executes(input4 -> {
            if (!Toshiba.INSTANCE.staffStorage.isEmpty()) {
                Toshiba.INSTANCE.staffStorage.clear();
                ChatUtil.sendMessage("Список стаффов очищен!");
            } else {
                ChatUtil.sendMessage("Список стаффов пуст!");
            }
            return 1;
        }));
    }
}
