$(function() {
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
