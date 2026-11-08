#version 150

in vec4 FragColor;

uniform float uTime;
uniform vec2 uResolution;
uniform vec3 uColor;
uniform vec3 uColor2;
uniform float uAlpha;
uniform float uSpeed;
uniform float uScale;
uniform float uIntensity;
uniform vec2 uCameraDir;
uniform float uFov;
uniform vec4 ColorModulator;

out vec4 fragColor;

mat3 rotx(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(1.0, 0.0, 0.0, 0.0, c, s, 0.0, -s, c);
}

mat3 roty(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat3(c, 0.0, s, 0.0, 1.0, 0.0, -s, 0.0, c);
}

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    vec3 q = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    q += dot(q, q.yzx + 33.33);
    return fract((q.x + q.y) * q.z);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p = p * 2.03 + vec2(5.2, 1.7);
        a *= 0.52;
    }
    return v;
}

vec3 raydir() {
    vec2 uv = gl_FragCoord.xy / uResolution.xy;
    vec2 sp = uv * 2.0 - 1.0;
    float aspect = uResolution.x / uResolution.y;
    float tanv = tan(radians(uFov) * 0.5);
    vec3 rv = normalize(vec3(sp.x * tanv * aspect, sp.y * tanv, 1.0));
    return roty(uCameraDir.x) * rotx(uCameraDir.y) * rv;
}

float stars(vec3 rd) {
    vec2 p = normalize(rd.xz) * (1.0 / max(0.2, 1.25 - rd.y)) * 42.0;
    vec2 id = floor(p);
    vec2 gv = fract(p) - 0.5;
    float h = hash21(id);
    float d = length(gv);
    float s = smoothstep(0.11, 0.0, d) * step(0.985, h);
    float tw = 0.55 + 0.45 * sin(uTime * 1.3 + h * 18.0);
    return s * tw * smoothstep(-0.18, 0.38, rd.y);
}

void main() {
    vec3 rd = raydir();
    float h = rd.y;
    float t = uTime;
    vec3 top = vec3(0.01, 0.045, 0.11);
    vec3 mid = vec3(0.03, 0.085, 0.17);
    vec3 hor = mix(vec3(0.02, 0.05, 0.08), vec3(0.04, 0.1, 0.08), 0.35);
    float sky = smoothstep(-0.22, 0.7, h);
    vec3 col = mix(hor, mix(mid, top, sky), sky);

    float s = stars(rd);
    col += vec3(0.85, 0.94, 1.0) * s * 0.95;

    vec2 drift = rd.xz * uScale;
    float c1 = fbm(drift * 0.65 + vec2(t * 0.035, -t * 0.018));
    float c2 = fbm(drift * 1.15 + vec2(-t * 0.048, t * 0.022) + vec2(2.7, 7.4));
    float c3 = fbm((rd.xz + rd.y) * 1.8 + vec2(t * 0.06, -t * 0.04) + vec2(4.4, 1.2));
    float covera = smoothstep(0.34, 0.78, fbm(drift * 0.42 + vec2(t * 0.03, -t * 0.015) + vec2(1.8, 6.3)));
    float coverb = smoothstep(0.36, 0.82, fbm(drift * 0.76 + vec2(-t * 0.028, t * 0.022) + vec2(8.4, 2.1)));
    float alive = mix(covera, coverb, 0.45);
    float band = smoothstep(0.42, 0.88, c1 * 0.55 + c2 * 0.35 + c3 * 0.28);
    float curtain = smoothstep(0.02, 0.5, h) * (1.0 - smoothstep(0.56, 0.92, h));
    float ripple = sin(rd.x * 14.0 + t * 0.9 + c2 * 3.2) * 0.5 + 0.5;
    float beam = smoothstep(0.3, 0.85, ripple) * curtain;
    float pulse = 0.76 + 0.24 * sin(t * 1.45 + c1 * 5.0 + coverb * 2.4);
    float fade = smoothstep(0.18, 0.88, alive) * (0.42 + 0.58 * pulse);
    float gaps = 1.0 - smoothstep(0.62, 0.92, fbm(drift * 1.6 + vec2(t * 0.052, -t * 0.033) + vec2(3.6, 9.7)));
    vec3 au1 = mix(uColor, uColor2, 0.24);
    vec3 au2 = mix(uColor2, uColor, 0.58);
    vec3 au3 = mix(uColor, uColor2, c3);
    col += mix(au1, au2, c2) * band * beam * fade * gaps * (0.92 + uIntensity * 28.0);
    col += au3 * band * curtain * (0.12 + alive * 0.26) * (0.55 + 0.45 * covera);
    col += mix(uColor, uColor2, 0.5) * curtain * smoothstep(0.38, 0.86, c2) * 0.12;

    float haze = smoothstep(-0.3, 0.18, h);
    col += mix(uColor, uColor2, 0.4) * haze * 0.09;
    fragColor = vec4(col, uAlpha) * ColorModulator;
}
