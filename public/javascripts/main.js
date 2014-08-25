$(function() {
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
	var dateSocket = new WS("ws://localhost:9000/Building0/Room1/LightSensor0/push");

	var receiveEvent = function(event) {
		$("#data").append("Last data: " + event.data + "<br />");
	}
	dateSocket.onmessage = receiveEvent;
});
