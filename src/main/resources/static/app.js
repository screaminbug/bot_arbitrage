var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/traderesults', function (traderesponse) {
            showTradeResults(JSON.parse(traderesponse.body).message, JSON.parse(traderesponse.body).finished);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function startArb() {
    stompClient.send(
    "/app/simulatetrade",
    {},
    JSON.stringify({
        "exchanges" : ["KRAKEN", "GDAX", "BITTREX", "BINANCE", "POLONIEX", "GEMINI"],
        "currencyPairs" : ["BTC/USD"],
        "loopIterations" : 20,
        "timeInterval" : 5000,
        "arbMargin" : 0.03,
        "valueBase" : 0.2
    }));
}

function showTradeResults(message, finished) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
    if (finished) { disconnect(); }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { startArb(); });
});