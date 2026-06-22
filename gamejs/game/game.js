let ws;
console.log("game.js open")
const canvas = document.getElementById("game");
const ctx = canvas.getContext("2d");
const TILE_SIZE = 32;
init();
async function init() {
	await loadworld();
	ws = new WebSocket("ws://192.168.1.30:9080");
	ws.onopen = () => ws.send(JSON.stringify({ a: localStorage.getItem("token"), t:"0" }));
	//ws catchers beg
	ws.onmessage = (e) => {
		const data = JSON.parse(e.data);
		console.log(data);
		if (data.t == "200") {
			renderplayer(data)
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
	const grid = [];
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
}
document.addEventListener("keydown", (e) => {
	console.log(e);
	ws.send(JSON.stringify({t:"1", k:e.keyCode}))
});

//ws senders beg

//ws senders end


