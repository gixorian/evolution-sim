import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.loadFont
import org.openrndr.draw.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.random.Random

class GraphicsSystem (private val drawer: Drawer){

    fun drawStats() {
        if(selectedEntity != null) {
            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = loadFont("data/fonts/default.otf", 20.0)

            drawer.writer {
                box = Rectangle(
                    SCREEN_WIDTH - CANVAS_WIDTH.toDouble(),
                    CANVAS_HEIGHT.toDouble(),
                    SCREEN_WIDTH.toDouble(),
                    SCREEN_HEIGHT.toDouble()
                )

                when (selectedEntity) {
                    is Creature -> {
                        newLine()
                        text("Species: ${(selectedEntity as Creature).speciesName}")
                        newLine()
                        text("Health: ${(selectedEntity as Creature).health}")
                        newLine()
                        text("Damage: ${(selectedEntity as Creature).damage}")
                        newLine()
                        text("Energy: ${(selectedEntity as Creature).energy}")
                        newLine()
                        text("Mass: ${(selectedEntity as Creature).mass}")
                        newLine()
                        text("Attack Cooldown: ${(selectedEntity as Creature).currAttackCooldown}")
                    }

                    is Consumable -> {
                        newLine()
                        text("Species: ${(selectedEntity as Consumable).speciesName}")
                        newLine()
                        text("Health: ${(selectedEntity as Consumable).health}")
                        newLine()
                        text("Damage: ${(selectedEntity as Consumable).damage}")
                        newLine()
                        text("Mass: ${(selectedEntity as Consumable).mass}")
                        newLine()
                        text("Nutrition: ${(selectedEntity as Consumable).nutrition}")
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
            }
        }
    }

    fun drawGraphics() {
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
    }
}