
arel.sceneReady(function()
{
	console.log("sceneReady");

	//set a listener to tracking to get information about when the image is tracked
	arel.Events.setListener(arel.Scene, function(type, param){trackingHandler(type, param);});

	// Check initial state of tracking (we could already be tracking without trackingHandler being
	// called because it was registered too late)
	arel.Scene.getTrackingValues(function(trackingValues) {
		if (trackingValues.length > 0)
		{
			if ((document.getElementById('radio4').checked))
				arel.Scene.getObject("movie").startMovieTexture();
		}
	});

	//load geometry
	var metaioman = arel.Object.Model3D.create("metaioman","Assets/metaioman.md2","Assets/metaioman.png");
	metaioman.setVisibility(true);
	metaioman.setCoordinateSystemID(1);
	metaioman.setScale(new arel.Vector3D(4.0,4.0,4.0));
	arel.Scene.addObject(metaioman);

	var truck = arel.Object.Model3D.create("truck","Assets/truck/truck.obj","Assets/truck/truck.png");
	truck.setVisibility(false);
	truck.setCoordinateSystemID(1);
	truck.setScale(new arel.Vector3D(2.0,2.0,2.0));
	var truckRotation = new arel.Rotation();
	truckRotation.setFromEulerAngleDegrees(new arel.Vector3D(90.0,0.0,180.0));
	truck.setRotation(truckRotation);
	arel.Scene.addObject(truck);

	var image = arel.Object.Model3D.createFromImage("image","Assets/frame.png");
	image.setVisibility(false);
	image.setCoordinateSystemID(1);
	image.setScale(new arel.Vector3D(3.0,3.0,3.0));
	arel.Scene.addObject(image);

	var movie = arel.Object.Model3D.createFromMovie("movie","Assets/demo_movie.alpha.3g2"); //add alpha here
	movie.setVisibility(false);
	movie.setCoordinateSystemID(1);
	movie.setScale(new arel.Vector3D(2.0,2.0,2.0));
	var movieRotation = new arel.Rotation();
	movieRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,-90.0));
	movie.setRotation(movieRotation);
	arel.Scene.addObject(movie);
});

function trackingHandler(type, param)
{
	//check if there is tracking information available
	if(param[0] !== undefined)
	{
		//if the pattern is found start the movie texture
		if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_TRACKING)
		{
			if (document.getElementById('radio4').checked)
				arel.Scene.getObject("movie").startMovieTexture();
		}
		//if the pattern is lost tracking pause the movie texture
		else if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
		{
            arel.Scene.getObject("movie").pauseMovieTexture();
		}
	}
};

function clickHandler()
{
	if (document.getElementById('radio1').checked)
	{
		arel.Scene.getObject("metaioman").setVisibility(true);
		arel.Scene.getObject("image").setVisibility(false);
		arel.Scene.getObject("truck").setVisibility(false);
		arel.Scene.getObject("movie").setVisibility(false);
		arel.Scene.getObject("movie").stopMovieTexture();
	}
	if (document.getElementById('radio2').checked)
	{
		arel.Scene.getObject("metaioman").setVisibility(false);
		arel.Scene.getObject("image").setVisibility(true);
		arel.Scene.getObject("truck").setVisibility(false);
		arel.Scene.getObject("movie").setVisibility(false);
		arel.Scene.getObject("movie").stopMovieTexture();
	}
	if (document.getElementById('radio3').checked)
	{
		arel.Scene.getObject("metaioman").setVisibility(false);
		arel.Scene.getObject("image").setVisibility(false);
		arel.Scene.getObject("truck").setVisibility(true);
		arel.Scene.getObject("movie").setVisibility(false);
		arel.Scene.getObject("movie").stopMovieTexture();
	}
	if (document.getElementById('radio4').checked)
	{
		arel.Scene.getObject("metaioman").setVisibility(false);
		arel.Scene.getObject("image").setVisibility(false);
		arel.Scene.getObject("truck").setVisibility(false);
		arel.Scene.getObject("movie").setVisibility(true);

		// Start playback if tracking
		arel.Scene.getTrackingValues(function(trackingValues) {
			if (trackingValues.length > 0)
			{
				if (document.getElementById('radio4').checked)
					arel.Scene.getObject("movie").startMovieTexture();
			}
		});
	}
};