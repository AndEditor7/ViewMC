#version 100
#ifdef GL_ES
precision mediump float;
#endif

attribute vec4 a_position;

varying float yPos;

uniform mat4 projTrans;

void main()
{
	yPos = a_position.y;
	gl_Position = projTrans * a_position;
}
