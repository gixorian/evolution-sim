import org.openrndr.math.Vector2
import kotlin.random.Random

val RADIUS_RANGE = Pair(10.0, 30.0)
val SPEED_MODIFIER_RANGE = Pair(0.8, 1.2)
val SIGHT_DISTANCE_RANGE = Pair(50.0, 300.0)
val SIGHT_RADIUS_RANGE = Pair(10.0, 120.0)
val BEHAVIOUR_BIAS_WEIGHT = Pair(0.5, 1.01)
val MASS_MODIFIER_RANGE = Pair(0.8, 1.2)
val HEALTH_MODIFIER_RANGE = Pair(2.0, 4.0)
val DAMAGE_MODIFIER_RANGE = Pair(0.5, 1.5)
val CHANGE_DIRECTION_COOLDOWN_RANGE = Pair(12, 24)
val ATTACK_COOLDOWN_RANGE = Pair(0.5, 1.5)
val ENERGY_MODIFIER_RANGE = Pair(5.0, 20.0)
val SPEED_RANGE = Pair(0.25, 1.25)

sealed class Gene<T>(open val value: T) {
    data class DoubleGene(override val value: Double) : Gene<Double>(value)
    data class IntGene(override val value: Int) : Gene<Int>(value)
    data class BooleanGene(override val value: Boolean) : Gene<Boolean>(value)
    data class VectorGene(override val value: Vector2) : Gene<Vector2>(value)
}

data class DNA(val genes: Map<String, Gene<*>>) {
    fun crossover(partner: DNA): DNA {
        val childGenes = genes.mapValues { (key, geneA) ->
            val geneB = partner.genes[key]
            if (geneB != null && Math.random() < 0.5) geneB else geneA
        }
        return DNA(childGenes)
    }

    fun mutate(mutationRate: Double): DNA {
        val mutatedGenes = genes.mapValues { (_, gene) ->
            if (Math.random() < mutationRate) {
                mutateGene(gene)
            } else {
                gene
            }
        }
        return DNA(mutatedGenes)
    }

    private fun mutateGene(gene: Gene<*>): Gene<*> {
        return when (gene) {
            is Gene.DoubleGene -> gene.copy(value = gene.value + Random.nextDouble(-0.1, 0.1))
            is Gene.IntGene -> gene.copy(value = gene.value + (-1..1).random())
            is Gene.BooleanGene -> gene.copy(value = !gene.value)
            is Gene.VectorGene -> gene.copy(
                value = (gene.value + Vector2(
                    Random.nextDouble(-0.1, 0.1),
                    Random.nextDouble(-0.1, 0.1)
                )).normalized
            )
        }
    }
}

fun randomDNA(): DNA {
    return DNA(
        mapOf(
            "radius" to Gene.DoubleGene(Random.nextDouble(RADIUS_RANGE.first, RADIUS_RANGE.second)),
            "speedModifier" to Gene.DoubleGene(Random.nextDouble(SPEED_MODIFIER_RANGE.first, SPEED_MODIFIER_RANGE.second)),
            "sightDistance" to Gene.DoubleGene(Random.nextDouble(SIGHT_DISTANCE_RANGE.first, SIGHT_DISTANCE_RANGE.second)),
            "sightRadius" to Gene.DoubleGene(Random.nextDouble(SIGHT_RADIUS_RANGE.first, SIGHT_RADIUS_RANGE.second)),
            "behaviourBias" to Gene.IntGene(Random.nextInt(0, 3)),
            "behaviourBiasWeight" to Gene.DoubleGene(Random.nextDouble(BEHAVIOUR_BIAS_WEIGHT.first, BEHAVIOUR_BIAS_WEIGHT.second)),
            "massModifier" to Gene.DoubleGene(Random.nextDouble(MASS_MODIFIER_RANGE.first, MASS_MODIFIER_RANGE.second)),
            "healthModifier" to Gene.DoubleGene(Random.nextDouble(HEALTH_MODIFIER_RANGE.first, HEALTH_MODIFIER_RANGE.second)),
            "damageModifier" to Gene.DoubleGene(Random.nextDouble(DAMAGE_MODIFIER_RANGE.first, DAMAGE_MODIFIER_RANGE.second)),
            "energyModifier" to Gene.DoubleGene(Random.nextDouble(ENERGY_MODIFIER_RANGE.first, ENERGY_MODIFIER_RANGE.second)),
            "changeDirectionCooldown" to Gene.IntGene(Random.nextInt(CHANGE_DIRECTION_COOLDOWN_RANGE.first, CHANGE_DIRECTION_COOLDOWN_RANGE.second)),
            "attackCooldown" to Gene.DoubleGene(Random.nextDouble(ATTACK_COOLDOWN_RANGE.first, ATTACK_COOLDOWN_RANGE.second)),
            "colorR" to Gene.DoubleGene(Random.nextDouble(0.0, 1.0)),
            "colorG" to Gene.DoubleGene(Random.nextDouble(0.0, 1.0)),
            "colorB" to Gene.DoubleGene(Random.nextDouble(0.0, 1.0)),
        )
    )
}
