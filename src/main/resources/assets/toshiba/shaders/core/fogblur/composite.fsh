#version 150

in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 DepthParams;

out vec4 OutColor;

float linearizeDepth(float depth, float nearPlane, float farPlane) {
    return (2.0 * nearPlane * farPlane)
        / (farPlane + nearPlane - depth * (farPlane - nearPlane));
}

void main() {
    float rawDepth = texture(Sampler1, TexCoord).x;

    if (rawDepth >= 0.9999) {
        OutColor = vec4(texture(Sampler0, TexCoord).rgb, 1.0);
        return;
    }

    float distance = linearizeDepth(rawDepth, DepthParams.x, DepthParams.y) / DepthParams.y;
    if (distance > DepthParams.z) {
        float alpha = clamp((distance - DepthParams.z) / (DepthParams.w - DepthParams.z), 0.0, 1.0);
        OutColor = vec4(texture(Sampler0, TexCoord).rgb, alpha);
    } else {
        OutColor = vec4(0.0);
    }
}
