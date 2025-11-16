#version 460 core

#include "/util/Dither.glsl"
#include "/util/Math.glsl"
#include "/util/Rand.glsl"

uniform float uval_slice;
uniform float uval_zoom;
uniform int uval_colorMode;
uniform int uval_tilling;
uniform ivec4 uval_viewport;

uniform sampler3D usam_outputImageTiling;
uniform sampler3D usam_outputImage;

out vec4 fragColor;

vec3 dither(vec3 x, float noiseV) {
    vec3 result = x;
    result *= 255.0;
    result = round(result + (noiseV - 0.5));
    result /= 255.0;
    return result;
}

void main() {
    vec2 screenPos = (gl_FragCoord.xy - vec2(uval_viewport.xy)) / 1080.0;
    vec3 samplePos = vec3(screenPos, uval_slice);
    samplePos = samplePos * 2.0 - 1.0;
    samplePos.xy *= uval_zoom;
    samplePos = samplePos * 0.5 + 0.5;
    vec4 noiseV;
    if (uval_tilling == 1 )  {
        noiseV = texture(usam_outputImageTiling, samplePos);
    } else {
        noiseV = texture(usam_outputImage, samplePos);
    }

    if (uval_colorMode == 0) {
        fragColor.rgb = noiseV.rrr;
    } else if (uval_colorMode == 1) {
        fragColor.rgb = noiseV.aaa;
    } else {
        fragColor.rgb = noiseV.rgb;
    }

    fragColor.a = 1.0;
}