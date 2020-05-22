#version 300 es
precision mediump float;
uniform sampler2D sTexture;//������������
uniform float uFactor;
//���մӶ�����ɫ�������Ĳ���
in vec4 ambient;
in vec4 diffuse;
in vec4 specular;
in vec2 vTextureCoord;
out vec4 fragColor;
void main(){    
   //�����������ɫ����ƬԪ
   vec4 finalColor=texture(sTexture, vTextureCoord); 
   finalColor = vec4(finalColor.r,finalColor.g,finalColor.b,finalColor.a*uFactor);  
   //����ƬԪ��ɫֵ
   fragColor = finalColor*ambient+finalColor*specular+finalColor*diffuse;
}   