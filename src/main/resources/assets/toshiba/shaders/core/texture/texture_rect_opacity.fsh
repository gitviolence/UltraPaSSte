#version 150

#moj_import <toshiba:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;
in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform vec4 ColorModulator;
uniform float Opacity;
uniform float ExactBounds;

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;
    float inset = ExactBounds > 0.5 ? 0.0 : 1.0;
    float distance = roundedBoxSDF(center - (FragCoord * Size), center - inset, Radius);
    float alpha = ExactBounds > 0.5
        ? 1.0 - smoothstep(-Smoothness * 0.5, Smoothness * 0.5, distance)
        : 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);
    vec4 sample = texture(Sampler0, TexCoord);
    vec3 tinted = mix(sample.rgb, FragColor.rgb, FragColor.a);
    vec4 finalColor = vec4(tinted, alpha * sample.a * Opacity);

    if (finalColor.a == 0.0) {
        discard;
    }

    OutColor = finalColor * ColorModulator;
}
