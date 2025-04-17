import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class BlobMorphState(
    val startPoints: List<Vector2>,
    val endPoints: List<Vector2>,
    val t: Double
)

fun generateBlob(center: Vector2, radius: Double, count: Int): List<Vector2> {
    return List(count) { i ->
        val angle = i * (2 * PI) / count
        val r = radius * Random.nextDouble(0.85, 1.15)
        center + Vector2(cos(angle), sin(angle)) * r
    }
}

fun resamplePoints(points: List<Vector2>, targetCount: Int): List<Vector2> {
    val contour = ShapeContour.fromPoints(points, closed = true)
    return (0 until targetCount).map { i ->
        contour.position(i / targetCount.toDouble())
    }
}

fun interpolatePoints(a: List<Vector2>, b: List<Vector2>, t: Double): List<Vector2> {
    return a.zip(b).map { (pa, pb) -> pa * (1.0 - t) + pb * t }
}

fun catmullRomShape(points: List<Vector2>): Shape {
    val contour = contour {
        moveTo(points[0])
        for (i in points.indices) {
            val p0 = points[(i - 1 + points.size) % points.size]
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            val p3 = points[(i + 2) % points.size]

            val cp1 = p1 + (p2 - p0) * (1.0 / 6.0)
            val cp2 = p2 - (p3 - p1) * (1.0 / 6.0)

            curveTo(cp1, cp2, p2)
        }
        close()
    }
    return Shape(listOf(contour))
}
fun updateBlobMorph(
    state: BlobMorphState,
    center: Vector2,
    radius: Double,
    speed: Double = 0.05,
    resampleCount: Int = 8
): Pair<BlobMorphState, Shape> {
    val a = resamplePoints(state.startPoints, resampleCount)
    val b = resamplePoints(state.endPoints, resampleCount)
    val interpolated = interpolatePoints(a, b, state.t)

    val newT = state.t + speed

    val (newStart, newEnd, resetT) = if (newT >= 1.0) {
        Triple(state.endPoints, generateBlob(center, radius, listOf(6, 8, 12, 16).random()), 0.0)
    } else {
        Triple(state.startPoints, state.endPoints, newT)
    }

    val newState = BlobMorphState(newStart, newEnd, resetT)
    val shape = catmullRomShape(interpolated)

    return newState to shape
}