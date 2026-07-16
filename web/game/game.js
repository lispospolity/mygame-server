let ws;
let grid;
const keyMap = { "ArrowUp": 1, "ArrowDown": 2, "ArrowLeft": 3, "ArrowRight": 4, "w":5, "s":6, "a":7, "d":8 };
console.log("game.js open")
const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");
const TILE_SIZE = 32;
const players = {};
init();
async function init() {
	await loadworld();
	ws = new WebSocket("ws://192.168.1.30:9080");
	ws.onopen = () => ws.send(JSON.stringify({ a: getCookie("token"), t:"0" }));
	//ws catchers beg
	ws.onmessage = (e) => {
		const data = JSON.parse(e.data);
		//console.log(data);
		if (data.t === "200") {
			renderplayer(data);
			return;
		}
		if (data.t === "201") {

			deletePlayer(data);
			return;
		}
		if (data.t === "202") {
			movePlayer(data);
			return;
		}
	}
	//ws catchers end

	//ping
	setInterval(() => {
		ws.send(JSON.stringify({ t: "5" }));
	}, 3000);

	window.onbeforeunload = () => {
		ws.close();
	};
}
async function loadworld() {
	const response = await fetch("/world.txt");
	const world = await response.text();
	let worldtxt = world.split("\n").map(line => line.trim())
	grid = [];
	for (let y = 0; y < worldtxt.length; y++) {
		grid[y] = [];
		for (let x = 0; x < worldtxt[y].length; x++) {
			grid[y][x] = parseInt(worldtxt[y][x]);
		}
	}
	for (let y = 0; y < grid.length; y++) {
		for (let x = 0; x < grid[y].length; x++) {
			ctx.fillStyle = grid[y][x] === 1 ? "green" : "black";
			ctx.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		}
	}
	return;
}
//handlers for ws events
function renderplayer(data) {
	ctx.fillStyle = "red";
	ctx.fillRect(data.x * TILE_SIZE, data.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	players[data.unm] = {x:data.x, y:data.y};
}
function deletePlayer(data) {
	ctx.fillStyle = grid[players[data.unm].y][players[data.unm].x] === 1 ? "green" : "black";
	ctx.fillRect(players[data.unm].x * TILE_SIZE, players[data.unm].y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	delete players[data.unm];
}
function movePlayer(data) {
	let oldX = players[data.unm].x;
	let oldY = players[data.unm].y;
	players[data.unm] = {x:data.x, y:data.y}
	if (!isPlayerAt(oldX, oldY)) {
		ctx.fillStyle = grid[oldY][oldX] === 1 ? "green" : "black";
		ctx.fillRect(oldX * TILE_SIZE, oldY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
	}
	ctx.fillStyle = "red";
	ctx.fillRect(data.x * TILE_SIZE, data.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
}
function isPlayerAt(x, y) {
	for (let name in players) {
		if (players[name].x == x && players[name].y == y) return true;
	}
	return false;
}

function getCookie(name) {
    const cookies = document.cookie.split("; ");
    for (const cookie of cookies) {
        const [key, value] = cookie.split("=");

        if (key === name) {
            return decodeURIComponent(value);
        }
    }
    return null;
}
document.addEventListener("keydown", (e) => {
	//console.log(e);
	const k = keyMap[e.key];
	if (k) ws.send(JSON.stringify({t:"1", k:k}))
});

//ws senders beg

//ws senders end


