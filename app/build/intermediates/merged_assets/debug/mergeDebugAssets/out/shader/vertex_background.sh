#version 300 es
//��ɨ����渺����Ʊ����������ɫ��
uniform mat4 projectionMatrix;
in vec4 vertexPosition;
in vec2 vertexTexCoord;
out vec2 texCoord;
void main(){
	gl_Position = projectionMatrix * vertexPosition;
	texCoord = vertexTexCoord;
}                   