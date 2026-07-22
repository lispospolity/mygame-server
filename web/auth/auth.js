const emailInput = document.getElementById('email');
const codeInput = document.getElementById('code');
const authBtn = document.getElementById('submit');

authBtn.addEventListener("click", () => {
    let email = emailInput.value;
    const code = codeInput.value;
    if (email === "") {
        if (getCookie("email") === null) {
            alert("email cannot be empty");
            return;
        } else {
            email = getCookie("email");
        }
    }
    if (code === "") {
        alert("code cannot be empty")
        return;
    }
    authorize(code, email);
})

async function authorize(code, email) {
    const response = await fetch("http://192.168.1.30:9090/api/auth/user", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email: email,
            code: code
        })
    });
    const data = await response.json();
    if (data.error) {
        alert(data.error);
        return;
    }
    if (!data.success) {
        alert(data.message);
        return;
    }
    if (data.token) {
        document.cookie = "token="+data.token+"; path=/";
        document.cookie = "email=; max-age=0; path=/auth";
        window.location.href = "/game";
        return;
    }
    document.cookie = "email=; max-age=0; path=/auth";
    window.location.href = "/login";
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