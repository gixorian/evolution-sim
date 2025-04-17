import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.draw.loadFont
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

var lastTime = 0.0
var fps = 0.0
var frameC = 0
var currSeconds = 0.0

// Function for drawing various debugging helpers
fun drawDebug(drawer: Drawer, intersections: Boolean = false, direction: Boolean = false, sightRadius: Boolean = false, targetsInRange: Boolean = false, drawGrid: Boolean = false, drawFPS: Boolean = false){

    for (c in creatures) {

        if (drawGrid) {
            drawer.stroke = ColorRGBa.GRAY.opacify(0.25)
            drawer.strokeWeight = 1.0

            // Vertical lines
            for (x in 0..SCREEN_WIDTH step CELL_SIZE.toInt()) {
                drawer.lineSegment(Vector2(x.toDouble(), 0.0), Vector2(x.toDouble(), SCREEN_HEIGHT.toDouble()))
            }

            // Horizontal lines
            for (y in 0..SCREEN_HEIGHT step CELL_SIZE.toInt()) {
                drawer.lineSegment(Vector2(0.0, y.toDouble()), Vector2(SCREEN_WIDTH.toDouble(), y.toDouble()))
            }
        }

        if (intersections) {
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = ColorRGBa.BLACK.opacify(0.5)
            drawer.strokeWeight = 0.5

            drawer.circles(c.intersections.map {
                it.position
            }, 2.5)
        }

        if (direction) {
            drawer.strokeWeight = 0.5
            drawer.stroke = ColorRGBa.BLACK
            drawer.lineCap = LineCap.ROUND

            val start = c.position
            val end = start + c.direction.normalized * 50.0

            drawer.lineSegment(start, end)
        }

        if (sightRadius) {
            drawer.strokeWeight = 0.05
            drawer.stroke = null
            drawer.fill = ColorRGBa.fromHex("#ecec79").opacify(0.1)

            val start = c.position
            val end = start + c.direction.normalized * c.sightDistance
            val sightLeft = end.rotate(-(c.sightRadius/2.0), c.position)
            val sightRight = end.rotate((c.sightRadius/2.0), c.position)

            val sightC = contour {
                moveTo(sightRight)
                lineTo(start)
                lineTo(sightLeft)
                circularArcTo(end, sightRight)
                close()
            }

            drawer.contour(sightC)
        }

        if (targetsInRange) {

            if (c.targetsInRange.isEmpty()) continue

            drawer.strokeWeight = 0.5
            drawer.stroke = ColorRGBa.BLACK
            drawer.lineCap = LineCap.ROUND

            val start = c.position

            for (other in c.targetsInRange) {
                //if (!other.isAlive) continue

                if (other == c.target) {
                    drawer.stroke = ColorRGBa.RED
                    drawer.strokeWeight = 1.5
                } else {
                    drawer.stroke = c.strokeC
                    drawer.strokeWeight = 0.5
                }

                val end = other.position
                drawer.lineSegment(start, end)
            }
        }

        // Display FPS
//        if(drawFPS) {
//            val currentTime = currSeconds
//            frameC++
//            if (currentTime - lastTime >= 0.25) { // Update once per second
//                fps = frameC.toDouble() / 0.25 // FPS is simply the number of frames per second
//                frameC = 0 // Reset frame count after calculating FPS
//                lastTime = currentTime // Reset the last time
//            }
//
//            drawer.fill = ColorRGBa.BLACK
//            drawer.fontMap = loadFont("data/fonts/default.otf", 16.0)
//            drawer.text("FPS: ${"%.1f".format(fps)}", 20.0, 30.0)
//        }
    }
}