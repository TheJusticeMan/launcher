# Arbitrary Gesture Detection Implementation

## Summary

This implementation adds an arbitrary gesture detection system to the µLauncher Android app. The system works by comparing the **shape** (specifically the sequence of angles) of the user's input against a list of pre-recorded gesture paths.

## Files Added

### 1. ArbitraryGestureDetector.kt
**Location:** `app/src/main/java/de/jrpie/android/launcher/ui/ArbitraryGestureDetector.kt`

**Purpose:** Core gesture detection class implementing the full algorithm.

**Key Components:**
- `Offset` data class: Represents 2D points (x, y coordinates)
- `GestureCommand` data class: Stores gesture name and path
- Input collection methods: `startDrag()`, `addPoint()`, `endDrag()`
- Normalization: `normalizeLine()`, `resample()`
- Comparison: `detectGesture()`, `calculateDifference()`

**Key Constants:**
- `MIN_POINTS = 2`: Minimum points for valid gesture
- `MIN_LENGTH = 100f`: Minimum path length in pixels
- `RESAMPLE_POINT_COUNT = 40`: Fixed point count for normalization
- `ANGLE_THRESHOLD = 0.5`: Maximum angular difference in radians (~28.6°)

### 2. ArbitraryGestureExample.kt
**Location:** `app/src/main/java/de/jrpie/android/launcher/ui/ArbitraryGestureExample.kt`

**Purpose:** Demonstrates usage with concrete examples.

**Features:**
- Pre-configured gestures (circle, triangle, square, Z-shape, star)
- Touch event handling examples
- Gesture result processing
- Complete integration example in comments

### 3. arbitrary_gesture_detection.md
**Location:** `docs/arbitrary_gesture_detection.md`

**Purpose:** Comprehensive documentation of the algorithm and usage.

**Contents:**
- Detailed algorithm explanation
- Step-by-step breakdown
- Integration examples
- Customization guide

## Algorithm Implementation

### 1. Input Collection Phase
```kotlin
// User starts drawing
detector.startDrag()

// As user moves finger
detector.addPoint(x, y)

// User lifts finger
val result = detector.endDrag()
```

**Filtering:**
- Rejects gestures with < 2 points
- Rejects gestures with total length < 100 pixels

### 2. Normalization Phase

**Translation:**
```kotlin
// Shift entire path so first point is at (0, 0)
val firstPoint = path[0]
val translated = path.map { Offset(it.x - firstPoint.x, it.y - firstPoint.y) }
```

**Resampling:**
- Converts path to exactly 40 points
- Uses linear interpolation
- Maintains path shape regardless of drawing speed
- Equal intervals along total path length

### 3. Comparison Phase

**Angular Difference Calculation:**
```kotlin
for each segment i:
    angle1 = atan2(dy1, dx1)  // User input
    angle2 = atan2(dy2, dx2)  // Preset gesture
    diff = abs(angle1 - angle2)
    if (diff > π) diff = 2π - diff  // Normalize to [-π, π]
    totalDiff += diff
    
averageDiff = totalDiff / segmentCount
```

### 4. Decision Phase

- Compare with all registered gestures
- Track best match (lowest angular difference)
- Accept only if `averageDiff < 0.5 radians`
- Return gesture name or null

## Features

✓ **Scale Invariant**: Size doesn't matter (normalized)
✓ **Position Invariant**: Location on screen doesn't matter (translated to origin)
✓ **Speed Invariant**: Drawing speed doesn't matter (resampled)
✓ **Rotation Sensitive**: Orientation matters (intentional, by design)

## Integration Guide

### Basic Integration

```kotlin
class MyActivity : Activity() {
    private val gestureDetector = ArbitraryGestureDetector()
    
    init {
        // Register gestures
        val circle = listOf(/* points */)
        gestureDetector.addGestureCommand("CIRCLE", circle)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> gestureDetector.startDrag()
            MotionEvent.ACTION_MOVE -> gestureDetector.addPoint(event.x, event.y)
            MotionEvent.ACTION_UP -> {
                val gesture = gestureDetector.endDrag()
                handleGesture(gesture)
            }
        }
        return true
    }
}
```

### Integration with Existing TouchGestureDetector

The ArbitraryGestureDetector can be integrated alongside the existing gesture detection system in TouchGestureDetector.kt:

```kotlin
class TouchGestureDetector(...) {
    private val arbitraryGestureDetector = ArbitraryGestureDetector()
    
    fun onTouchEvent(event: MotionEvent) {
        // Existing code...
        
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            arbitraryGestureDetector.startDrag()
        }
        
        arbitraryGestureDetector.addPoint(event.x, event.y)
        
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            val customGesture = arbitraryGestureDetector.endDrag()
            if (customGesture != null) {
                // Handle custom gesture
                return
            }
            // Fall back to existing detection
            classifyPaths(...)
        }
    }
}
```

## Customization Options

### Adjust Threshold
```kotlin
// Make matching stricter
private const val ANGLE_THRESHOLD = 0.3  // ~17°

// Make matching more lenient
private const val ANGLE_THRESHOLD = 0.7  // ~40°
```

### Adjust Precision
```kotlin
// Higher precision (slower)
private const val RESAMPLE_POINT_COUNT = 60

// Lower precision (faster)
private const val RESAMPLE_POINT_COUNT = 20
```

### Adjust Minimum Length
```kotlin
// Require longer gestures
private const val MIN_LENGTH = 200f

// Allow shorter gestures
private const val MIN_LENGTH = 50f
```

## Testing Recommendations

1. **Unit Tests** (if test infrastructure added):
   - Test normalization with various paths
   - Test resampling accuracy
   - Test angle calculation
   - Test threshold matching

2. **Integration Tests**:
   - Test with different screen sizes
   - Test with different drawing speeds
   - Test with rotated gestures
   - Test with scaled gestures

3. **User Testing**:
   - Collect sample gestures from multiple users
   - Adjust threshold based on recognition rate
   - Validate false positive/negative rates

## Performance Considerations

- **Time Complexity**: O(n * m * p) where:
  - n = number of registered gestures
  - m = number of points after resampling (40)
  - p = number of input points (variable)

- **Space Complexity**: O(n * m) for stored gestures

- **Optimization Tips**:
  - Reduce `RESAMPLE_POINT_COUNT` for faster matching
  - Limit number of registered gestures
  - Consider caching normalized presets

## Known Limitations

1. **Rotation Sensitivity**: Gestures must be drawn in the same orientation as presets
2. **Multi-stroke Gestures**: Currently supports single-stroke gestures only
3. **Complex Shapes**: Very complex gestures may have lower recognition rates
4. **Similar Shapes**: Gestures with similar angle sequences may conflict

## Future Enhancements

- [ ] Add rotation invariance option
- [ ] Support multi-stroke gestures
- [ ] Implement gesture learning/recording UI
- [ ] Add gesture persistence (save/load from preferences)
- [ ] Optimize comparison with early termination
- [ ] Add visualization for debugging
- [ ] Support gesture variants (mirror, rotate)

## References

- Algorithm based on "$1 Unistroke Recognizer" principles
- Uses angle-based comparison for shape matching
- Normalization ensures invariance to size, position, and speed
