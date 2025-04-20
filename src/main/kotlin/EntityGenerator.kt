import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import java.lang.Math.random
import kotlin.random.Random

const val CONSUMABLE_TYPES = 5
const val CONSUMABLES_PER_PATCH = 5
const val CONSUMABLE_PATCHES = 10
const val CONSUMABLE_PATCH_SIZE = 100.0

class EntityGenerator () {

    fun generateConsumables (numConsumablesTypes: Int, numConsumablesPerPatch: Int, numConsumablePatches: Int = 1, patchSize: Double = CANVAS_HEIGHT.toDouble(), spawnPosition: Vector2? = null, color: ColorRGBa? = null, species: String? = null): MutableList<Consumable> {
        val newConsumables = mutableListOf<Consumable>()

        val consumableTypes = mutableListOf<ConsumableType>()

        for (i in 0 until numConsumablesTypes) {

            val radius = Random.nextDouble(4.0, 8.0)
            val mass = radius * Random.nextDouble(0.8, 1.2)

            val newColor = color ?: ColorRGBa(random(), random(), random())
            val speciesName = species ?: generatePlantGenusSpeciesName()

            val t = ConsumableType(
                radius = Random.nextDouble(3.0, 5.0),
                primaryColor = newColor,
                myShape = Circle(0.0, 0.0, radius).shape,
                health = mass * Random.nextDouble(0.1, 0.5),
                damage = 0.0,
                nutrition = mass * Random.nextDouble(1.0, 2.0),
                mass = mass,
                speciesName = speciesName
            )

            consumableTypes.add(t)
        }

        for (i in 0 until numConsumablePatches) {

            val consumableType = consumableTypes[Random.nextInt(0, consumableTypes.size)]

            val centerPos =
                spawnPosition
                    ?: Vector2(
                        Random.nextDouble(CANVAS_ORIGIN.x + (patchSize + consumableType.radius), CANVAS_ORIGIN.x + CANVAS_WIDTH - patchSize - consumableType.radius),
                        Random.nextDouble(CANVAS_ORIGIN.y + (patchSize + consumableType.radius), CANVAS_ORIGIN.y + CANVAS_HEIGHT - patchSize - consumableType.radius)
                    )

            for (j in 0 until numConsumablesPerPatch) {

                val c = Consumable(
                    position = Vector2(
                        Random.nextDouble(centerPos.x - (patchSize / 2.0), centerPos.x + (patchSize / 2.0)),
                        Random.nextDouble(centerPos.y - (patchSize / 2.0), centerPos.y + (patchSize / 2.0))
                    ),
                    radius = consumableType.radius,
                    myShape = consumableType.myShape,
                    intersections = emptyList(),
                    primaryColor = consumableType.primaryColor,
                    health = consumableType.health,
                    damage = consumableType.damage,
                    nutrition = consumableType.nutrition,
                    mass = consumableType.mass,
                    speciesName = consumableType.speciesName
                )
                c.myShape = Circle(c.position, c.radius).shape
                newConsumables.add(c)
            }
        }

        return newConsumables
    }

    fun quadraticInverse (controlValue: Double, modifier: Double, controlRange: Pair<Double, Double>, modifierRange: Pair<Double, Double>, outputRange: Pair<Double, Double>): Double {

        val speedFactor = outputRange.first +  (1.0 - ((controlValue - controlRange.first) / (controlRange.second - controlRange.first))) * (outputRange.second - outputRange.first)
        val randomVariation = modifier * (modifierRange.second - modifierRange.first) + modifierRange.first
        return speedFactor * randomVariation
    }


    fun generateCreatures(numCreatureTypes: Int, numCreatures: Int, posRange: Pair<Vector2, Vector2> = Pair(Vector2(CANVAS_ORIGIN.x, CANVAS_ORIGIN.y), Vector2(CANVAS_ORIGIN.x + CANVAS_WIDTH, CANVAS_ORIGIN.y + CANVAS_HEIGHT))): MutableList<Creature> {

        val newCreatures = mutableListOf<Creature>()

        for (i in 0 until numCreatureTypes) {

            val dna = randomDNA()

            for (j in 0 until numCreatures) {

                val c = Creature(
                    dna = dna,
                    direction = Vector2(
                        Random.nextDouble(-1.0, 1.0001),
                        Random.nextDouble(-1.0, 1.0001)
                    ),
                    position = Vector2(
                        Random.nextDouble(posRange.first.x, posRange.second.x),
                        Random.nextDouble(posRange.first.y, posRange.second.y)
                    ),
                )

                newCreatures.add(c)
            }
        }

        return newCreatures
    }

    fun generateBlobGenusSpeciesName(): String {
        val genusPrefixes = listOf("Micro", "Globus", "Plasma", "Vira", "Bioma", "Cellus", "Proto", "Spora")
        val genusSuffixes = listOf("us", "a", "ium", "is", "formis", "ium")
        val speciesDescriptors = listOf("cytosis", "mutans", "virox", "gelatus", "plasmis", "vita", "serpens", "flux", "tenebris", "viridis")

        val genus = genusPrefixes.random() + genusSuffixes.random()
        val species = speciesDescriptors.random()

        return "$genus $species"
    }

    private fun generatePlantGenusSpeciesName(): String {
        val genusPrefixes = listOf("Flora", "Solanum", "Betula", "Carya", "Quercus", "Rosa", "Prunus", "Salix", "Acer")
        val genusSuffixes = listOf("us", "a", "um", "ia", "is")
        val speciesDescriptors = listOf("alba", "rubra", "coccinea", "glabra", "floribunda", "viridis", "sativa", "nigra", "americana", "palustris")

        val genus = genusPrefixes.random() + genusSuffixes.random()
        val species = speciesDescriptors.random()

        return "$genus $species"
    }
}