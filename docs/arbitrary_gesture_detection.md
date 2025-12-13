# Arbitrary Gesture Detection System

## Overview

The `ArbitraryGestureDetector` class implements a gesture detection system that works by comparing the **shape** (specifically the sequence of angles) of the user's input against a list of pre-recorded gesture paths.

## Algorithm

### 1. Input Collection

The process begins by recording the raw path of the user's touch or mouse movement.

- **Capture**: As the user drags, points (`Offset` objects with x, y coordinates) are collected into an array using `addPoint(x, y)`.
- **Filtering**: When the drag ends (`endDrag()`), the code checks if the gesture is significant enough:
  - Rejects if the line has fewer than 2 points
  - Rejects if the total length is less than 100 pixels

### 2. Normalization

Before comparison, the raw input is standardized so that the speed of drawing or exact screen position doesn't matter.

- **Translation**: The entire path is shifted so that the first point starts at `(0, 0)` (in `normalizeLine`).
- **Resampling**: The path is converted into a fixed number of points (40 points) regardless of how long the physical line is. This is done in the `resample` method, which interpolates points at equal intervals along the total length of the path.

### 3. Comparison Algorithm

The core recognition logic happens in the `detectGesture` and `calculateDifference` methods. Instead of checking if the points overlap (which would fail if you drew the same shape but rotated or scaled differently), it checks the **direction** of movement.

For every known gesture in the `gestureCommands` list:
1. It retrieves the stored path and converts it into `Offset` objects.
2. It calls `calculateDifference(normalizedInput, normalizedPreset)`.

**The Metric (Angular Difference):**

The `calculateDifference` method compares the local direction of the two paths:
- It iterates through the segments of both lines.
- For each segment, it calculates the vector angle using `atan2` from `kotlin.math`.
- It computes the absolute difference between the angle of the user's input and the angle of the preset.
- It averages these differences to get a score.

### 4. Decision

- The algorithm keeps track of the `bestMatch` (lowest difference score).
- **Threshold**: It only accepts the match if the average angular difference (`minDiff`) is less than `0.5` radians (~28.6 degrees).
- If a match is found, it returns the corresponding gesture name; otherwise, it returns `null` for an "unknown" gesture.

## Usage Example

```kotlin
// Create the detector
val gestureDetector = ArbitraryGestureDetector()

// Register some gesture commands with preset paths
val squareGesture = listOf(
    ArbitraryGestureDetector.Offset(0f, 0f),
    ArbitraryGestureDetector.Offset(100f, 0f),
    ArbitraryGestureDetector.Offset(100f, 100f),
    ArbitraryGestureDetector.Offset(0f, 100f),
    ArbitraryGestureDetector.Offset(0f, 0f)
)
gestureDetector.addGestureCommand("square", squareGesture)

val zigzagGesture = listOf(
    ArbitraryGestureDetector.Offset(0f, 0f),
    ArbitraryGestureDetector.Offset(50f, 50f),
    ArbitraryGestureDetector.Offset(100f, 0f),
    ArbitraryGestureDetector.Offset(150f, 50f)
)
gestureDetector.addGestureCommand("zigzag", zigzagGesture)

// In touch event handlers:
// On touch down
gestureDetector.startDrag()

// On touch move
gestureDetector.addPoint(event.x, event.y)

// On touch up
val detectedGesture = gestureDetector.endDrag()
if (detectedGesture != null) {
    Log.i("Gesture", "Detected: $detectedGesture")
    // Execute corresponding action
} else {
    Log.i("Gesture", "Unknown or too short gesture")
}
```

## Integration with TouchGestureDetector

The `ArbitraryGestureDetector` can be integrated into the existing `TouchGestureDetector` class to provide additional gesture recognition capabilities alongside the existing directional swipes and taps.

Example integration:

```kotlin
class TouchGestureDetector(...) {
    private val arbitraryGestureDetector = ArbitraryGestureDetector()
    
    init {
        // Register custom gestures
        setupCustomGestures()
    }
    
    private fun setupCustomGestures() {
        // Add custom gesture patterns here
    }
    
    fun onTouchEvent(event: MotionEvent) {
        // ... existing code ...
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                arbitraryGestureDetector.startDrag()
                // ... existing code ...
            }
            MotionEvent.ACTION_MOVE -> {
                arbitraryGestureDetector.addPoint(event.x, event.y)
                // ... existing code ...
            }
            MotionEvent.ACTION_UP -> {
                val customGesture = arbitraryGestureDetector.endDrag()
                if (customGesture != null) {
                    // Handle custom gesture
                    handleCustomGesture(customGesture)
                } else {
                    // Fall back to existing gesture detection
                    classifyPaths(paths, event.downTime, event.eventTime)
                }
            }
        }
    }
}
```

## Key Constants

- `MIN_POINTS = 2`: Minimum number of points required for a valid gesture
- `MIN_LENGTH = 100f`: Minimum total path length in pixels
- `RESAMPLE_POINT_COUNT = 40`: Number of points to resample to for normalization
- `ANGLE_THRESHOLD = 0.5`: Maximum average angular difference in radians for a match

## Features

- **Scale Invariant**: Gestures are normalized, so size doesn't matter
- **Position Invariant**: Translated to origin, so screen position doesn't matter
- **Speed Invariant**: Resampling ensures drawing speed doesn't affect recognition
- **Rotation Sensitive**: Gestures must be drawn in the same orientation (by design, using angles)

## Customization

You can adjust the recognition parameters by modifying the constants:
- Increase `MIN_LENGTH` to require longer gestures
- Increase `RESAMPLE_POINT_COUNT` for more detailed comparison (slower)
- Decrease `ANGLE_THRESHOLD` for stricter matching
- Increase `ANGLE_THRESHOLD` for more lenient matching
