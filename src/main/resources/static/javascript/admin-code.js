const API_BASE = 'http://localhost:8081';

// --- Elements ---
const ingestPath = document.getElementById('ingestPath');
const ingestBtn = document.getElementById('ingestBtn');
const ingestStatus = document.getElementById('ingestStatus');

const searchQuery = document.getElementById('searchQuery');
const topK = document.getElementById('topK');
const searchBtn = document.getElementById('searchBtn');

const resultsCard = document.getElementById('resultsCard');
const resultCount = document.getElementById('resultCount');
const resultsList = document.getElementById('resultsList');

const weaviateUrl = document.getElementById('weaviateUrl');
const inspectBtn = document.getElementById('inspectBtn');
const clearBtn = document.getElementById('clearBtn');
const storeStatus = document.getElementById('storeStatus');
const storeObjects = document.getElementById('storeObjects');

// --- Helpers ---
function setStatus(el, msg, type) {
    el.textContent = msg;
    el.className = 'status-bar ' + (type || '');
}

function setLoading(el, msg) {
    el.innerHTML = '<span class="spinner"></span>' + msg;
    el.className = 'status-bar loading';
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function truncate(str, len) {
    return str.length > len ? str.substring(0, len) + '…' : str;
}

// --- Ingest ---
ingestBtn.addEventListener('click', async () => {
    const path = ingestPath.value.trim();
    if (!path) {
        setStatus(ingestStatus, 'Please enter a file or directory path.', 'error');
        return;
    }

    ingestBtn.disabled = true;
    setLoading(ingestStatus, 'Ingesting…');

    try {
        const res = await fetch(`${API_BASE}/ingest?path=${encodeURIComponent(path)}`, {
            method: 'POST'
        });
        const text = await res.text();
        if (res.ok) {
            setStatus(ingestStatus, text, 'success');
        } else {
            setStatus(ingestStatus, `Error: ${text}`, 'error');
        }
    } catch (err) {
        setStatus(ingestStatus, `Failed: ${err.message}`, 'error');
    } finally {
        ingestBtn.disabled = false;
    }
});

// --- Search ---
searchBtn.addEventListener('click', async () => {
    const query = searchQuery.value.trim();
    if (!query) return;

    searchBtn.disabled = true;
    searchBtn.textContent = 'Searching…';
    resultsCard.classList.remove('visible');

    try {
        const k = parseInt(topK.value) || 5;
        const res = await fetch(`${API_BASE}/search?query=${encodeURIComponent(query)}&topK=${k}`);
        const data = await res.json();

        resultsList.innerHTML = '';

        if (!data || data.length === 0) {
            resultCount.textContent = '0 chunks found';
            resultsList.innerHTML = '<div class="empty-state">No matching documents found.</div>';
        } else {
            // Deduplicate
            const unique = [...new Set(data)];
            resultCount.textContent = `${unique.length} chunk${unique.length !== 1 ? 's' : ''} found`;

            unique.forEach((text, i) => {
                const item = document.createElement('div');
                item.className = 'result-item';
                item.innerHTML = `
                    <div class="result-meta">
                        <span class="result-tag">chunk ${i + 1}</span>
                    </div>
                    <div class="result-text">${escapeHtml(text)}</div>
                `;
                resultsList.appendChild(item);
            });
        }

        resultsCard.classList.add('visible');
    } catch (err) {
        resultCount.textContent = '';
        resultsList.innerHTML = `<div class="empty-state" style="color: var(--danger)">Search failed: ${escapeHtml(err.message)}</div>`;
        resultsCard.classList.add('visible');
    } finally {
        searchBtn.disabled = false;
        searchBtn.textContent = 'Search';
    }
});

// Submit search on Ctrl+Enter
searchQuery.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        searchBtn.click();
    }
});

// --- Inspect Store ---
inspectBtn.addEventListener('click', async () => {
    const base = weaviateUrl.value.trim();
    inspectBtn.disabled = true;
    setLoading(storeStatus, 'Fetching objects…');
    storeObjects.innerHTML = '';

    try {
        const res = await fetch(`${base}/v1/objects?class=SpringAiWeaviate&limit=20`);
        const data = await res.json();
        const objects = data.objects || [];

        setStatus(storeStatus, `${objects.length} object${objects.length !== 1 ? 's' : ''} in store`, 'success');

        if (objects.length === 0) {
            storeObjects.innerHTML = '<div class="empty-state">Vector store is empty.</div>';
            return;
        }

        objects.forEach(obj => {
            const content = obj.properties?.content || '';
            const metadata = obj.properties?.metadata || '{}';
            let filename = '';
            try {
                const meta = JSON.parse(metadata);
                filename = meta.filename || '';
            } catch (e) { /* ignore */ }

            const item = document.createElement('div');
            item.className = 'store-item';
            item.innerHTML = `
                <div class="store-item-header">
                    <span class="store-item-filename">${escapeHtml(filename)}</span>
                    <span class="store-item-id">${obj.id ? obj.id.substring(0, 8) + '…' : ''}</span>
                </div>
                <div class="store-item-preview">${escapeHtml(truncate(content, 300))}</div>
            `;
            storeObjects.appendChild(item);
        });
    } catch (err) {
        setStatus(storeStatus, `Failed: ${err.message}`, 'error');
    } finally {
        inspectBtn.disabled = false;
    }
});

// --- Clear Store ---
clearBtn.addEventListener('click', async () => {
    if (!confirm('This will delete ALL documents from the vector store. Are you sure?')) return;

    const base = weaviateUrl.value.trim();
    clearBtn.disabled = true;
    setLoading(storeStatus, 'Clearing store…');

    try {
        const res = await fetch(`${base}/v1/schema/SpringAiWeaviate`, { method: 'DELETE' });
        if (res.ok) {
            setStatus(storeStatus, 'Store cleared. Schema will be recreated on next ingest.', 'success');
            storeObjects.innerHTML = '<div class="empty-state">Vector store is empty.</div>';
        } else {
            const text = await res.text();
            setStatus(storeStatus, `Error: ${text}`, 'error');
        }
    } catch (err) {
        setStatus(storeStatus, `Failed: ${err.message}`, 'error');
    } finally {
        clearBtn.disabled = false;
    }
});