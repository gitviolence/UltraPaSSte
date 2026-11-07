package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class DiscordEventHandlers extends Structure {

    public ReadyCallback ready;

    public DisconnectCallback disconnected;

    public ErrorCallback errored;

    public JoinGameCallback joinGame;

    public SpectateGameCallback spectateGame;

    public JoinRequestCallback joinRequest;

    @Override
    public List<String> getFieldOrder() {
        return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
    }
}
