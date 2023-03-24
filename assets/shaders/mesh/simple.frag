#version 300 es
#ifdef GL_ES
precision mediump float;
#endif

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
	fragColor = pixel;
}