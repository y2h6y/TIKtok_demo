package com.tiktok.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.tiktok.app.databinding.FragmentHomeBinding
import com.tiktok.app.ui.feed.FeedFragment

/**
 * 首页Fragment，包含推荐/关注/附近的视频流
 */
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val categories = listOf("recommend", "following", "nearby")
    private val tabTitles = listOf("推荐", "关注", "附近")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }
    
    private fun setupViewPager() {
        // 设置ViewPager2适配器
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = categories.size
            
            override fun createFragment(position: Int): Fragment {
                return FeedFragment.newInstance(categories[position])
            }
        }
        
        // 关联TabLayout和ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HomeFragment()
    }
}
