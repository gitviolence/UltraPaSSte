package brova.toshiba.client.utils.time;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class TpsCalculator {

    private float tPS = 20.0F;

    private float adjustTicks = 0.0F;

    private long timestamp;

    private long lastPacketTime;

    private final float[] tpsSamples = new float[20];

    private int sampleIndex = 0;

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == Type.RECEIVE && eventPacket.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            this.updateNanoTimeWithCurrentTimeMillis();
        }
    }

    public float getTPS() {
        if (this.lastPacketTime == 0L) {
            return this.tPS;
        } else {
            MinecraftClient instance = MinecraftClient.getInstance();
            return instance != null && instance.getNetworkHandler() != null && System.currentTimeMillis() - this.lastPacketTime <= 3500L ? this.tPS : 20.0F;
        }
    }

    private void updateNanoTimeWithCurrentTimeMillis() {
        long timestamp = System.nanoTime();
        this.lastPacketTime = System.currentTimeMillis();
        if (this.timestamp == 0L) {
            this.timestamp = timestamp;
        } else {
            long longValue = timestamp - this.timestamp;
            this.timestamp = timestamp;
            if (longValue > 0L) {
                float floatValue = 20.0F;
                float floatValueValue = floatValue * (1.0E9F / (float) longValue);
                float clampedValue = MathHelper.clamp(floatValueValue, 0.0F, floatValue);
                this.tpsSamples[this.sampleIndex % 20] = clampedValue;
                this.sampleIndex++;
                int intValue = Math.min(this.sampleIndex, 20);
                float floatValueResult = 0.0F;
                for (int index = 0; index < intValue; index++) {
                    float floatValueItem = this.tpsSamples[index];
                    floatValueResult += floatValueItem;
                }
                this.tPS = (float) this.round(floatValueResult / intValue);
                this.adjustTicks = this.tPS - floatValue;
            }
        }
    }

    public double round(double doubleValue) {
        return Math.round(doubleValue * 10.0) / 10.0;
    }

    public float getAdjustTicks() {
        return this.adjustTicks;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getLastPacketTime() {
        return this.lastPacketTime;
    }

    public float[] getTpsSamples() {
        return this.tpsSamples;
    }

    public int getSampleIndex() {
        return this.sampleIndex;
    }
}
