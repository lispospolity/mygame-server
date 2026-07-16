const name = document.getElementById('name');
const password = document.getElementById('password');
const loginbtn = document.getElementById('login');
loginbtn.addEventListener('click', (e) => {
    e.preventDefault();
    const username = name.value;
    const passwd = password.value;
    login(username, passwd);
});

async function login(name, password) {
    const loading = document.getElementById("loading");
    loading.textContent = "Waiting for server...";
    const response = await fetch("http://192.168.1.30:9090/api/session", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password}) });
    const data = await response.json();
    //console.log(data)
    loading.textContent = "";
    if (data.error) {
        alert(data.error);
        return;
    }
    if (data.success == true) {
        document.cookie = "token="+data.token+"; path=/";
	    window.location.href = "/game";
    } else {
        alert(data.message);
    }
}
window.logout = async function(token) {
response = await fetch("http://192.168.1.30:9090/api/session", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ token: token}) });
const data = await response.json();
//console.log(data);
}

window.deluser = async function(name, password, token) {
response = await fetch("http://192.168.1.30:9090/api/user", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password, token: token}) });
const data = await response.json();
//console.log(data);
}







