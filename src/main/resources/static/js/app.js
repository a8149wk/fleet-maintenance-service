(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        // Sidebar toggle.
        //  - On desktop (>= 992px): toggles a persistent "collapsed"
        //    state on <body>, remembered in localStorage.
        //  - On mobile (< 992px): toggles a transient drawer with an
        //    overlay backdrop; clicking the overlay or pressing Escape
        //    closes it.
        const sidebar = document.querySelector('[data-fms-sidebar]');
        const overlay = document.querySelector('[data-fms-sidebar-overlay]');
        const toggles = document.querySelectorAll('[data-fms-sidebar-toggle]');
        const STORAGE_KEY = 'fms-sidebar-collapsed';
        const desktopMQ = window.matchMedia('(min-width: 992px)');

        function isDesktop() { return desktopMQ.matches; }

        function openMobile() {
            if (!sidebar) return;
            sidebar.classList.add('show');
            if (overlay) overlay.classList.add('show');
        }
        function closeMobile() {
            if (!sidebar) return;
            sidebar.classList.remove('show');
            if (overlay) overlay.classList.remove('show');
        }

        // Restore persisted desktop state on load.
        if (localStorage.getItem(STORAGE_KEY) === '1') {
            document.body.classList.add('sidebar-collapsed');
        }

        toggles.forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                if (isDesktop()) {
                    const nowCollapsed = document.body.classList.toggle('sidebar-collapsed');
                    localStorage.setItem(STORAGE_KEY, nowCollapsed ? '1' : '0');
                } else {
                    if (sidebar && sidebar.classList.contains('show')) {
                        closeMobile();
                    } else {
                        openMobile();
                    }
                }
            });
        });

        if (overlay) overlay.addEventListener('click', closeMobile);
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') closeMobile();
        });

        // When crossing the desktop breakpoint, drop the transient
        // mobile state so the two modes don't fight each other.
        desktopMQ.addEventListener('change', function () {
            closeMobile();
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
