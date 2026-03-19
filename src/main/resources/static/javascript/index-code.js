const dropZone     = document.getElementById('dropZone');
const fileInput    = document.getElementById('fileInput');
const previewImg   = document.getElementById('previewImg');
const fileNameEl   = document.getElementById('fileName');
const removeBtn    = document.getElementById('removeBtn');
const messageEl    = document.getElementById('message');
const baseUrlEl    = document.getElementById('baseUrl');
const submitBtn    = document.getElementById('submitBtn');
const responseCard = document.getElementById('responseCard');
const responseBody = document.getElementById('responseBody');
const annotatedCard    = document.getElementById('annotatedCard');
const annotatedWrapper = document.getElementById('annotatedWrapper');
const annotatedImg     = document.getElementById('annotatedImg');
const boxLegend        = document.getElementById('boxLegend');

let selectedFile = null;

// Color palette for bounding boxes
const BOX_COLORS = [
    { border: '#f56e6e', bg: 'rgba(245, 110, 110, 0.12)', label: '#f56e6e' },  // red — primary risk
    { border: '#f5c26e', bg: 'rgba(245, 194, 110, 0.10)', label: '#f5c26e' },  // amber
    { border: '#6ec5f5', bg: 'rgba(110, 197, 245, 0.10)', label: '#6ec5f5' },  // blue
    { border: '#c8f56e', bg: 'rgba(200, 245, 110, 0.10)', label: '#c8f56e' },  // green
    { border: '#d06ef5', bg: 'rgba(208, 110, 245, 0.10)', label: '#d06ef5' },  // purple
];

// --- Drop zone interactions ---
dropZone.addEventListener('click', (e) => {
    if (e.target === removeBtn) return;
    fileInput.click();
});

dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('dragover');
});

dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    if (e.dataTransfer.files.length) handleFile(e.dataTransfer.files[0]);
});

fileInput.addEventListener('change', () => {
    if (fileInput.files.length) handleFile(fileInput.files[0]);
});

removeBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    clearFile();
});

function handleFile(file) {
    if (!file.type.startsWith('image/')) return;
    selectedFile = file;
    const reader = new FileReader();
    reader.onload = (e) => {
        previewImg.src = e.target.result;
        fileNameEl.textContent = `${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
        dropZone.classList.add('has-file');
        updateSubmitState();
    };
    reader.readAsDataURL(file);
}

function clearFile() {
    selectedFile = null;
    fileInput.value = '';
    previewImg.src = '';
    dropZone.classList.remove('has-file');
    annotatedCard.classList.remove('visible');
    updateSubmitState();
}

function updateSubmitState() {
    submitBtn.disabled = !selectedFile;
}

// -------------------------------------------------------
// Bounding-box parser
// Finds patterns like **Coordinates: [x_min, y_min, x_max, y_max]**
// preceded by a heading or label line containing the risk name.
// Filters out near-full-image boxes (>80% coverage) as non-useful.
// -------------------------------------------------------
function parseRisks(text) {
    const risks = [];
    const coordRegex = /\*?\*?Coordinates:\s*\[([^\]]+)\]\*?\*?/gi;
    let match;

    while ((match = coordRegex.exec(text)) !== null) {
        const nums = match[1].split(',').map(s => Number.parseFloat(s.trim()));
        if (nums.length !== 4 || nums.some(Number.isNaN)) continue;

        const [xMin, yMin, xMax, yMax] = nums;

        // Skip boxes that cover >80% of the image — they don't localize anything
        const area = (xMax - xMin) * (yMax - yMin);
        if (area > 0.8) continue;

        // Find the nearest ## heading directly above this coordinate line
        const before = text.slice(0, match.index);
        const lines = before.split('\n');

        let label = `Risk ${risks.length + 1}`;
        for (let i = lines.length - 1; i >= 0; i--) {
            const line = lines[i].trim();
            // Match "## Primary Risk: Exposed Electrical Conductors" etc.
            const headingMatch = line.match(/^#{1,3}\s+(?:Primary|Secondary|Tertiary)?\s*Risk:\s*(.+)/i);
            if (headingMatch) {
                label = headingMatch[1].replaceAll(/\*+/g, '').trim();
                break;
            }
            // Fallback: any markdown heading
            const genericHeading = line.match(/^#{1,3}\s+(.+)/);
            if (genericHeading) {
                label = genericHeading[1].replaceAll(/\*+/g, '').trim();
                break;
            }
        }

        risks.push({ label, coords: nums });
    }

    return risks;
}

// -------------------------------------------------------
// Bounding-box renderer
// -------------------------------------------------------
function renderBoxes(risks, imgEl) {
    // Remove old boxes
    annotatedWrapper.querySelectorAll('.bbox').forEach(el => el.remove());
    boxLegend.innerHTML = '';

    risks.forEach((risk, i) => {
        const color = BOX_COLORS[i % BOX_COLORS.length];
        const [xMin, yMin, xMax, yMax] = risk.coords;

        // Create box element
        const box = document.createElement('div');
        box.className = 'bbox bbox-pulse';
        box.style.left   = `${xMin * 100}%`;
        box.style.top    = `${yMin * 100}%`;
        box.style.width  = `${(xMax - xMin) * 100}%`;
        box.style.height = `${(yMax - yMin) * 100}%`;
        box.style.borderColor = color.border;
        box.style.backgroundColor = color.bg;

        // Label
        const lbl = document.createElement('span');
        lbl.className = 'bbox-label';
        lbl.textContent = risk.label;
        lbl.style.backgroundColor = color.border;
        lbl.style.color = '#0c0c0f';
        box.appendChild(lbl);

        annotatedWrapper.appendChild(box);

        // Legend entry
        const item = document.createElement('div');
        item.className = 'legend-item';

        const swatch = document.createElement('span');
        swatch.className = 'legend-swatch';
        swatch.style.borderColor = color.border;
        swatch.style.backgroundColor = color.bg;

        const name = document.createElement('span');
        name.textContent = risk.label;

        item.appendChild(swatch);
        item.appendChild(name);
        boxLegend.appendChild(item);
    });
}

// --- Submission ---
submitBtn.addEventListener('click', async () => {
    if (!selectedFile) return;

    const baseUrl = baseUrlEl.value.replace(/\/+$/, '');

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span>Analyzing…';
    responseCard.classList.remove('visible');
    annotatedCard.classList.remove('visible');
    responseBody.classList.remove('error');

    try {
        // Step 1: Upload the image file to the server
        const uploadData = new FormData();
        uploadData.append('file', selectedFile);

        const uploadRes = await fetch(`${baseUrl}/upload`, {
            method: 'POST',
            body: uploadData,
        });

        if (!uploadRes.ok) {
            throw new Error(`Upload failed (${uploadRes.status}): ${await uploadRes.text()}`);
        }

        const imagePath = await uploadRes.text();

        // Step 2: Call /analyze with the server-side path
        const params = new URLSearchParams();
        params.append('imagePath', imagePath.trim());

        const message = messageEl.value.trim();
        if (message) {
            params.append('message', message);
        }

        const res = await fetch(`${baseUrl}/analyze?${params.toString()}`, {
            method: 'POST',
        });

        if (!res.ok) {
            throw new Error(`Server responded ${res.status}: ${await res.text()}`);
        }

        const text = await res.text();

        // Step 3: Parse risks and render bounding boxes
        const risks = parseRisks(text);

        if (risks.length > 0) {
            annotatedImg.src = previewImg.src;
            renderBoxes(risks, annotatedImg);
            annotatedCard.classList.add('visible');
        }

        // Show raw response
        responseBody.textContent = text;
        responseCard.classList.add('visible');

    } catch (err) {
        responseBody.classList.add('error');
        responseBody.textContent = `Error: ${err.message}`;
        responseCard.classList.add('visible');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Analyze Image';
        updateSubmitState();
    }
});