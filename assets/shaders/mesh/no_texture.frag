#version 300 es
#ifdef GL_ES
precision mediump float;
#endif

in float ambientLight;
in float blockLight;
in float skyLight;

out vec4 fragColor;

uniform sampler2D u_lightMap;

void main() {
	vec4 pixel = vec4(1.0);
	pixel.rgb *= ambientLight;
	float skyLight = skyLight * 0.95;
	pixel.rgb *= texture(u_lightMap, vec2(blockLight, skyLight)).rgb, vec3(min(blockLight+skyLight, 1.0));
	fragColor = pixel;
}
