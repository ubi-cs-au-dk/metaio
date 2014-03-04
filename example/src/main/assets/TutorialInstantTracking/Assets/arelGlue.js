var timer;
var timer_is_on=0;
var isCloseToTiger;
var tigerAnimations = ["meow", "scratch", "look", "shake", "clean"];
var preview = true;

arel.sceneReady(function()
{
    console.log("sceneReady");
	isCloseToTiger = false;
});

function onAnimationEnd(arelObject, type, param)
{
	if (type == arel.Events.Object.ONANIMATIONENDED)
	{
		var randomIndex = parseInt(Math.random()*tigerAnimations.length);
		arelObject.startAnimation(tigerAnimations[randomIndex]);
	}
};


function clickHandler()
{
    
	clearInterval(timer);
    
    if (document.getElementById('radio1').checked)
    {
        var modelRotation = new arel.Rotation();
        modelRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,180.0));
        arel.Scene.getObject("1").setRotation(modelRotation);
        arel.Scene.startInstantTracking(arel.Tracking.INSTANT2D, preview);
        document.getElementById('radio2').disabled = preview;
        document.getElementById('radio3').disabled = preview;
        document.getElementById('radio1').checked  = !preview;
        preview = !preview;
    }
    if (document.getElementById('radio2').checked)
    {
		if (navigator.platform != 'Win32')
		{
			var modelRotation = new arel.Rotation();
			modelRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,-180.0));
			arel.Scene.getObject("1").setRotation(modelRotation);
			arel.Scene.startInstantTracking(arel.Tracking.INSTANT2DG, preview);
            document.getElementById('radio1').disabled = preview;
            document.getElementById('radio3').disabled = preview;
            document.getElementById('radio2').checked  = !preview;
            preview = !preview;
		} else {
			alert('This option is not available on Windows because this platforms lacks the needed sensors.');
		}
    }
    if (document.getElementById('radio3').checked)
    {
        arel.Scene.startInstantTracking(arel.Tracking.INSTANT3D);
    }
    
    arel.Scene.getObject("1").startAnimation("meow");
    timer = setInterval(function(){arel.Scene.getTrackingValues(function(tv){receiveCurrentTrackingValues(tv);});}, 1000);
    
	//set a listener to receive animation events
    arel.Events.setListener(arel.Scene.getObject("1"), function(arelObject, type, param){onAnimationEnd(arelObject, type, param);});
    
};

function receiveCurrentTrackingValues(tv)
{
    if(tv[0] !== undefined)
    {
        var quality = tv[0].getQuality();
        if (parseFloat(quality) > 0.0)
        {
        
            var poseTranslation = tv[0].getTranslation();
            var threshold = 200.0;
            var distanceToTarget = Math.sqrt(poseTranslation.getX() * poseTranslation.getX() + poseTranslation.getY() * poseTranslation.getY() +poseTranslation.getZ() * poseTranslation.getZ());
    
            if(parseFloat(distanceToTarget) < threshold)
            {
            	if (!isCloseToTiger)
            	{
            
            		isCloseToTiger = true;
            		
	            	if (navigator.platform == 'Win32')
	            	{
	                	arel.Media.startSound("Assets/meow.aif");
	                }
	                else
	                {
	                	arel.Media.startSound("Assets/meow.mp3");
	                }
	                
	                arel.Scene.getObject("1").setAnimationSpeed(60);
	                arel.Scene.getObject("1").startAnimation("tap");
				}
				
            }
            else
			{
				isCloseToTiger = false;
			}
        
        }
    }
    
};