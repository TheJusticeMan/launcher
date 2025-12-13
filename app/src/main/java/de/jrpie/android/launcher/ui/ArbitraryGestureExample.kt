package de.jrpie.android.launcher.ui

import android.util.Log

/**
 * Example demonstrating how to use the ArbitraryGestureDetector
 * 
 * This is a reference implementation showing how to:
 * 1. Create gesture presets
 * 2. Register them with the detector
 * 3. Process user input
 * 4. Handle detection results
 */
class ArbitraryGestureExample {
    
    private val gestureDetector = ArbitraryGestureDetector()
    
    init {
        setupGestures()
    }
    
    /**
     * Setup example gesture patterns
     */
    private fun setupGestures() {
        // Circle gesture (clockwise starting from top)
        val circle = listOf(
            ArbitraryGestureDetector.Offset(50f, 0f),
            ArbitraryGestureDetector.Offset(100f, 0f),
            ArbitraryGestureDetector.Offset(146f, 14f),
            ArbitraryGestureDetector.Offset(178f, 46f),
            ArbitraryGestureDetector.Offset(200f, 100f),
            ArbitraryGestureDetector.Offset(178f, 154f),
            ArbitraryGestureDetector.Offset(146f, 186f),
            ArbitraryGestureDetector.Offset(100f, 200f),
            ArbitraryGestureDetector.Offset(54f, 186f),
            ArbitraryGestureDetector.Offset(22f, 154f),
            ArbitraryGestureDetector.Offset(0f, 100f),
            ArbitraryGestureDetector.Offset(22f, 46f),
            ArbitraryGestureDetector.Offset(50f, 14f),
            ArbitraryGestureDetector.Offset(50f, 0f)
        )
        gestureDetector.addGestureCommand("CIRCLE", circle)
        
        // Triangle gesture (upward pointing)
        val triangle = listOf(
            ArbitraryGestureDetector.Offset(100f, 0f),
            ArbitraryGestureDetector.Offset(200f, 173f),
            ArbitraryGestureDetector.Offset(0f, 173f),
            ArbitraryGestureDetector.Offset(100f, 0f)
        )
        gestureDetector.addGestureCommand("TRIANGLE", triangle)
        
        // Square gesture
        val square = listOf(
            ArbitraryGestureDetector.Offset(0f, 0f),
            ArbitraryGestureDetector.Offset(200f, 0f),
            ArbitraryGestureDetector.Offset(200f, 200f),
            ArbitraryGestureDetector.Offset(0f, 200f),
            ArbitraryGestureDetector.Offset(0f, 0f)
        )
        gestureDetector.addGestureCommand("SQUARE", square)
        
        // Z-shaped gesture
        val zShape = listOf(
            ArbitraryGestureDetector.Offset(0f, 0f),
            ArbitraryGestureDetector.Offset(200f, 0f),
            ArbitraryGestureDetector.Offset(0f, 200f),
            ArbitraryGestureDetector.Offset(200f, 200f)
        )
        gestureDetector.addGestureCommand("Z_SHAPE", zShape)
        
        // Star gesture (5-pointed star)
        val star = listOf(
            ArbitraryGestureDetector.Offset(100f, 0f),
            ArbitraryGestureDetector.Offset(61f, 190f),
            ArbitraryGestureDetector.Offset(195f, 69f),
            ArbitraryGestureDetector.Offset(5f, 69f),
            ArbitraryGestureDetector.Offset(139f, 190f),
            ArbitraryGestureDetector.Offset(100f, 0f)
        )
        gestureDetector.addGestureCommand("STAR", star)
    }
    
    /**
     * Call this when user starts touching the screen
     */
    fun onTouchDown(x: Float, y: Float) {
        gestureDetector.startDrag()
        gestureDetector.addPoint(x, y)
    }
    
    /**
     * Call this when user moves finger on screen
     */
    fun onTouchMove(x: Float, y: Float) {
        gestureDetector.addPoint(x, y)
    }
    
    /**
     * Call this when user lifts finger from screen
     * @return The detected gesture name, or null if no match
     */
    fun onTouchUp(x: Float, y: Float): String? {
        gestureDetector.addPoint(x, y)
        return gestureDetector.endDrag()
    }
    
    /**
     * Example of handling detected gestures
     */
    fun handleGesture(gestureName: String?) {
        when (gestureName) {
            "CIRCLE" -> Log.i("ArbitraryGesture", "Circle gesture detected - Opening menu")
            "TRIANGLE" -> Log.i("ArbitraryGesture", "Triangle gesture detected - Going up")
            "SQUARE" -> Log.i("ArbitraryGesture", "Square gesture detected - Maximizing")
            "Z_SHAPE" -> Log.i("ArbitraryGesture", "Z gesture detected - Undo action")
            "STAR" -> Log.i("ArbitraryGesture", "Star gesture detected - Mark as favorite")
            null -> Log.i("ArbitraryGesture", "Unknown or invalid gesture")
            else -> Log.i("ArbitraryGesture", "Gesture detected: $gestureName")
        }
    }
}

/**
 * Example usage in an Activity or View:
 * 
 * ```kotlin
 * class MyActivity : Activity() {
 *     private val gestureExample = ArbitraryGestureExample()
 *     
 *     override fun onTouchEvent(event: MotionEvent): Boolean {
 *         when (event.actionMasked) {
 *             MotionEvent.ACTION_DOWN -> {
 *                 gestureExample.onTouchDown(event.x, event.y)
 *             }
 *             MotionEvent.ACTION_MOVE -> {
 *                 gestureExample.onTouchMove(event.x, event.y)
 *             }
 *             MotionEvent.ACTION_UP -> {
 *                 val gesture = gestureExample.onTouchUp(event.x, event.y)
 *                 gestureExample.handleGesture(gesture)
 *             }
 *         }
 *         return true
 *     }
 * }
 * ```
 */
