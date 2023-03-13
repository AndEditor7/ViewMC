#version 300 es
#ifdef GL_ES
precision mediump float;
#endif

in vec3 position;
in float ambientLight;
in float blockLight;
in float skyLight;
in vec2 texCoords;
in vec4 color;

out vec4 fragColor;

uniform sampler2D u_texture;
uniform sampler2D u_lightMap;
uniform vec4 u_fogColor;
uniform float u_fogStart;
uniform float u_fogEnd;

void main() {
	vec4 pixel = texture(u_texture, texCoords);
	pixel *= color;
	if (pixel.a < 0.01) discard;
	pixel.rgb *= ambientLight;
	float skyLight = skyLight * 0.95;
	pixel.rgb *= texture(u_lightMap, vec2(blockLight, skyLight)).rgb, vec3(min(blockLight+skyLight, 1.0));
	
	// float z = (gl_FragCoord.z/gl_FragCoord.w);
	float z = length(position.xz);
	float fogFactor = (u_fogEnd - z) / (u_fogEnd - u_fogStart);
	fragColor = mix(pixel, u_fogColor, clamp(fogFactor, 0.0, 1.0));
}