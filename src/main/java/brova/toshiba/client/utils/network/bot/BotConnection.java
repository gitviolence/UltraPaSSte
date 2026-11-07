package brova.toshiba.client.utils.network.bot;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;

public final class BotConnection {

    private final String cachedTextForName;

    private final String cachedTextForAddress;

    private final ClientConnection clientConnection;

    private final ClientPlayNetworkHandler clientPlayNetworkHandler;

    private final ClientWorld clientWorld;

    private final ClientPlayerEntity clientPlayerEntity;

    private final ClientPlayerInteractionManager clientPlayerInteractionManager;

    private final Session session;

    private final ServerInfo serverInfo;

    public BotConnection(String cachedTextForName, String cachedTextForAddress, ClientConnection clientConnection, ClientPlayNetworkHandler clientPlayNetworkHandler, ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager, Session session, ServerInfo serverInfo) {
        this.cachedTextForName = cachedTextForName;
        this.cachedTextForAddress = cachedTextForAddress;
        this.clientConnection = clientConnection;
        this.clientPlayNetworkHandler = clientPlayNetworkHandler;
        this.clientWorld = clientWorld;
        this.clientPlayerEntity = clientPlayerEntity;
        this.clientPlayerInteractionManager = clientPlayerInteractionManager;
        this.session = session;
        this.serverInfo = serverInfo;
    }

    public String name() {
        return this.cachedTextForName;
    }

    public String address() {
        return this.cachedTextForAddress;
    }

    public ClientConnection connection() {
        return this.clientConnection;
    }

    public ClientPlayNetworkHandler handler() {
        return this.clientPlayNetworkHandler;
    }

    public ClientWorld world() {
        return this.clientWorld;
    }

    public ClientPlayerEntity player() {
        return this.clientPlayerEntity;
    }

    public ClientPlayerInteractionManager interactionManager() {
        return this.clientPlayerInteractionManager;
    }

    public Session session() {
        return this.session;
    }

    public ServerInfo serverInfo() {
        return this.serverInfo;
    }
}
