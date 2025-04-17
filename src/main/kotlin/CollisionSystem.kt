import org.openrndr.math.Vector2

fun resolveCollision(movingCreature: Creature, nearbyEntities: List<Entity>, fromPos: Vector2, toPos: Vector2): Vector2 {
    var correctedPosition = toPos
    var hasCollision = false

    for (other in nearbyEntities) {
        if (other === movingCreature) continue

        val minDistance = movingCreature.radius + other.radius
        val offset = correctedPosition - other.position
        val distance = offset.length

        if (distance < minDistance && distance > 0.0) { // Collision detected
            hasCollision = true
            val pushDirection = offset.normalized
            correctedPosition = other.position + pushDirection * minDistance * 1.05 / (movingCreature.mass * 0.01) // Subtle push
        }
    }

    // Apply smooth transition back to the desired position
    if (hasCollision) {
        correctedPosition = (correctedPosition - fromPos) * 0.5 + fromPos // Apply a 50% smoothing to reduce jitter
    }

    // Apply velocity clamping
    val maxSpeed = 1.5
    val dir = correctedPosition - fromPos
    if (dir.length > maxSpeed) {
        correctedPosition = fromPos + dir.normalized * maxSpeed
    }

    return correctedPosition
}


//fun resolveCollision(movingCreature: Creature, nearbyCreatures: List<Creature>, fromPos: Vector2, toPos: Vector2): Vector2 {
//    var correctedPosition = toPos
//    var hasCollision = false
//
//    for (other in nearbyCreatures) {
//        if (other === movingCreature) continue
//
//        val minDistance = movingCreature.radius + other.radius
//        val offset = correctedPosition - other.position
//        val distance = offset.length
//
//        if (distance < minDistance && distance > 0.0) { // Collision detected
//            hasCollision = true
//            val pushDirection = offset.normalized
//            correctedPosition = other.position + pushDirection * minDistance * 1.05 // Subtle push
//        }
//    }
//
//    // Apply smooth transition back to the desired position
//    if (hasCollision) {
//        correctedPosition = (correctedPosition - fromPos) * 0.5 + fromPos // Apply a 50% smoothing to reduce jitter
//    }
//
//    // Apply velocity clamping
//    val maxSpeed = 1.5
//    val dir = correctedPosition - fromPos
//    if (dir.length > maxSpeed) {
//        correctedPosition = fromPos + dir.normalized * maxSpeed
//    }
//
//    return correctedPosition
//}

fun checkEdgeCollision(creature: Creature) {
    var posX = creature.position.x
    var posY = creature.position.y

    var dirX = creature.direction.x
    var dirY = creature.direction.y

    // Check if the creature hits the left or right edge
    if (creature.position.x - creature.radius < 0) {
        posX = creature.radius
        dirX *= 0.7 // Apply a subtle push back towards the center (smaller value for smoother movement)
    } else if (creature.position.x + creature.radius > SCREEN_WIDTH) {
        posX = SCREEN_WIDTH - creature.radius
        dirX *= 0.7 // Same subtle push
    }

    // Check if the creature hits the top or bottom edge
    if (creature.position.y - creature.radius < 0) {
        posY = creature.radius
        dirY *= 0.7 // Apply a subtle push back towards the center
    } else if (creature.position.y + creature.radius > SCREEN_HEIGHT) {
        posY = SCREEN_HEIGHT - creature.radius
        dirY *= 0.7 // Same subtle push
    }

    creature.position = Vector2(posX, posY)
    creature.direction = Vector2(dirX, dirY)
}
