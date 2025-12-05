const toggleButton = document.getElementById('darkModeToggle');
const icon = document.querySelector('#darkModeToggle i');

if (toggleButton && icon) {
    const html = document.documentElement;

    // Priority: 1. Saved setting, 2. Browser preference, 3. Light as general default
    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');

    html.setAttribute('data-bs-theme', initialTheme);
    updateIcon(initialTheme);

    toggleButton.addEventListener('click', function () {
        const currentTheme = html.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        html.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateIcon(newTheme);
    });
}

function updateIcon(theme) {
    if (theme === 'dark') {
        icon.classList.remove('bi-moon');
        icon.classList.add('bi-sun');
    } else {
        icon.classList.remove('bi-sun');
        icon.classList.add('bi-moon');
    }
}

