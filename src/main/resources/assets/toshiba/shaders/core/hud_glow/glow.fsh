#version 150

uniform vec2 Size;
uniform float Softness;
uniform float Radius;
uniform vec4 GlowColor;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 halfSize = Size * 0.5 - Softness;
    vec2 point = texCoord * Size - Size * 0.5;
    vec2 q = abs(point) - halfSize + Radius;
    float distance = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - Radius;
    float outside = smoothstep(-0.75, 0.0, distance);
    float falloff = 1.0 - smoothstep(0.0, Softness, max(distance, 0.0));
    float alpha = GlowColor.a * outside * falloff * falloff;
    if (alpha <= 0.0) discard;
    fragColor = vec4(GlowColor.rgb, alpha);
}
