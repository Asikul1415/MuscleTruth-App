package com.example.muscletruth.data.serviceClasses

import com.example.muscletruth.data.serviceClasses.ServingItem

class MealItem(
    val id: Int,
    var isExpanded: Boolean = true,
    var servings: List<ServingItem> = emptyList<ServingItem>(),
    var mealTypeID: Int,
    val picture: String? = null,
    val creationDate: String? = null,
)