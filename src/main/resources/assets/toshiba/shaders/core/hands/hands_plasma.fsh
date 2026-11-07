#version 150

uniform sampler2D Sampler0;
uniform vec3 color;
uniform vec3 color2;
uniform vec2 resolution;
uniform float time;
uniform float speed;
uniform float scale;
uniform float fill;
uniform float alpha;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    float mask = smoothstep(0.05, 0.95, texture(Sampler0, TexCoord).a);
    float opacity = mask * fill * alpha;
    if (opacity <= 0.001) discard;

    vec2 position = (TexCoord - 0.5) * vec2(resolution.x / max(resolution.y, 1.0), 1.0) * scale;
    float elapsed = time * speed * 0.55;
    vec2 warp = vec2(
        sin(position.y * 1.7 + elapsed) + cos(position.x * 1.2 - elapsed * 0.7),
        cos(position.x * 1.5 + elapsed * 0.8) + sin(position.y * 1.3 - elapsed)
    );
    vec2 flow = position + warp * 0.85;
    float field = sin(flow.x * 1.9 + elapsed * 0.9)
            + sin(flow.y * 2.3 - elapsed * 1.1)
            + sin((flow.x + flow.y) * 1.4 + elapsed * 0.6);
    float blend = 0.5 + 0.5 * sin(field * 1.1 + elapsed * 0.4);
    float energy = pow(1.0 - abs(sin(field * 1.8 - elapsed)), 6.0);
    vec3 plasma = mix(color, color2, smoothstep(0.1, 0.9, blend));
    plasma *= 0.6 + 0.4 * blend;
    plasma = mix(plasma, mix(plasma, vec3(1.0), 0.65), energy * 0.75);

    OutColor = vec4(plasma, clamp(opacity, 0.0, 1.0));
}
