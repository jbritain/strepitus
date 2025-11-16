#version 460 core

#extension GL_KHR_shader_subgroup_arithmetic : enable

layout(local_size_x = 32, local_size_y = 32) in;

layout(std430) buffer DataBuffer {
    ivec4 data;
} ssbo_data;

layout(r32f) uniform readonly image2D uimg_noiseImage;

shared float shared_minCount[32];
shared float shared_maxCount[32];

void main() {
    float count = imageLoad(uimg_noiseImage, ivec2(gl_GlobalInvocationID.xy)).r;

    float minCount1 = subgroupMin(count);
    float maxCount1 = subgroupMax(count);
    if (subgroupElect()) {
        shared_minCount[gl_SubgroupID] = minCount1;
        shared_maxCount[gl_SubgroupID] = maxCount1;
    }
    barrier();

    if (gl_SubgroupID == 0) {
        float minCount2 = shared_minCount[gl_SubgroupInvocationID];
        float maxCount2 = shared_maxCount[gl_SubgroupInvocationID];
        float minCount3 = subgroupMin(minCount2);
        float maxCount3 = subgroupMax(maxCount2);
        if (subgroupElect()) {
            atomicMin(ssbo_data.data.x, floatBitsToInt(minCount3));
            atomicMax(ssbo_data.data.y, floatBitsToInt(maxCount3));
        }
    }
}
