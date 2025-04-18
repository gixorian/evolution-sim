import kotlinx.coroutines.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.Writer
import org.openrndr.draw.loadFont
import org.openrndr.draw.writer
import org.openrndr.extra.color.presets.ANTIQUE_WHITE
import org.openrndr.extra.noise.random
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.*
import org.openrndr.writer
import java.awt.SystemColor.text
import kotlin.math.*
import kotlin.random.Random

const val SCREEN_WIDTH = 1600
const val SCREEN_HEIGHT = 900

const val CANVAS_WIDTH = 1400
const val CANVAS_HEIGHT = 750

val CANVAS_ORIGIN = Vector2(SCREEN_WIDTH.toDouble() - CANVAS_WIDTH.toDouble(), 0.0)

const val MAX_CREATURES = 250
const val CELL_SIZE = 5.0
const val SIMULATION_SPEED = 1.0

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

        val numCreatureTypes = Random.nextInt(10, 30)
        val numCreatures = MAX_CREATURES / numCreatureTypes


        creatures = generateCreatures(numCreatureTypes, numCreatures)
        consumables = generateConsumables(CONSUMABLE_TYPES, CONSUMABLES_PER_PATCH, CONSUMABLE_PATCHES, CONSUMABLE_PATCH_SIZE)

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

            currSeconds = seconds

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
                sightRadius = debug,
                targetsInRange = debug,
                drawGrid = drawGrid,
                intersections = drawIntersections
            )

            // Process creatures concurrently
            launch {
                runBlocking {
                    creatures.chunked(1).map { chunk ->
                        async(Dispatchers.Default) {
                            chunk.forEach { creature ->
                                processCreature(creature)
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
            drawGraphics(drawer)

            // Draw the GUI
            drawer.strokeWeight = 1.0
            drawer.stroke = ColorRGBa.fromHex("#262b2e")
            drawer.fill = ColorRGBa.fromHex("#353C40")
            // Sidebar
            drawer.rectangle(-2.0, -2.0, CANVAS_ORIGIN.x+2.0, SCREEN_HEIGHT.toDouble()+4.0)
            drawer.fill = ColorRGBa.ANTIQUE_WHITE
            // Bottom bar
            drawer.rectangle(SCREEN_WIDTH - CANVAS_WIDTH.toDouble() - 2.0, CANVAS_HEIGHT.toDouble(), SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble() +2.0)

            // Write the information of the selected Entity
            if(selectedEntity != null) {
                drawer.fill = ColorRGBa.BLACK
                drawer.fontMap = loadFont("data/fonts/default.otf", 20.0)

                writer {
                    box = Rectangle(
                        SCREEN_WIDTH - CANVAS_WIDTH.toDouble() - 2.0,
                        CANVAS_HEIGHT.toDouble(),
                        SCREEN_WIDTH.toDouble(),
                        SCREEN_HEIGHT.toDouble() + 2.0
                    )

                    if (selectedEntity is Creature) {
                        newLine()
                        text("Species: ${(selectedEntity as Creature).speciesName} \t\t\t Health: ${(selectedEntity as Creature).health}")
                        newLine()
                        text("Damage: ${(selectedEntity as Creature).damage} \t\t\t Energy: ${(selectedEntity as Creature).energy}")
                        newLine()
                        text("Mass: ${(selectedEntity as Creature).mass} \t\t\t Attack Cooldown: ${(selectedEntity as Creature).currAttackCooldown}")
                    } else if (selectedEntity is Consumable) {
                        newLine()
                        text("Species: ${(selectedEntity as Consumable).speciesName} \t\t\t Health: ${(selectedEntity as Consumable).health}")
                        newLine()
                        text("Damage: ${(selectedEntity as Consumable).damage} \t\t\t Mass: ${(selectedEntity as Consumable).mass}")
                        newLine()
                        text("Nutrition: ${(selectedEntity as Consumable).nutrition}")
                    } else {
                        newLine()
                        text("Species: \tHealth: ")
                        newLine()
                        text("Damage: \tMass: ")
                        newLine()
                        text("Energy: \tAttack Cooldown: ")
                    }
                }
            }

            // Display FPS
            if(drawFPS) {
                val currentTime = seconds
                frameC++
                if (currentTime - lastTime >= 0.25) { // Update once per second
                    fps = frameC.toDouble() / 0.25 // FPS is simply the number of frames per second
                    frameC = 0 // Reset frame count after calculating FPS
                    lastTime = currentTime // Reset the last time
                }

                drawer.fill = ColorRGBa.BLACK
                drawer.fontMap = loadFont("data/fonts/default.otf", 16.0)
                drawer.text("FPS: ${"%.1f".format(fps)}", 20.0, 30.0)
            }
        }
    }
}

fun drawGraphics(drawer: Drawer) {
    // Draw the creatures
    creatures.forEach { creature ->
        drawer.fill = creature.fillC.opacify(0.5)
        drawer.stroke = creature.strokeC.opacify(0.25)
        drawer.strokeWeight = 2.0
        drawer.shape(creature.myShape)

        // Nucleus drawing
        drawer.fill = creature.fillC.opacify(0.25)
        drawer.stroke = creature.strokeC.opacify(0.1)
        drawer.strokeWeight = 2.0
        drawer.circle(Vector2(Random.nextDouble(creature.position.x - 0.25, creature.position.x + 0.25),
            Random.nextDouble(creature.position.y - 0.25, creature.position.y + 0.25)),
            creature.radius / 2.5)
    }

    // Draw the consumables
    consumables.forEach { consumable ->
        drawer.fill = consumable.fillC.opacify(0.8)
        drawer.stroke = consumable.strokeC.opacify(0.25)
        drawer.strokeWeight = 1.5
        drawer.circle(consumable.position, consumable.radius)
    }
}

// Convert  x,y coordinates to a grid cell
fun toGridCell(pos: Vector2, cellSize: Double): Pair<Int, Int> {
    return (pos.x / cellSize).toInt() to (pos.y / cellSize).toInt()
}

// Get creature's neighbours - neighbours are all creatures within grid cells that the creature can reach with their sight range
fun getNeighbors(creature: Creature, cellSize: Double, radiusCells: Int): List<Entity> {
    val center = toGridCell(creature.position, cellSize)
    val neighbors = mutableListOf<Entity>()

    // Check if there are any creatures within the grid cells that this creature's sight range can reach and add them to the list of it's neighbours
    for (dx in -radiusCells..radiusCells) {
        for (dy in -radiusCells..radiusCells) {
            val cell = (center.first + dx) to (center.second + dy)
            grid[cell]?.let { cellEntities ->
                neighbors += cellEntities.filter { it != creature }
            }
        }
    }
    return neighbors
}

// Process the collision detection and find the creatures in range for each  creature
fun processCreature(creature: Creature) {

    if (!creature.isAlive) {
        deadCreatures.add(creature)
        return
    }

    tickCreatureStats(creature)

    val radiusCells = (creature.sightDistance / CELL_SIZE).toInt() + 1
    val nearbyEntities = getNeighbors(creature, CELL_SIZE, radiusCells)

    // Perform collision checks and target detection
    collisionDetection(creature, nearbyEntities)

    // Perform position update and blob morphing
    updateCreatureMovement(creature, nearbyEntities)
}

fun tickCreatureStats (creature: Creature) {
    creature.energy -= (creature.mass * creature.speed * 0.0025) * SIMULATION_SPEED

    if (creature.energy <= 0.0) {
        creature.isAlive = false
        deadCreatures.add(creature)
        return
    }

    creature.currAttackCooldown -= 0.01
    if (creature.currAttackCooldown <= 0.0) creature.currAttackCooldown = 0.0
}

fun targetDetection (creature: Creature, other: Entity): Boolean {

    // Calculate if the other creature is withing this creature's sight cone
    val posDifference = (other.position - creature.position).normalized
    val sightCenter = creature.direction.normalized
    val angle = acos(posDifference.dot(sightCenter))
    val sightAngleRadians = Math.toRadians(creature.sightRadius / 2.0)

    if (angle < sightAngleRadians) {
        creature.targetsInRange.add(other) // If it is, add it to this creature's targets in range list
        return true
    }

    return false
}

fun collisionDetection (creature: Creature, nearbyEntities: List<Entity>) {

    creature.intersections = emptyList()
    creature.targetsInRange.clear()

    // Creature logic: intersection checks, movement, etc.
    for (other in nearbyEntities) {
        if (creature == other || !other.isAlive) continue

        // Only check for intersections if it's possible for the two creatures to be close enough to collide
        if (other.position.distanceTo(creature.position) <= (creature.radius + creature.sightDistance)) {

            // Add the intersections between these two creatures to the list of all intersections of this creature
            creature.intersections += creature.myShape.intersections(other.myShape)

            if (targetDetection(creature, other)) {
                // Sanity check to make sure that the other creature really is withing this creature's sight cone and there are intersections between them
                if (creature.myShape.intersections(other.myShape).isNotEmpty())
                // At this point we know that the creatures are colliding and that the other creature is withing this creature's sight cone so we are processing the damage between them
                    processDamage(creature, other)
            }
        }
    }
}

// Process damage on collision
fun processDamage(attacker: Creature, defender: Entity) {
    if(attacker.currAttackCooldown > 0.0) return

    attacker.currAttackCooldown  = attacker.attackCooldown
    defender.health -= attacker.damage

    attacker.attack(defender)
}

// Updates the position and direction for all creatures
fun updateCreatureMovement(creature: Creature, nearbyEntities: List<Entity>) {
    creature.direction = creature.direction.normalized * creature.speed

    // Direction change logic
    if (creature.changeDirectionCooldown <= 0) {

        val newDirection = getNewDirection(creature)

        // Smoothly interpolate towards the new direction
        val smoothFactor = 0.5 // Change this value to make the transition more or less smooth
        creature.direction = creature.direction * (1 - smoothFactor) + newDirection * smoothFactor

        // Reset the cooldown for direction change
        creature.changeDirectionCooldown = Random.nextInt(60, 120) / SIMULATION_SPEED.toInt()  //Random.nextInt(60, 120) // Change direction every 60 to 120 frames
    } else {
        // Decrease the cooldown timer by one each frame
        creature.changeDirectionCooldown--
    }

    // If there is a collision (list of intersections is not empty), resolves the collision, otherwise continues along its direction vector
    if (creature.intersections.isNotEmpty()) {
        creature.position = resolveCollision(creature, nearbyEntities, creature.position, creature.position + creature.direction * creature.speed)
    } else {
        creature.position += creature.direction * creature.speed
    }

    updateBlobMorphFrame(creature)

    checkEdgeCollision(creature) // Checks for screen edge collisions

    creature.myShape = moveShapeTo(creature.myShape, creature.position) // Updates the creature's shape position to match the creatures position
}

fun updateBlobMorphFrame(creature: Creature) {
    // Update the Blob morphing animation frame
    val (newState, newShape) = updateBlobMorph(
        creature.blobMorphState,
        center = creature.position,
        radius = Random.nextDouble(creature.radius-1.0, creature.radius+1.01),
        speed = creature.morphSpeed
    )

    creature.blobMorphState = newState
    creature.myShape = newShape
}

fun getNewDirection (creature: Creature): Vector2 {
    var randDecision = 0
    var biasedDecision = 0

    if (creature.targetsInRange.isEmpty()) {
        biasedDecision = 0
        creature.target = null
    }
    else {
        randDecision = Random.nextInt(0, 3)
        biasedDecision = if (random() >= creature.behaviourBiasWeight) creature.behaviourBias else randDecision

        if (biasedDecision != 0){

            creature.target =
                if (creature.target == null || !creature.target!!.isAlive)
                    creature.targetsInRange[Random.nextInt(0, creature.targetsInRange.size)]
                else
                    creature.target!!
        }
    }

    when (biasedDecision) {
        0 -> { // Random Movement
            return Vector2(
                Random.nextDouble(-1.0, 1.0),
                Random.nextDouble(-1.0, 1.0)
            ).normalized
        }
        1 -> { // Move towards a target
            return (creature.target!!.position - creature.position).normalized
        }
        2 -> { // Move away from a target
            return (creature.target!!.position - creature.position).normalized * -1.0
        }
    }

    return Vector2.ZERO
}

fun moveShapeTo(shape: Shape, target: Vector2): Shape {
    // Get current shape center
    val bounds = shape.bounds
    val currentCenter = bounds.center

    // Compute offset
    val offset = target - currentCenter

    // Apply translation
    return shape.transform(transform {
        translate(offset)
    })
}

// Function to check if a point is inside a circle
fun isPointInCircle(mousePosition: Vector2, entity: Entity): Boolean {
    val distance = mousePosition.distanceTo(entity.position)
    return distance <= entity.radius
}
