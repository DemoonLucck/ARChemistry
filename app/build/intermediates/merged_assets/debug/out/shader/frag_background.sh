#version 300 es
precision mediump float;//����Ĭ�ϵĸ��㾫��
uniform sampler2D texSampler2D;//������������
in vec2 texCoord;
out vec4 fragColor;
void main() {
   	fragColor = texture(texSampler2D, texCoord);//���屾�����ɫ
}