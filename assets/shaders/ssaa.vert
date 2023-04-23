#version 300 es
#ifdef GL_ES
precision highp float;
#endif

in vec4 a_position;

out vec2 v_position;

void main()
{
	v_position = (a_position.xy + 1.0) * 0.5;
	gl_Position = a_position;
}