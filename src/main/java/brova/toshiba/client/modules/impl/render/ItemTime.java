package brova.toshiba.client.modules.impl.render;

import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.PlayerUpdateEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.events.implement.EventPacket;
import brova.toshiba.client.events.implement.EventPacket.Type;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.render.ItemTime.StructureType;
import brova.toshiba.client.modules.impl.render.ItemTime.TimedStructure;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.Renderer2D;
import brova.toshiba.client.utils.render.font.FontManager;
import brova.toshiba.client.utils.render.font.TextRenderer;
import brova.toshiba.mixin.BossBarHudAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBar.Style;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ItemTime extends Module {

    public static ItemTime INSTANCE = new ItemTime();

    private final List<Block> blockEntries = Arrays.asList(Blocks.QUARTZ_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.PURPUR_PILLAR, Blocks.END_STONE_BRICKS, Blocks.NETHER_BRICKS, Blocks.GILDED_BLACKSTONE, Blocks.PRISMARINE_BRICKS, Blocks.ICE, Blocks.NETHER_WART_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.NETHERITE_BLOCK, Blocks.WHITE_STAINED_GLASS, Blocks.BLACK_CONCRETE, Blocks.POLISHED_BASALT);

    private final List<TimedStructure> removeIfItemTimeTimedStructureEntries = new ArrayList<>();

    private final Set<BlockPos> encompassPositionsBlockPosEntries = new HashSet<>();

    private int cachedCount;

    private final Matrix4f matrix4f = new Matrix4f();

    private Vec3d pos = Vec3d.ZERO;

    private final Quaternionf quaternionf = new Quaternionf();

    private boolean stateFlag = false;

    public ItemTime() {
        super("ItemTime", "Отображает время до исчезновения структур трапки и пласта", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable() {
        for (TimedStructure timedStructure : this.removeIfItemTimeTimedStructureEntries) {
            timedStructure.removeBossBar();
        }
        this.removeIfItemTimeTimedStructureEntries.clear();
        this.encompassPositionsBlockPosEntries.clear();
        this.cachedCount = 0;
        this.stateFlag = false;
        super.onDisable();
    }

    @EventHandler
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.getType() == Type.RECEIVE && mc.world != null) {
            var packet = eventPacket.getPacket();
            if (packet instanceof BlockUpdateS2CPacket || packet instanceof ChunkDeltaUpdateS2CPacket) {
                ClientWorld world = mc.world;
                mc.execute(() -> {
                    if (this.isEnable() && mc.world == world) {
                        if (packet instanceof BlockUpdateS2CPacket blockUpdateS2CPacket) {
                            this.updateBlockWithBlockState(blockUpdateS2CPacket.getPos(), blockUpdateS2CPacket.getState());
                        } else if (packet instanceof ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket) {
                            chunkDeltaUpdateS2CPacket.visitUpdates(this::updateBlockWithBlockState);
                        }
                    }
                });
            }
        }
    }

    private void updateBlockWithBlockState(BlockPos position, BlockState blockState) {
        if (mc.world != null) {
            BlockState blockStateValue = mc.world.getBlockState(position);
            if (this.blockEntries.contains(blockState.getBlock()) && !this.blockEntries.contains(blockStateValue.getBlock())) {
                this.encompassPositionsBlockPosEntries.add(position.toImmutable());
                this.cachedCount = 2;
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player != null) {
            this.removeIfItemTimeTimedStructureEntries.removeIf(input -> {
                if (input.isExpired()) {
                    input.removeBossBar();
                    return true;
                } else {
                    return false;
                }
            });
            for (TimedStructure timedStructure : this.removeIfItemTimeTimedStructureEntries) {
                float floatValue = Math.max(0.0F, (float) timedStructure.getRemainingTime() / 1000.0F);
                if (timedStructure.getBox().contains(mc.player.getBlockPos())) {
                    timedStructure.updateBossBar(floatValue / ((float) timedStructure.getType().getCooldown() / 1000.0F), floatValue);
                } else {
                    timedStructure.removeBossBar();
                }
            }
            if (this.cachedCount > 0) {
                this.cachedCount--;
                if (this.cachedCount == 0) {
                    BlockBox blockBox = BlockBox.encompassPositions(this.encompassPositionsBlockPosEntries).orElse(null);
                    if (blockBox != null) {
                        for (StructureType structureType : StructureType.values()) {
                            if (structureType.matches(blockBox, this.encompassPositionsBlockPosEntries)) {
                                this.removeIfItemTimeTimedStructureEntries.add(new TimedStructure(structureType, blockBox));
                                break;
                            }
                        }
                    }
                    this.encompassPositionsBlockPosEntries.clear();
                }
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null) {
            this.matrix4f.set(event.getProjectionMatrix());
            this.pos = event.getCamera().getPos();
            this.quaternionf.set(event.getCamera().getRotation());
            this.stateFlag = true;
        }
    }

    @EventHandler
    public void onDraw(HudRenderEvent event) {
        if (this.stateFlag && !this.removeIfItemTimeTimedStructureEntries.isEmpty() && mc.player != null) {
            DrawContext context = event.getContext();
            MatrixStack matrices = context.getMatrices();
            TextRenderer font = FontManager.getFont("manrope", 14);
            for (TimedStructure timedStructure : this.removeIfItemTimeTimedStructureEntries) {
                float floatValue = Math.max(0.0F, (float) timedStructure.getRemainingTime() / 1000.0F);
                String text = String.format(Locale.US, "%.1f", floatValue);
                BlockPos center = timedStructure.getBox().getCenter();
                Vec3d position = new Vec3d(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
                Vec3d positionValue = this.createScaledWidthWithScaledHeight(position);
                if (positionValue != null) {
                    float floatValueValue = font != null ? font.getStringWidth(text) : mc.textRenderer.getWidth(text);
                    if (font != null) {
                        font.getHeight();
                    } else {
                        float floatValueResult = 9.0F;
                    }
                    float floatValueItem = floatValueValue + 6.0F;
                    float floatValueEntry = 14.0F + floatValueItem;
                    float floatValueCopy = (float) positionValue.x - floatValueEntry / 2.0F;
                    float floatValue2 = (float) positionValue.y - 7.0F;
                    Renderer2D.drawRoundedRect(matrices, floatValueCopy, floatValue2, 14.0F, 14.0F, 3.0F, ColorMathUtil.rgba(0, 0, 0, 160));
                    ItemStack defaultStack = timedStructure.getType().getItem().getDefaultStack();
                    matrices.push();
                    matrices.translate(floatValueCopy + 1.0F, floatValue2 + 1.0F, 0.0F);
                    matrices.scale(0.75F, 0.75F, 1.0F);
                    context.drawItem(defaultStack, 0, 0);
                    matrices.pop();
                    float floatValue3 = floatValueCopy + 15.0F;
                    Renderer2D.drawRoundedRect(matrices, floatValue3, floatValue2, floatValueItem, 14.0F, 3.0F, ColorMathUtil.rgba(0, 0, 0, 160));
                    if (font != null) {
                        font.drawString(matrices, text, floatValue3 + 3.0F, floatValue2 + 4.0F, -1);
                    } else {
                        context.drawText(mc.textRenderer, text, (int) (floatValue3 + 3.0F), (int) (floatValue2 + 4.0F), -1, true);
                    }
                }
            }
        }
    }

    private Vec3d createScaledWidthWithScaledHeight(Vec3d position) {
        if (mc.getWindow() == null) {
            return null;
        } else {
            Vector3f vector3f = new Vector3f((float) (position.x - this.pos.x), (float) (position.y - this.pos.y), (float) (position.z - this.pos.z));
            Quaternionf quaternion = new Quaternionf(this.quaternionf).conjugate();
            vector3f.rotate(quaternion);
            Vector4f vector4f = new Vector4f(vector3f.x, vector3f.y, vector3f.z, 1.0F);
            this.matrix4f.transform(vector4f);
            float w = vector4f.w;
            if (w <= 1.0E-5F) {
                return null;
            } else {
                float floatValue = vector4f.x / w;
                float floatValueValue = vector4f.y / w;
                float floatValueResult = vector4f.z / w;
                float floatValueItem = (floatValue * 0.5F + 0.5F) * mc.getWindow().getScaledWidth();
                float floatValueEntry = (1.0F - (floatValueValue * 0.5F + 0.5F)) * mc.getWindow().getScaledHeight();
                if (!Float.isNaN(floatValueItem) && !Float.isNaN(floatValueEntry) && !Float.isInfinite(floatValueItem) && !Float.isInfinite(floatValueEntry)) {
                    return !(floatValueItem < -400.0F) && !(floatValueEntry < -400.0F) && !(floatValueItem > mc.getWindow().getScaledWidth() + 400) && !(floatValueEntry > mc.getWindow().getScaledHeight() + 400) ? new Vec3d(floatValueItem, floatValueEntry, floatValueResult) : null;
                } else {
                    return null;
                }
            }
        }
    }

    static Map<UUID, ClientBossBar> createMapFromToshiba$getBossBarsWithBossBarHud() {
        return mc.inGameHud == null ? null : ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).toshiba$getBossBars();
    }

    public static class TimedStructure {

        private final StructureType type;

        private final BlockBox box;

        private final long startTime;

        private ClientBossBar bossBar;

        public TimedStructure(StructureType type, BlockBox box) {
            this.type = type;
            this.box = box;
            this.startTime = System.currentTimeMillis();
        }

        public long getRemainingTime() {
            return Math.max(0L, this.startTime + this.type.getCooldown() - System.currentTimeMillis());
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.startTime >= this.type.getCooldown();
        }

        public void updateBossBar(float floatValue, float floatValueValue) {
            MutableText mutableText = Text.literal(String.format(Locale.US, "%s — %.1f сек", this.type.getDisplayName(), floatValueValue));
            var entries = ItemTime.createMapFromToshiba$getBossBarsWithBossBarHud();
            if (entries != null) {
                if (this.bossBar == null) {
                    this.bossBar = new ClientBossBar(UUID.randomUUID(), mutableText, MathHelper.clamp(floatValue, 0.0F, 1.0F), Color.WHITE, Style.PROGRESS, false, false, false);
                    entries.put(this.bossBar.getUuid(), this.bossBar);
                } else {
                    this.bossBar.setName(mutableText);
                    this.bossBar.setPercent(MathHelper.clamp(floatValue, 0.0F, 1.0F));
                }
            }
        }

        public void removeBossBar() {
            if (this.bossBar != null) {
                var entries = ItemTime.createMapFromToshiba$getBossBarsWithBossBarHud();
                if (entries != null) {
                    entries.remove(this.bossBar.getUuid());
                }
                this.bossBar = null;
            }
        }

        public StructureType getType() {
            return this.type;
        }

        public BlockBox getBox() {
            return this.box;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public ClientBossBar getBossBar() {
            return this.bossBar;
        }
    }

    public enum StructureType {

        DRAGON_TRAPKA("Драконья трапка", new int[][] { { 7, 7, 7 }, { 7, 7, 6 } }, true, 30000L, Items.NETHERITE_SCRAP), TRAPKA("Трапка", new int[][] { { 5, 5, 5 }, { 5, 5, 4 }, { 5, 6, 5 }, { 5, 6, 4 } }, true, 15000L, Items.NETHERITE_SCRAP), DRAGON_PLAST("Драконий пласт", new int[][] { { 7, 7, 2 }, { 7, 7, 1 } }, true, 20000L, Items.DRIED_KELP), PLAST("Пласт", new int[][] { { 5, 5, 2 }, { 5, 5, 1 } }, true, 20000L, Items.DRIED_KELP), GARMOSHKA("Гармошка", new int[][] { { 5, 5, 5 }, { 5, 6, 5 } }, false, 20000L, Items.DRIED_KELP);

        private final String displayName;

        private final int[][] dimensions;

        private final boolean hollow;

        private final long cooldown;

        private final Item item;

        private StructureType(String nullxx, int[][] nullxxx, boolean nullxxxx, long nullxxxxx, Item nullxxxxxx) {
            this.displayName = nullxx;
            this.dimensions = nullxxx;
            this.hollow = nullxxxx;
            this.cooldown = nullxxxxx;
            this.item = nullxxxxxx;
        }

        public boolean matches(BlockBox blockBox, Set<BlockPos> positions) {
            int[] values = new int[] { blockBox.getBlockCountX(), blockBox.getBlockCountY(), blockBox.getBlockCountZ() };
            Arrays.sort(values);
            for (int[] valuesValue : this.dimensions) {
                int[] valuesResult = valuesValue.clone();
                Arrays.sort(valuesResult);
                if (Arrays.equals(values, valuesResult)) {
                    return this.hollow == positions.stream().noneMatch(position -> position.getX() > blockBox.getMinX() && position.getX() < blockBox.getMaxX() && position.getY() > blockBox.getMinY() && position.getY() < blockBox.getMaxY() && position.getZ() > blockBox.getMinZ() && position.getZ() < blockBox.getMaxZ());
                }
            }
            return false;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public int[][] getDimensions() {
            return this.dimensions;
        }

        public boolean isHollow() {
            return this.hollow;
        }

        public long getCooldown() {
            return this.cooldown;
        }

        public Item getItem() {
            return this.item;
        }
    }
}
