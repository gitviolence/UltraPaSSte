#version 150

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float glow;
    float core;

    if (texCoord.x < 1.5) {
        vec2 point = texCoord * 2.0 - 1.0;
        float radiusSquared = dot(point, point);
        float edge = 1.0 - smoothstep(0.45, 1.0, radiusSquared);
        float halo = exp(-radiusSquared * 4.5) * edge;
        float glint = exp(-abs(point.x) * 44.0 - abs(point.y) * 8.0)
                    + exp(-abs(point.y) * 44.0 - abs(point.x) * 8.0);
        core = exp(-radiusSquared * 72.0);
        glow = (halo * 0.4 + core + glint * 0.18 * edge);
    } else {
        float along = clamp(texCoord.x - 2.0, 0.0, 1.0);
        float across = texCoord.y * 2.0 - 1.0;
        float edge = 1.0 - smoothstep(0.7, 1.0, abs(across));
        float tail = smoothstep(0.0, 0.22, along) * pow(along, 0.65);
        float halo = exp(-across * across * 4.5) * edge;
        core = exp(-across * across * 70.0);
        glow = (halo * 0.3 + core * 0.95) * tail;
    }

    float alpha = clamp(glow * vertexColor.a, 0.0, 1.0);
    if (alpha < 0.002) discard;

    vec3 hotColor = mix(vertexColor.rgb, vec3(1.0), core * 0.85);
    fragColor = vec4(hotColor, alpha);
}
