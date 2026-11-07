package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.modules.impl.render.BlockESP;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockespCommand extends CommandBase {

    public BlockespCommand() {
        super("blockesp");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        literalArgumentBuilder.then(this.literal("add").then(this.arg("block", StringArgumentType.word()).suggests((input, inputValue) -> {
            Registries.BLOCK.stream().map(Registries.BLOCK::getId).map(Identifier::getPath).filter(inputResult -> inputResult.startsWith(inputValue.getRemaining().toLowerCase())).limit(20L).forEach(inputValue::suggest);
            return inputValue.buildFuture();
        }).executes(inputItem -> {
            String argument = inputItem.getArgument("block", String.class);
            if (BlockESP.INSTANCE.isTracking(argument)) {
                ChatUtil.sendMessage("§cБлок §e" + argument + "§c уже отслеживается!");
                return 1;
            } else {
                boolean flag = Registries.BLOCK.stream().anyMatch(inputEntry -> {
                    String path = Registries.BLOCK.getId(inputEntry).getPath();
                    return path.equalsIgnoreCase(argument);
                });
                if (!flag) {
                    ChatUtil.sendMessage("§cБлок §e" + argument + "§c не найден!");
                    return 1;
                } else {
                    BlockESP.INSTANCE.addBlock(argument);
                    ChatUtil.sendMessage("§aБлок §e" + argument + "§a добавлен в отслеживание!");
                    return 1;
                }
            }
        }))).then(this.literal("remove").then(this.arg("block", StringArgumentType.word()).suggests((inputCopy, input2) -> {
            BlockESP.INSTANCE.getTrackedBlocks().stream().sorted(String::compareTo).filter(input3 -> input3.startsWith(input2.getRemaining().toLowerCase())).forEach(input2::suggest);
            return input2.buildFuture();
        }).executes(input4 -> {
            String argumentValue = input4.getArgument("block", String.class);
            if (!BlockESP.INSTANCE.isTracking(argumentValue)) {
                ChatUtil.sendMessage("§cБлок §e" + argumentValue + "§c не отслеживается!");
                return 1;
            } else {
                BlockESP.INSTANCE.removeBlock(argumentValue);
                ChatUtil.sendMessage("§aБлок §e" + argumentValue + "§a удалён из отслеживания!");
                return 1;
            }
        }))).then(this.literal("list").executes(input5 -> {
            Set<String> trackedBlocks = BlockESP.INSTANCE.getTrackedBlocks();
            if (trackedBlocks.isEmpty()) {
                ChatUtil.sendMessage("§cСписок отслеживаемых блоков пуст!");
                return 1;
            } else {
                String text = trackedBlocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                ChatUtil.sendMessage("§aОтслеживаемые блоки §7(§e" + trackedBlocks.size() + "§7)§a: §e" + text);
                return 1;
            }
        })).then(this.literal("clear").executes(input6 -> {
            if (BlockESP.INSTANCE.getTrackedBlocks().isEmpty()) {
                ChatUtil.sendMessage("§cСписок отслеживаемых блоков уже пуст!");
                return 1;
            } else {
                BlockESP.INSTANCE.clearBlocks();
                ChatUtil.sendMessage("§aСписок отслеживаемых блоков очищен!");
                return 1;
            }
        }));
    }
}
