#version 150

uniform sampler2D Sampler0;
uniform vec2 TextureSize;
uniform float Range;
uniform float Weight;
uniform vec4 Color;

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

vec2 sqr(vec2 value) {
    return value * value;
}

float screenPxRange() {
    vec2 unitRange = vec2(Range) / TextureSize;
    vec2 screenTexSize = inversesqrt(sqr(dFdx(texCoord)) + sqr(dFdy(texCoord)));
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec2 pixelX = dFdx(texCoord);
    vec2 pixelY = dFdy(texCoord);
    float sampleRange = screenPxRange() * 4.0;
    float alpha = 0.0;

    for (int sampleY = 0; sampleY < 4; sampleY++) {
        for (int sampleX = 0; sampleX < 4; sampleX++) {
            vec2 offset = pixelX * ((float(sampleX) + 0.5) * 0.25 - 0.5)
                    + pixelY * ((float(sampleY) + 0.5) * 0.25 - 0.5);
            vec3 msd = texture(Sampler0, texCoord + offset).rgb;
            float signedDistance = median(msd.r, msd.g, msd.b) - 0.5 + Weight;
            alpha += clamp(sampleRange * signedDistance + 0.5, 0.0, 1.0);
        }
    }

    alpha *= 0.0625;
    if (alpha <= 0.0) discard;
    fragColor = vec4(Color.rgb * vertexColor.rgb, Color.a * vertexColor.a * alpha);
}
