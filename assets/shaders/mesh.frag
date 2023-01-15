#ifdef GL_ES
precision mediump float;
#endif

varying float v_ambientLight;
varying float v_blockLight;
varying float v_skyLight;
varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_texture;

void main() {
	vec4 fragColor = texture2D(u_texture, v_texCoords);
	fragColor *= v_color;
	if (fragColor.a <= 0.0) discard;
	fragColor.rgb *= v_ambientLight;
	gl_FragColor = fragColor;
}