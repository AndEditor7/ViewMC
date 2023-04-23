#version 300 es
#ifdef GL_ES
precision mediump float;
#endif

in float light;
in vec2 texCoords;
in vec4 color;

out vec4 fragColor;

uniform sampler2D u_texture;

void main() {
	vec4 pixel = texture(u_texture, texCoords);
	pixel *= color;
	if (pixel.a < 0.01) discard;
	pixel.rgb *= light;
	
	fragColor = pixel;
}
