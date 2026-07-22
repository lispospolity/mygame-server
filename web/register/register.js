const registerbtn = document.getElementById('register');

registerbtn.addEventListener('click', (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');

    const username = usernameInput.value;
    const email = emailInput.value;
    const passwd = passwordInput.value;
    register(username, passwd, email);
});


async function register(name, password, email) {
    const loading = document.getElementById("loading");
    loading.textContent = "Waiting for server...";
    const response = await fetch("http://192.168.1.30:9090/api/user", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username: name,
            password: password,
            email: email
        })
    });
    loading.textContent = "";
    const data = await response.json();
    if (data.error) {
        alert(data.error);
        return;
    }
    if (!data.success) {
        alert(data.message);
        return;
    }
    document.cookie = "email="+email+"; path=/auth"
    window.location.href = "/auth";
}

