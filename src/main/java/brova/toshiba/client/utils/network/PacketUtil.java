package brova.toshiba.client.utils.network;

import brova.toshiba.client.core.MinecraftAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.Packet;

public final class PacketUtil implements MinecraftAccess {

    private static final List<Packet<?>> silentPackets = new ArrayList<>();

    public static void sendSilentPacket(Packet<?> packet) {
        silentPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static void sendPacket(Packet<?> packet) {
        mc.getNetworkHandler().sendPacket(packet);
    }

    private PacketUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<Packet<?>> getSilentPackets() {
        return silentPackets;
    }
}
