#version 100
#ifdef GL_ES
precision mediump float;
#endif

varying float yPos;

uniform vec4 fogColor;
uniform vec4 skyColor;

void main()
{
	gl_FragColor = mix(fogColor, skyColor, vec4(clamp((yPos*6.0)-0.25, 0.0, 1.0)));
}
