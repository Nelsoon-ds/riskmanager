# Ideas & Future Features

## SSE Progress Bar for Analysis
Show real-time progress during the two-step Claude analysis pipeline.

**Approach:** Server-Sent Events (SSE)
- Add a `GET /analyze/progress` endpoint returning `text/event-stream`
- Emit named steps from `RiskManagerService` as the pipeline progresses:
  1. "Uploading image..."
  2. "Running initial analysis..." (before Claude call 1)
  3. "Searching standards..." (before Weaviate search)
  4. "Generating report..." (before Claude call 2)
- Frontend listens with `EventSource` and updates a CSS progress bar
- No extra dependencies needed — plain HTTP, fits existing stack

---

## PDF Report Export
Generate a formal, printable risk assessment report from an analysis result.

**Approach:** Use PDFBox (already a dependency) to produce a structured PDF
- Cover page with timestamp, image thumbnail, overall severity
- One section per hazard: bounding box crop, description, severity, cited standards
- Footer with regulatory references
- Download button on the analyze page triggers `GET /report/{id}.pdf`
- Requires persisting analysis results (see: Analysis History below)

---

## Analysis History
Store past analyses so users can revisit, compare, and track hazards over time.

**Approach:** Persist results to a database (H2 for dev, Postgres for prod)
- Save: image path, timestamp, full `RiskAssessment` JSON, location/tag metadata
- New page `GET /history` listing past analyses with thumbnail + overall severity
- Clicking an entry replays the annotated image view with stored results
- Enables trend tracking: "has this location improved over time?"

---

## Batch Analysis
Analyze multiple images in one go — useful for inspecting an entire worksite.

**Approach:** Accept a ZIP upload or directory path on the server
- Queue each image as an async task (Spring `@Async`)
- Show per-image progress and a summary table when all complete
- Aggregate severity: highlight if any image contains PRIMARY hazards
- Natural extension of the existing `/ingest` directory-walking pattern

---

## Feedback Loop on Detections
Let users mark hazard detections as correct, incorrect, or missed.

**Approach:** Thumbs up/down buttons per hazard bounding box in the UI
- Store feedback alongside the analysis in the database
- Use accumulated feedback to refine the system prompt over time
- Surfaces bad detections early and builds a local ground-truth dataset
- Low effort to add to the existing bounding box rendering in `analyze.html`

---

## Mobile / On-Site Mode
Make the tool usable on a phone directly at the worksite.

**Approach:** Responsive CSS + camera capture input
- Replace drag-drop upload with `<input type="file" accept="image/*" capture="environment">` for direct camera use on mobile
- Simplified single-column layout for small screens
- Could be packaged as a PWA (manifest + service worker) for home screen install
- No backend changes needed — purely a frontend improvement

---

## Custom Standards Upload via UI
Let non-technical users add new regulatory documents without using the admin ingest form.

**Approach:** File upload form on the admin page
- Accept PDF or TXT, pass to `DocumentIngestionService.ingestFile()`
- Show ingested document count and chunk count after upload
- List currently loaded standards with the ability to clear individual ones
- Removes the need to SSH or copy files to the server's data directory

---

## Multi-Language Support
The prompts currently instruct Claude to respond in Danish. Make this configurable.

**Approach:** Language selector on the analyze page
- Pass selected locale into the StringTemplate prompt as a variable
- Store preference in a cookie or session
- Useful if the tool is used across teams in different countries
- Minimal change — one new template variable in `initial-analysis.st` and `augmented-analysis.st`
