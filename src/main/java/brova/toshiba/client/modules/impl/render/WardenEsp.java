package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.data.Waypoint;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.gui.hud.notification.NotificationManager;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.WardenEsp.WardenBounds;
import brova.toshiba.client.modules.impl.render.WardenEsp.WardenLocation;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class WardenEsp extends Module {

    public static WardenEsp INSTANCE = new WardenEsp();

    private static final Pattern hologramLabel = Pattern.compile("(\\d{1,2}):(\\d{2})");

    private final FloatSetting distance = new FloatSetting("Дистанция", 120.0F, 10.0F, 120.0F, 1.0F);

    private final BooleanSetting notifications = new BooleanSetting("Уведомления", true);

    private final BooleanSetting markers = new BooleanSetting("Метки", true);

    public final Map<BlockPos, String> foundBlocks = new ConcurrentHashMap<>();

    private final Set<ChunkPos> chunkPosEntries = ConcurrentHashMap.newKeySet();

    public final Map<BlockPos, WardenLocation> chestTimers = new ConcurrentHashMap<>();

    private ChunkPos chunkPos;

    private int cachedCountForClearState = -1;

    private long lastUpdateTimestampForClearState;

    private final Matrix4f projectionMatrix = new Matrix4f();

    private final Matrix4f viewMatrix = new Matrix4f();

    private Vec3d pos = Vec3d.ZERO;

    private int scaledWidth;

    private int scaledHeight;

    private boolean stateFlag;

    private final Vector4f vector4f = new Vector4f();

    private final WardenBounds wardenEspWardenBounds = new WardenBounds();

    private static int removeIf = 1;

    public WardenEsp() {
        super("WardenEsp", "Подсвечивает сундуки и показывает таймер cундука в мире Вардна", ModuleCategory.RENDER);
        this.addSettings(this.distance, this.notifications, this.markers);
    }

    @Override
    public void onEnable() {
        this.clearState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        for (WardenLocation wardenLocation : this.chestTimers.values()) {
            if (wardenLocation.waypointName != null) {
                Waypoint activeWaypoint = Toshiba.INSTANCE.waypointStorage.getActiveWaypoint();
                if (activeWaypoint != null && activeWaypoint.getName().equals(wardenLocation.waypointName)) {
                    Toshiba.INSTANCE.waypointStorage.clear();
                }
            }
        }
        this.clearState();
        this.stateFlag = false;
        super.onDisable();
    }

    private void clearState() {
        this.foundBlocks.clear();
        this.chunkPosEntries.clear();
        this.chestTimers.clear();
        this.chunkPos = null;
        this.lastUpdateTimestampForClearState = 0L;
        this.cachedCountForClearState = -1;
    }

    private int calculateInteger() {
        return Math.round(this.distance.get());
    }

    @EventHandler(priority = 100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null) {
            this.stateFlag = true;
            this.projectionMatrix.set(event.getProjectionMatrix());
            this.viewMatrix.set(event.getPositionMatrix());
            this.pos = event.getCamera().getPos();
            this.scaledWidth = mc.getWindow().getScaledWidth();
            this.scaledHeight = mc.getWindow().getScaledHeight();
            int cachedCountForClearState = this.calculateInteger();
            ChunkPos chunkPos = new ChunkPos(mc.player.getBlockPos());
            if (cachedCountForClearState != this.cachedCountForClearState) {
                this.clearState();
                this.cachedCountForClearState = cachedCountForClearState;
            }
            if (this.chunkPos == null || !this.chunkPos.equals(chunkPos)) {
                this.chunkPosEntries.clear();
                this.chunkPos = chunkPos;
            }
            long lastUpdateTimestampForClearState = System.currentTimeMillis();
            if (lastUpdateTimestampForClearState - this.lastUpdateTimestampForClearState >= 50L) {
                this.updateBlockPosWithSort(cachedCountForClearState);
                this.lastUpdateTimestampForClearState = lastUpdateTimestampForClearState;
            }
            this.updateRemoveIfWithEntrySet(mc.player.getPos(), cachedCountForClearState);
            this.renderDepthMaskWithCamera(event.getMatrices());
            this.updateMessageWithParseInt();
        }
    }

    @EventHandler(priority = 100)
    public void onRender2D(HudRenderEvent event) {
        if (this.stateFlag && mc.world != null && mc.player != null && !this.chestTimers.isEmpty()) {
            TextRenderer font = FontManager.getFont("manrope", 24);
            TextRenderer fontValue = FontManager.getFont("manrope", 28);
            if (font != null && fontValue != null) {
                Vec3d pos = this.pos;
                MatrixStack matrices = event.getContext().getMatrices();
                for (WardenLocation wardenLocation : this.chestTimers.values()) {
                    BlockPos posValue = wardenLocation.pos;
                    double doubleValue = pos.x - (posValue.getX() + 0.5);
                    double doubleValueValue = pos.y - (posValue.getY() + 1.2);
                    double doubleValueResult = pos.z - (posValue.getZ() + 0.5);
                    double doubleValueItem = doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult;
                    double squareRoot = Math.sqrt(doubleValueItem);
                    if (wardenLocation.timeRemainingSeconds <= 0 && squareRoot <= 20.0) {
                        wardenLocation.hasBeenCloseToUpdated = true;
                    }
                    if (!(squareRoot > 120.0) && this.isTransformWithNaN(posValue.getX() + 0.5, posValue.getY() + 1.2, posValue.getZ() + 0.5, this.wardenEspWardenBounds) && this.isWardenEspWardenBounds(this.wardenEspWardenBounds)) {
                        String text = (int) Math.round(squareRoot) + "m";
                        String textValue;
                        int intValue;
                        if (wardenLocation.timeRemainingSeconds <= 0 && wardenLocation.hasBeenCloseToUpdated) {
                            textValue = "Обновлен";
                            intValue = ColorMathUtil.rgba(95, 255, 20, 255);
                        } else {
                            float floatValue = 0.0F;
                            if (wardenLocation.timeRemainingSeconds <= 15) {
                                long elapsedMillis = System.currentTimeMillis() - wardenLocation.lastUpdatedMs;
                                double doubleValueEntry = wardenLocation.timeRemainingSeconds - elapsedMillis / 1000.0;
                                if (doubleValueEntry < 0.0) {
                                    doubleValueEntry = 0.0;
                                }
                                floatValue = (float) ((15.0 - doubleValueEntry) / 15.0);
                                floatValue = MathHelper.clamp(floatValue, 0.0F, 1.0F);
                            }
                            intValue = ColorMathUtil.interpolateColor(ColorMathUtil.rgba(255, 255, 255, 255), ColorMathUtil.rgba(255, 41, 25, 255), floatValue);
                            textValue = this.formatTextFromFormat(Math.max(0, wardenLocation.timeRemainingSeconds));
                        }
                        float stringWidth = font.getStringWidth(text);
                        float stringWidthValue = fontValue.getStringWidth(textValue);
                        float floatValueValue = 8.0F;
                        float floatValueResult = 8.0F;
                        float floatValueItem = floatValueValue + stringWidth + floatValueResult + stringWidthValue + floatValueValue;
                        float floatValueEntry = 20.0F;
                        float floatValueCopy = 0.5F;
                        float projectedX = this.wardenEspWardenBounds.projectedX;
                        float projectedY = this.wardenEspWardenBounds.projectedY;
                        matrices.push();
                        matrices.translate(projectedX, projectedY, 0.0F);
                        matrices.scale(floatValueCopy, floatValueCopy, 1.0F);
                        float floatValue2 = floatValueItem / 2.0F;
                        float floatValue3 = floatValueEntry / 2.0F;
                        matrices.translate(-floatValue2, -floatValue3, 0.0F);
                        Renderer2D.drawRoundedRect(matrices, 0.0F, 0.0F, floatValueItem, floatValueEntry, 5.0F, ColorMathUtil.rgba(0, 0, 0, 123));
                        float floatValue4 = floatValueValue + stringWidth / 2.0F;
                        float floatValue5 = 10.0F - font.getHeight() / 2.0F + 6.0F;
                        font.drawCenteredString(matrices, text, floatValue4, floatValue5, ColorMathUtil.rgba(126, 126, 126, 255));
                        float floatValue6 = floatValueValue + stringWidth + floatValueResult + stringWidthValue / 2.0F;
                        float floatValue7 = 10.0F - fontValue.getHeight() / 2.0F + 6.0F;
                        fontValue.drawCenteredString(matrices, textValue, floatValue6, floatValue7, intValue);
                        matrices.pop();
                    }
                }
            }
        }
    }

    private boolean isTransformWithNaN(double doubleValue, double doubleValueValue, double doubleValueResult, WardenBounds wardenBounds) {
        float floatValue = (float) (doubleValue - this.pos.x);
        float floatValueValue = (float) (doubleValueValue - this.pos.y);
        float floatValueResult = (float) (doubleValueResult - this.pos.z);
        this.vector4f.set(floatValue, floatValueValue, floatValueResult, 1.0F);
        this.viewMatrix.transform(this.vector4f);
        this.projectionMatrix.transform(this.vector4f);
        float w = this.vector4f.w;
        if (w <= 1.0E-5F) {
            return false;
        } else {
            float floatValueItem = this.vector4f.x / w;
            float floatValueEntry = this.vector4f.y / w;
            float cachedScalarForTransformWithNaN = this.vector4f.z / w;
            float projectedX = (floatValueItem * 0.5F + 0.5F) * this.scaledWidth;
            float projectedY = (1.0F - (floatValueEntry * 0.5F + 0.5F)) * this.scaledHeight;
            if (Float.isNaN(projectedX) || Float.isNaN(projectedY)) {
                return false;
            } else if (!Float.isInfinite(projectedX) && !Float.isInfinite(projectedY)) {
                wardenBounds.projectedX = projectedX;
                wardenBounds.projectedY = projectedY;
                wardenBounds.cachedScalarForTransformWithNaN = cachedScalarForTransformWithNaN;
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isWardenEspWardenBounds(WardenBounds wardenBounds) {
        return wardenBounds.projectedX >= -300.0F && wardenBounds.projectedY >= -300.0F && wardenBounds.projectedX <= this.scaledWidth + 300.0F && wardenBounds.projectedY <= this.scaledHeight + 300.0F;
    }

    private String formatTextFromFormat(int intValue) {
        int intValueValue = intValue / 60;
        int intValueResult = intValue % 60;
        return String.format("%02d:%02d", intValueValue, intValueResult);
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
                        if (!blockState.isAir() && blockState.isOf(Blocks.CHEST)) {
                            this.foundBlocks.put(mutablePosition.toImmutable(), "chest");
                        }
                    }
                }
            }
        }
    }

    private long calculateChunkPos(ChunkPos chunkPosition, int intValue, int intValueValue) {
        long longValue = chunkPosition.x - intValue;
        long longValueValue = chunkPosition.z - intValueValue;
        return longValue * longValue + longValueValue * longValueValue;
    }

    private void updateRemoveIfWithEntrySet(Vec3d position, int intValue) {
        if (mc.world == null) {
            this.foundBlocks.clear();
        } else {
            int intValueValue = intValue * intValue;
            this.foundBlocks.entrySet().removeIf(input -> {
                BlockPos key = input.getKey();
                BlockState blockState = mc.world.getBlockState(key);
                if (blockState.isAir()) {
                    return true;
                } else {
                    return !blockState.isOf(Blocks.CHEST) ? true : key.getSquaredDistance(position) > intValueValue;
                }
            });
        }
    }

    private void renderDepthMaskWithCamera(MatrixStack matrices) {
        if (!this.foundBlocks.isEmpty()) {
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
            for (BlockPos position : this.foundBlocks.keySet()) {
                this.renderColorWithVertex(buffer, positionMatrix, position, 0.76F, 1.0F, 0.15F, 0.18F);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            matrices.pop();
        }
    }

    private void renderColorWithVertex(BufferBuilder buffer, Matrix4f matrix, BlockPos position, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
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

    private void updateMessageWithParseInt() {
        if (mc.world != null && mc.player != null) {
            HashSet<net.minecraft.util.math.BlockPos> values = new HashSet<>();
            for (Entity entity : mc.world.getEntities()) {
                if (entity != null) {
                    double doubleValue = entity.squaredDistanceTo(mc.player);
                    if (!(doubleValue > 14400.0)) {
                        String text = this.createTextFromCustomName(entity);
                        if (text != null) {
                            Matcher matcher = hologramLabel.matcher(text);
                            if (matcher.find()) {
                                int intValue = Integer.parseInt(matcher.group(1));
                                int intValueValue = Integer.parseInt(matcher.group(2));
                                int timeRemainingSeconds = intValue * 60 + intValueValue;
                                BlockPos position = this.createBlockPosFromKeySetWithSquaredDistance(entity);
                                if (position != null) {
                                    values.add(position);
                                    WardenLocation wardenLocation = this.chestTimers.get(position);
                                    if (wardenLocation == null) {
                                        wardenLocation = new WardenLocation(position, timeRemainingSeconds);
                                        this.chestTimers.put(position, wardenLocation);
                                    } else {
                                        wardenLocation.timeRemainingSeconds = timeRemainingSeconds;
                                        wardenLocation.lastUpdatedMs = System.currentTimeMillis();
                                    }
                                    if (wardenLocation.timeRemainingSeconds <= 15) {
                                        if (this.notifications.isState() && !wardenLocation.notified15s) {
                                            NotificationManager.pushCustom("Сундук скоро обновиться, кординаты: " + position.getX() + " " + position.getY() + " " + position.getZ() + ". Время до обновления: " + wardenLocation.timeRemainingSeconds + " секунд", "p");
                                            wardenLocation.notified15s = true;
                                        }
                                        if (this.markers.isState() && wardenLocation.waypointName == null) {
                                            String waypointName = "Warden" + removeIf++;
                                            wardenLocation.waypointName = waypointName;
                                            Waypoint waypoint = new Waypoint(waypointName, position.getX(), position.getY(), position.getZ());
                                            Toshiba.INSTANCE.waypointStorage.set(waypoint);
                                            ChatUtil.sendMessage("Метка '" + waypointName + "' поставлена: " + position.getX() + " " + position.getY() + " " + position.getZ());
                                        }
                                    } else if (wardenLocation.waypointName != null) {
                                        Waypoint activeWaypoint = Toshiba.INSTANCE.waypointStorage.getActiveWaypoint();
                                        if (activeWaypoint != null && activeWaypoint.getName().equals(wardenLocation.waypointName)) {
                                            Toshiba.INSTANCE.waypointStorage.clear();
                                            ChatUtil.sendMessage("Метка удалена!");
                                        }
                                        wardenLocation.waypointName = null;
                                        wardenLocation.notified15s = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            long lastUpdatedMs = System.currentTimeMillis();
            this.chestTimers.entrySet().removeIf(input -> {
                BlockPos key = input.getKey();
                WardenLocation value = input.getValue();
                if (this.isBlockEntityWithAnimationProgress(key)) {
                    if (value.waypointName != null) {
                        Waypoint activeWaypointValue = Toshiba.INSTANCE.waypointStorage.getActiveWaypoint();
                        if (activeWaypointValue != null && activeWaypointValue.getName().equals(value.waypointName)) {
                            Toshiba.INSTANCE.waypointStorage.clear();
                            ChatUtil.sendMessage("Метка удалена!");
                        }
                    }
                    return true;
                } else {
                    if (!values.contains(key)) {
                        long longValue = lastUpdatedMs - value.lastUpdatedMs;
                        if (longValue >= 1000L) {
                            value.timeRemainingSeconds -= (int) (longValue / 1000L);
                            value.lastUpdatedMs = lastUpdatedMs;
                            if (value.timeRemainingSeconds <= 15) {
                                if (this.notifications.isState() && !value.notified15s) {
                                    NotificationManager.pushCustom("Сундук скоро обновиться, кординаты: " + key.getX() + " " + key.getY() + " " + key.getZ() + ". Время до обновления: " + value.timeRemainingSeconds + " секунд", "p");
                                    value.notified15s = true;
                                }
                                if (this.markers.isState() && value.waypointName == null) {
                                    String waypointNameValue = "Warden" + removeIf++;
                                    value.waypointName = waypointNameValue;
                                    Waypoint waypointValue = new Waypoint(waypointNameValue, key.getX(), key.getY(), key.getZ());
                                    Toshiba.INSTANCE.waypointStorage.set(waypointValue);
                                    ChatUtil.sendMessage("Метка '" + waypointNameValue + "' поставлена: " + key.getX() + " " + key.getY() + " " + key.getZ());
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    private boolean isBlockEntityWithAnimationProgress(BlockPos position) {
        if (mc.world == null) {
            return false;
        } else {
            return mc.world.getBlockEntity(position) instanceof ChestBlockEntity chestBlockEntity ? chestBlockEntity.getAnimationProgress(1.0F) > 0.0F : false;
        }
    }

    private String createTextFromCustomName(Entity entity) {
        return entity.getCustomName() != null ? entity.getCustomName().getString() : null;
    }

    private BlockPos createBlockPosFromKeySetWithSquaredDistance(Entity entity) {
        Vec3d pos = entity.getPos();
        BlockPos position = null;
        double MAX_VALUE = Double.MAX_VALUE;
        for (BlockPos positionValue : this.foundBlocks.keySet()) {
            double squaredDistance = positionValue.getSquaredDistance(pos);
            if (squaredDistance <= 16.0 && squaredDistance < MAX_VALUE) {
                MAX_VALUE = squaredDistance;
                position = positionValue;
            }
        }
        if (position != null) {
            return position;
        } else {
            BlockPos positionResult = new BlockPos(MathHelper.floor(pos.x), MathHelper.floor(pos.y), MathHelper.floor(pos.z));
            for (int index = -4; index <= 2; index++) {
                for (int indexValue = -2; indexValue <= 2; indexValue++) {
                    for (int indexResult = -2; indexResult <= 2; indexResult++) {
                        BlockPos positionItem = positionResult.add(indexValue, index, indexResult);
                        if (mc.world.getBlockState(positionItem).isOf(Blocks.CHEST)) {
                            this.foundBlocks.put(positionItem, "chest");
                            return positionItem;
                        }
                    }
                }
            }
            return null;
        }
    }

    public boolean isHologramLabel(String text) {
        return text == null ? false : hologramLabel.matcher(text).find();
    }

    public static class WardenLocation {

        public final BlockPos pos;

        public long lastUpdatedMs;

        public int timeRemainingSeconds;

        public boolean notified15s = false;

        public String waypointName = null;

        public boolean hasBeenCloseToUpdated = false;

        public WardenLocation(BlockPos pos, int timeRemainingSeconds) {
            this.pos = pos;
            this.timeRemainingSeconds = timeRemainingSeconds;
            this.lastUpdatedMs = System.currentTimeMillis();
        }
    }

    public static class WardenBounds {

        float projectedX;

        float projectedY;

        float cachedScalarForTransformWithNaN;

        WardenBounds() {
        }
    }
}
