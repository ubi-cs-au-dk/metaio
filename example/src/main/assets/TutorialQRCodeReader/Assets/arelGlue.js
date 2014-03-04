
arel.sceneReady(function()
{
	 arel.Scene.setTrackingConfiguration("QRCODE");
	 arel.Events.setListener(arel.Scene, function(type, param){trackingHandler(type, param);});
});

function trackingHandler(type, param)
{
	//check if there is tracking information available
	if(param[0] !== undefined)
	{
		//if the pattern is found, hide the information to hold your phone over the pattern
		if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_TRACKING)
		{
             arel.Scene.getTrackingValues(function(tv){receiveCurrentTrackingValues(tv);});
		}
		//if the pattern is lost tracking, show the information to hold your phone over the pattern
		else if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
		{

		}
	}
};

function receiveCurrentTrackingValues(tv)
{
    if(tv[0] !== undefined)
    {
		window.alert(tv[0].getContent());
    }

};


