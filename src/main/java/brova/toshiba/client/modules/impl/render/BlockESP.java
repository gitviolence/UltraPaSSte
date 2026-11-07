package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.FloatSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

public final class BlockESP extends Module {

    public static BlockESP INSTANCE = new BlockESP();

    private final FloatSetting distance = new FloatSetting("Дистанция", 60.0F, 10.0F, 120.0F, 1.0F);

    private final Set<String> trackedBlocks = ConcurrentHashMap.newKeySet();

    private final Map<BlockPos, String> entrySetStringByBlockPos = new ConcurrentHashMap<>();

    private final Set<ChunkPos> chunkPosEntries = ConcurrentHashMap.newKeySet();

    private ChunkPos chunkPos;

    private int cachedCountForRender3D = -1;

    private long lastUpdateTimestampForRender3D;

    public BlockESP() {
        super("BlockESP", "Подсвечивает блоки", ModuleCategory.RENDER);
        this.addSettings(this.distance);
    }

    @Override
    public void onEnable() {
        this.clearState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.clearState();
        super.onDisable();
    }

    @EventHandler(priority = 100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null && !this.trackedBlocks.isEmpty()) {
            int cachedCountForRender3D = this.calculateInteger();
            ChunkPos chunkPos = new ChunkPos(mc.player.getBlockPos());
            if (cachedCountForRender3D != this.cachedCountForRender3D) {
                this.clearState();
                this.cachedCountForRender3D = cachedCountForRender3D;
            }
            if (this.chunkPos == null || !this.chunkPos.equals(chunkPos)) {
                this.chunkPosEntries.clear();
                this.chunkPos = chunkPos;
            }
            long lastUpdateTimestampForRender3D = System.currentTimeMillis();
            if (lastUpdateTimestampForRender3D - this.lastUpdateTimestampForRender3D >= 50L) {
                this.updateBlockPosWithSort(cachedCountForRender3D);
                this.lastUpdateTimestampForRender3D = lastUpdateTimestampForRender3D;
            }
            this.updateRemoveIfWithEntrySet(mc.player.getPos(), cachedCountForRender3D);
            this.renderDepthMaskWithBegin(event.getMatrices());
        }
    }

    private void updateBlockPosWithSort(int intValue) {
        if (mc.world != null && mc.player != null) {
            BlockPos blockPos = mc.player.getBlockPos();
            int intValueValue = blockPos.getX() >> 4;
            int intValueResult = blockPos.getZ() >> 4;
            int intValueItem = (intValue >> 4) + 2;
            ArrayList<ChunkPos> chunkPositions = new ArrayList<>();
            for (int index = -intValueItem; index <= intValueItem; index++) {
                for (int indexValue = -intValueItem; indexValue <= intValueItem; indexValue++) {
                    ChunkPos chunkPosition = new ChunkPos(intValueValue + index, intValueResult + indexValue);
                    if (!this.chunkPosEntries.contains(chunkPosition)) {
                        chunkPositions.add(chunkPosition);
                    }
                }
            }
            chunkPositions.sort((leftChunkPosition, rightChunkPosition) -> {
                long longValue = this.calculateChunkPos(leftChunkPosition, intValueValue, intValueResult);
                long longValueValue = this.calculateChunkPos(rightChunkPosition, intValueValue, intValueResult);
                return Long.compare(longValue, longValueValue);
            });
            int intValueEntry = 0;
            for (ChunkPos chunkPositionValue : chunkPositions) {
                if (intValueEntry >= 2) {
                    break;
                }
                WorldChunk chunk = mc.world.getChunk(chunkPositionValue.x, chunkPositionValue.z);
                if (chunk != null) {
                    this.updateStartXWithStartZ(chunk, blockPos, intValue);
                    this.chunkPosEntries.add(chunkPositionValue);
                    intValueEntry++;
                }
            }
        }
    }

    private void updateStartXWithStartZ(WorldChunk worldChunk, BlockPos position, int intValue) {
        int startX = worldChunk.getPos().getStartX();
        int startZ = worldChunk.getPos().getStartZ();
        int intValueValue = startX + 15;
        int intValueResult = startZ + 15;
        int intValueItem = Math.max(mc.world.getBottomY(), position.getY() - intValue);
        int intValueEntry = Math.min(mc.world.getTopYInclusive(), position.getY() + intValue);
        int intValueCopy = intValue * intValue;
        Mutable mutablePosition = new Mutable();
        for (int index = startX; index <= intValueValue; index++) {
            for (int indexValue = startZ; indexValue <= intValueResult; indexValue++) {
                for (int indexResult = intValueItem; indexResult <= intValueEntry; indexResult++) {
                    mutablePosition.set(index, indexResult, indexValue);
                    if (!(mutablePosition.getSquaredDistance(position) > intValueCopy)) {
                        BlockState blockState = worldChunk.getBlockState(mutablePosition);
                        if (!blockState.isAir()) {
                            String normalizedText = Registries.BLOCK.getId(blockState.getBlock()).getPath().toLowerCase();
                            if (this.trackedBlocks.contains(normalizedText)) {
                                this.entrySetStringByBlockPos.put(mutablePosition.toImmutable(), normalizedText);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateRemoveIfWithEntrySet(Vec3d position, int intValue) {
        if (mc.world == null) {
            this.entrySetStringByBlockPos.clear();
        } else {
            int intValueValue = intValue * intValue;
            this.entrySetStringByBlockPos.entrySet().removeIf(input -> {
                BlockPos key = input.getKey();
                BlockState blockState = mc.world.getBlockState(key);
                if (blockState.isAir()) {
                    return true;
                } else {
                    String normalizedText = Registries.BLOCK.getId(blockState.getBlock()).getPath().toLowerCase();
                    return !this.trackedBlocks.contains(normalizedText) ? true : key.getSquaredDistance(position) > intValueValue;
                }
            });
        }
    }

    private void renderDepthMaskWithBegin(MatrixStack matrices) {
        if (!this.entrySetStringByBlockPos.isEmpty()) {
            Vec3d pos = mc.gameRenderer.getCamera().getPos();
            matrices.push();
            matrices.translate(-pos.x, -pos.y, -pos.z);
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Tessellator instance = Tessellator.getInstance();
            BufferBuilder buffer = instance.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (BlockPos position : this.entrySetStringByBlockPos.keySet()) {
                this.appendFilledBlockVertices(buffer, positionMatrix, position, 0.76F, 1.0F, 0.15F, 0.18F);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.lineWidth(2.0F);
            BufferBuilder bufferValue = instance.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (BlockPos positionValue : this.entrySetStringByBlockPos.keySet()) {
                this.appendBlockOutlineVertices(bufferValue, positionMatrix, positionValue, 0.76F, 1.0F, 0.15F, 1.0F);
            }
            BufferRenderer.drawWithGlobalProgram(bufferValue.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            matrices.pop();
        }
    }

    private void appendFilledBlockVertices(BufferBuilder buffer, Matrix4f matrix, BlockPos position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float x = position.getX();
        float y = position.getY();
        float z = position.getZ();
        float floatValueEntry = x + 1.0F;
        float floatValueCopy = y + 1.0F;
        float floatValue2 = z + 1.0F;
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    private void appendBlockOutlineVertices(BufferBuilder buffer, Matrix4f matrix, BlockPos position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        float x = position.getX();
        float y = position.getY();
        float z = position.getZ();
        float floatValueEntry = x + 1.0F;
        float floatValueCopy = y + 1.0F;
        float floatValue2 = z + 1.0F;
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, z).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, floatValueEntry, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, y, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
        buffer.vertex(matrix, x, floatValueCopy, floatValue2).color(floatValue, floatValueValue, floatValueResult, floatValueItem);
    }

    public void addBlock(String text) {
        this.trackedBlocks.add(text.toLowerCase());
        this.chunkPosEntries.clear();
        this.entrySetStringByBlockPos.clear();
    }

    public void removeBlock(String text) {
        this.trackedBlocks.remove(text.toLowerCase());
        this.entrySetStringByBlockPos.entrySet().removeIf(input -> input.getValue().equalsIgnoreCase(text));
    }

    public void clearBlocks() {
        this.trackedBlocks.clear();
        this.clearState();
    }

    public Set<String> getTrackedBlocks() {
        return new HashSet<>(this.trackedBlocks);
    }

    public boolean isTracking(String text) {
        return this.trackedBlocks.contains(text.toLowerCase());
    }

    private int calculateInteger() {
        return Math.round(this.distance.get());
    }

    private long calculateChunkPos(ChunkPos chunkPosition, int intValue, int intValueValue) {
        long longValue = chunkPosition.x - intValue;
        long longValueValue = chunkPosition.z - intValueValue;
        return longValue * longValue + longValueValue * longValueValue;
    }

    private void clearState() {
        this.entrySetStringByBlockPos.clear();
        this.chunkPosEntries.clear();
        this.chunkPos = null;
        this.lastUpdateTimestampForRender3D = 0L;
        this.cachedCountForRender3D = -1;
    }
}
