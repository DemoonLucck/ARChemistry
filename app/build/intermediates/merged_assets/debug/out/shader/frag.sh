#version 300 es
precision mediump float;
uniform sampler2D sTexture;//纹理内容数据
uniform float uFactor;
//接收从顶点着色器过来的参数
in vec4 ambient;
in vec4 diffuse;
in vec4 specular;
in vec2 vTextureCoord;
out vec4 fragColor;
void main(){    
   //将计算出的颜色给此片元
   vec4 finalColor=texture(sTexture, vTextureCoord); 
   finalColor = vec4(finalColor.r,finalColor.g,finalColor.b,finalColor.a*uFactor);  
   //给此片元颜色值
   fragColor = finalColor*ambient+finalColor*specular+finalColor*diffuse;
}   