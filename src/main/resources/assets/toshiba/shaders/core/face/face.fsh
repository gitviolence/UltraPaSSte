#version 150

uniform vec2 location;
uniform vec2 size;
uniform sampler2D Sampler0;
uniform vec4 radius;
uniform float alpha;
uniform float u;
uniform float v;
uniform float w;
uniform float h;
uniform float hurtPercent;

in vec2 texCoord;
out vec4 fragColor;

float calcLength(vec2 pos, vec2 halfSize, vec4 rad) {
    rad.xy = (pos.x > 0.0) ? rad.xy : rad.wz;
    rad.x  = (pos.y > 0.0) ? rad.x : rad.y;
    vec2 b = halfSize - rad.x - 1.0;
    return length(max(abs(pos) - b, 0.0)) - rad.x;
}

void main() {
    vec2 halfSize = size * 0.5;
    vec2 st = texCoord;
    st.x = u + st.x * w;
    st.y = v + st.y * h;
    float dist = calcLength(halfSize - (texCoord * size), halfSize, radius);
    float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, dist)) * alpha;
    vec4 color = texture(Sampler0, st);
    color.rgb = mix(color.rgb, vec3(1.0, 0.0, 0.0), hurtPercent);
    fragColor = vec4(color.rgb, color.a * smoothedAlpha);
}