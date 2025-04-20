import kotlin.random.Random

class GeneticAlgorithm() {

    //val population = generateInitialPopulation()

    fun generateInitialPopulation (
        speciesRange: Pair<Int, Int> = Pair(5, 15),
        creaturesPerSpeciesRange: Pair<Int, Int> = Pair(10, 20),
        ): MutableList<Creature> {

        val numSpecies = Random.nextInt(speciesRange.first, speciesRange.second)
        val numCreaturesPerSpecies = Random.nextInt(creaturesPerSpeciesRange.first, creaturesPerSpeciesRange.second)

        return EntityGenerator().generateCreatures(numSpecies, numCreaturesPerSpecies)
    }




}