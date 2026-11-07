package brova.toshiba.client.commands.impl;

import brova.toshiba.Toshiba;
import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

public class FriendCommand extends CommandBase {

    public FriendCommand() {
        super("friend");
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
            if (!Toshiba.INSTANCE.friendStorage.isFriend(argument)) {
                Toshiba.INSTANCE.friendStorage.add(argument);
                ChatUtil.sendMessage("Игрок " + argument + " добавлен в друзья!");
            } else {
                ChatUtil.sendMessage("Игрок " + argument + " уже в списке друзей!");
            }
            return 1;
        }))).then(this.literal("remove").then(this.arg("player", StringArgumentType.word()).suggests((inputItem, inputEntry) -> {
            Toshiba.INSTANCE.friendStorage.getFriends().stream().sorted(String::compareTo).filter(inputCopy -> inputCopy.startsWith(inputEntry.getRemaining())).forEach(inputEntry::suggest);
            return inputEntry.buildFuture();
        }).executes(input2 -> {
            String argumentValue = input2.getArgument("player", String.class);
            if (Toshiba.INSTANCE.friendStorage.isFriend(argumentValue)) {
                Toshiba.INSTANCE.friendStorage.remove(argumentValue);
                ChatUtil.sendMessage("Игрок " + argumentValue + " удалён из друзей!");
            } else {
                ChatUtil.sendMessage("Игрок " + argumentValue + " не найден в списке друзей!");
            }
            return 1;
        }))).then(this.literal("list").executes(input3 -> {
            if (Toshiba.INSTANCE.friendStorage.getFriends().isEmpty()) {
                ChatUtil.sendMessage("Список друзей пуст!");
            } else {
                StringBuilder textBuilder = new StringBuilder();
                for (int index = 0; index < Toshiba.INSTANCE.friendStorage.getFriends().size(); index++) {
                    textBuilder.append(Toshiba.INSTANCE.friendStorage.getFriends().get(index));
                    if (index < Toshiba.INSTANCE.friendStorage.getFriends().size() - 1) {
                        textBuilder.append(", ");
                    }
                }
                ChatUtil.sendMessage("Друзья: " + textBuilder);
            }
            return 1;
        })).then(this.literal("clear").executes(input4 -> {
            if (!Toshiba.INSTANCE.friendStorage.isEmpty()) {
                Toshiba.INSTANCE.friendStorage.clear();
                ChatUtil.sendMessage("Список друзей очищен!");
            } else {
                ChatUtil.sendMessage("Список друзей пуст!");
            }
            return 1;
        }));
    }
}
