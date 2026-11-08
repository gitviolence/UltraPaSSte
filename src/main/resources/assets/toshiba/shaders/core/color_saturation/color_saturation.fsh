#version 150

uniform sampler2D Sampler0;
uniform float Saturation;
uniform float HudPass;

in vec2 TexCoord;

out vec4 fragColor;

vec3 saturateColor(vec3 color) {
    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    return mix(vec3(luminance), color, Saturation);
}

void main() {
    vec4 scene = texture(Sampler0, TexCoord);
    vec3 color = saturateColor(scene.rgb);
    float maximum = HudPass > 0.5 ? scene.a : 1.0;
    fragColor = vec4(clamp(color, 0.0, maximum), scene.a);
}
