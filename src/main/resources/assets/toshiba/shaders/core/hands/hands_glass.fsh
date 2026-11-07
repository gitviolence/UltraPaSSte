#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform vec2 resolution;
uniform vec3 color;
uniform float time;
uniform float transparency;
uniform float distortion;
uniform float speed;
uniform float glow;
uniform float outline;
uniform float blurIntensity;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    float mask = texture(Sampler0, TexCoord).a;
    if (mask <= 0.001) discard;

    vec2 pixel = 1.0 / max(resolution, vec2(1.0));
    vec2 sampleOffset = pixel * max(outline, 0.5) * 2.0;
    vec2 normal = vec2(
        texture(Sampler0, TexCoord - vec2(sampleOffset.x, 0.0)).a - texture(Sampler0, TexCoord + vec2(sampleOffset.x, 0.0)).a,
        texture(Sampler0, TexCoord - vec2(0.0, sampleOffset.y)).a - texture(Sampler0, TexCoord + vec2(0.0, sampleOffset.y)).a
    );
    vec2 surface = TexCoord * vec2(resolution.x / max(resolution.y, 1.0), 1.0);
    float elapsed = time * speed;
    vec2 wave = vec2(
        sin(surface.y * 24.0 + elapsed * 0.8) + cos(surface.x * 17.0 - elapsed * 0.6),
        cos(surface.x * 21.0 + elapsed * 0.7) + sin(surface.y * 19.0 - elapsed)
    );
    vec2 offset = (normal * 5.0 + wave * 0.65) * pixel * distortion * 4.0;
    vec2 refractedUV = clamp(TexCoord + offset, pixel * 0.5, vec2(1.0) - pixel * 0.5);
    vec3 background = texture(Sampler1, refractedUV).rgb;
    if (blurIntensity > 0.001) {
        vec3 blurred = texture(Sampler3, refractedUV).rgb;
        background = mix(background, blurred, smoothstep(0.0, 1.0, blurIntensity));
    }
    vec3 original = texture(Sampler2, TexCoord).rgb;
    vec3 glass = mix(original, background, transparency);
    glass = mix(glass, color, transparency * 0.035);

    float rim = clamp(length(normal), 0.0, 1.0);
    float sheen = pow(0.5 + 0.5 * sin(surface.x * 10.0 + surface.y * 7.0 + elapsed * 0.45), 18.0);
    float highlight = (rim * 0.28 + sheen * 0.075) * (0.4 + glow * 0.6);
    glass += mix(color, vec3(1.0), 0.85) * highlight;
    OutColor = vec4(glass, clamp(mask, 0.0, 1.0));
}
