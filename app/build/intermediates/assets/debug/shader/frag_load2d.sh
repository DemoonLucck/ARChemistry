#version 300 es
precision mediump float;
uniform sampler2D uSampleBottom;//纹理内容数据
uniform sampler2D uSampleTop;
uniform float uXPosition;
in vec2 vTextureCoord; //接收从顶点着色器过来的参数
in float  vPositionX;
out vec4 fragColor;
void main()                         
{
	if((vPositionX+0.5)<uXPosition)
	{
		fragColor = texture(uSampleTop, vTextureCoord);
	}else {
		fragColor = texture(uSampleBottom, vTextureCoord);
	}
}              