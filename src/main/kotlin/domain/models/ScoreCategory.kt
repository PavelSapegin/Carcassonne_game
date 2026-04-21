package domain.models

enum class ScoreCategory {

    // UPPER PART
    ONES,
    TWOS,
    THREES,
    FOURTHS,
    FIFTHS,
    SIXS,

    // LOWER PART
    THREEKIND,
    FOURKIND,
    FULL_HOUSE,
    SMALL_STRAIGHT,
    LARGE_STRAIGHT,
    YAHTZEE,
    CHANCE
}