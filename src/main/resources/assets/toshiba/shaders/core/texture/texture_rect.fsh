#version 150

#moj_import <toshiba:common.glsl>

in vec2 FragCoord; // normalized fragment.fsh coord relative to the primitive
in vec4 FragColor;
in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform vec4 ColorModulator;

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;
    float distance = roundedBoxSDF(center - (FragCoord * Size), center - 1.0, Radius);

    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);

    vec4 sample = texture(Sampler0, TexCoord);
    vec3 tinted = mix(sample.rgb, FragColor.rgb, FragColor.a);
    vec4 finalColor = vec4(tinted, alpha * sample.a);

    if (finalColor.a == 0.0) {
        discard;
    }

    OutColor = finalColor * ColorModulator;
}
