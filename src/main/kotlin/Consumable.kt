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
    override var primaryColor: ColorRGBa,
    override var health: Double,
    override var damage: Double,
    override var mass: Double,
    override var speciesName: String,
    override var myFoodType: FoodType,
    var nutrition: Double
) : Entity

data class ConsumableType(
    var radius: Double,
    var primaryColor: ColorRGBa,
    var myShape: Shape,
    var health: Double,
    var damage: Double,
    var nutrition: Double,
    var mass: Double,
    var speciesName: String,
    var myFoodType: FoodType
)