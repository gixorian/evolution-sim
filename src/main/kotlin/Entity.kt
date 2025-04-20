import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.ContourIntersection
import org.openrndr.shape.Shape

interface Entity {
    var position: Vector2
    var radius: Double
    var myShape: Shape
    var intersections: List<ContourIntersection>
    var isAlive: Boolean
    var primaryColor: ColorRGBa
    var health: Double
    var damage: Double
    var mass: Double
    var speciesName:  String
}