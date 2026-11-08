#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform vec2 resolution;
uniform float time;
uniform float itemColor;
uniform vec4 fireSettings;
uniform vec4 fireColor;
uniform vec4 fireAdvanced;
uniform vec4 fireHand;

in vec2 TexCoord;
out vec4 OutColor;

float fireHash(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float fireNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(fireHash(i), fireHash(i + vec2(1, 0)), f.x),
               mix(fireHash(i + vec2(0, 1)), fireHash(i + vec2(1, 1)), f.x), f.y);
}

void main() {
    vec2 texelSize = 1.0 / max(resolution, vec2(1.0));
    vec4 original = texture(Sampler0, TexCoord);
    vec4 result = original;
    float maskValue = texture(Sampler1, TexCoord).r;
    float intensity = fireSettings.y;
    float flameSize = fireSettings.z;
    float density = fireSettings.w;
    float smoke = fireAdvanced.x;
    float trailSoftness = max(0.5, fireAdvanced.y);
    float trailBlur = max(0.5, fireAdvanced.z);
    float densityAmount = clamp((density - 0.2) / 2.3, 0.0, 1.0);
    float densityGain = mix(0.82, 1.48, densityAmount);
    vec4 fireSample = texture(Sampler2, TexCoord);
    float trail = fireSample.a;
    vec2 px = texelSize * (1.5 + flameSize * 2.2) * trailBlur;
    float nearBlur = trail * 0.30;
    float farBlur = trail * 0.10;
    for (int i = 0; i < 16; i++) {
        float angle = float(i) * 0.39269908;
        vec2 direction = vec2(cos(angle), sin(angle));
        nearBlur += texture(Sampler2, TexCoord + direction * px * 0.62).a * 0.035;
        farBlur += texture(Sampler2, TexCoord + direction * px * 1.48).a * 0.026;
    }
    float aura = clamp(nearBlur * 0.82 + farBlur * 0.18, 0.0, 1.0);
    float flicker = 0.68 + fireNoise(TexCoord * vec2(52.0, 35.0)
            + vec2(time * 1.9, -time * 3.7)) * 0.55;
    float outsideMask = 1.0 - smoothstep(0.015, 0.32, maskValue);
    float outside = pow(clamp(aura, 0.0, 1.0), trailSoftness) * outsideMask;
    float edgeDensity = max(nearBlur - trail * 0.24, 0.0);
    float trailEdge = pow(clamp(edgeDensity, 0.0, 1.0), 1.22) * outsideMask;
    float liveNeighbour = 0.0;
    vec2 rimStep = texelSize * (1.0 + flameSize * 0.72) * max(0.5, fireHand.z);
    for (int i = 0; i < 16; i++) {
        float angle = float(i) * 0.39269908;
        vec2 direction = vec2(cos(angle), sin(angle));
        liveNeighbour = max(liveNeighbour, texture(Sampler1, TexCoord + direction * rimStep).r);
    }
    float handThreshold = mix(0.52, 0.22, clamp(fireHand.x, 0.1, 0.9));
    float liveRim = smoothstep(0.035, handThreshold * max(0.5, fireHand.y), liveNeighbour) * outsideMask;
    float edge = max(trailEdge * 0.28, liveRim);
    float bodyFlame = clamp(outside * 1.42 * flicker * intensity * densityGain, 0.0, 1.15);
    float rimFlame = clamp(edge * 1.24 * flicker * intensity * densityGain, 0.0, 1.35);
    float flame = max(bodyFlame, rimFlame);
    vec3 baseFireColor = fireColor.rgb;
    if (itemColor > 0.5 && fireSample.a > 0.001) baseFireColor = fireSample.rgb;
    vec3 flameRgb = mix(baseFireColor * 0.48, baseFireColor, clamp(flame, 0.0, 1.0));
    flameRgb = mix(flameRgb, mix(baseFireColor, vec3(1.0), 0.45),
            clamp(edge * intensity * 0.45, 0.0, 0.38));
    float glow = max(0.0, fireColor.a);
    result.rgb += flameRgb * bodyFlame * outsideMask * (0.24 + glow * 0.24);
    result.rgb += flameRgb * rimFlame * outsideMask * (0.30 + glow * 0.30);
    result.rgb += baseFireColor * edge * intensity * glow * outsideMask * 0.55;
    float smokeBody = pow(clamp(farBlur, 0.0, 1.0), 1.15) * outsideMask * smoke;
    result.rgb += mix(baseFireColor * 0.18, baseFireColor * 0.55, flicker) * smokeBody * intensity;
    result.rgb = clamp(result.rgb, 0.0, 1.0);
    vec3 light = max(result.rgb - original.rgb, vec3(0.0));
    if (dot(light, light) < 0.00000001) discard;
    OutColor = vec4(light, 0.0);
}
