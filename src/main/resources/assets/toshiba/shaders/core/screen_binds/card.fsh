#version 150

#moj_import <toshiba:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec4 Divider;
uniform vec2 CardSize;
uniform float BlurEnabled;
uniform float Opacity;
uniform float BackgroundOpacity;
uniform vec3 BackgroundColor;
uniform vec3 ForegroundColor;

out vec4 OutColor;

float coverage(vec2 p, vec2 origin, vec2 size, float radius, float smoothness) {
    vec2 local = p - origin;
    if (any(lessThan(local, vec2(0.0))) || any(greaterThanEqual(local, size))) return 0.0;
    vec2 center = size * 0.5;
    float distance = roundedBoxSDF(center - local, center - 1.0, vec4(radius));
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, distance);
}

vec4 over(vec4 base, vec3 color, float alpha) {
    return vec4(color * alpha + base.rgb * (1.0 - alpha), alpha + base.a * (1.0 - alpha));
}

void main() {
    vec2 p = FragCoord * (CardSize + vec2(5.0)) - 2.5;
    float opacity = Opacity;
    float overlayOpacity = opacity * clamp(BackgroundOpacity / 0.9, 0.0, 1.0);
    float distance = length(max(abs(CardSize * 0.5 - p) - (CardSize * 0.5 - vec2(7.0)), 0.0)) - 7.0;
    float shadow = (1.0 - smoothstep(-2.5, 2.5, distance)) * floor(80.0 * overlayOpacity) / 255.0;
    vec4 result = vec4(0.0, 0.0, 0.0, shadow);
    vec4 panel = vec4(0.0);
    if (BlurEnabled > 0.0) {
        vec4 blurred = texture(Sampler0, TexCoord);
        panel = over(panel, blurred.rgb, BlurEnabled * blurred.a);
    }
    float background = coverage(p, vec2(0.0), CardSize, 7.0, 1.0);
    panel = over(panel, BackgroundColor, clamp(BackgroundOpacity, 0.0, 1.0));
    if (panel.a > 0.0) {
        result = over(result, panel.rgb / panel.a, panel.a * background * opacity);
    }
    if (all(greaterThanEqual(p, Divider.xy)) && all(lessThan(p, Divider.xy + Divider.zw))) {
        result = over(result, ForegroundColor, floor(13.0 * overlayOpacity) / 255.0);
    }
    float keycap = coverage(p, vec2(20.0, 3.3), vec2(CardSize.x - 24.0, 13.0), 6.0, 1.0);
    result = over(result, ForegroundColor, keycap * floor(5.0 * overlayOpacity) / 255.0);
    if (result.a == 0.0) discard;
    OutColor = vec4(result.rgb / result.a, result.a);
}
