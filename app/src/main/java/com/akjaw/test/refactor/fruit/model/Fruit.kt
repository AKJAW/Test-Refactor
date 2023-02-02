package com.akjaw.test.refactor.fruit.model

data class Fruit(
    val name: String = "",
    val id: Int = -1,
    val nutritions: Nutritions = Nutritions(),
    val isFavorited: Boolean = false,
)

data class Nutritions(
    val carbohydrates: Float = 0.0f,
    val protein: Float = 0.0f,
    val fat: Float = 0.0f,
    val calories: Float = 0.0f,
    val sugar: Float = 0.0f,
)