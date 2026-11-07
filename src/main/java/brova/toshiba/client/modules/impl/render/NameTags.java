package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.event.impl.WorldRenderEvent;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.modules.impl.render.NameTags.ItemLabelCache;
import brova.toshiba.client.modules.impl.render.NameTags.PlayerInfo;
import brova.toshiba.client.modules.impl.render.NameTags.PlayerLabel;
import brova.toshiba.client.modules.impl.render.NameTags.PlayerModelPose;
import brova.toshiba.client.modules.impl.render.NameTags.ProjectedPosition;
import brova.toshiba.client.modules.impl.render.NameTags.TextSegment;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.FloatSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.player.HealthUtil;
import brova.toshiba.client.utils.render.ColorMathUtil;
import brova.toshiba.client.utils.render.font.RankGlyphReplacement;
import brova.toshiba.client.utils.render.font.StyledFontRenderer;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterAnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class NameTags extends Module {

    public static NameTags INSTANCE = new NameTags();

    private final BooleanSetting players = new BooleanSetting("Игроки", true);

    private final BooleanSetting mobs = new BooleanSetting("Мобы", true);

    private final BooleanSetting animals = new BooleanSetting("Животные", true);

    private final BooleanSetting items = new BooleanSetting("Предметы", true);

    private final MultiSetting displayOptions = new MultiSetting("Отображать", this.players, this.mobs, this.animals, this.items);

    private final MultiSetting elements = new MultiSetting("Элементы", new BooleanSetting("Теги", true), new BooleanSetting("Броня", true), new BooleanSetting("Скелет", true));

    private final FloatSetting skeletonWidth = new FloatSetting("Толщина скелета", 2.5F, 2.5F, 4.0F, 0.1F).visible(() -> this.elements.is("Скелет"));

    private final Matrix4f matrix4f = new Matrix4f();

    private double x;

    private double y;

    private double z;

    private float tickDelta;

    private int scaledWidth;

    private int scaledHeight;

    private boolean stateFlag;

    private int cachedCountForRender3D = -1;

    private long lastUpdateTimestampForDisable;

    private final Map<UUID, PlayerLabel> entrySetNameTagsPlayerLabelByUUID = new HashMap<>();

    private final Map<UUID, PlayerInfo> entrySetNameTagsPlayerInfoByUUID = new HashMap<>();

    private final Map<UUID, PlayerModelPose> entrySetNameTagsPlayerModelPoseByUUID = new HashMap<>();

    private final Map<Integer, ItemLabelCache> entrySetNameTagsItemLabelCacheEntries = new HashMap<>();

    private final MatrixStack matrixStack = new MatrixStack();

    private final PlayerModelPose copyFromNameTagsPlayerModelPose = new PlayerModelPose();

    private final Vector4f vector4f = new Vector4f();

    private final ProjectedPosition nameTagsProjectedPosition = new ProjectedPosition();

    private final ItemStack[] itemStackValues = new ItemStack[6];

    private final String[] textValues = new String[6];

    private final Vector3f neckPosition = new Vector3f();

    private final Vector3f headPosition = new Vector3f();

    private final Vector3f chestPosition = new Vector3f();

    private final Vector3f pelvisPosition = new Vector3f();

    private final Vector3f rightShoulderPosition = new Vector3f();

    private final Vector3f leftShoulderPosition = new Vector3f();

    private final Vector3f rightHandPosition = new Vector3f();

    private final Vector3f leftHandPosition = new Vector3f();

    private final Vector3f rightHipPosition = new Vector3f();

    private final Vector3f leftHipPosition = new Vector3f();

    private final Vector3f rightFootPosition = new Vector3f();

    private final Vector3f leftFootPosition = new Vector3f();

    private final Vector3f normalVector = new Vector3f();

    public NameTags() {
        super("NameTags", "Показывает игроков через стенки.", ModuleCategory.RENDER);
        this.addSettings(this.displayOptions, this.elements, this.skeletonWidth);
    }

    @Override
    public void onDisable() {
        this.stateFlag = false;
        this.entrySetNameTagsPlayerLabelByUUID.clear();
        this.entrySetNameTagsPlayerInfoByUUID.clear();
        this.entrySetNameTagsPlayerModelPoseByUUID.clear();
        this.entrySetNameTagsItemLabelCacheEntries.clear();
        this.lastUpdateTimestampForDisable = 0L;
        super.onDisable();
    }

    @EventHandler(priority = 100)
    public void onRender3D(WorldRenderEvent event) {
        if (mc.world != null && mc.player != null) {
            this.stateFlag = true;
            this.matrix4f.set(event.getProjectionMatrix()).mul(event.getPositionMatrix());
            Vec3d pos = event.getCamera().getPos();
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
            this.tickDelta = event.getTickDelta();
            this.scaledWidth = mc.getWindow().getScaledWidth();
            this.scaledHeight = mc.getWindow().getScaledHeight();
            this.cachedCountForRender3D = this.calculateThemesWithThemeColor();
            long currentTimeMillis = System.currentTimeMillis();
            this.updateRemoveIfWithEntrySet(currentTimeMillis);
            if (this.elements.is("Скелет") && this.players.isState()) {
                MatrixStack matrices = event.getMatrices();
                float tickDelta = this.tickDelta;
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (this.isAlive(player)) {
                        double x = player.getX();
                        double y = player.getY();
                        double z = player.getZ();
                        double doubleValue = this.x - x;
                        double doubleValueValue = this.y - y;
                        double doubleValueResult = this.z - z;
                        double doubleValueItem = doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult;
                        if (!(doubleValueItem < 1.0) && !(doubleValueItem > 16384.0)) {
                            double interpolatedValue = MathHelper.lerp((double) tickDelta, player.prevX, x);
                            double interpolatedValueValue = MathHelper.lerp((double) tickDelta, player.prevY, y);
                            double interpolatedValueResult = MathHelper.lerp((double) tickDelta, player.prevZ, z);
                            if (this.isNaNWithInfinite(interpolatedValue, interpolatedValueValue + player.getHeight() * 0.5, interpolatedValueResult, this.nameTagsProjectedPosition) && this.isNameTagsProjectedPosition(this.nameTagsProjectedPosition)) {
                                this.updateLerpWithUuid(matrices, player, tickDelta, this.calculateFriendWithName(player));
                            }
                        }
                    }
                }
            }
        } else {
            this.stateFlag = false;
        }
    }

    @EventHandler(priority = 100)
    public void onRender2D(HudRenderEvent event) {
        if (this.stateFlag && mc.world != null && mc.player != null) {
            boolean flag = !this.elements.getSettings().isEmpty() && this.elements.getSettings().get(0).isState();
            boolean flagValue = this.elements.getSettings().size() > 1 && this.elements.getSettings().get(1).isState();
            if (flag || flagValue) {
                StyledFontRenderer font = SystemFontManager.getFont("Manrope-Bold.ttf", 15.0F);
                if (font != null) {
                    boolean state = this.players.isState();
                    boolean stateValue = this.mobs.isState();
                    boolean stateResult = this.animals.isState();
                    boolean stateItem = this.items.isState();
                    int intValue = 0;
                    long currentTimeMillis = System.currentTimeMillis();
                    for (Entity entity : mc.world.getEntities()) {
                        if (!entity.isRemoved()) {
                            if (entity instanceof PlayerEntity player) {
                                if (state && player.isAlive() && (player != mc.player || !this.isThirdPersonWithCamera())) {
                                    double doubleValue = this.x - player.getX();
                                    double doubleValueValue = this.y - player.getY();
                                    double doubleValueResult = this.z - player.getZ();
                                    if (!(doubleValue * doubleValue + doubleValueValue * doubleValueValue + doubleValueResult * doubleValueResult > 16384.0) && this.isLerp(player, player.getHeight() + 0.25, this.nameTagsProjectedPosition) && this.isNameTagsProjectedPosition(this.nameTagsProjectedPosition)) {
                                        float projectedX = this.nameTagsProjectedPosition.projectedX;
                                        float projectedY = this.nameTagsProjectedPosition.projectedY;
                                        float floatValue = projectedY - 2.0F;
                                        if (flag) {
                                            floatValue = this.calculateContextWithHealthWithAbsorption(event, player, projectedX, projectedY, font, currentTimeMillis);
                                        }
                                        if (flagValue) {
                                            this.onHudRender(event, player, projectedX, floatValue);
                                        }
                                    }
                                }
                            } else if (flag) {
                                if (entity instanceof ItemEntity itemEntity) {
                                    if (stateItem && itemEntity.isAlive() && intValue < 48) {
                                        double doubleValueItem = this.x - itemEntity.getX();
                                        double doubleValueEntry = this.y - itemEntity.getY();
                                        double doubleValueCopy = this.z - itemEntity.getZ();
                                        if (!(doubleValueItem * doubleValueItem + doubleValueEntry * doubleValueEntry + doubleValueCopy * doubleValueCopy > 4096.0) && this.isLerp(itemEntity, itemEntity.getHeight() + 0.25, this.nameTagsProjectedPosition) && this.isNameTagsProjectedPosition(this.nameTagsProjectedPosition)) {
                                            this.onHudRender(event, itemEntity, this.nameTagsProjectedPosition.projectedX, this.nameTagsProjectedPosition.projectedY, font, currentTimeMillis);
                                            intValue++;
                                        }
                                    }
                                } else if (entity instanceof LivingEntity entityValue && !(entityValue instanceof ArmorStandEntity) && entityValue.isAlive()) {
                                    boolean passiveAnimal = this.isPassiveAnimal(entityValue);
                                    if (passiveAnimal ? stateResult : this.isHostileMob(entityValue) && stateValue) {
                                        double doubleValue2 = this.x - entityValue.getX();
                                        double doubleValue3 = this.y - entityValue.getY();
                                        double doubleValue4 = this.z - entityValue.getZ();
                                        if (!(doubleValue2 * doubleValue2 + doubleValue3 * doubleValue3 + doubleValue4 * doubleValue4 > 16384.0) && this.isLerp(entityValue, entityValue.getHeight() + 0.25, this.nameTagsProjectedPosition) && this.isNameTagsProjectedPosition(this.nameTagsProjectedPosition)) {
                                            this.onHudRender(event, entityValue, this.nameTagsProjectedPosition.projectedX, this.nameTagsProjectedPosition.projectedY, font);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private float calculateContextWithHealthWithAbsorption(HudRenderEvent event, PlayerEntity player, float floatValue, float floatValueValue, StyledFontRenderer styledFontRenderer, long longValue) {
        MatrixStack matrices = event.getContext().getMatrices();
        PlayerInfo playerInfo = this.createNameTagsPlayerInfoFromUuidWithFriend(player, styledFontRenderer, longValue);
        int intValue = Math.max(0, Math.round(HealthUtil.getHealthWithAbsorption(player)));
        String healthText = playerInfo.getHealthText(intValue);
        float floatValueResult = 2.0F;
        float floatValueItem = 1.0F;
        float floatValueEntry = 1.0F;
        float floatValueCopy = 1.0F;
        float stringWidth = styledFontRenderer.getStringWidth(healthText);
        float floatValue2 = playerInfo.prefixWidth + playerInfo.primaryTextWidth + playerInfo.secondaryTextWidth + floatValueResult + stringWidth;
        float floatValue3 = floatValue2 + floatValueItem * 2.0F;
        float floatValue4 = styledFontRenderer.getStringHeight(playerInfo.stringHeightText) + 2.0F;
        float floatValue5 = floatValue4 + floatValueEntry + floatValueCopy;
        float floatValue6 = this.calculateScaleFactor(floatValue - floatValue3 * 0.5F);
        float floatValue7 = this.calculateScaleFactor(floatValueValue - floatValue5 - 3.0F + 6.0F);
        float floatValue8 = floatValue6 + floatValueItem;
        float floatValue9 = floatValue7 + floatValueEntry + 1.5F;
        this.renderColorWithVertex(matrices, floatValue6, floatValue7, floatValue3, floatValue5);
        if (playerInfo.stateFlag) {
            this.updateString(styledFontRenderer, "[F] ", floatValue8, floatValue9, -11141291);
        }
        float floatValue10 = floatValue8 + playerInfo.prefixWidth;
        floatValue10 = this.calculateTextWithColor(styledFontRenderer, playerInfo.primaryTextRuns, floatValue10, floatValue9);
        floatValue10 = this.calculateTextWithColor(styledFontRenderer, playerInfo.secondaryTextRuns, floatValue10, floatValue9);
        this.updateString(styledFontRenderer, healthText, floatValue10 + floatValueResult, floatValue9, -169324207);
        return floatValue7;
    }

    private PlayerInfo createNameTagsPlayerInfoFromUuidWithFriend(PlayerEntity player, StyledFontRenderer styledFontRenderer, long longValue) {
        PlayerInfo playerInfo = this.entrySetNameTagsPlayerInfoByUUID.get(player.getUuid());
        if (playerInfo != null && longValue < playerInfo.lastUpdateTimestamp) {
            return playerInfo;
        } else {
            if (playerInfo == null) {
                playerInfo = new PlayerInfo();
                this.entrySetNameTagsPlayerInfoByUUID.put(player.getUuid(), playerInfo);
            }
            playerInfo.stateFlag = Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString());
            playerInfo.prefixWidth = playerInfo.stateFlag ? styledFontRenderer.getStringWidth("[F] ") : 0.0F;
            playerInfo.primaryTextRuns = this.createEmptyListWithText(player, longValue);
            playerInfo.secondaryTextRuns = this.createListFromNameWithReplace(player);
            playerInfo.stringHeightText = this.createTextFromTextWithAppend(playerInfo.secondaryTextRuns);
            playerInfo.primaryTextWidth = this.calculateStringWidthWithText(styledFontRenderer, playerInfo.primaryTextRuns);
            playerInfo.secondaryTextWidth = this.calculateStringWidthWithText(styledFontRenderer, playerInfo.secondaryTextRuns);
            playerInfo.lastUpdateTimestamp = longValue + 250L;
            return playerInfo;
        }
    }

    private List<TextSegment> createListFromNameWithReplace(PlayerEntity player) {
        Text displayName = player.getDisplayName();
        if (displayName != null) {
            ArrayList<TextSegment> values = new ArrayList<>();
            String string = player.getName().getString();
            String text = this.createTextFromPatch(string);
            this.updateGameProfileWithName(player, this.textValues);
            displayName.visit((input, inputValue) -> {
                if (inputValue != null && !inputValue.isEmpty()) {
                    String textValue = inputValue.replace('\n', ' ').replace('\r', ' ');
                    if (this.calculateText(textValue, this.textValues) >= 0) {
                        int intValue = input.getColor() != null ? 0xFF000000 | input.getColor().getRgb() : -1;
                        values.add(new TextSegment(this.createTextFromPatch(textValue), intValue));
                    }
                    return Optional.empty();
                } else {
                    return Optional.empty();
                }
            }, Style.EMPTY);
            if (!values.isEmpty()) {
                return values;
            } else {
                values.add(new TextSegment(text, -1));
                return values;
            }
        } else {
            return Collections.singletonList(new TextSegment(this.createTextFromPatch(player.getName().getString()), -1));
        }
    }

    private void updateGameProfileWithName(PlayerEntity player, String[] texts) {
        String text = player.getGameProfile() != null ? player.getGameProfile().getName() : "";
        String nameForScoreboard = player.getNameForScoreboard();
        String string = player.getName().getString();
        texts[0] = string;
        texts[1] = this.createTextFromPatch(string);
        texts[2] = nameForScoreboard;
        texts[3] = this.createTextFromPatch(nameForScoreboard);
        texts[4] = text;
        texts[5] = this.createTextFromPatch(text);
    }

    private String createTextFromTextWithAppend(List<TextSegment> textSegments) {
        if (textSegments.isEmpty()) {
            return "";
        } else if (textSegments.size() == 1) {
            return textSegments.get(0).text();
        } else {
            StringBuilder textBuilder = new StringBuilder();
            for (TextSegment textSegment : textSegments) {
                textBuilder.append(textSegment.text());
            }
            return textBuilder.toString();
        }
    }

    private float calculateStringWidthWithText(StyledFontRenderer styledFontRenderer, List<TextSegment> textSegments) {
        float floatValue = 0.0F;
        for (TextSegment textSegment : textSegments) {
            floatValue += styledFontRenderer.getStringWidth(textSegment.text());
        }
        return floatValue;
    }

    private float calculateScaleFactor(float floatValue) {
        double scaleFactor = mc.getWindow().getScaleFactor();
        return (float) (Math.round(floatValue * scaleFactor) / scaleFactor);
    }

    private void updateString(StyledFontRenderer styledFontRenderer, String text, float floatValue, float floatValueValue, int intValue) {
        styledFontRenderer.drawString(text, this.calculateScaleFactor(floatValue), this.calculateScaleFactor(floatValueValue), intValue);
    }

    private float calculateTextWithColor(StyledFontRenderer styledFontRenderer, List<TextSegment> textSegments, float floatValue, float floatValueValue) {
        float floatValueResult = floatValue;
        for (TextSegment textSegment : textSegments) {
            this.updateString(styledFontRenderer, textSegment.text(), floatValueResult, floatValueValue, textSegment.color());
            floatValueResult += styledFontRenderer.getStringWidth(textSegment.text());
        }
        return floatValueResult;
    }

    private void onHudRender(HudRenderEvent event, PlayerEntity player, float floatValue, float floatValueValue) {
        int intValue = 0;
        ItemStack mainHandStack = player.getMainHandStack();
        if (!mainHandStack.isEmpty()) {
            this.itemStackValues[intValue++] = mainHandStack;
        }
        ItemStack offHandStack = player.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            this.itemStackValues[intValue++] = offHandStack;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack armorStack = inventory.getArmorStack(3);
        if (!armorStack.isEmpty()) {
            this.itemStackValues[intValue++] = armorStack;
        }
        ItemStack armorStackValue = inventory.getArmorStack(2);
        if (!armorStackValue.isEmpty()) {
            this.itemStackValues[intValue++] = armorStackValue;
        }
        ItemStack armorStackResult = inventory.getArmorStack(1);
        if (!armorStackResult.isEmpty()) {
            this.itemStackValues[intValue++] = armorStackResult;
        }
        ItemStack armorStackItem = inventory.getArmorStack(0);
        if (!armorStackItem.isEmpty()) {
            this.itemStackValues[intValue++] = armorStackItem;
        }
        if (intValue != 0) {
            MatrixStack matrices = event.getContext().getMatrices();
            float floatValueResult = intValue * 11.0F - 1.0F;
            float floatValueItem = floatValue - floatValueResult * 0.5F;
            float floatValueEntry = floatValueValue - 12.0F - 1.0F;
            this.renderColorWithVertex(matrices, floatValueItem - 1.0F, floatValueEntry - 1.0F, floatValueResult + 2.0F, 12.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            DrawContext context = event.getContext();
            TextRenderer textRenderer = mc.textRenderer;
            for (int index = 0; index < intValue; index++) {
                ItemStack stack = this.itemStackValues[index];
                matrices.push();
                matrices.translate(floatValueItem + index * 11.0F, floatValueEntry, 0.0F);
                matrices.scale(0.625F, 0.625F, 1.0F);
                context.drawItem(stack, 0, 0);
                context.drawStackOverlay(textRenderer, stack, 0, 0, null);
                matrices.pop();
                this.itemStackValues[index] = ItemStack.EMPTY;
            }
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private void onHudRender(HudRenderEvent event, LivingEntity entity, float floatValue, float floatValueValue, StyledFontRenderer styledFontRenderer) {
        MatrixStack matrices = event.getContext().getMatrices();
        String text = entity instanceof PlayerEntity player ? this.createTextFromPatch(player.getDisplayName().getString()) : entity.getDisplayName().getString();
        int intValue = Math.max(0, Math.round(HealthUtil.getHealthWithAbsorption(entity)));
        String textValue = "[" + intValue + "]";
        float floatValueResult = 2.0F;
        float floatValueItem = 1.0F;
        float floatValueEntry = 1.0F;
        float floatValueCopy = 1.0F;
        float stringWidth = styledFontRenderer.getStringWidth(text);
        float stringWidthValue = styledFontRenderer.getStringWidth(textValue);
        float floatValue2 = stringWidth + floatValueResult + stringWidthValue;
        float floatValue3 = floatValue2 + floatValueItem * 2.0F;
        float floatValue4 = styledFontRenderer.getStringHeight(text) + 2.0F;
        float floatValue5 = floatValue4 + floatValueEntry + floatValueCopy;
        float floatValue6 = this.calculateScaleFactor(floatValue - floatValue3 * 0.5F);
        float floatValue7 = this.calculateScaleFactor(floatValueValue - floatValue5 - 3.0F + 6.0F);
        float floatValue8 = floatValue6 + floatValueItem;
        float floatValue9 = floatValue7 + floatValueEntry + 1.5F;
        this.renderColorWithVertex(matrices, floatValue6, floatValue7, floatValue3, floatValue5);
        this.updateString(styledFontRenderer, text, floatValue8, floatValue9, -1);
        this.updateString(styledFontRenderer, textValue, floatValue8 + stringWidth + floatValueResult, floatValue9, -169324207);
    }

    private void onHudRender(HudRenderEvent event, ItemEntity itemEntity, float floatValue, float floatValueValue, StyledFontRenderer styledFontRenderer, long longValue) {
        ItemStack stack = itemEntity.getStack();
        if (!stack.isEmpty()) {
            ItemLabelCache itemLabelCache = this.entrySetNameTagsItemLabelCacheEntries.get(itemEntity.getId());
            int previousHealth = System.identityHashCode(stack.getItem());
            int previousAbsorption = stack.getCount();
            if (itemLabelCache == null || itemLabelCache.previousAbsorption != previousAbsorption || itemLabelCache.previousHealth != previousHealth || longValue >= itemLabelCache.lastUpdateTimestamp) {
                if (itemLabelCache == null) {
                    itemLabelCache = new ItemLabelCache();
                    this.entrySetNameTagsItemLabelCacheEntries.put(itemEntity.getId(), itemLabelCache);
                }
                itemLabelCache.previousHealth = previousHealth;
                itemLabelCache.previousAbsorption = previousAbsorption;
                itemLabelCache.stringWidthText = previousAbsorption + "x";
                itemLabelCache.nameTagsTextSegmentEntries = this.createListFromText(stack.getName(), this.calculateColorWithName(stack));
                itemLabelCache.spaceWidth = styledFontRenderer.getStringWidth(" ");
                float nameWidth = 0.0F;
                for (TextSegment textSegment : itemLabelCache.nameTagsTextSegmentEntries) {
                    nameWidth += styledFontRenderer.getStringWidth(textSegment.text());
                }
                itemLabelCache.nameWidth = nameWidth;
                itemLabelCache.totalTextWidth = nameWidth + itemLabelCache.spaceWidth + styledFontRenderer.getStringWidth(itemLabelCache.stringWidthText);
                itemLabelCache.textHeight = styledFontRenderer.getStringHeight(stack.getName().getString()) + 4.0F;
                itemLabelCache.lastUpdateTimestamp = longValue + 1000L;
            }
            MatrixStack matrices = event.getContext().getMatrices();
            float floatValueResult = 1.0F;
            float floatValueItem = 1.0F;
            float floatValueEntry = itemLabelCache.totalTextWidth + floatValueResult * 2.0F;
            float floatValueCopy = this.calculateScaleFactor(floatValue - floatValueEntry * 0.5F);
            float floatValue2 = this.calculateScaleFactor(floatValueValue - itemLabelCache.textHeight - 3.0F + 6.0F);
            float floatValue3 = floatValueCopy + floatValueResult;
            float floatValue4 = floatValue2 + floatValueItem + 1.5F;
            this.renderColorWithVertex(matrices, floatValueCopy, floatValue2, floatValueEntry, itemLabelCache.textHeight);
            float floatValue5 = floatValue3;
            for (TextSegment textSegmentValue : itemLabelCache.nameTagsTextSegmentEntries) {
                this.updateString(styledFontRenderer, textSegmentValue.text(), floatValue5, floatValue4, textSegmentValue.color());
                floatValue5 += styledFontRenderer.getStringWidth(textSegmentValue.text());
            }
            this.updateString(styledFontRenderer, itemLabelCache.stringWidthText, floatValue5 + itemLabelCache.spaceWidth, floatValue4, -6579301);
        }
    }

    private int calculateColorWithName(ItemStack stack) {
        Text name = stack.getName();
        if (name != null) {
            int[] values = new int[] { 0 };
            boolean[] valuesValue = new boolean[] { false };
            name.visit((input, inputValue) -> {
                if (!valuesValue[0] && input != null && input.getColor() != null) {
                    values[0] = 0xFF000000 | input.getColor().getRgb();
                    valuesValue[0] = true;
                }
                return valuesValue[0] ? Optional.of(inputValue) : Optional.empty();
            }, Style.EMPTY);
            if (valuesValue[0]) {
                return values[0];
            }
        }
        return switch(stack.getRarity()) {
            case UNCOMMON ->
                -171;
            case RARE ->
                -11141121;
            case EPIC ->
                -43521;
            case COMMON ->
                -1;
        };
    }

    private void renderColorWithVertex(MatrixStack matrices, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, floatValue, floatValueValue + floatValueItem, 0.0F).color(0, 0, 0, 80);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue + floatValueItem, 0.0F).color(0, 0, 0, 80);
        buffer.vertex(positionMatrix, floatValue + floatValueResult, floatValueValue, 0.0F).color(0, 0, 0, 80);
        buffer.vertex(positionMatrix, floatValue, floatValueValue, 0.0F).color(0, 0, 0, 80);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    public boolean shouldHideVanillaTags() {
        return this.isEnable() && !this.elements.getSettings().isEmpty() && this.elements.getSettings().get(0).isState();
    }

    private String createTextFromPatch(String text) {
        NameProtect nameProtect = ModuleRegistry.INSTANCE != null ? ModuleRegistry.nameProtect : null;
        return nameProtect != null && nameProtect.isEnable() ? nameProtect.patch(text) : text;
    }

    private int calculateText(String text, String[] texts) {
        if (text != null && !text.isEmpty() && texts != null) {
            int intValue = -1;
            for (String textValue : texts) {
                if (textValue != null && !textValue.isEmpty()) {
                    int intValueValue = this.calculateRegionMatches(text, textValue);
                    if (intValueValue >= 0 && (intValue == -1 || intValueValue < intValue)) {
                        intValue = intValueValue;
                    }
                }
            }
            return intValue;
        } else {
            return -1;
        }
    }

    private int calculateRegionMatches(String text, String textValue) {
        if (text != null && textValue != null && !textValue.isEmpty()) {
            int intValue = text.length() - textValue.length();
            for (int index = 0; index <= intValue; index++) {
                if (text.regionMatches(true, index, textValue, 0, textValue.length())) {
                    return index;
                }
            }
            return -1;
        } else {
            return -1;
        }
    }

    private void updateTextWithSubstring(List<TextSegment> textSegments, int intValue) {
        int intValueValue = Math.max(0, intValue);
        ArrayList<TextSegment> values = new ArrayList<>(textSegments.size());
        for (TextSegment textSegment : textSegments) {
            if (intValueValue <= 0) {
                break;
            }
            String text = textSegment.text();
            if (text.length() <= intValueValue) {
                values.add(textSegment);
                intValueValue -= text.length();
            } else {
                values.add(new TextSegment(text.substring(0, intValueValue), textSegment.color()));
                intValueValue = 0;
            }
        }
        textSegments.clear();
        textSegments.addAll(values);
    }

    private List<TextSegment> createEmptyListWithText(PlayerEntity player, long longValue) {
        PlayerLabel playerLabel = this.entrySetNameTagsPlayerLabelByUUID.get(player.getUuid());
        if (playerLabel != null && longValue < playerLabel.lastUpdateTimestamp) {
            return playerLabel.nameTagsTextSegmentEntries;
        } else {
            if (playerLabel == null) {
                playerLabel = new PlayerLabel();
                this.entrySetNameTagsPlayerLabelByUUID.put(player.getUuid(), playerLabel);
            }
            if (mc.getNetworkHandler() == null) {
                playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                playerLabel.lastUpdateTimestamp = longValue + 1000L;
                return playerLabel.nameTagsTextSegmentEntries;
            } else {
                PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
                if (playerListEntry == null) {
                    playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                    playerLabel.lastUpdateTimestamp = longValue + 1000L;
                    return playerLabel.nameTagsTextSegmentEntries;
                } else {
                    Text displayName = playerListEntry.getDisplayName();
                    if (displayName == null) {
                        displayName = player.getDisplayName();
                    }
                    if (displayName == null) {
                        playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                        playerLabel.lastUpdateTimestamp = longValue + 1000L;
                        return playerLabel.nameTagsTextSegmentEntries;
                    } else {
                        this.updateGameProfileWithName(player, this.textValues);
                        boolean[] values = new boolean[] { false };
                        ArrayList<TextSegment> valuesValue = new ArrayList<>();
                        displayName.visit((input, inputValue) -> {
                            if (!values[0] && inputValue != null && !inputValue.isEmpty()) {
                                String text = inputValue.replace('\n', ' ').replace('\r', ' ');
                                int intValue = this.calculateText(text, this.textValues);
                                String textValue = intValue >= 0 ? text.substring(0, intValue) : text;
                                if (!textValue.isEmpty()) {
                                    int intValueValue = input.getColor() != null ? input.getColor().getRgb() : 16777215;
                                    this.updateCharAtWithCodePointAt(valuesValue, textValue, intValueValue);
                                }
                                if (intValue >= 0) {
                                    values[0] = true;
                                }
                                return Optional.empty();
                            } else {
                                return Optional.empty();
                            }
                        }, Style.EMPTY);
                        if (!values[0]) {
                            valuesValue.clear();
                            Team scoreboardTeam = player.getScoreboardTeam();
                            if (scoreboardTeam != null && scoreboardTeam.getPrefix() != null) {
                                this.updateColorWithReplace(valuesValue, scoreboardTeam.getPrefix());
                            }
                        }
                        if (valuesValue.isEmpty()) {
                            playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                            playerLabel.lastUpdateTimestamp = longValue + 1000L;
                            return playerLabel.nameTagsTextSegmentEntries;
                        } else {
                            StringBuilder textBuilder = new StringBuilder();
                            for (TextSegment textSegment : valuesValue) {
                                textBuilder.append(textSegment.text());
                            }
                            int intValueResult = this.calculateText(textBuilder.toString(), this.textValues);
                            if (intValueResult >= 0) {
                                if (intValueResult == 0) {
                                    playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                                    playerLabel.lastUpdateTimestamp = longValue + 1000L;
                                    return playerLabel.nameTagsTextSegmentEntries;
                                }
                                this.updateTextWithSubstring(valuesValue, intValueResult);
                            }
                            if (valuesValue.isEmpty()) {
                                playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                                playerLabel.lastUpdateTimestamp = longValue + 1000L;
                                return playerLabel.nameTagsTextSegmentEntries;
                            } else {
                                StringBuilder textBuilderValue = new StringBuilder();
                                for (TextSegment textSegmentValue : valuesValue) {
                                    textBuilderValue.append(textSegmentValue.text());
                                }
                                if (textBuilderValue.toString().trim().isEmpty()) {
                                    playerLabel.nameTagsTextSegmentEntries = Collections.emptyList();
                                    playerLabel.lastUpdateTimestamp = longValue + 1000L;
                                    return playerLabel.nameTagsTextSegmentEntries;
                                } else {
                                    TextSegment textSegmentResult = valuesValue.get(valuesValue.size() - 1);
                                    if (!textSegmentResult.text().endsWith(" ")) {
                                        valuesValue.set(valuesValue.size() - 1, new TextSegment(textSegmentResult.text() + " ", textSegmentResult.color()));
                                    }
                                    playerLabel.nameTagsTextSegmentEntries = List.copyOf(valuesValue);
                                    playerLabel.lastUpdateTimestamp = longValue + 1000L;
                                    return playerLabel.nameTagsTextSegmentEntries;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateColorWithReplace(List<TextSegment> textSegments, Text text) {
        text.visit((input, inputValue) -> {
            if (inputValue != null && !inputValue.isEmpty()) {
                int intValue = input.getColor() != null ? input.getColor().getRgb() : 16777215;
                this.updateCharAtWithCodePointAt(textSegments, inputValue.replace('\n', ' ').replace('\r', ' '), intValue);
                return Optional.empty();
            } else {
                return Optional.empty();
            }
        }, Style.EMPTY);
    }

    private List<TextSegment> createListFromText(Text text, int intValue) {
        ArrayList<TextSegment> values = new ArrayList<>();
        if (text != null) {
            this.updateColorWithReplace(values, text);
        }
        if (values.isEmpty() && text != null && !text.getString().isEmpty()) {
            values.add(new TextSegment(text.getString(), intValue));
        }
        return values;
    }

    private void updateCharAtWithCodePointAt(List<TextSegment> textSegments, String text, int intValue) {
        if (text != null && !text.isEmpty()) {
            boolean flag = false;
            for (int index = 0; index < text.length(); index++) {
                char character = text.charAt(index);
                if (character == 167 || character > 127) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                textSegments.add(new TextSegment(text, intValue));
            } else {
                int intValueValue = intValue;
                StringBuilder textBuilder = new StringBuilder();
                int intValueResult = intValue;
                int intValueItem = 0;
                while (intValueItem < text.length()) {
                    int intValueEntry = text.codePointAt(intValueItem);
                    int intValueCopy = Character.charCount(intValueEntry);
                    if (intValueEntry == 167 && intValueItem + intValueCopy < text.length()) {
                        this.updateToStringWithLength(textSegments, textBuilder, intValueResult);
                        char normalizedText = Character.toLowerCase(text.charAt(intValueItem + intValueCopy));
                        Integer value = this.createIntegerFromChar(normalizedText);
                        if (value != null) {
                            intValueValue = value;
                        } else if (normalizedText == 'r') {
                            intValueValue = intValue;
                        }
                        intValueResult = intValueValue;
                        intValueItem += intValueCopy + 1;
                    } else {
                        String textValue = RankGlyphReplacement.replaceCodePoint(intValueEntry);
                        if (textValue == null) {
                            if (!textBuilder.isEmpty() && intValueResult != intValueValue) {
                                this.updateToStringWithLength(textSegments, textBuilder, intValueResult);
                            }
                            intValueResult = intValueValue;
                            textBuilder.appendCodePoint(intValueEntry);
                            intValueItem += intValueCopy;
                        } else {
                            this.updateToStringWithLength(textSegments, textBuilder, intValueResult);
                            int intValue2 = Math.max(1, textValue.length());
                            for (int indexValue = 0; indexValue < textValue.length(); indexValue++) {
                                int gradientColorForReplacement = RankGlyphReplacement.getGradientColorForReplacement(intValueEntry, indexValue, intValue2, 1.0F, intValueValue);
                                if (!textBuilder.isEmpty() && intValueResult != gradientColorForReplacement) {
                                    this.updateToStringWithLength(textSegments, textBuilder, intValueResult);
                                }
                                intValueResult = gradientColorForReplacement;
                                textBuilder.append(textValue.charAt(indexValue));
                            }
                            intValueItem += intValueCopy;
                        }
                    }
                }
                this.updateToStringWithLength(textSegments, textBuilder, intValueResult);
            }
        }
    }

    private void updateToStringWithLength(List<TextSegment> textSegments, StringBuilder textBuilder, int intValue) {
        if (!textBuilder.isEmpty()) {
            textSegments.add(new TextSegment(textBuilder.toString(), intValue));
            textBuilder.setLength(0);
        }
    }

    private Integer createIntegerFromChar(char character) {
        return switch(character) {
            case '0' ->
                0;
            case '1' ->
                170;
            case '2' ->
                43520;
            case '3' ->
                43690;
            case '4' ->
                11141120;
            case '5' ->
                11141290;
            case '6' ->
                16755200;
            case '7' ->
                11184810;
            case '8' ->
                5592405;
            case '9' ->
                5592575;
            default ->
                null;
            case 'a' ->
                5635925;
            case 'b' ->
                5636095;
            case 'c' ->
                16733525;
            case 'd' ->
                16733695;
            case 'e' ->
                16777045;
            case 'f' ->
                16777215;
        };
    }

    private void updateRemoveIfWithEntrySet(long longValue) {
        if (longValue >= this.lastUpdateTimestampForDisable) {
            this.lastUpdateTimestampForDisable = longValue + 3000L;
            if (mc.world == null) {
                this.entrySetNameTagsPlayerLabelByUUID.clear();
                this.entrySetNameTagsPlayerInfoByUUID.clear();
                this.entrySetNameTagsPlayerModelPoseByUUID.clear();
                this.entrySetNameTagsItemLabelCacheEntries.clear();
            } else {
                this.entrySetNameTagsPlayerLabelByUUID.entrySet().removeIf(input -> mc.world.getPlayerByUuid(input.getKey()) == null);
                this.entrySetNameTagsPlayerInfoByUUID.entrySet().removeIf(inputValue -> mc.world.getPlayerByUuid(inputValue.getKey()) == null);
                this.entrySetNameTagsPlayerModelPoseByUUID.entrySet().removeIf(inputResult -> mc.world.getPlayerByUuid(inputResult.getKey()) == null);
                this.entrySetNameTagsItemLabelCacheEntries.entrySet().removeIf(inputItem -> mc.world.getEntityById(inputItem.getKey()) == null);
            }
        }
    }

    private boolean isNaNWithInfinite(double doubleValue, double doubleValueValue, double doubleValueResult, ProjectedPosition projectedPosition) {
        float floatValue = (float) (doubleValue - this.x);
        float floatValueValue = (float) (doubleValueValue - this.y);
        float floatValueResult = (float) (doubleValueResult - this.z);
        this.vector4f.set(floatValue, floatValueValue, floatValueResult, 1.0F);
        this.matrix4f.transform(this.vector4f);
        float w = this.vector4f.w;
        if (w <= 1.0E-5F) {
            return false;
        } else {
            float floatValueItem = 1.0F / w;
            float floatValueEntry = this.vector4f.x * floatValueItem;
            float floatValueCopy = this.vector4f.y * floatValueItem;
            float projectedX = (floatValueEntry * 0.5F + 0.5F) * this.scaledWidth;
            float projectedY = (1.0F - (floatValueCopy * 0.5F + 0.5F)) * this.scaledHeight;
            if (!Float.isNaN(projectedX) && !Float.isNaN(projectedY) && !Float.isInfinite(projectedX) && !Float.isInfinite(projectedY)) {
                projectedPosition.projectedX = projectedX;
                projectedPosition.projectedY = projectedY;
                projectedPosition.cachedScalarForNaNWithInfinite = this.vector4f.z * floatValueItem;
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isLerp(Entity entity, double doubleValue, ProjectedPosition projectedPosition) {
        double interpolatedValue = MathHelper.lerp((double) this.tickDelta, entity.prevX, entity.getX());
        double doubleValueValue = MathHelper.lerp((double) this.tickDelta, entity.prevY, entity.getY()) + doubleValue;
        double interpolatedValueValue = MathHelper.lerp((double) this.tickDelta, entity.prevZ, entity.getZ());
        return this.isNaNWithInfinite(interpolatedValue, doubleValueValue, interpolatedValueValue, projectedPosition);
    }

    private boolean isNameTagsProjectedPosition(ProjectedPosition projectedPosition) {
        return projectedPosition.projectedX >= -300.0F && projectedPosition.projectedY >= -300.0F && projectedPosition.projectedX <= this.scaledWidth + 300.0F && projectedPosition.projectedY <= this.scaledHeight + 300.0F;
    }

    private boolean isThirdPersonWithCamera() {
        return mc != null && mc.gameRenderer != null && !mc.gameRenderer.getCamera().isThirdPerson();
    }

    private boolean isAlive(PlayerEntity player) {
        return this.players.isState() && player != null && player.isAlive() ? player != mc.player || !this.isThirdPersonWithCamera() : false;
    }

    private boolean isPassiveAnimal(Entity entity) {
        return entity instanceof AnimalEntity || entity instanceof WaterAnimalEntity || entity instanceof AmbientEntity;
    }

    private boolean isHostileMob(Entity entity) {
        return entity instanceof MobEntity && !this.isPassiveAnimal(entity) && !(entity instanceof PlayerEntity);
    }

    private int calculateFriendWithName(PlayerEntity player) {
        boolean flag = Toshiba.INSTANCE.friendStorage != null && Toshiba.INSTANCE.friendStorage.isFriend(player.getName().getString());
        return flag ? -11206828 : this.cachedCountForRender3D;
    }

    private void updateLerpWithUuid(MatrixStack matrices, PlayerEntity player, float floatValue, int intValue) {
        if ((Object) mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerEntityRenderer playerEntityRenderer) {
            float floatValueValue = this.skeletonWidth.getValue().floatValue();
            boolean flag = mc.player == null || mc.player.canSee(player);
            PlayerEntityModel model = playerEntityRenderer.getModel();
            PlayerModelPose playerModelPose = this.entrySetNameTagsPlayerModelPoseByUUID.get(player.getUuid());
            boolean flagValue = false;
            if (flag) {
                AbstractClientPlayerEntity playerValue = (AbstractClientPlayerEntity) player;
                PlayerEntityRenderState andUpdateRenderState = playerEntityRenderer.getAndUpdateRenderState(playerValue, floatValue);
                model.setAngles(andUpdateRenderState);
                if (playerModelPose == null) {
                    playerModelPose = new PlayerModelPose();
                    this.entrySetNameTagsPlayerModelPoseByUUID.put(player.getUuid(), playerModelPose);
                }
                playerModelPose.copyFrom(model);
            } else {
                if (playerModelPose == null) {
                    return;
                }
                this.copyFromNameTagsPlayerModelPose.copyFrom(model);
                playerModelPose.apply(model);
                flagValue = true;
            }
            double interpolatedValue = MathHelper.lerp((double) floatValue, player.prevX, player.getX());
            double interpolatedValueValue = MathHelper.lerp((double) floatValue, player.prevY, player.getY());
            double interpolatedValueResult = MathHelper.lerp((double) floatValue, player.prevZ, player.getZ());
            this.matrixStack.push();
            this.matrixStack.translate(interpolatedValue, interpolatedValueValue, interpolatedValueResult);
            float floatValueResult = MathHelper.lerpAngleDegrees(floatValue, player.prevBodyYaw, player.bodyYaw);
            this.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - floatValueResult));
            if (flag) {
                AbstractClientPlayerEntity playerResult = (AbstractClientPlayerEntity) player;
                PlayerEntityRenderState andUpdateRenderStateValue = playerEntityRenderer.getAndUpdateRenderState(playerResult, floatValue);
                this.updateMultiplyWithRotationDegrees(this.matrixStack, andUpdateRenderStateValue);
            }
            this.matrixStack.scale(-1.0F, -1.0F, 1.0F);
            this.matrixStack.translate(0.0F, -1.501F, 0.0F);
            this.updatePushWithRotate(this.matrixStack, model.head, 0.0F, 0.0F, 0.0F, this.neckPosition);
            this.updatePushWithRotate(this.matrixStack, model.head, 0.0F, -4.0F, 0.0F, this.headPosition);
            this.updatePushWithRotate(this.matrixStack, model.body, 0.0F, 2.0F, 0.0F, this.chestPosition);
            this.updatePushWithRotate(this.matrixStack, model.body, 0.0F, 12.0F, 0.0F, this.pelvisPosition);
            this.updatePushWithRotate(this.matrixStack, model.rightArm, 0.0F, 0.0F, 0.0F, this.rightShoulderPosition);
            this.updatePushWithRotate(this.matrixStack, model.leftArm, 0.0F, 0.0F, 0.0F, this.leftShoulderPosition);
            this.updatePushWithRotate(this.matrixStack, model.rightArm, 0.0F, 10.0F, 0.0F, this.rightHandPosition);
            this.updatePushWithRotate(this.matrixStack, model.leftArm, 0.0F, 10.0F, 0.0F, this.leftHandPosition);
            this.updatePushWithRotate(this.matrixStack, model.rightLeg, 0.0F, 0.0F, 0.0F, this.rightHipPosition);
            this.updatePushWithRotate(this.matrixStack, model.leftLeg, 0.0F, 0.0F, 0.0F, this.leftHipPosition);
            this.updatePushWithRotate(this.matrixStack, model.rightLeg, 0.0F, 12.0F, 0.0F, this.rightFootPosition);
            this.updatePushWithRotate(this.matrixStack, model.leftLeg, 0.0F, 12.0F, 0.0F, this.leftFootPosition);
            this.matrixStack.pop();
            this.renderDepthMaskWithLineWidth(matrices, intValue, floatValueValue);
            if (flagValue) {
                this.copyFromNameTagsPlayerModelPose.apply(model);
            }
        }
    }

    private void updateMultiplyWithRotationDegrees(MatrixStack matrices, PlayerEntityRenderState playerEntityRenderState) {
        if (playerEntityRenderState.isGliding) {
            float glidingProgress = playerEntityRenderState.getGlidingProgress();
            if (!playerEntityRenderState.usingRiptide) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(glidingProgress * (-90.0F - playerEntityRenderState.pitch)));
            }
            if (playerEntityRenderState.applyFlyingRotation) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(playerEntityRenderState.flyingRotation));
            }
        } else if (!(playerEntityRenderState.leaningPitch <= 0.0F)) {
            float floatValue = playerEntityRenderState.touchingWater ? -90.0F - playerEntityRenderState.pitch : -90.0F;
            float interpolatedValue = MathHelper.lerp(playerEntityRenderState.leaningPitch, 0.0F, floatValue);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(interpolatedValue));
            if (playerEntityRenderState.isSwimming) {
                matrices.translate(0.0F, -1.0F, 0.3F);
            }
        }
    }

    private void updatePushWithRotate(MatrixStack matrices, ModelPart modelPart, float floatValue, float floatValueValue, float floatValueResult, Vector3f vector3f) {
        matrices.push();
        modelPart.rotate(matrices);
        matrices.translate(floatValue * 0.0625F, floatValueValue * 0.0625F, floatValueResult * 0.0625F);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        vector3f.set(positionMatrix.m30(), positionMatrix.m31(), positionMatrix.m32());
        matrices.pop();
    }

    private void renderDepthMaskWithLineWidth(MatrixStack matrices, int intValue, float floatValue) {
        float shaderLineWidth = RenderSystem.getShaderLineWidth();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(floatValue);
        try {
            Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
            float x = (float) this.x;
            float y = (float) this.y;
            float z = (float) this.z;
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
            this.renderNormalWithColor(buffer, positionMatrix, this.headPosition, this.neckPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.neckPosition, this.chestPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.chestPosition, this.pelvisPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.rightShoulderPosition, this.leftShoulderPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.rightShoulderPosition, this.rightHandPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.leftShoulderPosition, this.leftHandPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.rightHipPosition, this.leftHipPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.rightHipPosition, this.rightFootPosition, x, y, z, intValue);
            this.renderNormalWithColor(buffer, positionMatrix, this.leftHipPosition, this.leftFootPosition, x, y, z, intValue);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            RenderSystem.lineWidth(shaderLineWidth);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private void renderNormalWithColor(BufferBuilder buffer, Matrix4f matrix, Vector3f vector3f, Vector3f vector3fValue, float floatValue, float floatValueValue, float floatValueResult, int intValue) {
        matrix.transformDirection(vector3fValue.x - vector3f.x, vector3fValue.y - vector3f.y, vector3fValue.z - vector3f.z, this.normalVector);
        float floatValueItem = this.normalVector.lengthSquared();
        if (!(floatValueItem < 1.0E-8F)) {
            this.normalVector.mul(1.0F / (float) Math.sqrt(floatValueItem));
            buffer.vertex(matrix, vector3f.x - floatValue, vector3f.y - floatValueValue, vector3f.z - floatValueResult).color(intValue).normal(this.normalVector.x, this.normalVector.y, this.normalVector.z);
            buffer.vertex(matrix, vector3fValue.x - floatValue, vector3fValue.y - floatValueValue, vector3fValue.z - floatValueResult).color(intValue).normal(this.normalVector.x, this.normalVector.y, this.normalVector.z);
        }
    }

    private int calculateThemesWithThemeColor() {
        if (Toshiba.INSTANCE != null && Toshiba.INSTANCE.themeStorage != null && Toshiba.INSTANCE.themeStorage.getThemes() != null) {
            Theme theme = Toshiba.INSTANCE.themeStorage.getThemes().getTheme();
            return theme != null && theme.color != null && theme.color.length != 0 ? theme.color[0] : ColorMathUtil.getThemeColor(0);
        } else {
            return ColorMathUtil.getThemeColor(0);
        }
    }

    public static class ProjectedPosition {

        float projectedX;

        float projectedY;

        float cachedScalarForNaNWithInfinite;

        ProjectedPosition() {
        }
    }

    public static class PlayerModelPose {

        private float headPitch;

        private float headYaw;

        private float headRoll;

        private float bodyPitch;

        private float bodyYaw;

        private float bodyRoll;

        private float rightArmPitch;

        private float rightArmYaw;

        private float rightArmRoll;

        private float leftArmPitch;

        private float leftArmYaw;

        private float leftArmRoll;

        private float rightLegPitch;

        private float rightLegYaw;

        private float rightLegRoll;

        private float leftLegPitch;

        private float leftLegYaw;

        private float leftLegRoll;

        PlayerModelPose() {
        }

        public void copyFrom(PlayerEntityModel playerEntityModel) {
            this.headPitch = playerEntityModel.head.pitch;
            this.headYaw = playerEntityModel.head.yaw;
            this.headRoll = playerEntityModel.head.roll;
            this.bodyPitch = playerEntityModel.body.pitch;
            this.bodyYaw = playerEntityModel.body.yaw;
            this.bodyRoll = playerEntityModel.body.roll;
            this.rightArmPitch = playerEntityModel.rightArm.pitch;
            this.rightArmYaw = playerEntityModel.rightArm.yaw;
            this.rightArmRoll = playerEntityModel.rightArm.roll;
            this.leftArmPitch = playerEntityModel.leftArm.pitch;
            this.leftArmYaw = playerEntityModel.leftArm.yaw;
            this.leftArmRoll = playerEntityModel.leftArm.roll;
            this.rightLegPitch = playerEntityModel.rightLeg.pitch;
            this.rightLegYaw = playerEntityModel.rightLeg.yaw;
            this.rightLegRoll = playerEntityModel.rightLeg.roll;
            this.leftLegPitch = playerEntityModel.leftLeg.pitch;
            this.leftLegYaw = playerEntityModel.leftLeg.yaw;
            this.leftLegRoll = playerEntityModel.leftLeg.roll;
        }

        public void apply(PlayerEntityModel playerEntityModel) {
            playerEntityModel.head.pitch = this.headPitch;
            playerEntityModel.head.yaw = this.headYaw;
            playerEntityModel.head.roll = this.headRoll;
            playerEntityModel.body.pitch = this.bodyPitch;
            playerEntityModel.body.yaw = this.bodyYaw;
            playerEntityModel.body.roll = this.bodyRoll;
            playerEntityModel.rightArm.pitch = this.rightArmPitch;
            playerEntityModel.rightArm.yaw = this.rightArmYaw;
            playerEntityModel.rightArm.roll = this.rightArmRoll;
            playerEntityModel.leftArm.pitch = this.leftArmPitch;
            playerEntityModel.leftArm.yaw = this.leftArmYaw;
            playerEntityModel.leftArm.roll = this.leftArmRoll;
            playerEntityModel.rightLeg.pitch = this.rightLegPitch;
            playerEntityModel.rightLeg.yaw = this.rightLegYaw;
            playerEntityModel.rightLeg.roll = this.rightLegRoll;
            playerEntityModel.leftLeg.pitch = this.leftLegPitch;
            playerEntityModel.leftLeg.yaw = this.leftLegYaw;
            playerEntityModel.leftLeg.roll = this.leftLegRoll;
        }
    }

    public static class ItemLabelCache {

        int previousHealth;

        int previousAbsorption;

        List<TextSegment> nameTagsTextSegmentEntries = Collections.emptyList();

        String stringWidthText = "";

        float totalTextWidth;

        float textHeight;

        float nameWidth;

        float spaceWidth;

        long lastUpdateTimestamp;

        ItemLabelCache() {
        }
    }

    public static class PlayerLabel {

        List<TextSegment> nameTagsTextSegmentEntries = Collections.emptyList();

        long lastUpdateTimestamp;

        PlayerLabel() {
        }
    }

    public static record TextSegment(String text, int color) {
    }

    public static class PlayerInfo {

        List<TextSegment> primaryTextRuns = Collections.emptyList();

        List<TextSegment> secondaryTextRuns = Collections.emptyList();

        String stringHeightText = "";

        float primaryTextWidth;

        float secondaryTextWidth;

        float prefixWidth;

        boolean stateFlag;

        long lastUpdateTimestamp;

        private int cachedCount = -1;

        private String cachedText = "[0]";

        PlayerInfo() {
        }

        public String getHealthText(int cachedCount) {
            if (cachedCount != this.cachedCount) {
                this.cachedCount = cachedCount;
                this.cachedText = "[" + cachedCount + "]";
            }
            return this.cachedText;
        }
    }
}
