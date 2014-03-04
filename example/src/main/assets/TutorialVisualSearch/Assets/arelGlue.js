var request;
var foundPattern;

arel.sceneReady(function()
{
	console.log("sceneReady");

	//set a listener to tracking to get information about when the image is tracked
	arel.Events.setListener(arel.Scene, function(type, param){sceneHandler(type, param);});
	request = true;

	foundPattern = document.createTextNode("");
	document.body.appendChild(foundPattern);

	setInterval(function(){requestSearch();}, 1000);
});

function requestSearch()
{
	if (request) 
	{
		arel.Scene.requestVisualSearch("sdktest", true);
		request = false;
	}	
}

function sceneHandler(type, param)
{
	if(type && type == arel.Events.Scene.ONVISUALSEARCHRESULT)
	{
		if(param.length > 0)
		{
			var metaioman = arel.Object.Model3D.create("metaioman","Assets/metaioman.md2","Assets/metaioman.png");
			metaioman.setVisibility(true);
			metaioman.setScale(new arel.Vector3D(4.0,4.0,4.0));
			metaioman.setCoordinateSystemID(1);
			arel.Scene.addObject(metaioman);
			
			var imageName = param[0].toString();
			var patternName = imageName.substring(0, imageName.indexOf("."));
			foundPattern.nodeValue = patternName;
			arel.Scene.setTrackingConfiguration(param[0]);	
		}
		else request = true;
	}

	if(type && type == arel.Events.Scene.ONTRACKING && param[0] !== undefined)	
	{
		if  (param[0].getState() == arel.Tracking.STATE_TRACKING)
			request = false;
		else if (param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
			request = true;
	}
};

