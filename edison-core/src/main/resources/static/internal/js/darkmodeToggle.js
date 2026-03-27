const toggleButton = document.getElementById('darkModeToggle');

if (toggleButton) {
    const html = document.documentElement;
    const icon = toggleButton.querySelector('i');

    toggleButton.addEventListener('click', function () {
        const currentTheme = html.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        html.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);

        if (icon) {
            icon.className = newTheme === 'dark' ? 'bi bi-moon-stars-fill' : 'bi bi-sun-fill';
        }

        const savedTheme = localStorage.getItem('theme') || 'light';
        const initialClass = savedTheme === 'dark' ? 'bi bi-moon-stars-fill' : 'bi bi-sun-fill';
        if (icon) {
            icon.className = initialClass;
        }
    });
}
