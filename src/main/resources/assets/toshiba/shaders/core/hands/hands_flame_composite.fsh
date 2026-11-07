#version 150

in vec2 TexCoord;
#define texCoord TexCoord
out vec4 fragColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;

#define SceneSampler Sampler0
#define BeforeSampler Sampler1
#define TrailSampler Sampler2
#define DepthSampler Sampler3

uniform vec4 flameColor;
uniform vec4 params0;
uniform vec4 params1;
uniform vec4 screen;

void main() {
    vec2 px = screen.zw;
    float brightness = params1.x;

    vec2 blurPx = px * 3.5;
    vec4 trail = texture(TrailSampler, texCoord) * 0.40;
    trail += texture(TrailSampler, clamp(texCoord + vec2(blurPx.x, blurPx.y), vec2(0.0), vec2(1.0))) * 0.15;
    trail += texture(TrailSampler, clamp(texCoord - vec2(blurPx.x, blurPx.y), vec2(0.0), vec2(1.0))) * 0.15;
    trail += texture(TrailSampler, clamp(texCoord + vec2(blurPx.x, -blurPx.y), vec2(0.0), vec2(1.0))) * 0.15;
    trail += texture(TrailSampler, clamp(texCoord + vec2(-blurPx.x, blurPx.y), vec2(0.0), vec2(1.0))) * 0.15;

    float rawDepth = texture(DepthSampler, texCoord).r;
    float depthMask = rawDepth < 0.9999 ? 1.0 : 0.0;

    if (trail.a < 0.002 && depthMask < 0.5) {
        fragColor = texture(SceneSampler, texCoord);
        return;
    }

    vec4 scene = texture(SceneSampler, texCoord);
    vec4 sceneBefore = texture(BeforeSampler, texCoord);

    vec3 delta = abs(scene.rgb - sceneBefore.rgb);
    float peak = max(max(delta.r, delta.g), delta.b);
    float luma = dot(delta, vec3(0.299, 0.587, 0.114));
    float maskValue = peak * 0.78 + luma * 0.88 + abs(scene.a - sceneBefore.a);
    float colorMask = smoothstep(0.004, 0.060, maskValue);

    float mask = max(depthMask, colorMask);
    float itemCover = smoothstep(0.04, 0.40, mask);

    if (trail.a < 0.003 && itemCover < 0.01) {
        fragColor = vec4(scene.rgb, 1.0);
        return;
    }

    float behindItem = 1.0 - itemCover * 0.98;

    float field = smoothstep(0.006, 0.40, trail.a) * behindItem;

    float sceneLuma = dot(scene.rgb, vec3(0.299, 0.587, 0.114));
    float darkCover = 1.0 - smoothstep(0.16, 0.72, sceneLuma);
    float brightnessCurve = 0.42 + (1.0 - exp(-clamp(brightness, 0.0, 2.0) * 0.8)) * 0.64;

    float glowIntensity = pow(trail.a, 0.48) * field;
    float veil = clamp(glowIntensity * (0.20 + brightnessCurve * 0.22) * (1.0 + darkCover * 0.38), 0.0, 0.66);
    veil += itemCover * smoothstep(0.05, 0.32, trail.a) * 0.10;

    vec3 flameFill = min(trail.rgb * (0.78 + brightnessCurve * 0.42) + vec3(0.025), vec3(1.0));

    float blurAmount = field * smoothstep(0.10, 0.52, trail.a);
    vec3 sceneBase;
    if (blurAmount > 0.01) {
        vec2 bgPx = px * (4.0 + blurAmount * 4.5);
        vec3 blurred = sceneBefore.rgb * 0.40;
        blurred += texture(BeforeSampler, clamp(texCoord + vec2(bgPx.x, 0.0), vec2(0.0), vec2(1.0))).rgb * 0.15;
        blurred += texture(BeforeSampler, clamp(texCoord - vec2(bgPx.x, 0.0), vec2(0.0), vec2(1.0))).rgb * 0.15;
        blurred += texture(BeforeSampler, clamp(texCoord + vec2(0.0, bgPx.y), vec2(0.0), vec2(1.0))).rgb * 0.15;
        blurred += texture(BeforeSampler, clamp(texCoord - vec2(0.0, bgPx.y), vec2(0.0), vec2(1.0))).rgb * 0.15;
        sceneBase = mix(scene.rgb, blurred, blurAmount);
    } else {
        sceneBase = scene.rgb;
    }

    sceneBase *= (1.0 - veil * darkCover * 0.18);

    vec3 covered = mix(sceneBase, flameFill, veil);

    float glowPulse = 0.94 + sin((texCoord.y + texCoord.x * 0.35) * 16.0 + params1.y * 3.6) * 0.035;
    vec3 glow = trail.rgb * pow(trail.a, 0.48) * field * glowPulse;

    float coreIntensity = pow(trail.a * field, 2.15);
    vec3 core = min(trail.rgb * 1.22 + vec3(0.04), vec3(1.0)) * coreIntensity;

    vec3 color = covered
        + glow * (0.78 + brightnessCurve * 0.54)
        + core * (0.10 + brightnessCurve * 0.10);

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
