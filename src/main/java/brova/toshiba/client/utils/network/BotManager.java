package brova.toshiba.client.utils.network;

import brova.toshiba.client.utils.network.bot.BotConnection;
import brova.toshiba.mixin.IMinecraftClientAccessor;
import io.netty.channel.Channel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BotManager {

    static final List<BotConnection> connections = new CopyOnWriteArrayList<>();

    static volatile boolean ignoreBotMessages;

    private static volatile boolean executeEnabled;

    public static List<BotConnection> getConnections() {
        updateRemoveIf();
        return new ArrayList<>(connections);
    }

    public static boolean shouldBypassResourcePacks() {
        return executeEnabled;
    }

    public static void finishBotConnectStage() {
        executeEnabled = false;
    }

    public static String getCurrentSessionName() {
        MinecraftClient instance = MinecraftClient.getInstance();
        return instance.getSession() == null ? "" : instance.getSession().getUsername();
    }

    public static List<String> getSessionNames(boolean flag) {
        updateRemoveIf();
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        if (flag) {
            String currentSessionName = getCurrentSessionName();
            if (!currentSessionName.isBlank()) {
                linkedHashSet.add(currentSessionName);
            }
        }
        for (BotConnection botConnection : connections) {
            if (brova.toshiba.client.utils.runtime.AccessGuard.invoke(botConnection, new Object[] {}, (accessReceiver, accessArguments) -> ((BotConnection) accessReceiver).name()) != null && !botConnection.name().isBlank()) {
                linkedHashSet.add(botConnection.name());
            }
        }
        return new ArrayList<>(linkedHashSet);
    }

    public static boolean toggleIgnoreBotMessages() {
        ignoreBotMessages = !ignoreBotMessages;
        return ignoreBotMessages;
    }

    public static boolean isIgnoreBotMessages() {
        return ignoreBotMessages;
    }

    public static void connect(String text, String textValue) {
        MinecraftClient instance = MinecraftClient.getInstance();
        if (instance.getSession() != null && text != null && !text.isBlank() && textValue != null && !textValue.isBlank()) {
            Session session = instance.getSession();
            ServerInfo currentServerEntry = instance.getCurrentServerEntry();
            updateRemoveIf();
            calculateConnectionWithBlank(text, Text.literal("Replaced"));
            BotConnection botConnection = createBotConnectionFromSessionWithCurrentServerEntry();
            ((IMinecraftClientAccessor) instance).setSession(reflectConstruct(instance.getSession(), text));
            executeEnabled = true;
            instance.execute(() -> {
                try {
                    ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), instance, ServerAddress.parse(textValue), new ServerInfo(textValue, textValue, ServerType.OTHER), false, null);
                } catch (Exception failure) {
                    executeEnabled = false;
                    updateSession(instance, botConnection, session, currentServerEntry);
                }
            });
        }
    }

    public static void pulseBots(boolean flag) {
        for (BotConnection botConnection : connections) {
            if (isNameWithConnection(botConnection)) {
                if (flag) {
                    botConnection.handler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, botConnection.player().getYaw(), botConnection.player().getPitch()));
                } else {
                    botConnection.handler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    public static void sayAll(String text) {
        for (BotConnection botConnection : connections) {
            if (isNameWithConnection(botConnection)) {
                if (text.startsWith("/")) {
                    botConnection.handler().sendChatCommand(text.substring(1));
                } else {
                    botConnection.handler().sendChatMessage(text);
                }
            }
        }
    }

    public static boolean control(String text) {
        if (text != null && !text.isBlank()) {
            updateRemoveIf();
            MinecraftClient instance = MinecraftClient.getInstance();
            return instance.player != null && instance.world != null && text.equalsIgnoreCase(getCurrentSessionName()) ? true : connections.stream().filter(input -> isEqualsIgnoreCase(input.name(), text)).findFirst().map(inputValue -> {
                if (!isNameWithConnection(inputValue)) {
                    connections.remove(inputValue);
                    return false;
                } else {
                    BotConnection botConnection = createBotConnectionFromSessionWithCurrentServerEntry();
                    if (!isPlayerWithPipeline(inputValue)) {
                        if (botConnection != null && isPlayerWithPipeline(botConnection)) {
                            connections.remove(botConnection);
                        }
                        return false;
                    } else {
                        connections.remove(inputValue);
                        return true;
                    }
                }
            }).orElse(false);
        } else {
            return false;
        }
    }

    public static boolean say(String text, String textValue) {
        updateRemoveIf();
        return connections.stream().filter(input -> isEqualsIgnoreCase(input.name(), text)).findFirst().map(inputValue -> {
            if (!isNameWithConnection(inputValue)) {
                connections.remove(inputValue);
                return false;
            } else {
                if (textValue.startsWith("/")) {
                    inputValue.handler().sendChatCommand(textValue.substring(1));
                } else {
                    inputValue.handler().sendChatMessage(textValue);
                }
                return true;
            }
        }).orElse(false);
    }

    public static boolean remove(String text) {
        return text != null && !text.isBlank() ? calculateConnectionWithBlank(text, Text.literal("Removed")) > 0 : false;
    }

    public static boolean restore() {
        return restore(null);
    }

    public static boolean restore(String text) {
        updateRemoveIf();
        String textValue = text != null && !text.isBlank() ? text : (connections.isEmpty() ? "" : brova.toshiba.client.utils.runtime.AccessGuard.invoke(connections.get(connections.size() - 1), new Object[] {}, (accessReceiver, accessArguments) -> ((BotConnection) accessReceiver).name()));
        return !textValue.isBlank() && control(textValue);
    }

    private static BotConnection createBotConnectionFromSessionWithCurrentServerEntry() {
        MinecraftClient instance = MinecraftClient.getInstance();
        if (instance.getNetworkHandler() != null && instance.world != null && instance.player != null) {
            ClientPlayNetworkHandler networkHandler = instance.getNetworkHandler();
            updatePipelineWithConnection(networkHandler, instance.getSession().getUsername(), instance.player);
            BotConnection botConnection = new BotConnection(instance.getSession().getUsername(), instance.getCurrentServerEntry() != null ? instance.getCurrentServerEntry().address : "", networkHandler.getConnection(), networkHandler, instance.world, instance.player, instance.interactionManager, instance.getSession(), instance.getCurrentServerEntry());
            updateNameWithLiteral(botConnection);
            updateItemUseCooldownWithWorld(instance);
            return botConnection;
        } else {
            return null;
        }
    }

    private static boolean isPlayerWithPipeline(BotConnection botConnection) {
        if (!isNameWithConnection(botConnection)) {
            return false;
        } else {
            MinecraftClient instance = MinecraftClient.getInstance();
            IMinecraftClientAccessor iMinecraftClientAccessor = (IMinecraftClientAccessor) instance;
            Channel channel = reflectGetField(botConnection.connection());
            if (channel != null && channel.pipeline().get("bot_filter") != null) {
                channel.pipeline().remove("bot_filter");
            }
            try {
                reflectSetField(instance, ClientPlayNetworkHandler.class, botConnection.handler());
                iMinecraftClientAccessor.setSession(botConnection.session() != null ? botConnection.session() : reflectConstruct(instance.getSession(), botConnection.name()));
                reflectSetField(instance, ServerInfo.class, botConnection.serverInfo() != null ? botConnection.serverInfo() : createServerInfoFromBlank(botConnection.name(), brova.toshiba.client.utils.runtime.AccessGuard.invoke(botConnection, new Object[] {}, (accessReceiver, accessArguments) -> ((BotConnection) accessReceiver).address())));
                iMinecraftClientAccessor.setItemUseCooldown(0);
                instance.world = botConnection.world();
                instance.player = botConnection.player();
                instance.cameraEntity = botConnection.player();
                instance.interactionManager = botConnection.interactionManager();
                if (instance.worldRenderer != null) {
                    instance.worldRenderer.setWorld(botConnection.world());
                }
                botConnection.handler().sendPacket(new Full(botConnection.player().getX(), botConnection.player().getY(), botConnection.player().getZ(), botConnection.player().getYaw(), botConnection.player().getPitch(), botConnection.player().isOnGround(), botConnection.player().horizontalCollision));
                instance.setScreen(null);
                return true;
            } catch (Exception failure) {
                return false;
            }
        }
    }

    private static void updateItemUseCooldownWithWorld(MinecraftClient client) {
        IMinecraftClientAccessor iMinecraftClientAccessor = (IMinecraftClientAccessor) client;
        reflectSetField(client, ClientPlayNetworkHandler.class, null);
        iMinecraftClientAccessor.setItemUseCooldown(0);
        client.world = null;
        client.player = null;
        client.cameraEntity = null;
        client.interactionManager = null;
        if (client.worldRenderer != null) {
            client.worldRenderer.setWorld(null);
        }
    }

    private static void updateNameWithLiteral(BotConnection botConnection) {
        calculateConnectionWithBlank(botConnection.name(), Text.literal("Replaced"));
        connections.add(botConnection);
    }

    private static void updatePipelineWithConnection(ClientPlayNetworkHandler clientPlayNetworkHandler, String text, ClientPlayerEntity player) {
        Channel channel = reflectGetField(clientPlayNetworkHandler.getConnection());
        if (channel != null) {
            if (channel.pipeline().get("bot_filter") != null) {
                channel.pipeline().remove("bot_filter");
            }
            if (channel.pipeline().get("packet_handler") != null) {
                channel.pipeline().addBefore("packet_handler", "bot_filter", new BotChannelHandler(clientPlayNetworkHandler, player, text));
            }
        }
    }

    static boolean isText(String text) {
        return text.contains("Chat") || text.contains("Message") || text.contains("Title") || text.contains("Overlay");
    }

    private static Channel reflectGetField(ClientConnection clientConnection) {
        try {
            for (Field field : ClientConnection.class.getDeclaredFields()) {
                if (Channel.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (Channel) field.get(clientConnection);
                }
            }
        } catch (Exception failure) {
        }
        return null;
    }

    static void applyYawWithPitch(ClientPlayerEntity player, PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
        if (player != null && playerPositionLookS2CPacket != null) {
            double doubleValue = calculateObject(playerPositionLookS2CPacket, "x", player.getX());
            double doubleValueValue = calculateObject(playerPositionLookS2CPacket, "y", player.getY());
            double doubleValueResult = calculateObject(playerPositionLookS2CPacket, "z", player.getZ());
            float floatValue = (float) calculateObject(playerPositionLookS2CPacket, "yaw", player.getYaw());
            float floatValueValue = (float) calculateObject(playerPositionLookS2CPacket, "pitch", player.getPitch());
            Object value = reflectGetField(playerPositionLookS2CPacket, "change");
            if (value == null) {
                value = reflectGetField(playerPositionLookS2CPacket, "flags");
            }
            if (isEqualsWithName(value, "X")) {
                doubleValue += player.getX();
            }
            if (isEqualsWithName(value, "Y")) {
                doubleValueValue += player.getY();
            }
            if (isEqualsWithName(value, "Z")) {
                doubleValueResult += player.getZ();
            }
            if (isEqualsWithName(value, "Y_ROT")) {
                floatValue += player.getYaw();
            }
            if (isEqualsWithName(value, "X_ROT")) {
                floatValueValue += player.getPitch();
            }
            floatValueValue = MathHelper.clamp(floatValueValue, -90.0F, 90.0F);
            player.refreshPositionAndAngles(doubleValue, doubleValueValue, doubleValueResult, floatValue, floatValueValue);
            player.setYaw(floatValue);
            player.setPitch(floatValueValue);
        }
    }

    static void applyValuesWithDeltaMovement(ClientPlayerEntity player, EntityPositionSyncS2CPacket entityPositionSyncS2CPacket) {
        if (player != null && entityPositionSyncS2CPacket != null && entityPositionSyncS2CPacket.id() == player.getId() && entityPositionSyncS2CPacket.values() != null) {
            Vec3d position = entityPositionSyncS2CPacket.values().position();
            if (position != null) {
                float floatValue = entityPositionSyncS2CPacket.values().yaw();
                float clampedValue = MathHelper.clamp(entityPositionSyncS2CPacket.values().pitch(), -90.0F, 90.0F);
                player.refreshPositionAndAngles(position.x, position.y, position.z, floatValue, clampedValue);
                player.setYaw(floatValue);
                player.setPitch(clampedValue);
                if (entityPositionSyncS2CPacket.values().deltaMovement() != null) {
                    player.setVelocity(entityPositionSyncS2CPacket.values().deltaMovement());
                }
                player.setOnGround(entityPositionSyncS2CPacket.onGround());
            }
        }
    }

    private static double calculateObject(Object value, String text, double doubleValue) {
        return reflectGetField(value, text) instanceof Number number ? number.doubleValue() : doubleValue;
    }

    private static Object reflectGetField(Object value, String text) {
        if (value != null && text != null && !text.isBlank()) {
            try {
                Method method = value.getClass().getMethod(text);
                method.setAccessible(true);
                return method.invoke(value);
            } catch (Exception failure) {
                try {
                    Method methodValue = value.getClass().getMethod("get" + Character.toUpperCase(text.charAt(0)) + text.substring(1));
                    methodValue.setAccessible(true);
                    return methodValue.invoke(value);
                } catch (Exception failureValue) {
                    try {
                        RecordComponent[] recordComponents = value.getClass().getRecordComponents();
                        if (recordComponents != null) {
                            for (RecordComponent recordComponent : recordComponents) {
                                if (text.equals(recordComponent.getName())) {
                                    return recordComponent.getAccessor().invoke(value);
                                }
                            }
                        }
                    } catch (Exception failureResult) {
                    }
                    try {
                        for (Field field : value.getClass().getDeclaredFields()) {
                            if (text.equalsIgnoreCase(field.getName())) {
                                field.setAccessible(true);
                                return field.get(value);
                            }
                        }
                    } catch (Exception failureItem) {
                    }
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private static boolean isEqualsWithName(Object value, String text) {
        if (value instanceof Iterable<?> iterable && text != null) {
            for (Object valueValue : iterable) {
                if (valueValue instanceof Enum<?> enumValue && text.equals(enumValue.name())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private static Session reflectConstruct(Session session, String text) {
        try {
            var declaredConstructor = Session.class.getDeclaredConstructor(String.class, UUID.class, String.class, Optional.class, Optional.class, AccountType.class);
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance(text, UUID.randomUUID(), session == null ? "" : session.getAccessToken(), Optional.empty(), Optional.empty(), AccountType.MOJANG);
        } catch (Exception failure) {
            throw new RuntimeException(failure);
        }
    }

    private static void reflectSetField(MinecraftClient client, Class<?> type, Object value) {
        try {
            for (Field field : MinecraftClient.class.getDeclaredFields()) {
                if (field.getType() == type) {
                    field.setAccessible(true);
                    field.set(client, value);
                    return;
                }
            }
        } catch (Exception failure) {
        }
    }

    private static ServerInfo createServerInfoFromBlank(String text, String textValue) {
        String textResult = textValue == null ? "" : textValue;
        String textItem = text != null && !text.isBlank() ? text : textResult;
        return new ServerInfo(textItem, textResult, ServerType.OTHER);
    }

    private static void updateSession(MinecraftClient client, BotConnection botConnection, Session session, ServerInfo serverInfo) {
        try {
            executeEnabled = false;
            if (botConnection != null && isPlayerWithPipeline(botConnection)) {
                connections.remove(botConnection);
                return;
            }
            IMinecraftClientAccessor iMinecraftClientAccessor = (IMinecraftClientAccessor) client;
            iMinecraftClientAccessor.setSession(session);
            reflectSetField(client, ServerInfo.class, serverInfo);
        } catch (Exception failure) {
        }
    }

    private static int calculateConnectionWithBlank(String text, Text textValue) {
        if (text != null && !text.isBlank()) {
            int intValue = 0;
            for (BotConnection botConnection : new ArrayList<>(connections)) {
                if (isEqualsIgnoreCase(botConnection.name(), text)) {
                    connections.remove(botConnection);
                    intValue++;
                    try {
                        if (botConnection.connection() != null) {
                            botConnection.connection().disconnect(textValue);
                        }
                    } catch (Exception failure) {
                    }
                }
            }
            return intValue;
        } else {
            return 0;
        }
    }

    private static void updateRemoveIf() {
        connections.removeIf(input -> !isNameWithConnection(input));
    }

    private static boolean isNameWithConnection(BotConnection botConnection) {
        if (botConnection == null || botConnection.name() == null || botConnection.name().isBlank()) {
            return false;
        } else if (botConnection.connection() != null && botConnection.handler() != null && botConnection.world() != null && botConnection.player() != null) {
            if (botConnection.player().networkHandler != botConnection.handler()) {
                return false;
            } else {
                Channel channel = reflectGetField(botConnection.connection());
                return channel == null || channel.isOpen();
            }
        } else {
            return false;
        }
    }

    static boolean isEqualsIgnoreCase(String text, String textValue) {
        return text != null && textValue != null && text.equalsIgnoreCase(textValue);
    }

    private static Class<?>[] reflectGetField(Object[] values) {
        Class<?>[] types = new Class<?>[values.length];
        for (int index = 0; index < values.length; index++) {
            var classValue = values[index].getClass();
            try {
                types[index] = (Class<?>) classValue.getField("TYPE").get(null);
            } catch (Exception failure) {
                types[index] = classValue;
            }
        }
        return types;
    }
}
