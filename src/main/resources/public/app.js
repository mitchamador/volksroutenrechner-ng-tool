const TRIP_TYPES = ['C', 'A', 'B'];
const ALL_TYPES = ['C', 'A', 'B', 'accel'];
const pageState = { C: 1, A: 1, B: 1, accel: 1 };

function getPageSize() {
    return parseInt(document.getElementById('pageSize').value, 10);
}

function dateToMillis(inputValue, endOfDay) {
    if (!inputValue) return null;
    const d = new Date(inputValue + (endOfDay ? 'T23:59:59' : 'T00:00:00'));
    return d.getTime();
}

function millisToLocal(ms) {
    return new Date(ms).toLocaleString('ru-RU', {
        day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function currentPeriod() {
    return {
        from: dateToMillis(document.getElementById('filterFrom').value, false),
        to: dateToMillis(document.getElementById('filterTo').value, true)
    };
}

function buildQuery(params) {
    const parts = [];
    for (const key in params) {
        if (params[key] !== null && params[key] !== undefined) {
            parts.push(key + '=' + encodeURIComponent(params[key]));
        }
    }
    return parts.length ? '?' + parts.join('&') : '';
}

async function loadTrips(type) {
    const { from, to } = currentPeriod();
    const pageSize = getPageSize();
    let page = pageState[type];
    let body = await fetchTrips(type, from, to, page, pageSize);
    if (body.records.length === 0 && page > 1) {
        page = pageState[type] = page - 1;
        body = await fetchTrips(type, from, to, page, pageSize);
    }
    renderTripTable(type, body.since, body.records);
    renderPagination(type, body.total, body.page, body.pageSize);
}

async function fetchTrips(type, from, to, page, pageSize) {
    const res = await fetch('/api/trips/' + type + buildQuery({ from, to, page, pageSize }));
    return res.json();
}

async function loadAccel() {
    const { from, to } = currentPeriod();
    const pageSize = getPageSize();
    let page = pageState.accel;
    let body = await fetchAccel(from, to, page, pageSize);
    if (body.records.length === 0 && page > 1) {
        page = pageState.accel = page - 1;
        body = await fetchAccel(from, to, page, pageSize);
    }
    renderAccelTable(body.records);
    renderPagination('accel', body.total, body.page, body.pageSize);
}

async function fetchAccel(from, to, page, pageSize) {
    const res = await fetch('/api/accel' + buildQuery({ from, to, page, pageSize }));
    return res.json();
}

function loadAll() {
    ALL_TYPES.forEach(t => { pageState[t] = 1; });
    TRIP_TYPES.forEach(loadTrips);
    loadAccel();
}

function renderPagination(type, total, page, pageSize) {
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    document.getElementById('pageInfo-' + type).textContent =
        'Страница ' + page + ' из ' + totalPages + ' (всего записей: ' + total + ')';
    document.getElementById('prevPage-' + type).disabled = page <= 1;
    document.getElementById('nextPage-' + type).disabled = page >= totalPages;
}

function reloadType(type) {
    return type === 'accel' ? loadAccel() : loadTrips(type);
}

function renderTripTable(type, since, rows) {
    const table = document.getElementById('table-' + type);
    let html = '<thead><tr>' +
        '<th>Дата</th><th>Пробег, км</th><th>Ср. скорость, км/ч</th><th>Расход, л/100км</th><th>Топливо, л</th><th>Время в пути</th><th></th>' +
        '</tr></thead><tbody>';
    if (since !== null && since !== undefined) {
        html += '<tr class="text-muted fst-italic">' +
            '<td>' + millisToLocal(since) + '</td>' +
            '<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>' +
            '</tr>';
    }
    for (const r of rows) {
        const hours = Math.floor(r.totalMinutes / 60);
        const minutes = r.totalMinutes % 60;
        html += '<tr>' +
            '<td>' + millisToLocal(r.time) + '</td>' +
            '<td>' + r.odo.toFixed(1) + '</td>' +
            '<td>' + r.averageSpeed.toFixed(1) + '</td>' +
            '<td>' + r.averageFuel.toFixed(1) + '</td>' +
            '<td>' + r.totalFuel.toFixed(1) + '</td>' +
            '<td>' + String(hours).padStart(2, '0') + ':' + String(minutes).padStart(2, '0') + '</td>' +
            '<td class="text-end"><button class="btn btn-sm btn-outline-danger" data-type="' + type + '" data-id="' + r.id + '">Удалить</button></td>' +
            '</tr>';
    }
    table.innerHTML = html + '</tbody>';
    table.querySelectorAll('button[data-id]').forEach(btn => {
        btn.addEventListener('click', () => deleteTrip(btn.dataset.type, btn.dataset.id));
    });
}

function renderAccelTable(rows) {
    const table = document.getElementById('table-accel');
    let html = '<thead><tr><th>Дата</th><th>Разгон</th><th>Время, с</th><th></th></tr></thead><tbody>';
    for (const r of rows) {
        html += '<tr>' +
            '<td>' + millisToLocal(r.startTime) + '</td>' +
            '<td>' + r.lowerSpeed + ' - ' + r.upperSpeed + ' км/ч</td>' +
            '<td>' + (r.resultCs / 100).toFixed(2) + '</td>' +
            '<td class="text-end"><button class="btn btn-sm btn-outline-danger" data-id="' + r.id + '">Удалить</button></td>' +
            '</tr>';
    }
    table.innerHTML = html + '</tbody>';
    table.querySelectorAll('button[data-id]').forEach(btn => {
        btn.addEventListener('click', () => deleteAccel(btn.dataset.id));
    });
}

async function deleteTrip(type, id) {
    if (!confirm('Удалить запись?')) return;
    await fetch('/api/trips/' + type + '/' + id, { method: 'DELETE' });
    loadTrips(type);
}

async function deleteAccel(id) {
    if (!confirm('Удалить запись?')) return;
    await fetch('/api/accel/' + id, { method: 'DELETE' });
    loadAccel();
}

document.getElementById('applyFilter').addEventListener('click', loadAll);
document.getElementById('resetFilter').addEventListener('click', () => {
    document.getElementById('filterFrom').value = '';
    document.getElementById('filterTo').value = '';
    loadAll();
});
document.getElementById('pageSize').addEventListener('change', loadAll);

ALL_TYPES.forEach(type => {
    document.getElementById('prevPage-' + type).addEventListener('click', () => {
        if (pageState[type] > 1) {
            pageState[type]--;
            reloadType(type);
        }
    });
    document.getElementById('nextPage-' + type).addEventListener('click', () => {
        pageState[type]++;
        reloadType(type);
    });
});

document.getElementById('importBtn').addEventListener('click', async () => {
    const fileInput = document.getElementById('importFile');
    const status = document.getElementById('importStatus');
    if (!fileInput.files.length) {
        status.textContent = 'Выберите файл';
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    status.textContent = 'Импортирую...';
    try {
        const res = await fetch('/api/import', { method: 'POST', body: formData });
        const result = await res.json();
        if (!res.ok) {
            status.textContent = 'Ошибка: ' + (result.error || res.status);
            return;
        }
        status.textContent = 'Импортировано: ' + result.imported + ', пропущено (дубликаты): ' + result.skipped;
        loadAll();
    } catch (e) {
        status.textContent = 'Ошибка: ' + e;
    }
});

document.getElementById('exportBtn').addEventListener('click', () => {
    const format = document.getElementById('exportFormat').value;
    const size = document.getElementById('exportSize').value;
    window.location = '/api/export' + buildQuery({ format, size });
});

loadAll();
