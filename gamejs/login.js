const name = document.getElementById('name');
const password = document.getElementById('password');
const loginbtn = document.getElementById('login');
const registerbtn = document.getElementById('register');
loginbtn.addEventListener('click', (e) => {
    e.preventDefault();
    const username = name.value;
    const passwd = password.value;
    login(username, passwd);
});
registerbtn.addEventListener('click', (e) => {
    e.preventDefault();
    const username = name.value;
    const passwd = password.value;
    register(username, passwd);
});


async function login(name, password) {
    const response = await fetch("http://192.168.1.30:9090/api/session", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password}) });
    const data = await response.json();
    //console.log(data)
    if (data.error) {
        alert(data.error);
        return;
    }
    if (data.success == true) {
        localStorage.setItem("token", data.token)
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
async function register(name, password) {
response = await fetch("http://192.168.1.30:9090/api/user", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password}) });
const data = await response.json();
    if (data.error) {
        alert(data.error);
        return;
    }
    alert(data.message)
}
window.deluser = async function(name, password, token) {
response = await fetch("http://192.168.1.30:9090/api/user", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: name, password: password, token: token}) });
const data = await response.json();
//console.log(data);
}







