#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float BrightnessAdjust;

out vec4 fragColor;

float adjust(float x, float a) {
    return a*(1.0-pow(1.0-x, 2.0)) + (1.0-a)*x;
}

vec3 adjust(vec3 toAdjust, float amount) {
    return vec3(adjust(clamp(toAdjust.r * 265./190., 0., 1.), amount), adjust(clamp(toAdjust.g * 195./170., 0., 1.), amount), adjust(toAdjust.b * 152./212., amount));
}

void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);
    fragColor = vec4(adjust(InTexel.rgb, BrightnessAdjust), 1.0);
}