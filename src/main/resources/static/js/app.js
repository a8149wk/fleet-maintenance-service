(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        // Mobile sidebar drawer toggle.
        const sidebar = document.querySelector('[data-fms-sidebar]');
        const overlay = document.querySelector('[data-fms-sidebar-overlay]');
        const toggles = document.querySelectorAll('[data-fms-sidebar-toggle]');
        function openSidebar() {
            if (!sidebar) return;
            sidebar.classList.add('show');
            if (overlay) overlay.classList.add('show');
        }
        function closeSidebar() {
            if (!sidebar) return;
            sidebar.classList.remove('show');
            if (overlay) overlay.classList.remove('show');
        }
        toggles.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                if (sidebar && sidebar.classList.contains('show')) {
                    closeSidebar();
                } else {
                    openSidebar();
                }
            });
        });
        if (overlay) overlay.addEventListener('click', closeSidebar);
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') closeSidebar();
        });

        document.querySelectorAll('table.sortable').forEach(function (t) {
            t.querySelectorAll('th').forEach(function (th, idx) {
                th.style.cursor = 'pointer';
                th.addEventListener('click', function () {
                    const tbody = t.querySelector('tbody');
                    const rows = Array.from(tbody.querySelectorAll('tr'));
                    const asc = !th.classList.contains('sort-asc');
                    rows.sort(function (a, b) {
                        const av = a.children[idx].innerText.trim();
                        const bv = b.children[idx].innerText.trim();
                        return asc ? av.localeCompare(bv) : bv.localeCompare(av);
                    }).forEach(function (r) {
                        tbody.appendChild(r);
                    });
                    t.querySelectorAll('th').forEach(function (h) { h.classList.remove('sort-asc', 'sort-desc'); });
                    th.classList.add(asc ? 'sort-asc' : 'sort-desc');
                });
            });
        });
    });
})();
