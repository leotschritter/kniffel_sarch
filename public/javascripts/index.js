let playerID;
let timestamp;
let playersCount;
let alreadyStarted = false;

function setPlayerInSessionStorage(player, callback) {
    sessionStorage['player'] = JSON.stringify(player);

    if (sessionStorage['player'] === JSON.stringify(player)) {
        callback();
    } else {
        setTimeout(() => {
            console.log("Failed setting player %o in sessionStorage! Retrying...", player);
            setPlayerInSessionStorage(player, callback);
        }, 1000);
    }
}
function connectWebSocket(playerName) {
    const countdown = document.getElementById("countdown");
    const websocket = new WebSocket("ws://localhost:9000/lobbyWebsocket");

    websocket.onopen = function(event) {
        websocket.send(JSON.stringify({event: "newPlayer", name: playerName}));
        /*$.ajax({
            method: "GET", url: '/isRunning',
            success: function (data) {
              if (data.isRunning) {
                  websocket.send(JSON.stringify({"event": "clearAll"}));
              }
            }
        });*/
        console.log("Connected to Websocket");
    }

    websocket.onclose = function () {
        console.log('Connection with Websocket Closed!');
    };

    websocket.onerror = function (error) {
        console.log('Error in Websocket occurred: ' + error);
    };

    websocket.onmessage = (e) => {
        const numberOfPlayers = document.getElementById("numberOfPlayers");
        const playersReady = document.getElementById("playersReady");
        const data = JSON.parse(e.data);
        if (data.event === "updateTimeMessageEvent") {
            countdown.innerText = (60 - Math.floor(data.time / 1000)).toString()
            playersCount = data.numberOfPlayers;
            numberOfPlayers.innerText = playersCount;
            playersReady.innerText = data.readyCount;
            if ((data.readyCount === playersCount && playersCount > 1) || data.startGame) {
                websocket.send(JSON.stringify({event: "startGame", name: playerName, playerID: playerID}));
            }
        } else if (data.event === "newPlayerMessageEvent") {
            playerID = data.id;
            timestamp = data.timestamp;
            playersCount = data.numberOfPlayers;
            numberOfPlayers.innerText = playersCount;
            playersReady.innerText = data.readyCount;
        } else if (data.event === "readyMessageEvent") {
            playersReady.innerText = data.readyCount
        } else if (data.event === "newGameMessageEvent") {
            const playerData = { 'id': playerID, 'name': playerName, 'timestamp': timestamp };
            setPlayerInSessionStorage(playerData, () => {
                if (data.isInitiator) {
                    fetch('/new?players=' + data.players).then(() => {
                        window.location.href = '/kniffel'
                    });
                      /*  $.ajax({
                            method: "GET", url: '/new?players=' + data.players,
                            success: function () {
                                window.location.href = '/kniffel';
                            },
                            error: function(jqXHR, textStatus, errorThrown) {
                                console.error("Failed starting new game!");
                            }
                        });*/
                } else {
                    window.location.href = '/kniffel';
                }
            });
        }
    };
    return websocket;
}

function closeModal() {
    $('#confirmationDialog').modal('hide');
}

function setReadyButton() {
    const btnReady = document.getElementById("ready");
    btnReady.innerHTML = '';
    const spanCheck = document.createElement("span");
    spanCheck.classList.add("material-symbols-outlined");
    spanCheck.innerHTML = 'check';
    btnReady.style = 'background-color: green; border-color: green';
    btnReady.disabled = true;
    btnReady.appendChild(spanCheck);
}

function resetReadyButton() {
    const btnReady = document.getElementById("ready");
    btnReady.innerHTML = 'Ready';
    btnReady.style = '';
    btnReady.disabled = false;
}

async function closeSocketConnection(websocket, ready) {
    return new Promise((resolve, reject) => {
        websocket.send(JSON.stringify({event: "closeConnection", ready: ready, playerID: playerID}));
        function handleMessage(event) {
            const response = JSON.parse(event.data);
            resolve(response);
        }
        websocket.addEventListener('message', handleMessage);
    });
}
$(document).ready(function () {
    let ready = false;
    const playerName = document.getElementById("yourName");
    const btnJoin = document.querySelector(".startButton");
    const btnLeaveLobby = document.getElementById("leaveLobby");
    const btnConfirmLeaveLobby = document.getElementById("btnConfirm");
    const btnReady = document.getElementById("ready");

    fetch("/isRunning").then(data => data.json()).then((data) => {
        if (!data.isRunning) {
            playerName.addEventListener('keyup', () => {
                btnJoin.disabled = playerName.value === "";
            });
        } else {
            playerName.disabled = true;
            playerName.placeholder = 'Game is already running'
        }
    });

    let websocket;

    btnJoin.onclick = function () {
        websocket = connectWebSocket(playerName.value);
    }

    btnLeaveLobby.onclick = function () {
        $('#leaveLobbyConfirmationDialog').modal('show');
    }

    $('#confirmationDialog').on('hidden.bs.modal', function () {
        websocket.close()
    });

    btnConfirmLeaveLobby.onclick = function () {
        closeSocketConnection(websocket, ready).then(() => {
            if (ready) {
                ready = false;
                resetReadyButton();
            }
            closeModal();
        });
    }

    btnReady.onclick = function () {
        ready = true;
        setReadyButton();
        websocket.send(JSON.stringify({event: "ready", playerID: playerID}));
    }

    window.onbeforeunload = function (e) {
        closeSocketConnection(websocket, ready).then(() => {
            return confirm();
        })
    }
});