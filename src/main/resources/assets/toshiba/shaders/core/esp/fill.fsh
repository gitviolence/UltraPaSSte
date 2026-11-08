#version 150

in vec3 WorldPos;
in vec4 FragColor;

uniform float time;
uniform float speed;
uniform float intensity;
uniform float mode;
uniform vec3 cameraPos;

out vec4 OutColor;

// Направление от камеры до точки мира. Не зависит от поворота камеры (мышь),
// поэтому картинка не "плывёт" при обзоре - эффект привязан к миру.
vec3 worldDirection() {
    return normalize(WorldPos - cameraPos);
}

mat3 rotX(float a) {
    float c = cos(a), s = sin(a);
    return mat3(1.0, 0.0, 0.0,
                0.0,   c,   s,
                0.0,  -s,   c);
}

mat3 rotY(float a) {
    float c = cos(a), s = sin(a);
    return mat3(  c, 0.0,   s,
                0.0, 1.0, 0.0,
                 -s, 0.0,   c);
}

float hash13(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float vnoise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash13(i + vec3(0.0, 0.0, 0.0)), hash13(i + vec3(1.0, 0.0, 0.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 0.0)), hash13(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
        mix(mix(hash13(i + vec3(0.0, 0.0, 1.0)), hash13(i + vec3(1.0, 0.0, 1.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 1.0)), hash13(i + vec3(1.0, 1.0, 1.0)), f.x), f.y), f.z);
}

float fbm(vec3 p, int oct) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 6; i++) {
        if (i >= oct) break;
        v += a * vnoise(p);
        p = p * 2.07 + vec3(1.7, 9.2, 4.3);
        a *= 0.5;
    }
    return v;
}

float fbm3(vec3 p) {
    float v = 0.0;
    float a = 0.55;
    v += vnoise(p) * a;
    p = p * 2.03 + vec3(7.1, 3.2, 5.4);
    a *= 0.52;
    v += vnoise(p) * a;
    p = p * 2.11 + vec3(1.9, 8.3, 2.7);
    a *= 0.52;
    v += vnoise(p) * a;
    return v;
}

vec3 rotateY(vec3 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(p.x * c - p.z * s, p.y, p.x * s + p.z * c);
}

float caustic(vec2 uv, float t) {
    vec2 p = mod(uv * 6.28318, 6.28318) - 250.0;
    vec2 i = p;
    float c = 1.0;
    float inten = 0.0052;
    for (int n = 0; n < 5; n++) {
        float tn = t * 0.5 + float(n);
        i = p + vec2(cos(tn - i.x) + sin(tn + i.y), sin(tn - i.y) + cos(tn + i.x));
        c += 1.0 / length(vec2(p.x / (sin(i.x + tn) / inten), p.y / (cos(i.y + tn) / inten)));
    }
    c /= 5.0;
    c = 1.17 - pow(c, 1.4);
    return clamp(pow(abs(c), 8.0), 0.0, 5.0);
}

// --- Туман (из sky_fog) ---
float orb(vec3 p) {
    float t = time * 4.0;
    return length(p - vec3(
            sin(sin(t * 0.2) + t * 0.4) * 6.0,
            1.0 + sin(sin(t * 0.5) + t * 0.2) * 4.0,
            12.0 + time + cos(t * 0.3) * 8.0));
}

vec3 march(vec3 dir) {
    vec4 o = vec4(0.0);
    float d = 0.0, e = 0.0, s = 0.0;
    float t = time;
    float t07 = 0.7 * t;
    float t01 = 0.1 * t;
    vec3 timeOffset = vec3(0.0, 0.0, t);

    for (float i = 0.0; i < 24.0; i += 1.0) {
        vec3 p = dir * d + timeOffset;
        e = orb(p) - 0.1;

        float ang = t01 + p.z / 8.0;
        vec4 rot = cos(ang + vec4(0.0, 33.0, 11.0, 0.0));
        p.xy = mat2(rot.x, rot.y, rot.z, rot.w) * p.xy;

        s = 4.0 - abs(p.y);

        for (float a = 0.8; a < 16.0; a += a) {
            p += cos(t07 + p.yzx) * 0.2;
            s -= abs(dot(sin(t01 + p * a), vec3(0.6))) / a;
        }

        e = max(0.5 * e, 0.01);
        s = min(0.03 + 0.2 * abs(s), e);
        d += s;
        o += vec4(1.0 / (s + e * 3.0));
    }

    o = tanh(o / 10.0);
    return o.rgb;
}

void main() {
    vec3 accent = max(FragColor.rgb, vec3(0.05));
    vec3 d = worldDirection();
    float t = time * speed;
    vec3 color;

    if (mode < 0.5) {
        // Волна - своя заливка ESP
        float flow = (WorldPos.y * 1.8 + (WorldPos.x + WorldPos.z) * 0.45) - t * 2.0;
        float wave = 0.5 + 0.5 * sin(flow);
        float sharp = pow(wave, 3.0);
        float band = smoothstep(0.45, 1.0, wave);
        vec3 hot = mix(accent, vec3(1.0), 0.6);
        color = mix(accent * 0.55, hot, band);
        color *= 0.45 + 0.55 * sharp;
    } else if (mode < 1.5) {
        // Туман - шейдер клиента sky_fog
        color = march(d);
        color *= mix(vec3(1.0), accent * 1.6, 0.55);
    } else if (mode < 2.5) {
        // Каустика - шейдер клиента sky_caustic
        vec3 deep = accent * 0.05 + vec3(0.005, 0.008, 0.020);
        vec3 mid = accent * 0.42;
        vec3 hot = mix(accent, vec3(1.0), 0.55);

        vec2 uv_c = d.xz / (abs(d.y) + 0.30) * 0.50;
        float c1 = caustic(uv_c, t * 0.30);
        float c2 = caustic(uv_c * 1.30 + vec2(7.3, 11.1), t * 0.234 + 3.1) * 0.55;
        float c = c1 + c2;

        vec3 sky = mix(mid, deep, smoothstep(0.0, 0.75, d.y));
        float skyMask = smoothstep(-0.05, 0.22, d.y);
        color = sky + (accent * 0.95 + hot * 0.40) * c * skyMask;

        float core = smoothstep(0.65, 1.6, c);
        color += hot * core * 0.75;

        color = color / (1.0 + color * 0.55);
        color = pow(color, vec3(0.92));
    } else if (mode < 3.5) {
        // Закат - шейдер клиента sky_sunset
        vec3 sunDir = normalize(vec3(0.55 + sin(t * 0.04) * 0.04, 0.07, 0.80 + cos(t * 0.04) * 0.04));
        float toSun = max(0.0, dot(d, sunDir));

        vec3 skyZenith = vec3(0.045, 0.025, 0.165);
        vec3 skyHigh = vec3(0.420, 0.180, 0.420);
        vec3 skyMid = vec3(0.950, 0.400, 0.350);
        vec3 skyLow = vec3(1.000, 0.620, 0.250);
        vec3 skyEmber = vec3(0.450, 0.050, 0.100);

        vec3 sky;
        if (d.y > 0.55) {
            sky = mix(skyHigh, skyZenith, smoothstep(0.55, 1.0, d.y));
        } else if (d.y > 0.18) {
            sky = mix(skyMid, skyHigh, smoothstep(0.18, 0.55, d.y));
        } else if (d.y > 0.0) {
            sky = mix(skyLow, skyMid, smoothstep(0.0, 0.18, d.y));
        } else {
            sky = mix(skyEmber, skyLow, smoothstep(-0.45, 0.0, d.y));
        }

        sky = mix(sky, sky * mix(vec3(1.0), accent * 1.6, 0.55), 0.30);
        sky += vec3(1.00, 0.65, 0.25) * pow(toSun, 8.0) * 0.55;
        sky += vec3(1.00, 0.85, 0.40) * pow(toSun, 24.0) * 0.85;

        float shimmer = fbm(vec3(d.xz * 4.0, t * 0.6), 3) * 0.04;
        sky += vec3(1.0, 0.9, 0.7) * shimmer * (1.0 - smoothstep(0.0, 0.4, abs(d.y)));

        color = sky;
        color = color / (1.0 + color * 0.45);
        color = pow(color, vec3(0.92));
    } else {
        // Туманность - шейдер клиента sky_nebula
        float vertical = abs(d.y);
        vec3 deep = accent * 0.16 + vec3(0.004, 0.003, 0.014);
        vec3 horizon = mix(deep, accent * 0.55, 0.55);
        color = mix(horizon, deep, smoothstep(0.0, 0.9, vertical));

        vec3 rd = rotateY(d, t * 0.035);
        vec3 q = rd * 2.0 + vec3(t * 0.035, t * 0.018, -t * 0.025);
        float clouds = fbm3(q);
        float filaments = fbm3(rd * 5.0 + vec3(-t * 0.02, t * 0.03, t * 0.015));

        float nebula = smoothstep(0.42, 0.92, clouds) * 0.78 + smoothstep(0.55, 0.96, filaments) * 0.32;
        float band = 1.0 - abs(dot(d, normalize(vec3(0.52, 0.25, 0.82)))) * 1.35;
        band = smoothstep(0.18, 0.9, band);
        nebula += band * (0.18 + 0.45 * clouds);

        vec3 warm = mix(accent, vec3(1.0, 0.54, 0.2), 0.42);
        vec3 cool = mix(accent, vec3(0.16, 0.52, 1.0), 0.45);
        vec3 nebColor = mix(cool, warm, smoothstep(0.2, 0.95, clouds));
        vec3 core = mix(accent * 1.45, vec3(1.0, 0.95, 0.88), 0.35);
        color += nebColor * nebula * 1.2;
        color += core * pow(max(nebula - 0.52, 0.0), 1.7) * 1.25;

        float horizonGlow = pow(1.0 - smoothstep(0.0, 0.42, abs(d.y)), 1.8);
        color += mix(deep * 2.0, warm, 0.55) * horizonGlow * 0.35;

        color = color / (1.0 + color * 0.45);
        color = pow(color, vec3(0.92));
    }

    float alpha = FragColor.a * intensity;
    if (alpha <= 0.003) {
        discard;
    }

    OutColor = vec4(color, alpha);
}
