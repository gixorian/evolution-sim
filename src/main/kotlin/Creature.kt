import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle
import org.openrndr.shape.ContourIntersection
import org.openrndr.shape.Shape
import org.openrndr.shape.intersections
import kotlin.math.acos
import kotlin.random.Random

class Creature(

    val dna: DNA,

    // Entity Overrides
    override var position: Vector2,
    override var isAlive: Boolean = true,
    override var radius: Double = dna.getValue(GeneKey.Radius),
    override var myShape: Shape = Circle(0.0, 0.0, radius).shape,
    override var intersections: List<ContourIntersection> = emptyList(),
    override var mass: Double = radius * dna.getValue(GeneKey.MassModifier),
    override var health: Double = mass * dna.getValue(GeneKey.HealthModifier),
    override var damage: Double = mass * dna.getValue(GeneKey.DamageModifier),
    override var speciesName: String = dna.getValue(GeneKey.SpeciesName),
    override var primaryColor: ColorRGBa = ColorRGBa(
        dna.getValue(GeneKey.ColorR),
        dna.getValue(GeneKey.ColorG),
        dna.getValue(GeneKey.ColorB)
    ),
    override var myFoodType: FoodType = FoodType.MEAT,

    // DNA derived values
    var energy: Double = mass * dna.getValue(GeneKey.EnergyModifier),
    var behaviourBias: Int = dna.getValue(GeneKey.BehaviourBias),
    var behaviourBiasWeight: Double = dna.getValue(GeneKey.BehaviourBiasWeight),
    private val attackCooldown: Double = dna.getValue(GeneKey.AttackCooldown),
    private var changeDirectionCooldown: Int = dna.getValue(GeneKey.ChangeDirectionCooldown),
    var sightDistance: Double = dna.getValue(GeneKey.SightDistance),
    var sightRadius: Double = dna.getValue(GeneKey.SightRadius),
    private var speed: Double = EntityGenerator().quadraticInverse(
        radius, dna.getValue(GeneKey.SpeedModifier),
        Pair(RADIUS_RANGE.start, RADIUS_RANGE.endInclusive),
        Pair(SPEED_MODIFIER_RANGE.start, SPEED_MODIFIER_RANGE.endInclusive),
        Pair(SPEED_RANGE.start, SPEED_RANGE.endInclusive)
    ),
    var myDiet: FoodType = dna.getValue(GeneKey.Diet),

    // Creature specific values
    var direction: Vector2,
    private var blobMorphState: BlobMorphState =  BlobMorphState(
        generateBlob(Vector2(CANVAS_ORIGIN.x + CANVAS_WIDTH / 2.0, CANVAS_ORIGIN.y + CANVAS_HEIGHT/2.0), radius, 8),
        generateBlob(Vector2(CANVAS_ORIGIN.x + CANVAS_WIDTH / 2.0, CANVAS_ORIGIN.y + CANVAS_HEIGHT/2.0), radius, 16),
        0.0
    ),
    var targetsInRange: MutableList<Entity> = mutableListOf(),
    var target: Entity? = null,
    private var morphSpeed: Double = speed/20.0,
    var currAttackCooldown: Double = 0.0,


) : Entity {

    private fun attack (other: Entity) {

        other.health -= this.damage

        if (other is Creature) energy -= other.mass * Random.nextDouble(0.01, 0.025)
        else if (other is Consumable) energy += other.nutrition

        if(other.health <= 0.0) { // The defending entity is dead
            other.isAlive = false

            if(other is Creature) {
                deadCreatures.add(other)
                // Generate food when a creature dies
                consumables.addAll(
                    EntityGenerator().generateConsumables(
                        numConsumableTypes = 1,
                        numConsumablesPerPatch = (other.mass * 0.5).toInt(),
                        numConsumablePatches = 1,
                        patchSize = other.radius,
                        spawnPosition = other.position,
                        color = other.primaryColor,
                        species = other.speciesName,
                        foodType = FoodType.MEAT
                    ),
                )
            } else if (other is Consumable) {
                deadConsumables.add(other)
            }
        }
    }

    private fun getNeighbors(cellSize: Double, radiusCells: Int): List<Entity> {
        val center = toGridCell(this.position, cellSize)
        val neighbors = mutableListOf<Entity>()

        // Check if there are any creatures within the grid cells that this creature's sight range can reach and add them to the list of it's neighbours
        for (dx in -radiusCells..radiusCells) {
            for (dy in -radiusCells..radiusCells) {
                val cell = (center.first + dx) to (center.second + dy)
                grid[cell]?.let { cellEntities ->
                    neighbors += cellEntities.filter { it != this }
                }
            }
        }
        return neighbors
    }

    fun process() {

        if (!this.isAlive) {
            deadCreatures.add(this)
            return
        }

        tickStats(this)

        val radiusCells = (this.sightDistance / CELL_SIZE).toInt() + 1
        val nearbyEntities = this.getNeighbors(CELL_SIZE, radiusCells)

        // Perform collision checks and target detection
        this.collisionDetection(nearbyEntities)

        // Perform position update and blob morphing
        this.updateMovement(nearbyEntities)
    }

    private fun tickStats (creature: Creature) {
        creature.energy -= (creature.mass * creature.speed * 0.0025) * SIMULATION_SPEED

        if (creature.energy <= 0.0) {
            creature.isAlive = false
            deadCreatures.add(creature)
            return
        }

        creature.currAttackCooldown -= 0.01
        if (creature.currAttackCooldown <= 0.0) creature.currAttackCooldown = 0.0
    }

    private fun targetDetection (creature: Creature, other: Entity): Boolean {

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

    private fun collisionDetection (nearbyEntities: List<Entity>) {

        this.intersections = emptyList()
        this.targetsInRange.clear()

        // Creature logic: intersection checks, movement, etc.
        for (other in nearbyEntities) {
            if (this == other || !other.isAlive) continue

            // Only check for intersections if it's possible for the two creatures to be close enough to collide
            if (other.position.distanceTo(this.position) <= (this.radius + this.sightDistance)) {

                // Add the intersections between these two creatures to the list of all intersections of this creature
                this.intersections += this.myShape.intersections(other.myShape)

                if (targetDetection(this, other)) {
                    // Sanity check to make sure that the other creature really is withing this creature's sight cone and there are intersections between them
                    if (this.myShape.intersections(other.myShape).isNotEmpty())
                    // At this point we know that the creatures are colliding and that the other creature is within this creature's sight cone so we are checking if the creature wants to attack

                        if (this.isMyFoodType(other))
                            this.processDamage(other)
                }
            }
        }
    }

    private fun isMyFoodType(other: Entity): Boolean {
        return other.myFoodType === this.myDiet
    }

    // Process damage on collision
    private fun processDamage(defender: Entity) {
        if(this.currAttackCooldown > 0.0) return

        this.currAttackCooldown = this.attackCooldown

        this.attack(defender)
    }

    // Updates the position and direction for all creatures
    private fun updateMovement(nearbyEntities: List<Entity>) {
        this.direction = this.direction.normalized * this.speed

        // Direction change logic
        if (this.changeDirectionCooldown <= 0) {

            val newDirection = this.getNewDirection()

            // Smoothly interpolate towards the new direction
            val smoothFactor = 0.5 // Change this value to make the transition more or less smooth
            this.direction = this.direction * (1 - smoothFactor) + newDirection * smoothFactor

            // Reset the cooldown for direction change
            this.changeDirectionCooldown = Random.nextInt(60, 120) / SIMULATION_SPEED.toInt()  //Random.nextInt(60, 120) // Change direction every 60 to 120 frames
        } else {
            // Decrease the cooldown timer by one each frame
            this.changeDirectionCooldown--
        }

        // If there is a collision (list of intersections is not empty), resolves the collision, otherwise continues along its direction vector
        if (this.intersections.isNotEmpty()) {
            this.position = resolveCollision(this, nearbyEntities, this.position, this.position + this.direction * this.speed)
        } else {
            this.position += this.direction * this.speed
        }

        this.updateBlobMorphFrame()

        checkEdgeCollision(this) // Checks for screen edge collisions

        this.myShape = this.moveShapeTo(this.position) // Updates the creature's shape position to match the creatures position
    }

    private fun updateBlobMorphFrame() {
        // Update the Blob morphing animation frame
        val (newState, newShape) = updateBlobMorph(
            this.blobMorphState,
            center = this.position,
            radius = Random.nextDouble(this.radius-1.0, this.radius+1.01),
            speed = this.morphSpeed
        )

        this.blobMorphState = newState
        this.myShape = newShape
    }

    private fun getNewDirection (): Vector2 {
        val randDecision: Int
        val biasedDecision: Int

        if (this.targetsInRange.isEmpty()) {
            this.target = null
        }
        else {

            if(this.target == null || !this.target!!.isAlive || this.target!! !in this.targetsInRange) {
                randDecision = Random.nextInt(0, 3)
                biasedDecision = if (random() >= this.behaviourBiasWeight) this.behaviourBias else randDecision

                if (biasedDecision != 0)
                    this.target = this.targetsInRange[Random.nextInt(0, this.targetsInRange.size)]
                else
                    this.target = null

                when (biasedDecision) {
                    0 -> { // Random Movement
                        return Vector2(
                            Random.nextDouble(-1.0, 1.0),
                            Random.nextDouble(-1.0, 1.0)
                        ).normalized
                    }
                    1 -> { // Move towards a target
                        return (this.target!!.position - this.position).normalized
                    }
                    2 -> { // Move away from a target
                        return (this.target!!.position - this.position).normalized * -1.0
                    }
                }
            }
        }

        return this.direction
    }

    private fun moveShapeTo(target: Vector2): Shape {
        // Get current shape center
        val bounds = this.myShape.bounds
        val currentCenter = bounds.center

        // Compute offset
        val offset = target - currentCenter

        // Apply translation
        return this.myShape.transform(transform {
            translate(offset)
        })
    }
}
