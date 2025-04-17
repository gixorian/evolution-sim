import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2

sealed class Gene {
    data class FloatGene(val value: Double) : Gene()
    data class IntGene(val value: Int)  : Gene()
    data class BooleanGene(val value: Boolean) : Gene()
    data class VectorGene(val value: Vector2) : Gene()
}

data class DNA(val genes: List<Gene>) {
    fun crossover(partner: DNA): DNA {
        val childGenes = genes.zip(partner.genes).map { (geneA, geneB) ->
            if (Math.random() < 0.5) geneA else geneB
        }
        return DNA(childGenes)
    }

    fun mutate(mutationRate: Double): DNA {
        val mutatedGenes = genes.map { gene ->
            if (Math.random() < mutationRate) {
                mutateGene(gene)
            } else {
                gene
            }
        }
        return DNA(mutatedGenes)
    }

    private fun mutateGene(gene: Gene): Gene {
        return when (gene) {
            is Gene.FloatGene -> gene.copy(value = gene.value + random() * 0.2 - 0.1)
            is Gene.IntGene -> gene.copy(value = gene.value + (-1..1).random())
            is Gene.BooleanGene -> gene.copy(value = !gene.value)
            is Gene.VectorGene -> gene.copy(value = (gene.value * Vector2(random(-1.0, 1.0), random(-1.0, 1.0))).normalized)
        }
    }
}
