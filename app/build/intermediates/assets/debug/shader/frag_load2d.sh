#version 300 es
precision mediump float;
uniform sampler2D uSampleBottom;//������������
uniform sampler2D uSampleTop;
uniform float uXPosition;
in vec2 vTextureCoord; //���մӶ�����ɫ�������Ĳ���
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