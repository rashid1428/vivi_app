package com.app.vivi.features.homescreen.home.viewmodels

import RecommendedProductsResponse
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.app.vivi.R
import com.app.vivi.baseviewmodel.BaseViewModel
import com.app.vivi.data.remote.Resource
import com.app.vivi.data.remote.model.data.productdetailfragment.ThoughtsData
import com.app.vivi.data.remote.model.data.productfragment.CharacteristicsData
import com.app.vivi.data.remote.model.data.productfragment.SummaryData
import com.app.vivi.data.remote.model.response.FindYourNewFavoriteProductResponse
import com.app.vivi.data.remote.model.response.NewFavoriteProduct
import com.app.vivi.data.remote.model.response.PreferenceProductResponse
import com.app.vivi.data.remote.model.response.UserReviewsResponse
import com.app.vivi.domain.model.ErrorModel
import com.app.vivi.domain.repo.CacheRepo
import com.app.vivi.domain.repo.ProductRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepo: ProductRepo,
    private val cacheRepo: CacheRepo,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // StateFlow to hold the products list
    private val _characteristicsDataList = MutableStateFlow<List<CharacteristicsData>>(emptyList())
    val CharacteristicsDataList: StateFlow<List<CharacteristicsData>> = _characteristicsDataList

    // StateFlow to hold the products list
    private val _thoughtsDataList = MutableStateFlow<List<ThoughtsData>>(emptyList())
    val ThoughtsDataList: StateFlow<List<ThoughtsData>> = _thoughtsDataList

    // StateFlow to hold the summary list
    private val _summaryList = MutableStateFlow<List<SummaryData>>(emptyList())
    val summaryList: StateFlow<List<SummaryData>> = _summaryList

    // StateFlow to hold the products list
    private val _recommendedProducts = MutableStateFlow<RecommendedProductsResponse?>(null)
    val recommendedProducts: StateFlow<RecommendedProductsResponse?> = _recommendedProducts

    // StateFlow to hold the products list
    private val _preferenceProductDetail = MutableStateFlow<PreferenceProductResponse?>(null)
    val preferenceProductDetail: StateFlow<PreferenceProductResponse?> = _preferenceProductDetail

    // StateFlow to hold the products list
    private val _findYourNewFavoriteProduct = MutableStateFlow<List<List<NewFavoriteProduct>>?>(null)
    val findYourNewFavoriteProduct: StateFlow<List<List<NewFavoriteProduct>>?> = _findYourNewFavoriteProduct

    // StateFlow to hold the products list
    private val _userReviews = MutableStateFlow<UserReviewsResponse?>(null)
    val userReviews: StateFlow<UserReviewsResponse?> = _userReviews

    init {
        // Simulate loading data
        fetchProductList()
        fetchThoughtsList()
        fetchDummySummaryData()
    }


    private fun fetchProductList() {
        viewModelScope.launch {
            // Simulate fetching product list
            val productData = listOf(
                CharacteristicsData(id = 1, labelLight = "Light", labelBold = "Bold", sliderValue = 50f),
                CharacteristicsData(id = 2, labelLight = "Small", labelBold = "Large", sliderValue = 30f),
                CharacteristicsData(id = 3, labelLight = "Dim", labelBold = "Bright", sliderValue = 75f),
                CharacteristicsData(id = 2, labelLight = "Small", labelBold = "Large", sliderValue = 30f),
            )
            _characteristicsDataList.value = productData
        }
    }


    private fun fetchThoughtsList() {
        viewModelScope.launch {
            // Simulate fetching product list
            val productData = listOf(
                ThoughtsData("Citrus, lemon, grapefruit", "57 mentions of citrus fruit notes"),
                ThoughtsData("Peach, apricot, pear", "37 mentions of tree fruit notes"),
                ThoughtsData("Stone, honey, minerals", "25 mentions of earthy notes"),
                ThoughtsData("Additional Item 2", "Description for additional item 2"),
                ThoughtsData("Additional Item 1", "Description for additional item 1")
            )
            _thoughtsDataList.value = productData
        }
    }

    private fun fetchDummySummaryData() {
        viewModelScope.launch {
            // Simulate fetching product list
            val summaryData = listOf(
                SummaryData(
                    "Great value for money, similar vivi usually costs about 2 times as much",
                    R.drawable.ic_male_placeholder
                ),
                SummaryData(
                    "Among top 2% of all vivi in the world",
                    R.drawable.ic_female_placeholder
                ),
                SummaryData(
                    "You haven't tried this style yet. Try something new!",
                    R.drawable.ic_male_placeholder
                )
            )
            _summaryList.value = summaryData
        }
    }


    fun getRecommendedProducts() {
        viewModelScope.launch {
            showLoader()
            when (val call = productRepo.getRecommendedProducts()) {
                is Resource.Error -> {
                    if (call.code == 401) {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    } else {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    }
                }

                is Resource.Success -> {
                    hideLoader()
                    _recommendedProducts.value = call.data
                    /*if (call.data.responseCode == 200) {
                        _recommendedProducts.value = call.data.data
                    } else {
                        showError(ErrorModel("Error", call.data.message.toString()))
                    }*/
                }
            }
        }
    }

    fun getPreferenceProductDetail() {
        viewModelScope.launch {
            showLoader()
            when (val call = productRepo.getPreferenceProductDetail()) {
                is Resource.Error -> {
                    if (call.code == 401) {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    } else {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    }
                }

                is Resource.Success -> {
                    hideLoader()
                    _preferenceProductDetail.value = call.data
                }
            }
        }
    }

    fun getFindYourNewFavoriteProduct() {
        viewModelScope.launch {
            showLoader()
            when (val call = productRepo.getFindYourNewFavoriteProduct()) {
                is Resource.Error -> {
                    if (call.code == 401) {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    } else {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    }
                }

                is Resource.Success -> {
                    hideLoader()
                    val newItems: List<List<NewFavoriteProduct>> = call.data.products?.chunked(3) ?: emptyList()

                    _findYourNewFavoriteProduct.value = newItems
                }
            }
        }
    }

    fun getUserReviewsApi() {
        viewModelScope.launch {
            showLoader()
            when (val call = productRepo.getUserReviews()) {
                is Resource.Error -> {
                    if (call.code == 401) {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    } else {
                        showError(ErrorModel(title = call.title, message = call.message, call.code))
                    }
                }

                is Resource.Success -> {
                    hideLoader()
                    _userReviews.value = call.data
                }
            }
        }
    }
}