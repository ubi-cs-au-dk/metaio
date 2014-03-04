#ifdef GL_ES
precision mediump float;
#endif

const int MAX_LIGHTS = 6;

uniform sampler2D metaio_tex_color;
uniform vec3 metaio_vec3_globalAmbientColor;
uniform int metaio_int_numLights;
uniform vec3 metaio_vec3v_lightPositionsModelView[MAX_LIGHTS];
uniform vec3 metaio_vec3v_lightAmbientColors[MAX_LIGHTS];
uniform vec3 metaio_vec3v_lightDiffuseColors[MAX_LIGHTS];
uniform vec3 metaio_vec3v_lightAttenuations[MAX_LIGHTS];
uniform float metaio_floatv_lightRadii[MAX_LIGHTS];
uniform int metaio_intv_lightTypes[MAX_LIGHTS];
uniform vec3 metaio_vec3v_lightDirectionsModelViewNorm[MAX_LIGHTS];

varying vec2 outTexCoord;
varying vec3 outNormal;
varying vec3 outVertexModelView;

void main()
{
	// Normalize again (not really necessary because interpolation only gives small error)
	vec3 normal = normalize(outNormal); // model-view space!

	// This vector sums up the accumulated light values
	vec3 lightColor = vec3(0, 0, 0);

	// If no lights are active, disable lighting
	if (metaio_int_numLights == 0)
		lightColor = vec3(1, 1, 1);

	for (int i = 0; i < metaio_int_numLights; ++i)
	{
		if (metaio_intv_lightTypes[i] == 1) // point light
		{
			vec3 pointToLight = metaio_vec3v_lightPositionsModelView[i] - outVertexModelView;
			float pointToLightLen = length(pointToLight);
			float dist = pointToLightLen * 0.001; // distance in meters
			vec3 L = pointToLight / pointToLightLen; // roughly equivalent to normalize(pointToLight)
			float diffuse = clamp(dot(normal, L), 0.0, 1.0);
			float att = 1.0 / (metaio_vec3v_lightAttenuations[i].x + metaio_vec3v_lightAttenuations[i].y * dist + metaio_vec3v_lightAttenuations[i].z * dist*dist);

			lightColor += metaio_vec3v_lightAmbientColors[i];
			lightColor += diffuse * metaio_vec3v_lightDiffuseColors[i] * att;
		}
		else if (metaio_intv_lightTypes[i] == 2) // spot light
		{
			vec3 lightToPoint = outVertexModelView - metaio_vec3v_lightPositionsModelView[i];
			float lightToPointLen = length(lightToPoint);
			vec3 lightToPointN = lightToPoint / lightToPointLen; // roughly equivalent to normalize(lightToPoint)

			float NdotL = dot(normal, -lightToPointN);

			// Ambient part is always added (TODO: is this okay? cut off / attenuate??)
			lightColor += metaio_vec3v_lightAmbientColors[i];

			// Don't go on if triangle not facing the light
			if (NdotL > 0.0)
			{
				// Angle between spot direction and light-to-vertex (used for spot cutoff)
				float cosAngle = dot(lightToPointN, metaio_vec3v_lightDirectionsModelViewNorm[i]);

				// Radius to smoothen on the outer cone (radians)
				float cosLightRadius = cos(metaio_floatv_lightRadii[i]);
				float smoothRadius = 0.1 * (1.0 - cosLightRadius);

				// Apply cutoff (only illuminate fragments inside spot cone)
				if (cosAngle >= cosLightRadius - smoothRadius)
				{
					float spotIntensity = clamp(cosAngle, 0.0, 1.0);
					float dist = lightToPointLen * 0.001; // distance in meters
					float att = 1.0 / (metaio_vec3v_lightAttenuations[i].x + metaio_vec3v_lightAttenuations[i].y * dist + metaio_vec3v_lightAttenuations[i].z * dist*dist);

					if (cosAngle < cosLightRadius) // within smoothing radius
					{
						// Smooth by linear interpolation on the outer "edge" of the spot cone
						att = mix(att, 0.0, (cosLightRadius - cosAngle) / smoothRadius);
					}

					lightColor += spotIntensity * NdotL * metaio_vec3v_lightDiffuseColors[i] * att;
				}
			} // end if (NdotL > 0.0)
		}
		else if (metaio_intv_lightTypes[i] == 0) // directional light
		{
			lightColor += metaio_vec3v_lightAmbientColors[i];
			lightColor += clamp(dot(-metaio_vec3v_lightDirectionsModelViewNorm[i], normal), 0.0, 1.0) * metaio_vec3v_lightDiffuseColors[i];
		}
	}

	vec3 calcColor = clamp(lightColor + metaio_vec3_globalAmbientColor, 0.0, 1.0);

	vec4 texColor = texture2D(metaio_tex_color, outTexCoord);

	// For debugging purposes:
	// Normals represented by color (0 => -1, 128 => 0, 255 => 1)
	// gl_FragColor.rgba=vec4(N/2+vec3(0.5,0.5,0.5),1);

	// Show light intensity and color only
	// gl_FragColor.rgba = vec4(calcColor, 1);

	// Lit result
	gl_FragColor.rgba = texColor.rgba * vec4(calcColor, 1);
}