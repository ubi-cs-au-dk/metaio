arel.sceneReady(function()
{
	console.log("sceneReady");

	var munich = new arel.LLA(48.142573, 11.550321, 0);
	var london = new arel.LLA(51.50661, -0.130463, 0);
	var tokyo = new arel.LLA(35.657464, 139.773865, 0);
	var paris = new arel.LLA(48.85658, 2.348671, 0);
	var rome = new arel.LLA(41.90177, 12.45987, 0);

	arel.Scene.getObject("1").setLocation(munich);
    arel.Scene.getObject("1").setVisibility(true,false,true);
	arel.Scene.getObject("1").setLLALimitsEnabled(true);

	arel.Scene.setLLAObjectRenderingLimits(80, 2500);

	createPOIGeometry("2", "London", london);
	createPOIGeometry("3", "Tokyo", tokyo);
	createPOIGeometry("4", "Paris", paris);
	createPOIGeometry("5", "Rome", rome);
});

function createPOIGeometry(id, title, location)
{
	var newPOI = new arel.Object.POI();
	newPOI.setID(id);
	newPOI.setTitle(title);
	newPOI.setLocation(location);
	newPOI.setThumbnail("");
	newPOI.setIcon("");
	newPOI.setVisibility(true,false,true);
	arel.Scene.addObject(newPOI);
}