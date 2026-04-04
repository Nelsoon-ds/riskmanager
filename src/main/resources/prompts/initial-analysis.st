Analyze this image for workplace safety and operational risks.
If you don't find any, be honest and say so.

The image dimensions are {width}x{height} pixels.

For each risk identified, provide:
1. A severity level (PRIMARY, SECONDARY, TERTIARY)
2. A short name for the hazard
3. Bounding box coordinates [x_min, y_min, x_max, y_max] normalized 0-1
4. A brief description

Bounding box guidelines:
- Each box should tightly enclose ONLY the specific hazard, not the general area
- Avoid boxes that span more than 40% of the image unless the hazard truly covers that area
- Think step by step: identify the hazard's exact location, estimate its edges, then convert to normalized coordinates
- x_min is the left edge, x_max is the right edge, y_min is the top edge, y_max is the bottom edge