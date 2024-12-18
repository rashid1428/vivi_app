package com.app.vivi.features.homescreen.home.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.vivi.R
import com.app.vivi.basefragment.BaseFragmentVB
import com.app.vivi.customviews.CircleDrawable
import com.app.vivi.customviews.CustomTextView
import com.app.vivi.data.remote.model.data.productfragment.ProductDetailsData
import com.app.vivi.data.remote.model.data.productfragment.SummaryData
import com.app.vivi.databinding.FragmentProductDetailBinding
import com.app.vivi.databinding.ProductItemBinding
import com.app.vivi.dialog.rating.RatingDialogHelper
import com.app.vivi.extension.collectWhenStarted
import com.app.vivi.extension.navigateWithSingleTop
import com.app.vivi.extension.roundToTwoDecimalPlaces
import com.app.vivi.extension.showShortToast
import com.app.vivi.features.homescreen.home.adapters.ReviewsAdapter
import com.app.vivi.features.homescreen.home.adapters.SummaryAdapter
import com.app.vivi.features.homescreen.home.adapters.productdetail.BestOfProductAdapter
import com.app.vivi.features.homescreen.home.adapters.productdetail.CharacteristicsAdapter
import com.app.vivi.features.homescreen.home.adapters.productdetail.ExpandableAdapter
import com.app.vivi.features.homescreen.home.adapters.productdetail.VintageAdapter
import com.app.vivi.features.homescreen.home.viewmodels.ProductViewModel
import com.app.vivi.helper.createRatingDescription
import com.app.vivi.helper.cutOnText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import loadImageWithCache

@AndroidEntryPoint
class ProductDetailFragment :
    BaseFragmentVB<FragmentProductDetailBinding>(FragmentProductDetailBinding::inflate) {

    private val viewModel by viewModels<ProductViewModel>()

    private val summaryAdapter by lazy {
        SummaryAdapter {
            handleItemClick(it)
        }
    }

    private val reviewsAdapter by lazy {
        ReviewsAdapter(onCommentClick = {
            it.review_id?.let { it1 -> navigateToReviewCommentFragment(it1) }
        })
    }

    private val characteristicsAdapter by lazy {
        CharacteristicsAdapter()
    }

    private val thoughtsAdapter by lazy {
        ExpandableAdapter()
    }

    private val vintageAdapter by lazy {
        VintageAdapter()
    }

    private val mBestOfProductAdapter by lazy {
        BestOfProductAdapter(onItemClick = {
//            navigateToProductDetailFragment()
        }, onDiscountButtonClick = {
            "Discount Button Clicked".showShortToast(requireContext())
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getDataFromBundle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        handleOnScrollChangeListener()
        setupAdapters()
        initListeners()
        addObservers()
        handleBackPress()
//        handleAppBar()
//        animateImage()
        getUserReviewsApi("Helpful")
    }

    override fun onResume() {
        super.onResume()
        handleAppBar()
        animateImage()
    }

    private fun getDataFromBundle(){
        arguments?.let {
            val productData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("ProductDetails", ProductDetailsData::class.java)
            } else {
                it.getParcelable("ProductDetails")
            }
            viewModel.sendProductDetailsData(productData)
        }
    }

    private fun setupProductDetailsView(
        item: FragmentProductDetailBinding,
        product: ProductDetailsData?
    ) {

        with(item) {
            item.root.visibility = View.VISIBLE
            tvProductName.text = product?.name
            tvProductDetails.text = product?.description
            tvProductAddress.text = product?.city.plus(", ${product?.country}")
            inRatingLayout.tvRatingText.text = product?.averagerating
            inRatingLayout.ratingBar.rating = product?.averagerating?.toFloatOrNull()
                ?: 0f // Safely convert to float, defaulting to 0
            inRatingLayout.ratingsCount.text = product?.totalratings.plus(" ${getString(R.string.ratings_txt)}")
            val discountedPrice = product?.discountedprice?.toDouble()?.roundToTwoDecimalPlaces()
            tvAveragePrice.text = "CA$${discountedPrice}"

            inSaveLayout.labelText.text = "${getString(R.string.save_txt)} ${product?.discountpercentage}"

            tvOrginalPrice.text =
                cutOnText(requireContext().applicationContext, "CA$${product?.originalprice}")

            val htmlContent = createRatingDescription(
                binding.root.context,
                product?.userrating?.description, product?.userrating?.rating
            )
//            tvRatingDescription.text = htmlContent
            tvRatingUser.text = product?.userrating?.userName

            product?.producturl?.let { ivBottle.loadImageWithCache(it, R.drawable.ic_bottle) }
            product?.imageurl?.let { ivProductBackground.loadImageWithCache(it, R.drawable.ic_bg_coffee) }
        }
    }


    private fun setupUI() {
        binding.ivPreference.background = CircleDrawable(
            ContextCompat.getColor(requireContext(), R.color.red)
        )

        with(binding){
            inProductRankingLayout.tvTitle.text = getString(R.string.ranking_txt, getString(R.string.app_name))
            inProductRankingLayout.tvWorldRankingTitle.text = getString(R.string.of_in_the_world_txt, getString(R.string.app_name))
            inProductRankingLayout.tvRegionRankingTitle.text = getString(R.string.of_from_txt, getString(R.string.app_name))

            inPremium.tvPremiumTitle.text = getString(R.string.instantly_pair_any_dish_or_wine_you_choose_txt, getString(R.string.app_name))
            inProductLocationDetailLayout.tvProductCount.text = "39 ${getString(R.string.app_name)}s"
            inBestOfProductLayout.tvYouMightInterested.text = getString(R.string.best_of_txt, getString(R.string.app_name))
        }
    }

    private fun setupAdapters() {
        with(binding) {
            inSummaryLayout.rvSummary.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = summaryAdapter
            }
            inReviewsLayout.rvReviews.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = reviewsAdapter
            }

            inTasteCharacteristicsLayout.rvCharacteristics.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = characteristicsAdapter
            }

            inTasteCharacteristicsLayout.rvThoughts.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = thoughtsAdapter
            }

            inVintageLayout.rvVintage.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = vintageAdapter
            }

            inBestOfProductLayout.rvYouMightInterested.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = mBestOfProductAdapter
            }
        }

    }

    private fun initListeners() {

        binding.apply {
            inAppBar.centerImageView.setOnClickListener {
                val action =
                    ProductDetailFragmentDirections.actionProductDetailFragmentToNotificationsFragment()
                findNavController().navigate(action)
            }

            tvRate.setOnClickListener {
                showRatingDialog()
            }

            inSummaryLayout.apply {
                tvHighllights.setOnClickListener { toggleSummaryView(true) }
                tvFacts.setOnClickListener { toggleSummaryView(false) }
            }

            inReviewsLayout.apply {
                tvHelpful.setOnClickListener { toggleReviewsView(true) }
                tvRecent.setOnClickListener { toggleReviewsView(false) }
                tvShowAllReviews.setOnClickListener { navigateToReviewsFragment() }
            }

            // Button Click Listeners
            inVintageLayout.btnRecent.setOnClickListener {
                viewModel.fetchRecentList()
                highlightButton(binding.inVintageLayout.btnRecent)
            }

            inVintageLayout.btnBestPrice.setOnClickListener {
                viewModel.fetchBestPriceList()
                highlightButton(inVintageLayout.btnBestPrice)
            }

            inVintageLayout.btnTopRating.setOnClickListener {
                viewModel.fetchTopRatingList()
                highlightButton(inVintageLayout.btnTopRating)
            }

            inVintageLayout.btnShowAllVintages.setOnClickListener {
                // Implement your action to show all vintages
            }
        }
    }

    private fun navigateToReviewCommentFragment(reviewId: Int){
        val action = ProductDetailFragmentDirections.actionProductDetailFragmentToProductReviewFragmentt(reviewId)
        findNavController().navigate(action)
    }

    private fun navigateToReviewsFragment(){
        val action = ProductDetailFragmentDirections.actionProductDetailFragmentToProductFiltersFragment()
        findNavController().navigateWithSingleTop(action)
    }

    private fun highlightButton(activeButton: Button) {
        val buttons = listOf(binding.inVintageLayout.btnRecent, binding.inVintageLayout.btnBestPrice,
            binding.inVintageLayout.btnTopRating)
        buttons.forEach { button ->
//            button.setBackgroundColor(resources.getColor(R.color.colorTransparent, null))
            button.setTextColor(resources.getColor(R.color.white, null))
        }
//        activeButton.setBackgroundColor(resources.getColor(R.color.colorTransparent, null))
        activeButton.setTextColor(resources.getColor(R.color.red, null))
    }

    private fun animateProgress(worldProgress: Float, regionProgress: Float) {
        binding.inProductRankingLayout.cpWorldRanking.setProgressWithAnimation(worldProgress)
        binding.inProductRankingLayout.cpRegionRanking.setProgressWithAnimation(worldProgress)

        // Update the percentage text
        binding.inProductRankingLayout.percentageText.text = "${(regionProgress * 100).toInt()}%"
        binding.inProductRankingLayout.tvRegionRankingpercentage.text = "${(regionProgress * 100).toInt()}%"
    }

    private fun toggleSummaryView(showSummary: Boolean) {
        binding.inSummaryLayout.apply {
            rvSummary.visibility = if (showSummary) View.VISIBLE else View.INVISIBLE
            clFacts.visibility = if (showSummary) View.INVISIBLE else View.VISIBLE
            updateSelection(if (showSummary) tvHighllights else tvFacts)
        }
    }

    private fun toggleReviewsView(showReviews: Boolean) {
        binding.inReviewsLayout.apply {
            if (showReviews) getUserReviewsApi("Helpful")
            if (!showReviews) getUserReviewsApi("Recent")
            updateReviewsSelection(if (showReviews) tvHelpful else tvRecent)
        }
    }

    private fun updateSelection(selectedTextView: CustomTextView) {
        binding.inSummaryLayout.apply {
            listOf(tvHighllights, tvFacts).forEach {
                it.updateBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.greyLight
                    )
                )
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            }
            selectedTextView.apply {
                updateBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            }
        }
    }

    private fun updateReviewsSelection(selectedTextView: CustomTextView) {
        binding.inReviewsLayout.apply {
            listOf(tvHelpful, tvRecent).forEach {
                it.updateBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.greyLight
                    )
                )
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            }
            selectedTextView.apply {
                updateBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
            }
        }
    }

    private fun handleBackPress() {
        binding.inAppBar.ivBack.setOnClickListener { findNavController().popBackStack() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )
    }

    private fun startAnimation() {
        binding?.ivProductBackground?.let { view ->
            if (view.isAttachedToWindow) {
                view.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // Remove the listener to avoid multiple calls
                        view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        view.translationX = -view.width.toFloat()
                        view.scaleX = 0.5f
                        view.scaleY = 0.5f
                        view.animate().translationX(0f).scaleX(1f).scaleY(1f).setDuration(800)
                            .start()

                    }
                })
            }
        }
    }


    private fun animateImage() {
        // Create a DefaultLifecycleObserver to handle lifecycle events
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                startAnimation()
            }
        }

        // Add the observer to the lifecycle of the viewLifecycleOwner
        viewLifecycleOwner.lifecycle.addObserver(observer)
    }

    private fun handleAppBar() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val percentage = Math.abs(verticalOffset) / scrollRange.toFloat()

            when (percentage) {
                1f -> updateAppBar("Black Coffee", R.color.colorPrimary)
                0f -> updateAppBar("", R.color.colorTransparent)
            }
        }
    }

    private fun handleOnScrollChangeListener() {
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // Get the location of the scrollable Add to Cart button
            val buttonLocation = IntArray(2)
//            addToCartButtonScroll.getLocationOnScreen(buttonLocation)
            binding.tvAddToCart.getLocationOnScreen(buttonLocation)
            animateProgress(0.8f, 0.7f)
            // Check if the button has moved out of view
            if (scrollY > oldScrollY && buttonLocation[1] < 0) {
                // Button is scrolled out of view, show the fixed button
                binding.clFixeAddToCart.visibility = View.VISIBLE
            } else if (scrollY < oldScrollY && buttonLocation[1] > 0) {
                // Button is still in view, hide the fixed button
                binding.clFixeAddToCart.visibility = View.GONE
            }
        }
    }

    private fun handleOnScrollChangeListenerForRankingLayout() {
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // Get the location of the scrollable Add to Cart button
            val buttonLocation = IntArray(2)
//            addToCartButtonScroll.getLocationOnScreen(buttonLocation)
            binding.inProductRankingLayout.cpWorldRanking.getLocationOnScreen(buttonLocation)
            animateProgress(0.8f, 0.7f)
            // Check if the button has moved out of view
            if (scrollY > oldScrollY && buttonLocation[1] < 0) {
                // Button is scrolled out of view, show the fixed button
                binding.clFixeAddToCart.visibility = View.VISIBLE
//                animateProgress(1f)
            } else if (scrollY < oldScrollY && buttonLocation[1] > 0) {
                // Button is still in view, hide the fixed button

                binding.clFixeAddToCart.visibility = View.GONE
                animateProgress(0.8f, 0.7f)
            }
        }
    }

    private fun updateAppBar(title: String, @ColorRes backgroundColor: Int) {
        binding.inAppBar.textView.text = title
        binding.toolbar.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                backgroundColor
            )
        )
    }

    private fun handleItemClick(item: SummaryData) {
        // Navigate or handle item click
    }


    // Inside an Activity or Fragment
    private val ratingDialogHelper by lazy {
        RatingDialogHelper(requireContext()) { rating, review ->
            // Handle review submission logic here
        }
    }

    private fun showRatingDialog() {
        ratingDialogHelper.showRatingDialog()
    }


    private fun addObservers() {
        collectWhenStarted {
            viewModel.productDetailsDataFlow.collectLatest { data ->
                setupProductDetailsView(binding, data)
            }
        }

        collectWhenStarted {
            viewModel.summaryList.collectLatest { summaryList ->
                summaryAdapter.submitList(summaryList)
            }
        }

        collectWhenStarted {
            viewModel.CharacteristicsDataList.collectLatest { list ->
                characteristicsAdapter.submitList(list)
            }
        }

        collectWhenStarted {
            viewModel.ThoughtsDataList.collectLatest { list ->
                thoughtsAdapter.submitList(list)
            }
        }

        collectWhenStarted {
            viewModel.userReviews.collectLatest {
                it?.reviews.let { reviewList ->
                    reviewsAdapter.submitList(reviewList)
                }
            }
        }

        collectWhenStarted {
            viewModel.bestOfProductsDetail.collectLatest {
                it?.let { list ->
                    mBestOfProductAdapter.submitList(list)
                }
            }
        }

        collectWhenStarted {
            viewModel.vintageDataList.collectLatest {
                it?.let { list ->
                    vintageAdapter.submitList(list)
                }
            }
        }
    }

    private fun getUserReviewsApi(type: String) {
        viewModel.getUserReviewsApi(type)
    }

    override fun getMyViewModel() = viewModel
}
