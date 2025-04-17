import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import java.lang.Math.random
import kotlin.random.Random


const val CONSUMABLE_TYPES = 10
const val CONSUMABLES_PER_PATCH = 10
const val CONSUMABLE_PATCHES = 80
const val CONSUMABLE_PATCH_SIZE = 50.0

fun generateConsumables (numConsumablesTypes: Int, numConsumablesPerPatch: Int, numConsumablePatches: Int = 1, patchSize: Double = SCREEN_HEIGHT.toDouble(), spawnPosition: Vector2? = null, color: ColorRGBa? = null): MutableList<Consumable> {
    val newConsumables = mutableListOf<Consumable>()

    val consumableTypes = mutableListOf<ConsumableType>()

    for (i in 0 until numConsumablesTypes) {

        val radius = Random.nextDouble(4.0, 8.0)
        val mass = radius * Random.nextDouble(0.75, 1.25)

        val newColor = color ?: ColorRGBa(random(), random(), random())

        val t = ConsumableType(
            radius = Random.nextDouble(3.0, 5.0),
            strokeC = newColor,
            myShape = Circle(0.0, 0.0, radius).shape,
            health = mass * Random.nextDouble(2.0, 5.0),
            damage = 0.0,
            nutrition = mass * Random.nextDouble(1.0, 2.0),
            mass = mass,
            speciesName = generatePlantGenusSpeciesName()
        )

        consumableTypes.add(t)
    }

    for (i in 0 until numConsumablePatches) {

        val consumableType = consumableTypes[Random.nextInt(0, consumableTypes.size)]

        val centerPos =
            spawnPosition
                ?: Vector2(
                    Random.nextDouble(patchSize + consumableType.radius, SCREEN_WIDTH.toDouble() - patchSize - consumableType.radius),
                    Random.nextDouble(patchSize + consumableType.radius, SCREEN_HEIGHT.toDouble() - patchSize - consumableType.radius)
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
                strokeC = consumableType.strokeC,
                fillC = consumableType.strokeC,
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

fun generateCreatures(numCreatureTypes: Int, numCreatures: Int, posRange: Pair<Vector2, Vector2> = Pair(Vector2.ZERO, Vector2(SCREEN_WIDTH.toDouble(), SCREEN_HEIGHT.toDouble()))): MutableList<Creature> {

    val newCreatures = mutableListOf<Creature>()

    for (i in 0 until numCreatureTypes) {

        val radius = Random.nextDouble(10.0, 30.0)
        val speed = (radius / Random.nextDouble(SPEED_MULT_RANGE.first, SPEED_MULT_RANGE.second)) * SIMULATION_SPEED
        val strokeC = ColorRGBa(random(), random(), random())
        val changeDirectionCooldown = Random.nextInt(12, 24) //Random.nextInt(60, 120)
        val sightDistance = Random.nextDouble(radius + 15.0, radius + 200.0)
        val sightRadius = Random.nextDouble(10.0, 120.0)
        val morphSpeed = speed/20.0
        val myShape = Circle(0.0, 0.0, radius).shape
        val  blobMorphState  =  BlobMorphState(
            generateBlob(Vector2(SCREEN_WIDTH / 2.0, SCREEN_HEIGHT/2.0), radius, 8),
            generateBlob(Vector2(SCREEN_WIDTH / 2.0, SCREEN_HEIGHT/2.0), radius, 16),
            0.0
        )
        val mass = radius * Random.nextDouble(0.75, 1.25)
        val health = mass * Random.nextDouble(HEALTH_MULT_RANGE.first, HEALTH_MULT_RANGE.second)
        val damage = mass * Random.nextDouble(DAMAGE_MULT_RANGE.first, DAMAGE_MULT_RANGE.second)
        val attackCooldown = (mass / Random.nextDouble(ATTACK_COOLDOWN_MULT_RANGE.first, ATTACK_COOLDOWN_MULT_RANGE.second)) / 5.0
        val energy = mass * Random.nextDouble(5.0, 20.0)
        val behaviourBias = Random.nextInt(0, 3)
        val speciesName = generateBlobGenusSpeciesName()

        for (j in 0 until numCreatures) {
            val c = Creature(
                direction = Vector2(
                    Random.nextDouble(-1.0, 1.0001),
                    Random.nextDouble(-1.0, 1.0001)
                ),
                position = Vector2(
                    Random.nextDouble(posRange.first.x, posRange.second.x),
                    Random.nextDouble(posRange.first.y, posRange.second.y)
                ),
                speed = speed,
                radius = radius,
                myShape = myShape,
                intersections = emptyList(),
                strokeC = strokeC,
                fillC = strokeC,
                blobMorphState = blobMorphState,
                changeDirectionCooldown = changeDirectionCooldown,
                sightDistance = sightDistance,
                sightRadius = sightRadius,
                targetsInRange = mutableListOf(),
                morphSpeed = morphSpeed,
                health = health,
                damage = damage,
                attackCooldown = attackCooldown,
                energy = energy,
                mass = mass,
                behaviourBias = behaviourBias,
                behaviourBiasWeight = Random.nextDouble(0.5, 1.01),
                speciesName = speciesName
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

fun generatePlantGenusSpeciesName(): String {
    val genusPrefixes = listOf("Flora", "Solanum", "Betula", "Carya", "Quercus", "Rosa", "Prunus", "Salix", "Acer")
    val genusSuffixes = listOf("us", "a", "um", "ia", "is")
    val speciesDescriptors = listOf("alba", "rubra", "coccinea", "glabra", "floribunda", "viridis", "sativa", "nigra", "americana", "palustris")

    val genus = genusPrefixes.random() + genusSuffixes.random()
    val species = speciesDescriptors.random()

    return "$genus $species"
}
