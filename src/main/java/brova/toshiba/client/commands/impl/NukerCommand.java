package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.modules.impl.player.Nuker;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class NukerCommand extends CommandBase {

    public NukerCommand() {
        super("nuker");
    }

    public NukerCommand(String text) {
        super(text);
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("block", StringArgumentType.word()).suggests((input, inputValue) -> {
            Registries.BLOCK.stream().map(Registries.BLOCK::getId).map(Identifier::getPath).filter(inputResult -> inputResult.startsWith(inputValue.getRemaining().toLowerCase())).limit(20L).forEach(inputValue::suggest);
            return inputValue.buildFuture();
        }).executes(inputItem -> {
            String text = Nuker.normalizeBlockName(inputItem.getArgument("block", String.class));
            if (Nuker.INSTANCE.isTargetBlock(text)) {
                ChatUtil.sendMessage("§cБлок §e" + text + "§c уже в списке Nuker!");
                return 1;
            } else if (!this.isAnyMatchWithStream(text)) {
                ChatUtil.sendMessage("§cБлок §e" + text + "§c не найден!");
                return 1;
            } else {
                Nuker.INSTANCE.addBlock(text);
                ChatUtil.sendMessage("§aБлок §e" + text + "§a добавлен в Nuker!");
                return 1;
            }
        }))).then(this.literal("remove").then(this.arg("block", StringArgumentType.word()).suggests((inputEntry, inputCopy) -> {
            Nuker.INSTANCE.getTargetBlocks().stream().sorted(String::compareTo).filter(input2 -> input2.startsWith(inputCopy.getRemaining().toLowerCase())).forEach(inputCopy::suggest);
            return inputCopy.buildFuture();
        }).executes(input3 -> {
            String textValue = Nuker.normalizeBlockName(input3.getArgument("block", String.class));
            if (!Nuker.INSTANCE.isTargetBlock(textValue)) {
                ChatUtil.sendMessage("§cБлока §e" + textValue + "§c нет в списке Nuker!");
                return 1;
            } else {
                Nuker.INSTANCE.removeBlock(textValue);
                ChatUtil.sendMessage("§aБлок §e" + textValue + "§a удален из Nuker!");
                return 1;
            }
        }))).then(this.literal("list").executes(input4 -> {
            Set<String> targetBlocks = Nuker.INSTANCE.getTargetBlocks();
            if (targetBlocks.isEmpty()) {
                ChatUtil.sendMessage("§cСписок Nuker пуст!");
                return 1;
            } else {
                String textResult = targetBlocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                ChatUtil.sendMessage("§aБлоки Nuker §7(§e" + targetBlocks.size() + "§7)§a: §e" + textResult);
                return 1;
            }
        })).then(this.literal("clear").executes(input5 -> {
            if (Nuker.INSTANCE.getTargetBlocks().isEmpty()) {
                ChatUtil.sendMessage("§cСписок Nuker уже пуст!");
                return 1;
            } else {
                Nuker.INSTANCE.clearBlocks();
                ChatUtil.sendMessage("§aСписок Nuker очищен!");
                return 1;
            }
        }));
    }

    private boolean isAnyMatchWithStream(String text) {
        return Registries.BLOCK.stream().anyMatch(input -> Registries.BLOCK.getId(input).getPath().equalsIgnoreCase(text));
    }
}
