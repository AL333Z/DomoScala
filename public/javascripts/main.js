$(function() {

	var template = "{{#buildings}}	\
		<div class='{{buildingId}}'>	\
			<h1>{{buildingId}}</h1>	\
			{{#rooms}}	\
			<h2>{{roomId}}</h2>	\
		    <ul class='list-group {{roomId}}'>	\
		    	{{#devices}}	\
				<li class='list-group-item {{deviceId}}'>	\
			    	<span class='badge' id='{{deviceId}}'>{{status}}</span>	\
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
					console.log("		Device: " + deviceIdx + " actor: " + device);

					var d = {};
					d["deviceId"] = device.id;
					d["device"] = device.devType;

					// TODO put here initial status
					d["status"] = "";

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

	// values
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
	var r = jsRoutes.controllers.Application.reqPushSystemStatus();
	var ws = new WebSocket(r.webSocketURL());

	var receiveEvent = function(event) {
		
		console.log("Received event: " + event.data);
		
		$("#data").append("Last data: " + event.data + "<br />");
		var obj = $.parseJSON(event.data);
		$("#"+obj.deviceId).html(obj.um + ": " + obj.status.value);
		
	}
	ws.onmessage = receiveEvent;
});
