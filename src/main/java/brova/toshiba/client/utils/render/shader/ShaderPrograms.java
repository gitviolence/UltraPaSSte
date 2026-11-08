package brova.toshiba.client.utils.render.shader;

import brova.toshiba.client.core.MinecraftAccess;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class ShaderPrograms implements MinecraftAccess {

    public static final ShaderProgramKey roundedRect = createShaderProgramKeyFromText("rect", "rounded_rect", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey roundedRectOutline = createShaderProgramKeyFromText("rect", "rounded_rect_outline", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey ringArc = createShaderProgramKeyFromText("ring_arc", "ring_arc", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey roundedTexture = createShaderProgramKeyFromText("texture", "texture_rect", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey roundedTextureOpacity = createShaderProgramKeyFromText("texture", "texture_rect_opacity", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey guiFade = createShaderProgramKeyFromText("gui_fade", "fade", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey screenBindCard = createShaderProgramKeyFromText("screen_binds", "card", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey hudGlow = createShaderProgramKeyFromText("hud_glow", "glow", VertexFormats.POSITION_TEXTURE);

    public static final ShaderProgramKey colorSaturation = createShaderProgramKeyFromText("color_saturation", "color_saturation", VertexFormats.POSITION_TEXTURE);

    public static final ShaderProgramKey liquidGlass = createShaderProgramKeyFromText("liquidglass", "liquid", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey kawaseDown = createShaderProgramKeyFromText("kawase_down", "kawase_down", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey kawaseUp = createShaderProgramKeyFromText("kawase_up", "kawase_up", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey gradientRect = createShaderProgramKeyFromText("gradient_rect", "gradient", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shadowRect = createShaderProgramKeyFromText("shadow_rect", "shadow", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey shadow6Rect = createShaderProgramKeyFromText("shadow6", "shadow", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey fontsMsdf = createShaderProgramKeyFromText("fonts", "fonts", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey watermarkFontsMsdf = createShaderProgramKeyFromText("watermark_fonts", "fonts", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey face = createShaderProgramKeyFromText("face", "face", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey gradient6Rect = createShaderProgramKeyFromText("gradient6", "gradient", VertexFormats.POSITION_COLOR);

    public static final ShaderProgramKey blockOverlay = createShaderProgramKeyFromText("blockoverlay", "block_overlay", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey attackSparks = createShaderProgramKeyFromText("attack", "sparks", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsMaskDiff = createShaderProgramKeyFromText("hands", "hands_mask_diff", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsOverlay = createShaderProgramKeyFromText("hands", "hands_overlay", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsPlasma = createShaderProgramKeyFromText("hands", "hands_plasma", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsGlass = createShaderProgramKeyFromText("hands", "hands_glass", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsGlow = createShaderProgramKeyFromText("hands", "hands_glow", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsKawaseDown = createShaderProgramKeyFromText("hands", "hands_kawase_down", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsKawaseUp = createShaderProgramKeyFromText("hands", "hands_kawase_up", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsFlameTrail = createShaderProgramKeyFromText("hands", "hands_flame_trail", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsFlameComposite = createShaderProgramKeyFromText("hands", "hands_flame_composite", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsFireTrail = createShaderProgramKeyFromText("hands", "hands_fire_trail", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey shaderHandsFireComposite = createShaderProgramKeyFromText("hands", "hands_fire_composite", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey skyFog = createShaderProgramKeyFromText("sky", "sky_fog", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey skyCaustic = createShaderProgramKeyFromText("sky", "sky_caustic", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey skySunset = createShaderProgramKeyFromText("sky", "sky_sunset", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey skyNebula = createShaderProgramKeyFromText("sky", "sky_nebula", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey skySevernoesiyanie = createShaderProgramKeyFromText("sky", "sky_severnoesiyanie", VertexFormats.POSITION_TEXTURE_COLOR);

    public static final ShaderProgramKey fogBlurComposite = createShaderProgramKeyFromText("fogblur", "composite", VertexFormats.POSITION_TEXTURE_COLOR);

    private static ShaderProgramKey createShaderProgramKeyFromText(String text, String textValue, VertexFormat vertexFormat) {
        return new ShaderProgramKey(Identifier.of("toshiba", "core/" + text + "/" + textValue), vertexFormat, Defines.EMPTY);
    }

    private ShaderPrograms() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
