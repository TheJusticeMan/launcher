package de.jrpie.android.launcher.ui

import kotlin.math.atan2
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detects arbitrary gestures by comparing the shape (sequence of angles) of user input
 * against pre-recorded gesture paths.
 */
class ArbitraryGestureDetector {
    
    companion object {
        private const val MIN_POINTS = 2
        private const val MIN_LENGTH = 100f
        private const val RESAMPLE_POINT_COUNT = 40
        private const val ANGLE_THRESHOLD = 0.5 // radians
    }
    
    /**
     * Represents a 2D point/offset
     */
    data class Offset(val x: Float, val y: Float)
    
    /**
     * Represents a gesture command with its preset path
     */
    data class GestureCommand(val name: String, val path: List<Offset>)
    
    /**
     * Current line being drawn
     */
    private var line = mutableListOf<Offset>()
    
    /**
     * List of pre-recorded gesture commands
     */
    private val gestureCommands = mutableListOf<GestureCommand>()
    
    /**
     * Adds a point to the current gesture path
     */
    fun addPoint(x: Float, y: Float) {
        line.add(Offset(x, y))
    }
    
    /**
     * Clears the current gesture path
     */
    fun startDrag() {
        line.clear()
    }
    
    /**
     * Ends the current drag and attempts to detect a gesture
     * @return The detected gesture name, or null if no match found or gesture too short
     */
    fun endDrag(): String? {
        // Filtering: reject if too few points or too short
        if (line.size < MIN_POINTS) {
            return null
        }
        
        val totalLength = calculatePathLength(line)
        if (totalLength < MIN_LENGTH) {
            return null
        }
        
        // Normalize and detect
        val normalizedInput = normalizeLine(line)
        val detectedGesture = detectGesture(normalizedInput)
        
        // Clear the line for next gesture
        line.clear()
        
        return detectedGesture
    }
    
    /**
     * Registers a gesture command with its preset path
     */
    fun addGestureCommand(name: String, path: List<Offset>) {
        gestureCommands.add(GestureCommand(name, path))
    }
    
    /**
     * Clears all registered gesture commands
     */
    fun clearGestureCommands() {
        gestureCommands.clear()
    }
    
    /**
     * Calculates the total length of a path
     */
    private fun calculatePathLength(path: List<Offset>): Float {
        var length = 0f
        for (i in 1 until path.size) {
            length += distance(path[i-1], path[i])
        }
        return length
    }
    
    /**
     * Calculates the distance between two points
     */
    private fun distance(p1: Offset, p2: Offset): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Normalizes a line by:
     * 1. Translating to origin (first point at 0,0)
     * 2. Resampling to fixed number of points
     */
    private fun normalizeLine(path: List<Offset>): List<Offset> {
        if (path.isEmpty()) return emptyList()
        
        // Translation: shift so first point is at (0, 0)
        val firstPoint = path[0]
        val translated = path.map { Offset(it.x - firstPoint.x, it.y - firstPoint.y) }
        
        // Resampling: convert to fixed number of points
        return resample(translated, RESAMPLE_POINT_COUNT)
    }
    
    /**
     * Resamples a path to have a fixed number of points at equal intervals
     */
    private fun resample(path: List<Offset>, numPoints: Int): List<Offset> {
        if (path.isEmpty()) return emptyList()
        if (path.size == 1) return List(numPoints) { path[0] }
        
        val totalLength = calculatePathLength(path)
        if (totalLength == 0f) return List(numPoints) { path[0] }
        
        val intervalLength = totalLength / (numPoints - 1)
        val resampled = mutableListOf<Offset>()
        resampled.add(path[0])
        
        var accumulatedDistance = 0f
        var currentSegmentIndex = 0
        
        for (i in 1 until numPoints - 1) {
            val targetDistance = i * intervalLength
            
            while (accumulatedDistance < targetDistance && currentSegmentIndex < path.size - 1) {
                val segmentLength = distance(path[currentSegmentIndex], path[currentSegmentIndex + 1])
                
                if (accumulatedDistance + segmentLength >= targetDistance) {
                    // Interpolate within this segment
                    val remainingDistance = targetDistance - accumulatedDistance
                    val t = remainingDistance / segmentLength
                    val p1 = path[currentSegmentIndex]
                    val p2 = path[currentSegmentIndex + 1]
                    val interpolated = Offset(
                        p1.x + t * (p2.x - p1.x),
                        p1.y + t * (p2.y - p1.y)
                    )
                    resampled.add(interpolated)
                    break
                } else {
                    accumulatedDistance += segmentLength
                    currentSegmentIndex++
                }
            }
        }
        
        // Add the last point
        resampled.add(path.last())
        
        return resampled
    }
    
    /**
     * Detects a gesture by comparing with all preset gestures
     * @return The name of the best matching gesture, or null if no match found
     */
    private fun detectGesture(normalizedInput: List<Offset>): String? {
        if (gestureCommands.isEmpty() || normalizedInput.size < 2) {
            return null
        }
        
        var bestMatch: String? = null
        var minDiff = Float.MAX_VALUE
        
        for (command in gestureCommands) {
            val normalizedPreset = normalizeLine(command.path)
            val diff = calculateDifference(normalizedInput, normalizedPreset)
            
            if (diff < minDiff) {
                minDiff = diff
                bestMatch = command.name
            }
        }
        
        // Only accept if difference is below threshold
        return if (minDiff < ANGLE_THRESHOLD) bestMatch else null
    }
    
    /**
     * Calculates the average angular difference between two paths
     */
    private fun calculateDifference(path1: List<Offset>, path2: List<Offset>): Float {
        if (path1.size < 2 || path2.size < 2) {
            return Float.MAX_VALUE
        }
        
        val minSize = minOf(path1.size, path2.size)
        var totalDiff = 0.0
        var count = 0
        
        for (i in 0 until minSize - 1) {
            // Calculate angle for path1 segment
            val dx1 = path1[i + 1].x - path1[i].x
            val dy1 = path1[i + 1].y - path1[i].y
            val angle1 = atan2(dy1.toDouble(), dx1.toDouble())
            
            // Calculate angle for path2 segment
            val dx2 = path2[i + 1].x - path2[i].x
            val dy2 = path2[i + 1].y - path2[i].y
            val angle2 = atan2(dy2.toDouble(), dx2.toDouble())
            
            // Calculate absolute difference between angles
            var diff = abs(angle1 - angle2)
            
            // Normalize to [-π, π]
            if (diff > Math.PI) {
                diff = 2 * Math.PI - diff
            }
            
            totalDiff += diff
            count++
        }
        
        return if (count > 0) (totalDiff / count).toFloat() else Float.MAX_VALUE
    }
}
