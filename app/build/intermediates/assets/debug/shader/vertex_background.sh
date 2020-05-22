#version 300 es
//在扫描界面负责绘制背景画面的着色器
uniform mat4 projectionMatrix;
in vec4 vertexPosition;
in vec2 vertexTexCoord;
out vec2 texCoord;
void main(){
	gl_Position = projectionMatrix * vertexPosition;
	texCoord = vertexTexCoord;
}                   