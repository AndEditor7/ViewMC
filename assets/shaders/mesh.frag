#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_position;
varying float v_ambientLight;
varying float v_blockLight;
varying float v_skyLight;
varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_texture;
uniform sampler2D u_lightMap;
uniform vec3 u_camPos;
uniform vec4 u_fogColor;
uniform float u_start;
uniform float u_end;

void main() {
	vec4 fragColor = texture2D(u_texture, v_texCoords);
	fragColor *= v_color;
	if (fragColor.a < 0.1) discard;
	fragColor.rgb *= v_ambientLight;
	float skyLight = v_skyLight * 0.96;
	fragColor.rgb *= mix(texture2D(u_lightMap, vec2(v_blockLight, skyLight)).rgb, vec3(min(v_blockLight+skyLight, 1.0)), 0.04);
	
	// float z = (gl_FragCoord.z/gl_FragCoord.w);
	float z = length(v_position.xz - u_camPos.xz);
	float fogFactor = (u_end - z) / (u_end - u_start);
	gl_FragColor = mix(fragColor, u_fogColor, clamp(fogFactor, 0.0, 1.0));
}