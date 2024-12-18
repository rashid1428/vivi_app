package com.app.vivi.entrypoint

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.app.vivi.R
import com.app.vivi.databinding.ActivityMainBinding
import com.app.vivi.entrypoint.adapter.DrawerAdapter
import com.app.vivi.extension.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private val mDrawerAdapter by lazy { DrawerAdapter() }

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        WindowCompat.setDecorFitsSystemWindows(window, false)


        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.keepSplashScreen }
        }

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
//        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        window.statusBarColor = ContextCompat.getColor(this, R.color.white)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
//            binding.navigationView.visibility = if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE
        }

//        setUpDrawerList()
//        addDrawerListener()
        addObservers()
        checkNotificationSelfPermission()

    }

    private fun checkNotificationSelfPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            showDummyNotification()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

    }

    private fun addObservers() {

        collectWhenStarted {
            viewModel.drawerItems.collectLatest {
                mDrawerAdapter.submitList(it)
            }
        }

        collectWhenStarted {
            viewModel.channel.collectLatest { event ->
                when (event) {
                    is MainActivityViewModel.NavigationEvents.NavigateToMainScreen -> {
                        navigateToHomeScreen()
                       /* navController.navigate(
                            LoginFragmentDirections.actionLoginFragmentToHomeFragment(event.loginResponse)
                        )*/
                    }
                }
            }
        }
    }

    private fun navigateToHomeScreen(){

        val navGraph = navController.navInflater.inflate(R.navigation.login_graph)
        navGraph.setStartDestination(R.id.home_graph)
        navController.graph = navGraph
    }
}