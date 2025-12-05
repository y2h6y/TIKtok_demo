package com.tiktok.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tiktok.app.databinding.ActivityMainBinding
import com.tiktok.app.ui.home.HomeFragment
import com.tiktok.app.ui.profile.ProfilePageFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    private var homeFragment: HomeFragment? = null
    private var profileFragment: ProfilePageFragment? = null
    private var currentFragment: Fragment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始显示首页
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.containerMain, homeFragment!!, "home")
                .commit()
            currentFragment = homeFragment
        } else {
            // 恢复Fragment
            homeFragment = supportFragmentManager.findFragmentByTag("home") as? HomeFragment
            profileFragment = supportFragmentManager.findFragmentByTag("profile") as? ProfilePageFragment
        }
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeFragment()
                    true
                }
                R.id.nav_profile -> {
                    showProfileFragment()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showHomeFragment() {
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance()
        }
        
        val transaction = supportFragmentManager.beginTransaction()
        
        // 隐藏当前Fragment
        currentFragment?.let { transaction.hide(it) }
        
        if (homeFragment!!.isAdded) {
            transaction.show(homeFragment!!)
        } else {
            transaction.add(R.id.containerMain, homeFragment!!, "home")
        }
        
        transaction.commitAllowingStateLoss()
        currentFragment = homeFragment
    }
    
    private fun showProfileFragment() {
        if (profileFragment == null) {
            profileFragment = ProfilePageFragment.newInstance()
        }
        
        val transaction = supportFragmentManager.beginTransaction()
        
        // 隐藏当前Fragment
        currentFragment?.let { transaction.hide(it) }
        
        if (profileFragment!!.isAdded) {
            transaction.show(profileFragment!!)
        } else {
            transaction.add(R.id.containerMain, profileFragment!!, "profile")
        }
        
        transaction.commitAllowingStateLoss()
        currentFragment = profileFragment
    }
}
