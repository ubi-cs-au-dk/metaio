arel.sceneReady(function()
{
	console.log("sceneReady");

	//get the metaio man model reference
	var metaioman = arel.Scene.getObject("1");

	arel.Events.setListener(metaioman, function(id, type, args) {
		console.log("type = "+type+"; args="+JSON.stringify(args));
	});
});