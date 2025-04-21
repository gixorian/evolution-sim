import org.openrndr.math.Vector2
import kotlin.random.Random

val RADIUS_RANGE = 10.0..30.0
val SPEED_MODIFIER_RANGE = 0.8..1.2
val SIGHT_DISTANCE_RANGE = 50.0..300.0
val SIGHT_RADIUS_RANGE = 10.0..120.0
val BEHAVIOUR_BIAS_WEIGHT = 0.5..1.01
val MASS_MODIFIER_RANGE = 0.8..1.2
val HEALTH_MODIFIER_RANGE = 2.0..4.0
val DAMAGE_MODIFIER_RANGE = 0.5..1.5
val CHANGE_DIRECTION_COOLDOWN_RANGE = 12..24
val ATTACK_COOLDOWN_RANGE = 0.5..1.5
val ENERGY_MODIFIER_RANGE = 5.0..20.0
val SPEED_RANGE = 0.25..1.25

sealed class Gene<T>(open val value: T) {
    data class DoubleGene(override val value: Double) : Gene<Double>(value)
    data class IntGene(override val value: Int) : Gene<Int>(value)
    data class BooleanGene(override val value: Boolean) : Gene<Boolean>(value)
    data class VectorGene(override val value: Vector2) : Gene<Vector2>(value)
    data class StringGene(override val value: String) : Gene<String>(value)
    data class FoodPreferenceGene(override val value: FoodType) : Gene<FoodType>(value)
}

sealed interface GeneKey<T> {
    data object Radius : GeneKey<Double>
    data object SpeedModifier : GeneKey<Double>
    data object SightDistance : GeneKey<Double>
    data object SightRadius : GeneKey<Double>
    data object BehaviourBias : GeneKey<Int>
    data object BehaviourBiasWeight : GeneKey<Double>
    data object MassModifier : GeneKey<Double>
    data object HealthModifier : GeneKey<Double>
    data object DamageModifier : GeneKey<Double>
    data object EnergyModifier : GeneKey<Double>
    data object ChangeDirectionCooldown : GeneKey<Int>
    data object AttackCooldown : GeneKey<Double>
    data object ColorR : GeneKey<Double>
    data object ColorG : GeneKey<Double>
    data object ColorB : GeneKey<Double>
    data object SpeciesName : GeneKey<String>
    data object Diet : GeneKey<FoodType>
}

enum class FoodType() {
    MEAT,
    PLANT,
    ALL
}

data class DNA(val genes: Map<GeneKey<*>, Gene<*>>) {
    fun crossover(partner: DNA): DNA {
        val childGenes = genes.mapValues { (key, geneA) ->
            val geneB = partner.genes[key]
            if (geneB != null && Random.nextDouble() < 0.5) geneB else geneA
        }
        return DNA(childGenes)
    }

    fun mutate(mutationRate: Double): DNA {
        var mutated = false

        val mutatedGenes = genes.mapValues { (key, gene) ->
            if (key != GeneKey.SpeciesName && Random.nextDouble() < mutationRate) {
                mutated = true
                mutateGene(gene)
            } else {
                gene
            }
        }.toMutableMap()

        if(mutated) {
            mutatedGenes[GeneKey.SpeciesName] = mutateGene(genes.getValue(GeneKey.SpeciesName))
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
            is Gene.StringGene -> gene.copy(value = EntityGenerator().generateBlobGenusSpeciesName())
            is Gene.FoodPreferenceGene -> gene.copy(value = FoodType.entries.random())
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> DNA.getValue(key: GeneKey<T>): T = (genes[key] as? Gene<T>)?.value ?: error("Missing gene for key $key")


fun randomDNA(): DNA {
    return DNA(
        mapOf(
            GeneKey.Radius to Gene.DoubleGene(RADIUS_RANGE.random()),
            GeneKey.SpeedModifier to Gene.DoubleGene(SPEED_MODIFIER_RANGE.random()),
            GeneKey.SightDistance to Gene.DoubleGene(SIGHT_DISTANCE_RANGE.random()),
            GeneKey.SightRadius to Gene.DoubleGene(SIGHT_RADIUS_RANGE.random()),
            GeneKey.BehaviourBias to Gene.IntGene(Random.nextInt(3)),
            GeneKey.BehaviourBiasWeight to Gene.DoubleGene(BEHAVIOUR_BIAS_WEIGHT.random()),
            GeneKey.MassModifier to Gene.DoubleGene(MASS_MODIFIER_RANGE.random()),
            GeneKey.HealthModifier to Gene.DoubleGene(HEALTH_MODIFIER_RANGE.random()),
            GeneKey.DamageModifier to Gene.DoubleGene(DAMAGE_MODIFIER_RANGE.random()),
            GeneKey.EnergyModifier to Gene.DoubleGene(ENERGY_MODIFIER_RANGE.random()),
            GeneKey.ChangeDirectionCooldown to Gene.IntGene(CHANGE_DIRECTION_COOLDOWN_RANGE.random()),
            GeneKey.AttackCooldown to Gene.DoubleGene(ATTACK_COOLDOWN_RANGE.random()),
            GeneKey.ColorR to Gene.DoubleGene(Random.nextDouble()),
            GeneKey.ColorG to Gene.DoubleGene(Random.nextDouble()),
            GeneKey.ColorB to Gene.DoubleGene(Random.nextDouble()),
            GeneKey.SpeciesName to Gene.StringGene(EntityGenerator().generateBlobGenusSpeciesName()),
            GeneKey.Diet to Gene.FoodPreferenceGene(FoodType.entries.random()),
        )
    )
}

fun ClosedFloatingPointRange<Double>.random(): Double {
    return Random.nextDouble(start, endInclusive)
}

fun IntRange.random(): Int {
    return Random.nextInt(start, endInclusive + 1)
}