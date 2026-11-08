#version 150

#moj_import <toshiba:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform vec4 ColorModulator;
uniform float Diagonal;

uniform vec4 TopLeftColor;
uniform vec4 BottomLeftColor;
uniform vec4 TopRightColor;
uniform vec4 BottomRightColor;

out vec4 OutColor;

vec4 bilinearInterpolation(vec2 uv) {
    vec4 topColor = mix(TopLeftColor, TopRightColor, uv.x);
    vec4 bottomColor = mix(BottomLeftColor, BottomRightColor, uv.x);
    return mix(topColor, bottomColor, uv.y);
}

float quantizationNoise(uvec2 pixel, uint seed) {
    uint value = (pixel.x * 0x9e3779b9u) ^ (pixel.y * 0x85ebca6bu) ^ seed;
    value ^= value >> 16u;
    value *= 0x7feb352du;
    value ^= value >> 15u;
    value *= 0x846ca68bu;
    value ^= value >> 16u;
    return float(value & 0x00ffffffu) / 16777216.0 - 0.5;
}

void main() {
    vec2 center = Size * 0.5;
    vec2 uv = FragCoord;

    vec4 gradientColor = bilinearInterpolation(uv);
    if (Diagonal > 0.5) {
        float progress = clamp(dot(uv * Size, Size) / max(dot(Size, Size), 1.0), 0.0, 1.0);
        gradientColor = mix(TopLeftColor, BottomRightColor, progress);
    }

    float distance = roundedBoxSDF(center - (FragCoord * Size), center - 1.0, Radius);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);

    vec4 finalColor = vec4(gradientColor.rgb, gradientColor.a * alpha);

    if (finalColor.a == 0.0) {
        discard;
    }

    finalColor *= ColorModulator;
    if (Diagonal > 0.5) {
        uvec2 pixel = uvec2(gl_FragCoord.xy);
        if (any(notEqual(TopLeftColor.rgb, BottomRightColor.rgb))) {
            float noise = quantizationNoise(pixel, 0x68bc21ebu) / 255.0;
            finalColor.rgb = clamp(finalColor.rgb + vec3(noise), 0.0, 1.0);
        }
        if (TopLeftColor.a != BottomRightColor.a) {
            float noise = quantizationNoise(pixel, 0x02e5be93u) / 255.0;
            finalColor.a = clamp(finalColor.a + noise * alpha, 0.0, 1.0);
        }
    }
    OutColor = finalColor;
}
