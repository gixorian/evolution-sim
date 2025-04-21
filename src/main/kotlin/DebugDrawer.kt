import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.draw.loadFont
import org.openrndr.math.Vector2
import org.openrndr.shape.contour

fun drawTargetLines(drawer: Drawer, creature: Creature, opacity: Double = 0.35, strokeWeight: Double = 0.25){
    if (creature.targetsInRange.isEmpty()) return

    drawer.strokeWeight = 0.5
    drawer.stroke = ColorRGBa.BLACK
    drawer.lineCap = LineCap.ROUND

    val start = creature.position

    for (other in creature.targetsInRange) {

        if (other == creature.target) {
            drawer.stroke = ColorRGBa.RED.opacify(opacity * 2.0)
            drawer.strokeWeight = 1.5
        } else {
            drawer.stroke = creature.primaryColor.opacify(opacity)
            drawer.strokeWeight = strokeWeight
        }

        val end = other.position
        drawer.lineSegment(start, end)
    }
}

fun drawSightRadius(drawer: Drawer, creature: Creature, opacity: Double = 0.05){
    drawer.strokeWeight = 0.05
    drawer.stroke = null
    drawer.fill = ColorRGBa.fromHex("#ecec79").opacify(opacity)

    val start = creature.position
    val end = start + creature.direction.normalized * creature.sightDistance
    val sightLeft = end.rotate(-(creature.sightRadius/2.0), creature.position)
    val sightRight = end.rotate((creature.sightRadius/2.0), creature.position)

    val sightC = contour {
        moveTo(sightRight)
        lineTo(start)
        lineTo(sightLeft)
        circularArcTo(end, sightRight)
        close()
    }

    drawer.contour(sightC)
}

fun drawDirectionLine(drawer: Drawer, creature: Creature) {
    drawer.strokeWeight = 0.5
    drawer.stroke = ColorRGBa.BLACK
    drawer.lineCap = LineCap.ROUND

    val start = creature.position
    val end = start + creature.direction.normalized * 50.0

    drawer.lineSegment(start, end)
}

fun drawIntersections(drawer: Drawer, creature: Creature) {
    drawer.fill = ColorRGBa.WHITE
    drawer.stroke = ColorRGBa.BLACK.opacify(0.5)
    drawer.strokeWeight = 0.5

    drawer.circles(creature.intersections.map {
        it.position
    }, 2.5)
}

fun drawGrid(drawer: Drawer) {
    drawer.stroke = ColorRGBa.GRAY.opacify(0.5)
    drawer.strokeWeight = 1.0

    // Vertical lines
    for (x in CANVAS_ORIGIN.x.toInt()..CANVAS_ORIGIN.x.toInt() + CANVAS_WIDTH step CELL_SIZE.toInt()) {
        drawer.lineSegment(Vector2(x.toDouble(), CANVAS_ORIGIN.y), Vector2(x.toDouble(), CANVAS_ORIGIN.y + CANVAS_HEIGHT))
    }

    // Horizontal lines
    for (y in CANVAS_ORIGIN.y.toInt()..CANVAS_ORIGIN.y.toInt() + CANVAS_HEIGHT step CELL_SIZE.toInt()) {
        drawer.lineSegment(Vector2(CANVAS_ORIGIN.x, y.toDouble()), Vector2(CANVAS_ORIGIN.x + CANVAS_WIDTH, y.toDouble()))
    }
}

var lastTime = 0.0
var fps = 0.0
var frameC = 0
var currSeconds = 0.0

fun drawFPS(drawer: Drawer, seconds: Double) {
    frameC++
    if (seconds - lastTime >= 0.25) { // Update once per second
        fps = frameC.toDouble() / 0.25 // FPS is simply the number of frames per second
        frameC = 0 // Reset frame count after calculating FPS
        lastTime = seconds // Reset the last time
    }

    drawer.fill = ColorRGBa.BLACK
    drawer.fontMap = loadFont("data/fonts/default.otf", 16.0)
    drawer.text("FPS: ${"%.1f".format(fps)}", CANVAS_ORIGIN.x + 5.0, CANVAS_ORIGIN.y + 15.0)
}

// Function for drawing various debugging helpers
fun drawDebug(drawer: Drawer, seconds: Double = 0.0, intersections: Boolean = false, direction: Boolean = false, sightRadius: Boolean = false, targetsInRange: Boolean = false, drawGrid: Boolean = false, fps: Boolean = false, panOffset: Vector2 = Vector2.ZERO, zoom: Double = 1.0){


    if (drawGrid) drawGrid(drawer)
    if (fps) drawFPS(drawer, seconds)

    // Apply zoom and pan transformations to all debug drawings
    drawer.pushTransforms()
    drawer.translate(panOffset)
    drawer.scale(zoom)

    if (selectedEntity != null){
        if (selectedEntity is Creature) {
            val creature = selectedEntity as Creature

            if (sightRadius) drawSightRadius(drawer, creature, 0.5)
            if (intersections) drawIntersections(drawer, creature)
            if (direction) drawDirectionLine(drawer, creature)
            if (targetsInRange) drawTargetLines(drawer, creature, 0.8, 1.0)

            drawer.popTransforms()
            return
        }
    }

    for (c in creatures) {
        if (sightRadius) drawSightRadius(drawer, c)
        if (intersections) drawIntersections(drawer, c)
        if (direction) drawDirectionLine(drawer, c)
        if (targetsInRange) drawTargetLines(drawer, c)
    }

    drawer.popTransforms()  // Pop the transformation after drawing all creatures
}