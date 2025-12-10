(function() {
    try {
        // Priority: 1. Saved setting, 2. Browser preference, 3. Light as general default
        const savedTheme = localStorage.getItem('theme');
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');

        document.documentElement.setAttribute('data-bs-theme', initialTheme);

    } catch(e) {}
})();

