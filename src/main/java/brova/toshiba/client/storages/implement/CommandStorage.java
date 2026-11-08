package brova.toshiba.client.storages.implement;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.commands.impl.BindCommand;
import brova.toshiba.client.commands.impl.BlockespCommand;
import brova.toshiba.client.commands.impl.BotCommand;
import brova.toshiba.client.commands.impl.ConfigCommand;
import brova.toshiba.client.commands.impl.FriendCommand;
import brova.toshiba.client.commands.impl.GpsCommand;
import brova.toshiba.client.commands.impl.MacroCommand;
import brova.toshiba.client.commands.impl.NukerCommand;
import brova.toshiba.client.commands.impl.ProfilerCommand;
import brova.toshiba.client.commands.impl.StaffCommand;
import brova.toshiba.client.commands.impl.VclipCommand;
import brova.toshiba.client.commands.impl.WardenCommand;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

public class CommandStorage {

    private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    private final List<CommandBase> commands = new ArrayList<>();

    private String prefix = ".";

    public CommandStorage() {
        this.updateLocalState();
    }

    private void updateLocalState() {
        this.updateRegister(new FriendCommand(), new MacroCommand(), new BotCommand(), new BlockespCommand(), new NukerCommand(), new GpsCommand(), new VclipCommand(), new BindCommand(), new StaffCommand(), new WardenCommand(), new ProfilerCommand(), new ConfigCommand());
    }

    public CommandSource getSource() {
        return new ClientCommandSource(null, MinecraftClient.getInstance());
    }

    private void updateRegister(CommandBase... commandBase) {
        for (CommandBase commandBaseValue : commandBase) {
            commandBaseValue.register(this.dispatcher);
            this.commands.add(commandBaseValue);
        }
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return this.dispatcher;
    }

    public List<CommandBase> getCommands() {
        return this.commands;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
