#version 300 es
#ifdef GL_ES
precision highp float;
#endif

in vec4 a_position;
in vec2 a_texCoord0;
in vec4 a_color;
in vec4 a_data;

uniform mat4 u_projTrans;
uniform vec3 u_camPos;
uniform vec2 u_factPos;

out vec3 position;
out float ambientLight;
out float blockLight;
out float skyLight;
out vec2 texCoords;
out vec4 color;

void main() {
	ambientLight = a_data.r;
	blockLight = a_data.g;
	skyLight = a_data.b;
	texCoords = a_texCoord0;
	color = a_color;
	
	vec3 pos = (a_position.xyz - u_camPos);
	pos.xz += u_factPos.xy;
	position = pos;
	gl_Position = u_projTrans * vec4(pos, a_position.w);
}