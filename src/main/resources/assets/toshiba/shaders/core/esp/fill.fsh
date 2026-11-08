#version 150

in vec3 WorldPos;
in vec4 FragColor;

uniform float time;
uniform float speed;
uniform float intensity;

out vec4 OutColor;

void main() {
    // Бегущая волна по мировым координатам: не зависит от камеры, движется по миру.
    float flow = (WorldPos.y * 1.8 + (WorldPos.x + WorldPos.z) * 0.45) - time * speed * 2.0;
    float wave = 0.5 + 0.5 * sin(flow);
    float sharp = pow(wave, 3.0);
    float band = smoothstep(0.45, 1.0, wave);

    vec3 base = FragColor.rgb;
    vec3 hot = mix(base, vec3(1.0), 0.6);
    vec3 rgb = mix(base * 0.55, hot, band);

    float alpha = FragColor.a * (0.25 + 0.75 * sharp) * intensity;
    if (alpha <= 0.003) {
        discard;
    }

    OutColor = vec4(rgb, alpha);
}
