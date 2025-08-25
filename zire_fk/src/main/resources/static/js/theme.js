// theme.js

document.addEventListener("DOMContentLoaded", () => {
    const toggleButton = document.getElementById("mode-toggle");
    const icon = document.getElementById("mode-icon");
    const passwordToggle = document.getElementById("togglePassword");
    const passwordInput = document.getElementById("password");

    // Theme toggle varsa
    if (toggleButton && icon) {
        toggleButton.addEventListener("click", () => {
            const body = document.body;

            if (body.classList.contains("dark-mode")) {
                body.classList.remove("dark-mode");
                body.classList.add("light-mode");

                icon.classList.remove("fa-sun");
                icon.classList.add("fa-moon");

                setTheme("light");
            } else {
                body.classList.remove("light-mode");
                body.classList.add("dark-mode");

                icon.classList.remove("fa-moon");
                icon.classList.add("fa-sun");

                setTheme("dark");
            }
        });
    }

    function setTheme(theme) {
        fetch("/set-theme?theme=" + theme, { method: "POST" });
    }

    // Password toggle varsa
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener("click", () => {
            const type = passwordInput.getAttribute("type") === "password" ? "text" : "password";
            passwordInput.setAttribute("type", type);
        });
    }
});
