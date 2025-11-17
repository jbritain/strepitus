/*
    References:
        [QUI08] Quilez, Inigo. "Value Noise Derivatives". 2008.
            https://iquilezles.org/articles/morenoise/
        [QUI13a] Quilez, Inigo. "Noise - value - 2D". 2013.
            MIT License. Copyright (c) 2013 Inigo Quilez.
            https://www.shadertoy.com/view/lsf3WH
        [QUI13b] Quilez, Inigo. "Noise - value - 3D". 2013.
            MIT License. Copyright (c) 2013 Inigo Quilez.
            https://www.shadertoy.com/view/4sfGzS
        [QUI17a] Quilez, Inigo. "Noise - Value - 2D - Deriv". 2017.
            MIT License. Copyright (c) 2017 Inigo Quilez.
            https://www.shadertoy.com/view/4dXBRH
        [QUI17b] Quilez, Inigo. "Noise - Value - 3D - Deriv". 2017.
            MIT License. Copyright (c) 2017 Inigo Quilez.
            https://www.shadertoy.com/view/XsXfRH

        You can find full license texts in /licenses
*/

#ifndef INCLUDE_util_noise_ValueNoise_glsl
#define INCLUDE_util_noise_ValueNoise_glsl a

#include "_Common.glsl"

// -------------------------------------------------- Value Noise 2D --------------------------------------------------
float _ValueNoise_2D_hash(uvec2 x) {
    return hash_uintToFloat(hash_21_q5(x)) * 2.0 - 1.0;
}

// [QUI17a]
vec3 ValueNoise_2D_valueGrad(vec2 x, vec2 freq) {
    vec2 w = fract(x);

    vec2 u = _NOISE_INTERPO(w);
    vec2 du = _NOISE_INTERPO_GRAD(w);

    float va = _ValueNoise_2D_hash(hashCoordWarp(x + vec2(0.0, 0.0), freq));
    float vb = _ValueNoise_2D_hash(hashCoordWarp(x + vec2(1.0, 0.0), freq));
    float vc = _ValueNoise_2D_hash(hashCoordWarp(x + vec2(0.0, 1.0), freq));
    float vd = _ValueNoise_2D_hash(hashCoordWarp(x + vec2(1.0, 1.0), freq));

    float xy0 = mix(va, vb, u.x);
    float xy1 = mix(vc, vd, u.x);
    float value = mix(xy0, xy1, u.y);

    vec2 grad = du * vec2(
        mix(vb - va, vd - vc, u.y),
        mix(vc - va, vd - vb, u.x)
    );

    return vec3(value, grad);
}


// -------------------------------------------------- Value Noise 3D --------------------------------------------------
float _ValueNoise_3D_hash(uvec3 x) {
    return hash_uintToFloat(hash_31_q5(x)) * 2.0 - 1.0;
}

// [QUI17b]
vec4 ValueNoise_3D_valueGrad(vec3 x, vec3 freq) {
    vec3 w = fract(x);

    vec3 u = _NOISE_INTERPO(w);
    vec3 du = _NOISE_INTERPO_GRAD(w);

    float va = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(0.0, 0.0, 0.0), freq));
    float vb = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(1.0, 0.0, 0.0), freq));
    float vc = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(0.0, 1.0, 0.0), freq));
    float vd = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(1.0, 1.0, 0.0), freq));
    float ve = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(0.0, 0.0, 1.0), freq));
    float vf = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(1.0, 0.0, 1.0), freq));
    float vg = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(0.0, 1.0, 1.0), freq));
    float vh = _ValueNoise_3D_hash(hashCoordWarp(x + vec3(1.0, 1.0, 1.0), freq));

    float xy0 = mix(mix(va, vb, u.x), mix(vc, vd, u.x), u.y);
    float xy1 = mix(mix(ve, vf, u.x), mix(vg, vh, u.x), u.y);
    float value = mix(xy0, xy1, u.z);

    vec3 grad = du * vec3(
        mix(mix(vb - va, vd - vc, u.y), mix(vf - ve, vh - vg, u.y), u.z),
        mix(mix(vc - va, vd - vb, u.x), mix(vg - ve, vh - vf, u.x), u.z),
        mix(mix(ve - va, vf - vb, u.x), mix(vg - vc, vh - vd, u.x), u.y)
    );

    return vec4(value, grad);
}

#endif