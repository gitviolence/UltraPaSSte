package brova.toshiba.client.commands;

import brova.toshiba.client.core.MinecraftAccess;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

public abstract class CommandBase implements MinecraftAccess {

    private final String command;

    public CommandBase(String command) {
        this.command = command;
    }

    public abstract void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder);

    public void register(CommandDispatcher<CommandSource> commandDispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal(this.command);
        this.execute(literalArgumentBuilder);
        commandDispatcher.register(literalArgumentBuilder);
    }

    public <T> RequiredArgumentBuilder<CommandSource, T> arg(String text, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(text, argumentType);
    }

    public LiteralArgumentBuilder<CommandSource> literal(String text) {
        return LiteralArgumentBuilder.literal(text);
    }

    public String getCommand() {
        return this.command;
    }
}
