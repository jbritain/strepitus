#version 460 core

layout(local_size_x = 1) in;

layout(std430) buffer DataBuffer {
    uvec4 data;
} ssbo_data;

void main() {
    ssbo_data.data = uvec4(0x7F7FFFFFu, 0xff7fffffu, 0u, 0u);
}
