const loginbtn = document.getElementById('login');
const registerbtn = document.getElementById('register');
loginbtn.addEventListener("click", () => {
    window.location.href = "/login";
})
registerbtn.addEventListener("click", () => {
    window.location.href = "/register";
})
