arel.sceneReady(function()
{
	// Comment this in if you want to enable the debug console
	// arel.Debug.activate();

	console.log("sceneReady");

	// Get reference to the 3D model
	var model = arel.Scene.getObject("1");

	// Add a little global illumination
	arel.Scene.setAmbientLight(new arel.Vector3D(0.05, 0.05, 0.05));

	// Create lights
	var directionalLight = arel.Object.Light.createLight("light1");
	directionalLight.setLightType(arel.Light.LIGHT_TYPE_DIRECTIONAL);
	directionalLight.setAmbientColor(new arel.Vector3D(0, 0.15, 0)); // slightly green
	directionalLight.setDiffuseColor(new arel.Vector3D(0.6, 0.2, 0)); // orange
	directionalLight.setCoordinateSystemID(1);
	var directionalLightGeo = createLightGeometry("lightgeo1");
	directionalLightGeo.setCoordinateSystemID(directionalLight.getCoordinateSystemID());
	directionalLightGeo.setDynamicLightingEnabled(false);

	var pointLight = arel.Object.Light.createLight("light2");
	pointLight.setLightType(arel.Light.LIGHT_TYPE_POINT);
	pointLight.setAmbientColor(new arel.Vector3D(0, 0, 0.15)); // slightly blue ambient
	pointLight.setAttenuation(new arel.Vector3D(0, 0, 40));
	pointLight.setDiffuseColor(new arel.Vector3D(0, 0.8, 0.05)); // green-ish
	pointLight.setCoordinateSystemID(1);
	var pointLightGeo = createLightGeometry("lightgeo2");
	pointLightGeo.setCoordinateSystemID(pointLight.getCoordinateSystemID());
	pointLightGeo.setDynamicLightingEnabled(false);

	var spotLight = arel.Object.Light.createLight("light3");
	spotLight.setAmbientColor(new arel.Vector3D(0.17, 0, 0)); // slightly red ambient
	spotLight.setLightType(arel.Light.LIGHT_TYPE_SPOT);
	spotLight.setRadiusDegrees(10);
	spotLight.setDiffuseColor(new arel.Vector3D(1, 1, 0)); // yellow
	spotLight.setCoordinateSystemID(1);
	var spotLightGeo = createLightGeometry("lightgeo3");
	spotLightGeo.setCoordinateSystemID(spotLight.getCoordinateSystemID());
	spotLightGeo.setDynamicLightingEnabled(false);

	var lightsReady = 0
	function lightObjectListener(obj, type, param)
	{
		if (type == arel.Events.Object.ONREADY)
		{
			++lightsReady;

			// All lights loaded and initialized with their properties? Then start our animation.
			if (lightsReady == 3)
			{
				setInterval(updateLightProperties, 20);
			}
		}
	}
	arel.Events.setListener(directionalLight, lightObjectListener);
	arel.Events.setListener(pointLight, lightObjectListener);
	arel.Events.setListener(spotLight, lightObjectListener);

	arel.Scene.addObject(directionalLight);
	arel.Scene.addObject(directionalLightGeo);
	arel.Scene.addObject(pointLight);
	arel.Scene.addObject(pointLightGeo);
	arel.Scene.addObject(spotLight);
	arel.Scene.addObject(spotLightGeo);

	function updateLightIndicator(indicatorGeo, light)
	{
		indicatorGeo.setVisibility(light.isEnabled());

		if (!light.isEnabled())
			return;

		if (light.getLightType() == arel.Light.LIGHT_TYPE_DIRECTIONAL)
		{
			var dir = light.getDirection().normalized();

			// Indicate "source" of directional light (not really the source because it's infinite)
			indicatorGeo.setTranslation(new arel.Vector3D(-200.0 * dir.x, -200.0 * dir.y, -200.0 * dir.z));
		}
		else
			indicatorGeo.setTranslation(light.getTranslation());
	}

	function updateLightProperties()
	{
		//console.log("directionalLight="+directionalLight);
		// Lights circle around
		var time = new Date().getTime() / 1000.0;
		var lightPos = new arel.Vector3D(200*Math.cos(time), 120*Math.sin(0.25*time), 200*Math.sin(time));

		var FREQ2MUL = 0.4;
		var lightPos2 = new arel.Vector3D(150*Math.cos(FREQ2MUL*2.2*time) * (1 + 2+2*Math.sin(FREQ2MUL*0.6*time)), 30*Math.sin(FREQ2MUL*0.35*time), 150*Math.sin(FREQ2MUL*2.2*time));

		var directionalLightDir = new arel.Vector3D(Math.cos(1.2*time), Math.sin(0.25*time), Math.sin(0.8*time));


		// This will only apply in the upcoming frame:

		// Directional light
		directionalLight.setDirection(directionalLightDir);
		updateLightIndicator(directionalLightGeo, directionalLight);

		// Point light
		pointLight.setTranslation(lightPos);
		updateLightIndicator(pointLightGeo, pointLight);

		// Spot light
		spotLight.setTranslation(lightPos2);
		spotLight.setDirection(new arel.Vector3D(-lightPos2.x, -lightPos2.y, -lightPos2.z)); // spot towards origin of COS
		updateLightIndicator(spotLightGeo, spotLight);
	}
});

function createLightGeometry(id)
{
	return arel.Object.Model3D.create(id, "Assets/sphere_10mm.obj");
}