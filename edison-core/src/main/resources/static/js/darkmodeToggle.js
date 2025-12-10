const toggleButton = document.getElementById('darkModeToggle');

if (toggleButton) {
    const html = document.documentElement;

    toggleButton.addEventListener('click', function () {
        const currentTheme = html.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        html.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);
    });
}
