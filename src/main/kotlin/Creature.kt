import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.ContourIntersection
import org.openrndr.shape.Shape
import kotlin.random.Random

data class Creature(
    override var position: Vector2,
    override var radius: Double,
    override var myShape: Shape,
    override var intersections: List<ContourIntersection>,
    override var fillC: ColorRGBa,
    override var strokeC: ColorRGBa,
    override var isAlive: Boolean = true,
    override var mass: Double,
    override var health: Double,
    override var damage: Double,
    override var speciesName: String,
    var direction: Vector2,
    var speed: Double,
    var blobMorphState: BlobMorphState,
    var changeDirectionCooldown: Int,
    var sightDistance: Double,
    var sightRadius: Double,
    var targetsInRange: MutableList<Entity>,
    var target: Entity? = null,
    var morphSpeed: Double,
    val attackCooldown: Double,
    var currAttackCooldown: Double = 0.0,
    var energy: Double,
    var behaviourBias: Int,
    var behaviourBiasWeight: Double
) : Entity {

    fun attack (other: Entity) {
        if (other is Creature) energy -= other.mass * Random.nextDouble(0.01, 0.025)
        else if (other is Consumable) energy += other.nutrition

        if(other.health <= 0.0) { // The defending entity is dead
            other.isAlive = false

            if(other is Creature) {
                deadCreatures.add(other)
                // Generate food when a creature dies
                consumables.addAll(generateConsumables(1, (other.mass * 0.5).toInt(), 1, other.radius, other.position, other.fillC))
            } else if (other is Consumable) {
                deadConsumables.add(other)
            }
        }
    }
}
