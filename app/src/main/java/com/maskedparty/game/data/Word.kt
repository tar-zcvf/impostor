package com.maskedparty.game.data

import kotlin.random.Random

/**
 * A category of secret words. The imposter receives a *different* word from
 * the same category, so players can deduce the category but not the word.
 */
enum class WordCategory(val displayName: String) {
    ANIMALS("Animals"),
    FOOD("Food & Drink"),
    NATURE("Nature"),
    VEHICLES("Vehicles"),
    PLACES("Places"),
    OBJECTS("Everyday Things"),
    JOBS("Jobs");

    companion object {
        fun all(): List<WordCategory> = values().toList()
    }
}

/**
 * The original word bank. Deliberately free of trademarks and duplicates;
 * each category holds enough distinct words that a non-imposter word can
 * always be drawn (see [isPlayable]).
 */
object WordBank {

    private val categories: Map<WordCategory, List<String>> = buildMap {
        put(
            WordCategory.ANIMALS,
            listOf(
                "Penguin", "Ostrich", "Hedgehog", "Octopus", "Flamingo", "Raccoon",
                "Platypus", "Kangaroo", "Lemur", "Chameleon", "Hippo", "Puffin",
                "Sloth", "Axolotl", "Wombat", "Gecko"
            )
        )
        put(
            WordCategory.FOOD,
            listOf(
                "Croissant", "Sushi", "Popcorn", "Hummus", "Bagel", "Ramen",
                "Pancake", "Tiramisu", "Burrito", "Miso", "Waffle", "Guacamole",
                "Lasagna", "Mochi", "Falafel", "Crepes"
            )
        )
        put(
            WordCategory.NATURE,
            listOf(
                "Volcano", "Waterfall", "Cave", "Geyser", "Rainbow", "Aurora",
                "Desert", "Lagoon", "Glacier", "Mangrove", "Tornado", "Seagrass",
                "Cliff", "Tidepool", "Canyon", "Mist"
            )
        )
        put(
            WordCategory.VEHICLES,
            listOf(
                "Submarine", "Helicopter", "Kayak", "Tram", "Scooter", "Ferry",
                "Gondola", "Tugboat", "Snowmobile", "Monorail", "Biplane", "Yacht",
                "Segway", "Tanker", "Ropeway", "Tricycle"
            )
        )
        put(
            WordCategory.PLACES,
            listOf(
                "Bookstore", "Aquarium", "Carnival", "Greenhouse", "Observatory",
                "Bakery", "Library", "Planetarium", "Fish Market", "Botanic Garden",
                "Lighthouse", "Ice Rink", "Zoo", "Winery", "Marina", "Stadium"
            )
        )
        put(
            WordCategory.OBJECTS,
            listOf(
                "Telescope", "Umbrella", "Puzzle", "Candle", "Periscope", "Kettle",
                "Mirror", "Sunglasses", "Accordion", "Kite", "Compass", "Sieve",
                "Magnifier", "Ladder", "Prism", "Chime"
            )
        )
        put(
            WordCategory.JOBS,
            listOf(
                "Florist", "Pilot", "Carpenter", "Dentist", "Baker", "Diver",
                "Painter", "Librarian", "Photographer", "Welder", "Surgeon", "Gardener",
                "Cartographer", "Vet", "Chef", "Archivist"
            )
        )
    }

    init {
        // Defensive: guarantee the bank invariants even if a future edit slips in a duplicate.
        for ((category, words) in categories) {
            require(words.distinct().size == words.size) {
                "Duplicate word in category $category"
            }
            require(words.size >= 4) { "Category $category needs at least 4 words"
            }
        }
    }

    /** True when the category can host a game (needs at least [minWords] distinct words). */
    fun isPlayable(category: WordCategory, minWords: Int = 4): Boolean =
        words(category).size >= minWords

    /** Distinct words for a category (order is preserved). */
    fun words(category: WordCategory): List<String> =
        categories.getValue(category)

    /**
     * Randomly picks a shared word and a distinct imposter word from the category.
     * Returns null when no playable category is available.
     */
    fun drawRound(
        category: WordCategory?,
        random: Random = Random.Default
    ): RoundWords? {
        val available = if (category == null)
            WordCategory.all().filter { isPlayable(it) }
        else listOfNotNull(category.takeIf { isPlayable(it) })

        if (available.isEmpty()) return null
        val chosen = available[random.nextInt(available.size)]
        val pool = words(chosen)
        val main = pool[random.nextInt(pool.size)]
        val rest = pool.filter { it != main }
        val imposter = rest[random.nextInt(rest.size)]
        return RoundWords(category = chosen, sharedWord = main, imposterWord = imposter)
    }
}

/** The two words drawn for one round. */
data class RoundWords(
    val category: WordCategory,
    val sharedWord: String,
    val imposterWord: String
) {
    init {
        require(sharedWord != imposterWord) { "Shared and imposter words must differ" }
    }

    /** The word shown to a player: the imposter's own word if they are the imposter. */
    fun wordFor(isImposter: Boolean): String =
        if (isImposter) imposterWord else sharedWord
}
