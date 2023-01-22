#ifdef GL_ES
precision highp float;
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec4 a_color;
attribute vec4 a_data;

uniform mat4 u_projTrans;
uniform vec3 u_camPos;
uniform vec2 u_factPos;

varying vec3 v_position;
varying float v_ambientLight;
varying float v_blockLight;
varying float v_skyLight;
varying vec2 v_texCoords;
varying vec4 v_color;

void main() {
	v_ambientLight = a_data.r;
	v_blockLight = a_data.g;
	v_skyLight = a_data.b;
	v_texCoords = a_texCoord0;
	v_color = a_color;
	v_position = a_position.xyz;
	
	vec3 pos = (a_position.xyz - u_camPos);
	pos.xz += u_factPos.xy;
	gl_Position = u_projTrans * vec4(pos, a_position.w);
}