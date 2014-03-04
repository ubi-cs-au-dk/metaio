#ifdef GL_ES
precision highp float;
#endif

uniform mat4 metaio_mat4_modelViewProjection;
uniform mat4 metaio_mat4_modelView;
uniform mat4 metaio_mat4_view;
uniform mat3 metaio_mat3_normalMatrix;
uniform int metaio_int_numLights;

attribute vec4 inVertex; // position in object space
attribute vec3 inNormal; // vertex normal
attribute vec2 inTexCoord; // UV coordinates

varying vec2 outTexCoord;
varying vec3 outNormal;
varying vec3 outVertexModelView;

void main()
{
	outVertexModelView = vec3(metaio_mat4_modelView * inVertex);
	outNormal = normalize(metaio_mat3_normalMatrix * inNormal);

	outTexCoord = inTexCoord.xy;
	gl_Position = metaio_mat4_modelViewProjection * inVertex;
}