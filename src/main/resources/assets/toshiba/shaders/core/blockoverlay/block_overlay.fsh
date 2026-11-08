#version 150

uniform sampler2D Sampler0;
uniform vec2 texelSize;
uniform vec3 color;
uniform vec3 color2;
uniform float time;

uniform float speed;
uniform float scale;
uniform float outline;
uniform float glow;
uniform float fill;
uniform float alpha;
uniform float outlineOnly;
uniform float optimize;

// New uniforms for shader modes
uniform float mode; // 0 - Fog, 1 - Caustic, 2 - Sunset, 3 - Nebula
uniform vec2 cameraDir;
uniform float fov;
uniform vec2 resolution;

in vec2 TexCoord;
out vec4 OutColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    int octaves = (optimize > 0.5) ? 1 : 4;
    for (int i = 0; i < 4; i++) {
        if (i >= octaves) break;
        value += noise(p) * amplitude;
        p = p * 2.02 + vec2(8.4, 5.7);
        amplitude *= 0.5;
    }
    return value;
}

float ridged(vec2 p) {
    float value = 0.0;
    float amplitude = 0.55;
    int octaves = (optimize > 0.5) ? 2 : 3;
    for (int i = 0; i < 3; i++) {
        if (i >= octaves) break;
        float r = 1.0 - abs(noise(p) * 2.0 - 1.0);
        value += r * amplitude;
        p = p * 2.18 + vec2(3.1, 9.2);
        amplitude *= 0.52;
    }
    return value;
}

float getEdge(vec2 uv, float s) {
    float a0 = texture(Sampler0, uv).a;
    float ax1 = texture(Sampler0, uv + vec2(texelSize.x * s, 0.0)).a;
    float ax2 = texture(Sampler0, uv - vec2(texelSize.x * s, 0.0)).a;
    float ay1 = texture(Sampler0, uv + vec2(0.0, texelSize.y * s)).a;
    float ay2 = texture(Sampler0, uv - vec2(0.0, texelSize.y * s)).a;
    float edge = abs(a0 - ax1) + abs(a0 - ax2) + abs(a0 - ay1) + abs(a0 - ay2);

    float diag1 = texture(Sampler0, uv + vec2(texelSize.x * s, texelSize.y * s)).a;
    float diag2 = texture(Sampler0, uv + vec2(-texelSize.x * s, texelSize.y * s)).a;
    float diag3 = texture(Sampler0, uv + vec2(texelSize.x * s, -texelSize.y * s)).a;
    float diag4 = texture(Sampler0, uv + vec2(-texelSize.x * s, -texelSize.y * s)).a;
    edge += 0.7 * (abs(a0 - diag1) + abs(a0 - diag2) + abs(a0 - diag3) + abs(a0 - diag4));

    return clamp(edge * 1.9, 0.0, 1.0);
}

// -------------------------------------------------------------
// Functions for modes (Caustic, Sunset, Nebula)
// -------------------------------------------------------------

float hash13(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
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

float vnoise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash13(i + vec3(0.0, 0.0, 0.0)), hash13(i + vec3(1.0, 0.0, 0.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 0.0)), hash13(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
        mix(mix(hash13(i + vec3(0.0, 0.0, 1.0)), hash13(i + vec3(1.0, 0.0, 1.0)), f.x),
            mix(hash13(i + vec3(0.0, 1.0, 1.0)), hash13(i + vec3(1.0, 1.0, 1.0)), f.x), f.y),
        f.z);
}

float fbmSunset(vec3 p, int oct) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        if (i >= oct) break;
        v += a * vnoise(p);
        p = p * 2.07 + vec3(1.7, 9.2, 4.3);
        a *= 0.5;
    }
    return v;
}

float noiseNebula(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    float a = mix(hash13(i + vec3(0.0, 0.0, 0.0)), hash13(i + vec3(1.0, 0.0, 0.0)), f.x);
    float b = mix(hash13(i + vec3(0.0, 1.0, 0.0)), hash13(i + vec3(1.0, 1.0, 0.0)), f.x);
    float c = mix(hash13(i + vec3(0.0, 0.0, 1.0)), hash13(i + vec3(1.0, 0.0, 1.0)), f.x);
    float d = mix(hash13(i + vec3(0.0, 1.0, 1.0)), hash13(i + vec3(1.0, 1.0, 1.0)), f.x);
    return mix(mix(a, b, f.y), mix(c, d, f.y), f.z);
}

float fbm3(vec3 p) {
    float v = 0.0;
    float a = 0.55;
    v += noiseNebula(p) * a;
    p = p * 2.03 + vec3(7.1, 3.2, 5.4);
    a *= 0.52;
    v += noiseNebula(p) * a;
    p = p * 2.11 + vec3(1.9, 8.3, 2.7);
    a *= 0.52;
    v += noiseNebula(p) * a;
    return v;
}

vec3 rotateY(vec3 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(p.x * c - p.z * s, p.y, p.x * s + p.z * c);
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

vec3 getRayDir(vec2 uvCoord) {
    vec2 sp = uvCoord * 2.0 - 1.0;
    float aspect = resolution.x / max(resolution.y, 1.0);
    float tanV = tan(radians(fov) * 0.5);
    vec3 rayV = normalize(vec3(sp.x * tanV * aspect, sp.y * tanV, 1.0));
    return normalize(rotY(cameraDir.x) * rotX(cameraDir.y) * rayV);
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler0, uv).a;

    if (mask <= 0.0) {
        if (outlineOnly <= 0.5) discard;

        float s = outline;
        float ax1 = texture(Sampler0, uv + vec2(texelSize.x * s, 0.0)).a;
        float ax2 = texture(Sampler0, uv - vec2(texelSize.x * s, 0.0)).a;
        float ay1 = texture(Sampler0, uv + vec2(0.0, texelSize.y * s)).a;
        float ay2 = texture(Sampler0, uv - vec2(0.0, texelSize.y * s)).a;

        float edgeSum = ax1 + ax2 + ay1 + ay2;
        if (edgeSum <= 0.001) discard;

        float edgeBand = smoothstep(0.02, 0.42, clamp(edgeSum * 2.5, 0.0, 1.0));
        float edgeAlpha = clamp(edgeBand * alpha, 0.0, 1.0);
        if (edgeAlpha <= 0.001) discard;
        OutColor = vec4(mix(color, color2, 0.35), edgeAlpha);
        return;
    }

    float edge = getEdge(uv, outline);
    float edgeBand = smoothstep(0.02, 0.42, edge);

    if (outlineOnly > 0.5) {
        float edgeAlpha = clamp(edgeBand * alpha, 0.0, 1.0);
        if (edgeAlpha <= 0.001) discard;
        OutColor = vec4(mix(color, color2, 0.35), edgeAlpha);
        return;
    }

    vec3 shaderColor;
    float fillStrengthMultiplier = 1.0;

    if (mode < 0.5) { // 0 - Fog
        float t = time * max(speed, 0.001);
        vec2 flow = uv * mix(1.2, 3.2, clamp(scale / 3.0, 0.0, 1.0));
        vec2 drift = vec2(t * 0.20, -t * 0.15);

        vec2 warp;
        if (optimize > 0.5) {
            float warpVal = fbm(flow * 0.84 + drift * 0.6);
            warp = vec2(sin(warpVal * 6.28318), cos(warpVal * 6.28318));
        } else {
            warp = vec2(
                fbm(flow * 0.90 + drift * 0.75 + vec2(0.0, 4.1)),
                fbm(flow * 0.78 - drift * 0.48 + vec2(3.7, 1.8))
            );
        }
        vec2 q = flow + (warp - 0.5) * 1.8;

        float mist = fbm(q * 0.72 - drift * 0.24 + vec2(4.2, 8.1));
        float veins = 0.0;
        if (optimize <= 0.5) {
            veins = ridged(q * 1.85 + vec2(mist * 2.5, mist * 1.6) - drift * 0.55);
            veins = pow(clamp(veins, 0.0, 1.0), 2.4);
        }

        float stripeA = 1.0 - abs(sin((q.x * 1.08 + q.y * 0.42) * 1.7 + time * 0.85 + mist * 4.3));
        float stripeB = 1.0 - abs(sin((q.x * -0.58 + q.y * 1.12) * 1.45 - time * 0.65 - mist * 2.9));
        stripeA = pow(clamp(stripeA, 0.0, 1.0), 4.8);
        stripeB = pow(clamp(stripeB, 0.0, 1.0), 5.4);

        float energy = clamp(mist * 0.22 + veins * 0.88 + stripeA * 0.55 + stripeB * 0.32, 0.0, 1.0);
        float core = smoothstep(0.18, 0.98, energy);
        float accent = pow(clamp(max(veins, stripeA), 0.0, 1.0), 1.25);

        shaderColor = mix(color, color2, clamp(core * 0.75 + stripeB * 0.25, 0.0, 1.0));
        fillStrengthMultiplier = (0.26 + core * 0.82 + accent * 0.28);
    } else if (mode < 1.5) { // 1 - Caustic
        vec3 d = getRayDir(uv);
        float t = time * 0.30 * speed;
        vec3 accent = max(color, vec3(0.05));
        vec3 deep = accent * 0.05 + vec3(0.005, 0.008, 0.020);
        vec3 mid = accent * 0.42;
        vec3 hot = mix(accent, vec3(1.0), 0.55);
        vec2 uv_c = d.xz / (abs(d.y) + 0.30) * 0.50;
        float c1 = caustic(uv_c, t);
        float c2 = caustic(uv_c * 1.30 + vec2(7.3, 11.1), t * 0.78 + 3.1) * 0.55;
        float c = c1 + c2;
        vec3 sky = mix(mid, deep, smoothstep(0.0, 0.75, d.y));
        float skyMask = smoothstep(-0.05, 0.22, d.y);
        shaderColor = sky + (accent * 0.95 + hot * 0.40) * c * skyMask;
        float core = smoothstep(0.65, 1.6, c);
        shaderColor += hot * core * 0.75;
        shaderColor = shaderColor / (1.0 + shaderColor * 0.55);
        shaderColor = pow(shaderColor, vec3(0.92));
    } else if (mode < 2.5) { // 2 - Sunset
        vec3 d = getRayDir(uv);
        float t = time * speed;
        vec3 sunDir = normalize(vec3(0.55 + sin(t * 0.04) * 0.04, 0.07, 0.80 + cos(t * 0.04) * 0.04));
        float toSun = max(0.0, dot(d, sunDir));
        vec3 accent = max(color, vec3(0.05));

        vec3 skyZenith = vec3(0.045, 0.025, 0.165);
        vec3 skyHigh = vec3(0.420, 0.180, 0.420);
        vec3 skyMid = vec3(0.950, 0.400, 0.350);
        vec3 skyLow = vec3(1.000, 0.620, 0.250);
        vec3 skyEmber = vec3(0.450, 0.050, 0.100);
        vec3 skyVal;
        if (d.y > 0.55) {
            skyVal = mix(skyHigh, skyZenith, smoothstep(0.55, 1.0, d.y));
        } else if (d.y > 0.18) {
            skyVal = mix(skyMid, skyHigh, smoothstep(0.18, 0.55, d.y));
        } else if (d.y > 0.0) {
            skyVal = mix(skyLow, skyMid, smoothstep(0.0, 0.18, d.y));
        } else {
            skyVal = mix(skyEmber, skyLow, smoothstep(-0.45, 0.0, d.y));
        }
        skyVal = mix(skyVal, skyVal * mix(vec3(1.0), accent * 1.6, 0.55), 0.30);
        skyVal += vec3(1.00, 0.65, 0.25) * pow(toSun, 8.0) * 0.55;
        skyVal += vec3(1.00, 0.85, 0.40) * pow(toSun, 24.0) * 0.85;
        shaderColor = skyVal;
    } else { // 3 - Nebula
        vec3 d = getRayDir(uv);
        vec3 accent = max(color, vec3(0.05));
        float vertical = abs(d.y);
        vec3 deep = accent * 0.16 + vec3(0.004, 0.003, 0.014);
        vec3 horizon = mix(deep, accent * 0.55, 0.55);
        vec3 nebColor = mix(horizon, deep, smoothstep(0.0, 0.9, vertical));
        vec3 rd = rotateY(d, time * 0.035 * speed);
        vec3 q_neb = rd * 2.0 + vec3(time * 0.035 * speed, time * 0.018 * speed, -time * 0.025 * speed);
        float clouds = fbm3(q_neb);
        float filaments = fbm3(rd * 5.0 + vec3(-time * 0.02 * speed, time * 0.03 * speed, time * 0.015 * speed));
        float nebula = smoothstep(0.42, 0.92, clouds) * 0.78 + smoothstep(0.55, 0.96, filaments) * 0.32;
        float band = 1.0 - abs(dot(d, normalize(vec3(0.52, 0.25, 0.82)))) * 1.35;
        band = smoothstep(0.18, 0.9, band);
        nebula += band * (0.18 + 0.45 * clouds);
        vec3 warm = mix(accent, vec3(1.0, 0.54, 0.2), 0.42);
        vec3 cool = mix(accent, vec3(0.16, 0.52, 1.0), 0.45);
        vec3 finalNebColor = mix(cool, warm, smoothstep(0.2, 0.95, clouds));
        vec3 nebCore = mix(accent * 1.45, vec3(1.0, 0.95, 0.88), 0.35);
        nebColor += finalNebColor * nebula * 1.2;
        nebColor += nebCore * pow(max(nebula - 0.52, 0.0), 1.7) * 1.25;
        float horizonGlow = pow(1.0 - smoothstep(0.0, 0.42, abs(d.y)), 1.8);
        nebColor += mix(deep * 2.0, warm, 0.55) * horizonGlow * 0.35;
        nebColor = nebColor / (1.0 + nebColor * 0.45);
        shaderColor = pow(nebColor, vec3(0.92));
    }

    vec3 outlineColor = mix(color, color2, 0.35);
    float innerMask = clamp(mask - edgeBand * 0.58, 0.0, 1.0);
    float fillStrength = fill * innerMask * fillStrengthMultiplier;
    float edgeStrength = edgeBand * (0.34 + glow * 0.12);

    vec3 rgb = shaderColor * fillStrength + outlineColor * edgeStrength;
    float outAlpha = clamp(alpha * (fillStrength * 0.92 + edgeBand * 0.48) * mask, 0.0, 1.0);

    if (outAlpha <= 0.001) discard;
    OutColor = vec4(rgb, outAlpha);
}
