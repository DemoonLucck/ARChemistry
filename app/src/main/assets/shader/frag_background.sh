#version 300 es
precision mediump float;//给出默认的浮点精度
uniform sampler2D texSampler2D;//纹理内容数据
in vec2 texCoord;
out vec4 fragColor;
void main() {
   	fragColor = texture(texSampler2D, texCoord);//物体本身的颜色
}