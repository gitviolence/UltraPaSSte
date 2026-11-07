#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform float colorFallback;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    vec2 uv = TexCoord;
    float depthBefore = texture(Sampler2, uv).r;
    float depthAfter = texture(Sampler3, uv).r;

    float result = step(0.0001, depthBefore - depthAfter);
    if (colorFallback > 0.5 && result < 0.5) {
        vec3 difference = abs(texture(Sampler1, uv).rgb - texture(Sampler0, uv).rgb);
        result = float(max(difference.r, max(difference.g, difference.b)) > 0.01);
    }
    OutColor = vec4(result, result, result, result);
}
