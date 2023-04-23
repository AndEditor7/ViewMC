#version 300 es
#ifdef GL_ES
precision highp float;
#endif

in vec2 v_position;

out vec4 fragColor;

uniform sampler2D u_texture;
uniform vec2 u_dim; // The scaled texture dimension
uniform int u_gridSize;

const vec4 gamma = vec4(2.0);

void main()
{
	vec2 offset = 0.5 / u_dim;
	
	vec4 color;
	for (int x = 0; x < u_gridSize; x++)
	for (int y = 0; y < u_gridSize; y++) {
		color += pow(texture(u_texture, v_position + (vec2(x, y) / u_dim) + offset), 1.0/gamma);
	}
	
	fragColor = pow(color / vec4(u_gridSize * u_gridSize), gamma);
}