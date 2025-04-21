import kotlin.random.Random

class GeneticAlgorithm() {

    //val population = generateInitialPopulation()

    fun generateInitialPopulation (
        speciesRange: IntRange = 5..15,
        creaturesPerSpeciesRange: IntRange = 10..20,
        ): MutableList<Creature> {

        val numSpecies = Random.nextInt(speciesRange.first, speciesRange.last)
        val numCreaturesPerSpecies = Random.nextInt(creaturesPerSpeciesRange.first, creaturesPerSpeciesRange.last)

        return EntityGenerator().generateCreatures(numSpecies, numCreaturesPerSpecies)
    }




}