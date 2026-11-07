#version 150

in vec2 TexCoord;
out vec4 OutColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform vec4 resolution;
uniform vec4 settings;
uniform vec4 motion;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x),
               mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}

vec3 nearestHandColor(vec2 uv, vec2 pixel, float radius, out float support) {
    vec4 sum = vec4(0.0);
    for (int y = -2; y <= 2; y++) {
        for (int x = -2; x <= 2; x++) {
            vec2 sampleUv = clamp(uv + vec2(float(x), float(y)) * pixel * radius,
                    pixel * 0.5, vec2(1.0) - pixel * 0.5);
            float weight = texture(Sampler1, sampleUv).r;
            vec3 color = texture(Sampler2, sampleUv).rgb;
            sum += vec4(color * weight, weight);
        }
    }
    support = clamp(sum.a, 0.0, 1.0);
    vec3 color = sum.a > 0.001 ? sum.rgb / sum.a : vec3(0.0);
    float maximum = max(color.r, max(color.g, color.b));
    return maximum > 0.001 ? color / maximum : vec3(0.0);
}

void main() {
    vec2 pixel = 1.0 / max(resolution.xy, vec2(1.0));
    float time = resolution.z;
    float dt = max(resolution.w, 0.001);
    float retention = clamp(settings.x, 0.70, 0.94);
    float speed = max(settings.y, 0.05);
    float size = max(settings.z, 0.1);
    float density = max(settings.w, 0.05);
    float densityAmount = clamp((density - 0.2) / 2.3, 0.0, 1.0);
    float densityGain = mix(0.82, 1.48, densityAmount);
    bool resetColor = motion.z < 0.0;
    float rawActivity = clamp(motion.z, 0.0, 2.5);
    float activity = clamp(rawActivity, 0.0, 1.0);
    float highSpeed = max(rawActivity - 1.0, 0.0);
    float swing = clamp(motion.w, 0.0, 1.0);

    float curl = noise(vec2(TexCoord.y * 12.0 - time * speed * 1.15,
                            TexCoord.x * 7.0 + time * speed * 0.62)) - 0.5;
    float flicker = sin(TexCoord.y * 28.0 + time * speed * 6.2) * 0.62
                  + sin(TexCoord.y * 15.0 - time * speed * 3.7 + 1.4) * 0.38;
    float wideSway = sin(time * speed * 2.15 + TexCoord.y * 8.0)
                  + sin(time * speed * 1.28 - TexCoord.y * 5.5 + 2.1) * 0.68;
    float idleScale = mix(0.82, 1.08, activity);
    float movementWind = clamp(motion.x, -0.65, 0.65) * (4.0 + size * 2.0);
    float swingWind = swing * sin(time * speed * 5.4 + TexCoord.y * 8.0) * (4.0 + size * 2.5);
    float horizontalFlow = (curl * (5.5 + size * 3.2)
            + flicker * (1.4 + size) + wideSway * (2.5 + size * 1.15)) * idleScale
            + movementWind + swingWind;
    float maxAdvectionPixels = 8.0 + size * 2.5;
    float advectionPixels = clamp(horizontalFlow * dt * 60.0,
            -maxAdvectionPixels * dt * 60.0, maxAdvectionPixels * dt * 60.0);
    float rise = speed * dt * (0.022 + size * 0.014
            + activity * (0.014 + size * 0.010) + highSpeed * (0.012 + size * 0.007));
    float verticalInertia = clamp(motion.y, -0.5, 0.5) * max(activity, 0.25) * dt * 0.018;
    vec2 previousUv = TexCoord - vec2(advectionPixels * pixel.x, rise + verticalInertia);

    vec2 spread = pixel * (2.0 + size * mix(3.4, 5.0, activity) + highSpeed * 1.45);
    vec4 p0 = texture(Sampler0, previousUv);
    vec4 p1 = texture(Sampler0, previousUv + vec2(spread.x, 0));
    vec4 p2 = texture(Sampler0, previousUv - vec2(spread.x, 0));
    vec4 p3 = texture(Sampler0, previousUv + vec2(0, spread.y));
    vec4 p4 = texture(Sampler0, previousUv - vec2(0, spread.y));
    vec4 p5 = texture(Sampler0, previousUv + spread);
    vec4 p6 = texture(Sampler0, previousUv - spread);
    vec4 p7 = texture(Sampler0, previousUv + vec2(spread.x, -spread.y));
    vec4 p8 = texture(Sampler0, previousUv + vec2(-spread.x, spread.y));
    float previous = p0.a * 0.22 + (p1.a + p2.a + p3.a + p4.a) * 0.11
            + (p5.a + p6.a + p7.a + p8.a) * 0.085;
    vec3 colorSum = p0.rgb * p0.a * 0.22
            + (p1.rgb * p1.a + p2.rgb * p2.a + p3.rgb * p3.a + p4.rgb * p4.a) * 0.11
            + (p5.rgb * p5.a + p6.rgb * p6.a + p7.rgb * p7.a + p8.rgb * p8.a) * 0.085;
    float colorWeight = p0.a * 0.22 + (p1.a + p2.a + p3.a + p4.a) * 0.11
            + (p5.a + p6.a + p7.a + p8.a) * 0.085;
    vec3 previousColor = colorWeight > 0.001 ? colorSum / colorWeight : vec3(0.0);

    if (resetColor) { previous = 0.0; colorWeight = 0.0; previousColor = vec3(0.0); }
    previous *= pow(retention, dt * 60.0 * (0.98 - activity * 0.26 + speed * 0.035));
    float dissolve = 0.94 + noise(TexCoord * vec2(31.0, 24.0)
            + vec2(time * speed * 0.7, -time * speed * 1.4)) * 0.06;
    previous *= dissolve;
    previous = max(0.0, previous - dt * (0.34 + (1.0 - retention) * 2.0));
    previous *= smoothstep(0.018, 0.075, previous);

    float centerMask = texture(Sampler1, TexCoord).r;
    float edgeSum = 0.0;
    float radius = 1.2 + size * mix(2.15, 3.0, activity) + highSpeed * 0.55;
    for (int i = 0; i < 12; i++) {
        float angle = float(i) * 0.5235988;
        vec2 offset = vec2(cos(angle), sin(angle)) * pixel * radius;
        float around = texture(Sampler1, TexCoord + offset).r;
        edgeSum += abs(centerMask - around);
    }
    float silhouetteEdge = smoothstep(0.018, 0.34, edgeSum / 12.0);
    float breakup = 0.72 + noise(TexCoord * vec2(42.0, 29.0) + vec2(0, -time * speed * 2.1)) * 0.48;
    float tongues = noise(TexCoord * vec2(71.0, 46.0) + vec2(time * speed * 1.3, -time * speed * 3.0));
    float source = silhouetteEdge * (0.92 + tongues * 0.72) * mix(0.82, 1.08, activity);
    source = clamp(source * breakup * densityGain, 0.0, 1.0);
    source = source * source * (3.0 - 2.0 * source);
    float injection = 1.0 - exp(-dt * 60.0 * (0.34 + densityGain * 0.20));
    float colorSupport;
    vec3 sourceColor = nearestHandColor(TexCoord, pixel, max(radius * 0.55, 1.0), colorSupport);
    float feedbackTrail = previous + source * injection * (1.0 - previous);
    float liveOutline = source * mix(0.68, 0.82, activity);
    float trail = clamp(max(feedbackTrail, liveOutline), 0.0, 1.0);
    trail *= smoothstep(0.006, 0.026, trail);
    float sourceWeight = clamp(source * injection * colorSupport, 0.0, 1.0);
    vec3 trailColor = !resetColor && colorWeight > 0.002 ? previousColor : sourceColor;
    trailColor = mix(trailColor, sourceColor, sourceWeight);
    OutColor = vec4(trailColor, trail);
}
