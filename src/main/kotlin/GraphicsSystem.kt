import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.loadFont
import org.openrndr.draw.writer
import org.openrndr.extra.color.presets.ANTIQUE_WHITE
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.random.Random

class GraphicsSystem (private val drawer: Drawer){

    var zoom = 1.0
    var panOffset = Vector2.ZERO

    fun screenToWorld(screenPos: Vector2): Vector2 {
        return (screenPos - panOffset) / zoom
    }

    fun worldToScreen(worldPos: Vector2): Vector2 {
        return worldPos * zoom + panOffset
    }

    fun drawStats() {

        if(selectedEntity != null) {
            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = loadFont("data/fonts/default.otf", 20.0)

            drawer.writer {
                box = Rectangle(
                    CANVAS_ORIGIN.x,
                    CANVAS_HEIGHT.toDouble(),
                    CANVAS_WIDTH.toDouble() / 4.0,
                    SCREEN_HEIGHT.toDouble()
                )

                when (selectedEntity) {
                    is Creature -> {
                        val creature = selectedEntity as Creature
                        newLine()
                        text("Species: ${creature.speciesName}")
                        newLine()
                        text("Health: ${creature.health}")
                        newLine()
                        text("Damage: ${creature.damage}")
                        newLine()
                        text("Energy: ${creature.energy}")
                        newLine()
                        text("Mass: ${creature.mass}")
                        newLine()
                        text("Attack Cooldown: ${creature.currAttackCooldown}")
                        newLine()
                        text("Food type: ${creature.myFoodType}")
                    }

                    is Consumable -> {
                        val consumable = selectedEntity as Consumable
                        newLine()
                        text("Species: ${consumable.speciesName}")
                        newLine()
                        text("Health: ${consumable.health}")
                        newLine()
                        text("Damage: ${consumable.damage}")
                        newLine()
                        text("Mass: ${consumable.mass}")
                        newLine()
                        text("Nutrition: ${consumable.nutrition}")
                    }

                    else -> {
                        newLine()
                        text("Species: \tHealth: ")
                        newLine()
                        text("Damage: \tMass: ")
                        newLine()
                        text("Energy: \tAttack Cooldown: ")
                    }
                }

                box = Rectangle(
                    CANVAS_ORIGIN.x + (CANVAS_WIDTH.toDouble() / 4.0),
                    CANVAS_HEIGHT.toDouble(),
                    CANVAS_WIDTH.toDouble(),
                    SCREEN_HEIGHT.toDouble()
                )

                when (selectedEntity) {
                    is Creature -> {
                        val creature = selectedEntity as Creature
                        newLine()
                        text("Food type: ${creature.myFoodType}")
                        newLine()
                        text("Diet: ${creature.myDiet}")
                        newLine()
                        text("Behaviour bias: ${creature.behaviourBias}")
                        newLine()
                        text("Behaviour bias weight: ${creature.behaviourBiasWeight}")
                        newLine()
                        text("Current target: ${creature.target?.speciesName}")
                    }

                    is Consumable -> {
                        val consumable = selectedEntity as Consumable
                        newLine()
                        text("Food type: ${consumable.myFoodType}")
                    }

                    else -> {
                    }
                }
            }
        }
    }

    fun drawGUI() {
        // Draw the GUI
        drawer.strokeWeight = 1.0
        drawer.stroke = ColorRGBa.fromHex("#262b2e")
        drawer.fill = ColorRGBa.fromHex("#353C40")
        // Sidebar
        drawer.rectangle(-2.0, -2.0, CANVAS_ORIGIN.x+2.0, SCREEN_HEIGHT.toDouble()+4.0)
        drawer.fill = ColorRGBa.ANTIQUE_WHITE
        // Bottom bar
        drawer.rectangle(SCREEN_WIDTH - CANVAS_WIDTH.toDouble() - 2.0, CANVAS_HEIGHT.toDouble(), SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble() +2.0)
    }

    fun drawGraphics() {

        drawer.pushTransforms()

        // Apply pan + zoom
        drawer.translate(panOffset)
        drawer.scale(zoom)

        // Draw the creatures
        creatures.forEach { creature ->
            drawer.fill = creature.primaryColor.opacify(0.5)
            drawer.stroke = creature.primaryColor.opacify(0.25)
            drawer.strokeWeight = 2.0
            drawer.shape(creature.myShape)

            // Nucleus drawing
            drawer.fill = creature.primaryColor.opacify(0.25)
            drawer.stroke = creature.primaryColor.opacify(0.1)
            drawer.strokeWeight = 2.0
            drawer.circle(
                Vector2(
                    Random.nextDouble(creature.position.x - 0.25, creature.position.x + 0.25),
                Random.nextDouble(creature.position.y - 0.25, creature.position.y + 0.25)),
                creature.radius / 2.5)
        }

        // Draw the consumables
        consumables.forEach { consumable ->
            drawer.fill = consumable.primaryColor.opacify(0.8)
            drawer.stroke = consumable.primaryColor.opacify(0.25)
            drawer.strokeWeight = 1.5
            drawer.circle(consumable.position, consumable.radius)
        }

        drawer.popTransforms()
    }
}

