package com.tiktok.app.ui.video

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tiktok.app.TikTokApplication
import com.tiktok.app.data.model.VideoItem
import com.tiktok.app.databinding.ActivityVideoDetailBinding
import com.tiktok.app.viewmodel.FeedViewModel

class VideoDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoDetailBinding
    private lateinit var feedViewModel: FeedViewModel
    
    private var videoList: List<VideoItem> = emptyList()
    private var initialPosition: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 启用转场动画
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        
        super.onCreate(savedInstanceState)
        binding = ActivityVideoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置共享元素转场动画
        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)
        window.sharedElementReturnTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)
        
        // 获取传入的参数
        initialPosition = intent.getIntExtra(EXTRA_VIDEO_POSITION, 0)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "recommend"
        
        setupViewModel(category)
        observeViewModel()
    }
    
    private fun setupViewModel(category: String) {
        val app = application as TikTokApplication
        feedViewModel = ViewModelProvider(this, app.viewModelFactory)[FeedViewModel::class.java]
        feedViewModel.loadVideos(category)
    }
    
    private fun observeViewModel() {
        feedViewModel.videoList.observe(this) { videos ->
            if (videos.isNotEmpty()) {
                videoList = videos
                setupViewPager()
            }
        }
    }
    
    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = videoList.size
            
            override fun createFragment(position: Int): Fragment {
                return VideoDetailFragment.newInstance(videoList[position])
            }
        }
        
        // 设置初始位置
        binding.viewPager.setCurrentItem(initialPosition, false)
        
        // 设置页面切换限制（预加载前后1页）
        binding.viewPager.offscreenPageLimit = 1
        
        // 监听页面滑动，实现上拉加载更多
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                
                // 滑到倒数第3个时加载更多
                if (position >= videoList.size - 3) {
                    loadMoreVideos()
                }
            }
        })
    }
    
    /**
     * 加载更多视频
     */
    private fun loadMoreVideos() {
        if (feedViewModel.isLoading.value == true) {
            return // 正在加载，避免重复请求
        }
        
        feedViewModel.loadMore()
        // Toast已被底部加载提示替代，这里不再显示
    }
    
    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_VIDEO_POSITION = "video_position"
        const val EXTRA_CATEGORY = "category"
    }
}
