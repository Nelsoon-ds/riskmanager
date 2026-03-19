# Risk Manager — Visual Risk Analysis Tool

AI-powered image analysis tool that identifies safety and operational risks in uploaded images, returning annotated results with bounding boxes highlighting areas of concern.

## How It Works

1. **Upload** an image through the web interface (drag-and-drop or file picker)
2. The image is saved server-side and passed to a vision LLM for analysis
3. The LLM identifies risks with severity levels, descriptions, and normalized bounding box coordinates
4. The frontend parses the response, overlays color-coded bounding boxes on the image, and displays the full risk assessment

## Tech Stack

- **Backend:** Java / Spring Boot
- **Frontend:** Vanilla HTML, CSS, JavaScript (no framework)
- **AI:** Vision LLM via API (returns structured risk assessments with coordinates)

## Project Structure

```
riskmanager/
├── src/main/java/.../
│   ├── controller/
│   │   └── RiskManagerController.java   # /upload and /analyze endpoints
│   └── service/
│       └── RiskManagerService.java      # LLM API integration
├── src/main/resources/static/
│   ├── analyze.html                     # Main page
│   ├── style.css                        # Styling
│   └── app.js                           # Upload, parsing, bounding box rendering
└── uploads/                             # Server-side image storage (auto-created)
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/upload` | Accepts multipart file, saves to `uploads/`, returns absolute path |
| `POST` | `/analyze?imagePath=...` | Sends image to vision LLM, returns risk assessment text |

## Running Locally

1. Configure your LLM API credentials in `application.properties`
2. Run the Spring Boot application
3. Open `http://localhost:8080/analyze.html`
4. Upload an image and click **Analyze Image**

## LLM Prompt Format

The backend sends a prompt instructing the LLM to return risks in this structure:

```
## Primary Risk: [Risk Name]
**Coordinates: [x_min, y_min, x_max, y_max]**

Description and recommended action...
```

Coordinates are normalized 0–1 relative to image dimensions (top-left origin). The frontend parses these and renders bounding boxes. Boxes covering >80% of the image are filtered out as non-localizing.

## Planned

- Vector database for matching detected risks against known risk procedures
- Risk repository lookup with response recommendations
- Persistent storage for analysis history
