package com.app.vivi.data.remote.model.response
data class PreferenceProductResponse(
    val products: List<products>? = null
)

data class products(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val tagline: String? = null,
    val producturl: String? = null,
    val imageurl: String? = null,
    val ratingtext: String? = null,
    val ratingvalue: String? = null,
    val averagerating: String? = null,
    val totalratings: String? = null,
    val discountedprice: String? = null,
    val discountpercentage: String? = null,
    val originalprice: String? = null,
    val city: String? = null,
    val country: String? = null,
    val countryflagurl: String? = null,
    val userrating: UserRating? = null
)

data class UserRating(
    val rating: String,
    val review: String,
    val username: String,
    val description: String,
    val userimageurl: String

)
