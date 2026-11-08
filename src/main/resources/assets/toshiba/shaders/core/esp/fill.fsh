#version 150

in vec3 WorldPos;
in vec4 FragColor;

uniform float time;
uniform float speed;
uniform float intensity;
uniform float mode;
uniform vec3 cameraPos;

out vec4 OutColor;

// Эффекты шейдеров клиента (sky_fog, sky_caustic, sky_sunset, sky_nebula) считаются
// от луча камеры в небо. Для бокса ESP такой луч уходит вниз, и эффект схлопывался
// в почти чёрный цвет. Поэтому здесь домен эффекта - мировые координаты точки бокса:
// картинка привязана к миру и не зависит от поворота камеры (мыши).

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

// Каустика из sky_caustic, но по мировой плоскости XZ.
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

// Слой звёзд из sky_nebula.
vec3 starLayer(vec3 d, float density, float threshold, vec3 tint) {
    vec3 p = d * density;
    vec3 cell = floor(p);
    float h = hash13(cell);
    if (h < threshold) {
        return vec3(0.0);
    }
    vec3 f = fract(p) - 0.5;
    float disc = 1.0 - smoothstep(0.0, 0.045, length(f));
    float twinkle = 0.65 + 0.35 * sin(time * 2.4 + h * 71.0);
    return mix(vec3(1.0), tint, 0.22) * disc * twinkle * ((h - threshold) / (1.0 - threshold));
}

void main() {
    vec3 accent = max(FragColor.rgb, vec3(0.05));
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
        // Туман - характер sky_fog: клубы, жилы и полосы, но по миру
        vec2 flow = WorldPos.xz * 0.85 + vec2(t * 0.22, -t * 0.17);
        float mist = fbm(vec3(flow * 0.72, WorldPos.y * 0.5 - t * 0.25), 4);
        float veins = pow(clamp(1.0 - abs(fbm(vec3(flow * 1.85 + vec3(mist * 2.5, mist * 1.6), t * 0.3), 3) * 2.0 - 1.0), 0.0, 1.0), 2.4);

        float stripeA = pow(clamp(1.0 - abs(sin((flow.x * 1.08 + flow.y * 0.42) * 1.7 - t * 0.85 + mist * 4.3)), 0.0, 1.0), 4.8);
        float stripeB = pow(clamp(1.0 - abs(sin((flow.x * -0.58 + flow.y * 1.12) * 1.45 + t * 0.65 + mist * 2.9)), 0.0, 1.0), 5.4);

        float energy = clamp(mist * 0.35 + veins * 0.85 + stripeA * 0.55 + stripeB * 0.35, 0.0, 1.0);
        float core = smoothstep(0.15, 0.95, energy);
        vec3 fogColor = mix(accent * 0.18, mix(accent, vec3(1.0), 0.55), clamp(core * 0.8 + stripeB * 0.2, 0.0, 1.0));
        color = fogColor * (0.45 + 0.85 * energy);
    } else if (mode < 2.5) {
        // Каустика - характер sky_caustic по мировой плоскости
        vec2 uv_c = WorldPos.xz * 0.32 + vec2(t * 0.05, t * 0.03);
        float c1 = caustic(uv_c, t * 0.30);
        float c2 = caustic(uv_c * 1.30 + vec2(7.3, 11.1), t * 0.234 + 3.1) * 0.55;
        float c = c1 + c2;

        vec3 deep = accent * 0.10 + vec3(0.005, 0.008, 0.020);
        vec3 mid = accent * 0.55;
        vec3 hot = mix(accent, vec3(1.0), 0.55);

        color = deep + (accent * 0.95 + hot * 0.40) * c;
        float core = smoothstep(0.55, 1.5, c);
        color += hot * core * 0.85;
        color = color / (1.0 + color * 0.55);
        color = pow(color, vec3(0.92));
        color = mix(color, mid, 0.18);
    } else if (mode < 3.5) {
        // Закат - палитра sky_sunset по высоте над камерой + мерцание
        float height = clamp((WorldPos.y - cameraPos.y) * 0.6 + 0.45, 0.0, 1.0);

        vec3 skyZenith = vec3(0.045, 0.025, 0.165);
        vec3 skyHigh = vec3(0.420, 0.180, 0.420);
        vec3 skyMid = vec3(0.950, 0.400, 0.350);
        vec3 skyLow = vec3(1.000, 0.620, 0.250);
        vec3 skyEmber = vec3(0.450, 0.050, 0.100);

        vec3 sky = mix(skyEmber, skyLow, smoothstep(0.0, 0.35, height));
        sky = mix(sky, skyMid, smoothstep(0.30, 0.62, height));
        sky = mix(sky, skyHigh, smoothstep(0.58, 0.85, height));
        sky = mix(sky, skyZenith, smoothstep(0.82, 1.0, height));

        sky = mix(sky, sky * mix(vec3(1.0), accent * 1.6, 0.45), 0.35);
        sky += vec3(1.00, 0.65, 0.25) * pow(clamp(height, 0.0, 1.0), 3.0) * 0.45;

        float shimmer = fbm(vec3(WorldPos.xz * 1.6, t * 0.6), 3) * 0.35;
        sky += vec3(1.0, 0.9, 0.7) * shimmer * (1.0 - smoothstep(0.0, 0.5, abs(height - 0.4)));

        color = sky;
        color = color / (1.0 + color * 0.45);
        color = pow(color, vec3(0.92));
    } else {
        // Туманность - характер sky_nebula по мировому объёму
        vec3 q = WorldPos * 0.55 + vec3(t * 0.035, t * 0.018, -t * 0.025);
        float clouds = fbm3(q);
        float filaments = fbm3(WorldPos * 1.6 + vec3(-t * 0.02, t * 0.03, t * 0.015));

        vec3 deep = accent * 0.22 + vec3(0.004, 0.003, 0.014);
        vec3 horizon = mix(deep, accent * 0.6, 0.55);
        color = mix(horizon, deep, smoothstep(0.0, 0.85, clouds));

        float nebula = smoothstep(0.40, 0.90, clouds) * 0.85 + smoothstep(0.52, 0.95, filaments) * 0.40;

        vec3 warm = mix(accent, vec3(1.0, 0.54, 0.2), 0.42);
        vec3 cool = mix(accent, vec3(0.16, 0.52, 1.0), 0.45);
        vec3 nebColor = mix(cool, warm, smoothstep(0.2, 0.95, clouds));
        vec3 core = mix(accent * 1.45, vec3(1.0, 0.95, 0.88), 0.35);
        color += nebColor * nebula * 1.35;
        color += core * pow(max(nebula - 0.5, 0.0), 1.7) * 1.3;

        vec3 starDir = rotateY(normalize(WorldPos - cameraPos + vec3(0.0, 0.5, 0.0)), t * 0.008);
        color += starLayer(starDir, 12.0, 0.989, mix(accent, vec3(1.0), 0.65)) * (1.0 - nebula * 0.45);

        color = color / (1.0 + color * 0.45);
        color = pow(color, vec3(0.92));
    }

    float alpha = FragColor.a * intensity;
    if (alpha <= 0.003) {
        discard;
    }

    OutColor = vec4(color, alpha);
}
