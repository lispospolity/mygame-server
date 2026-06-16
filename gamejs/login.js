const name = document.getElementById('name');
const password = document.getElementById('password');
const loginbtn = document.getElementById('login');
const registerbtn = document.getElementById('register');
loginbtn.addEventListener('click', () => {
  const username = name.value;
  const passwd = password.value;
  login(username, passwd);
});
registerbtn.addEventListener('click', () => {
  const username = name.value;
  const passwd = password.value;
  register(username, passwd);
});


async function login(name, password) {
response = await fetch("http://localhost:9090/api/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password}) });
const data = await response.json();
if (data.success == true) {
	localStorage.setItem("token", data.token)
	console.log(data.token);
	window.location.href = "/game";
}
}
window.logout = async function(token) {
response = await fetch("http://localhost:9090/api/session", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ token: token}) });
const data = await response.json();
console.log(data);
}
async function register(name, password) {
response = await fetch("http://localhost:9090/api/register", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password}) });
const data = await response.json();
console.log(data);
}
window.deluser = async function(name, password, token) {
response = await fetch("http://localhost:9090/api/user", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password, token: token}) });
const data = await response.json();
console.log(data);
}







