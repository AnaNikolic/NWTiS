var aplikacija = "/" + document.location.pathname.split("/")[1];
var wsUri = "ws://" + document.location.host + aplikacija + "/infoPutnik";
wsocket = new WebSocket(wsUri);
wsocket.onmessage = onMessage; 

function onMessage(evt){
    var gumb = document.getElementById("glavnaForma:gumbPreuzmi");
    var putnici = document.getElementById("glavnaForma:putnik");
    var odabrani = putnici.options[putnici.selectedIndex].value;
    if (odabrani === evt.data) {
        gumb.click();
    } 
}