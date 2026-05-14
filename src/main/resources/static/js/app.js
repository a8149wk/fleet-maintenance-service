(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
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
