package brova.toshiba.client.events.implement;

import brova.toshiba.client.event.EventBase;
import brova.toshiba.client.events.implement.EventPacket.Type;
import net.minecraft.network.packet.Packet;

public class EventPacket extends EventBase {

    private final Packet<?> packet;

    private final Type type;

    public EventPacket(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {

        SEND, RECEIVE
    }
}
