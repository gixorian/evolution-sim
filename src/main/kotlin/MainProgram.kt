import kotlinx.coroutines.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extra.color.presets.ANTIQUE_WHITE
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import org.openrndr.writer

const val SCREEN_WIDTH = 1600
const val SCREEN_HEIGHT = 900

const val CANVAS_WIDTH = 1400
const val CANVAS_HEIGHT = 750

val CANVAS_ORIGIN = Vector2(SCREEN_WIDTH.toDouble() - CANVAS_WIDTH.toDouble(), 0.0)

//const val MAX_CREATURES = 250
const val CELL_SIZE = 5.0
const val SIMULATION_SPEED = 1.0
const val SIMULATION_TIME = 10.0

val INITIAL_CREATURE_TYPES = Pair<Int, Int>(5, 15)
val INITIAL_CREATURES_PER_TYPE = Pair<Int, Int>(10, 20)

val grid = mutableMapOf<Pair<Int, Int>, MutableList<Entity>>()
var creatures = mutableListOf<Creature>()
var consumables = mutableListOf<Consumable>()
val deadCreatures = mutableListOf<Creature>()
val deadConsumables = mutableListOf<Consumable>()

var selectedEntity: Entity? = null

fun main() = application {
    configure {
        width = SCREEN_WIDTH
        height = SCREEN_HEIGHT

        vsync = false
    }

    program {

        var frame = 0
        var debug = false
        var drawGrid = false
        var drawFPS = false
        var drawIntersections = false

        var simulationOver = false

        //val numCreatureTypes = Random.nextInt(10, 30)
        //val numCreatures = MAX_CREATURES / numCreatureTypes


        creatures = GeneticAlgorithm().generateInitialPopulation(INITIAL_CREATURE_TYPES, INITIAL_CREATURES_PER_TYPE) //EntityGenerator().generateCreatures(numCreatureTypes, numCreatures)
        consumables = EntityGenerator().generateConsumables(CONSUMABLE_TYPES, CONSUMABLES_PER_PATCH, CONSUMABLE_PATCHES, CONSUMABLE_PATCH_SIZE)

        keyboard.keyUp.listen {
            if (it.name == "d") debug = !debug
            if (it.name == "g") drawGrid = !drawGrid
            if (it.name == "f") drawFPS = !drawFPS
            if (it.name == "i") drawIntersections = !drawIntersections
        }

        mouse.buttonUp.listen {
            creatures.forEach { c ->
                if (isPointInCircle(mouse.position, c)) {
                    selectedEntity = c
                    return@listen
                }
            }

            consumables.forEach { c ->
                if (isPointInCircle(mouse.position, c)) {
                    selectedEntity = c
                    return@listen
                }
            }

            selectedEntity = null
        }

        // Background gradient
        val gradient = RadialGradient(
            color0 = ColorRGBa(0.282, 0.647, 0.855, alpha = 1.0),   // Center color
            color1 = ColorRGBa(0.235, 0.565, 0.8, alpha = 1.0),   // Edge color
            offset = Vector2(0.0, 0.0),          // Center position (normalized)
            exponent = 3.0                       // Falloff sharpness
        )

        extend {

            if ((seconds+1).toInt() % 5 == 0){
                // TODO: Genetic algorithm (Mate, Mutate, Generate new population)
            }

            if (simulationOver) {
                // Clear the screen
                drawer.clear(ColorRGBa.PINK)

                drawer.fill = ColorRGBa.BLACK
                drawer.fontMap = loadFont("data/fonts/default.otf", 72.0)

                writer {
                    box = Rectangle(50.0, 50.0, width.toDouble(), height.toDouble())
                    newLine()
                    text("Simulation Over")
                    newLine()
                    text("All creatures died!")
                }
                return@extend
            }

            frame++

            // Clear the screen
            drawer.shadeStyle = gradient
            drawer.rectangle(-5.0, -5.0, width.toDouble()+10.0, height.toDouble()+10.0)
            drawer.shadeStyle = null


            // Populating the grid cells with the creatures that are inside each cell
            grid.clear()

            val allEntities = creatures + consumables
            for (entity in allEntities) {
                val cell = toGridCell(entity.position, CELL_SIZE)
                grid.getOrPut(cell) { mutableListOf() }.add(entity)
            }

            // Draw debug helpers
            drawDebug(
                drawer,
                seconds = seconds,
                intersections = drawIntersections,
                sightRadius = debug,
                targetsInRange = debug,
                drawGrid = drawGrid,
                fps = drawFPS
            )

            // Process creatures concurrently
            launch {
                runBlocking {
                    creatures.chunked(1).map { chunk ->
                        async(Dispatchers.Default) {
                            chunk.forEach { creature ->
                                creature.process()
                            }
                        }
                    }.awaitAll()
                }
            }

            // Remove the dead creatures from the list of creatures
            if (creatures.isNotEmpty())
                creatures.removeAll(deadCreatures)
            else {
                simulationOver = true
                return@extend
            }

            if (consumables.isNotEmpty()) consumables.removeAll(deadConsumables)

            // Draw the creatures and consumables
            GraphicsSystem(drawer).drawGraphics()

            // Draw the GUI
            drawer.strokeWeight = 1.0
            drawer.stroke = ColorRGBa.fromHex("#262b2e")
            drawer.fill = ColorRGBa.fromHex("#353C40")
            // Sidebar
            drawer.rectangle(-2.0, -2.0, CANVAS_ORIGIN.x+2.0, SCREEN_HEIGHT.toDouble()+4.0)
            drawer.fill = ColorRGBa.ANTIQUE_WHITE
            // Bottom bar
            drawer.rectangle(SCREEN_WIDTH - CANVAS_WIDTH.toDouble() - 2.0, CANVAS_HEIGHT.toDouble(), SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble() +2.0)


            GraphicsSystem(drawer).drawStats() // Write the information of the selected Entity
            drawDebug(drawer, seconds, fps = drawFPS) // Draw the FPS
        }
    }
}


// Convert  x,y coordinates to a grid cell
fun toGridCell(pos: Vector2, cellSize: Double): Pair<Int, Int> {
    return (pos.x / cellSize).toInt() to (pos.y / cellSize).toInt()
}

// Function to check if a point is inside a circle
fun isPointInCircle(mousePosition: Vector2, entity: Entity): Boolean {
    val distance = mousePosition.distanceTo(entity.position)
    return distance <= entity.radius
}
