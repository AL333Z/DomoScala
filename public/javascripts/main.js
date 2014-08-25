$(function() {

	var template = "{{#buildings}}	\
		<div class='{{buildingId}}'>	\
			<h1>{{buildingId}}</h1>	\
			{{#rooms}}	\
			<h2>{{roomId}}</h2>	\
		    <ul class='list-group {{roomId}}'>	\
		    	{{#devices}}	\
				<li class='list-group-item {{deviceId}}'>	\
			    	<span class='badge'>{{status}}</span>	\
			    	{{deviceId}}	\
			   </li>\
			   {{/devices}}\
			</ul>\
			{{/rooms}}\
		</div>\
		{{/buildings}}";

	// download initial buildings structure
	var url = jsRoutes.controllers.Application.getBuildings()
	$.getJSON(url.absoluteURL(), function(json) {

		var buildings = [];
		$.each(json.buildings, function(buildingIdx, building) {
			console.log("Building: " + building.id);

			var rooms = [];
			$.each(building.rooms, function(roomIdx, room) {
				console.log("	Room: " + room.id);

				var devices = [];
				$.each(room.devices, function(deviceIdx, device) {
					console.log("Device: " + deviceIdx + " actor: " + device);

					var d = {};
					d["deviceId"] = deviceIdx;
					d["device"] = device

					// TODO put here some status information, when ready ;)
					d["status"] = "On";

					devices.push(d);
				});

				var r = {};
				r["roomId"] = room.id;
				r["devices"] = devices;

				rooms.push(r);
			});

			var b = {};
			b["buildingId"] = building.id;
			b["rooms"] = rooms;

			buildings.push(b);

			var data = {};
			data["buildings"] = buildings;

			// console.log(data);
			// console.log(Mustache.render(template, data));

			$("#buildings").html(Mustache.render(template, data));

		});
	});

	// TODO register for web socket push events and update the view with new
	// values
	 var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
	 var b = "Building0";
	 var r = "Room1";
	 var s = "LightSensor0";
	
	 var r = jsRoutes.controllers.Application.reqPushDeviceStatus(b, r, s);
	 var ws = new WebSocket(r.webSocketURL());
	
	 var receiveEvent = function(event) {
	 $("#data").append("Last data: " + event.data + "<br />");
	 }
	 ws.onmessage = receiveEvent;
});
