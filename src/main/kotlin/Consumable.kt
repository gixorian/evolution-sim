import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.ContourIntersection
import org.openrndr.shape.Shape

data class Consumable(
    override var position: Vector2,
    override var radius: Double,
    override var myShape: Shape,
    override var intersections: List<ContourIntersection>,
    override var isAlive: Boolean = true,
    override var fillC: ColorRGBa,
    override var strokeC: ColorRGBa,
    override var health: Double,
    override var damage: Double,
    override var mass: Double,
    override var speciesName: String,
    var nutrition: Double
) : Entity

data class ConsumableType(
    var radius: Double,
    var strokeC: ColorRGBa,
    var myShape: Shape,
    var health: Double,
    var damage: Double,
    var nutrition: Double,
    var mass: Double,
    var speciesName: String,
)