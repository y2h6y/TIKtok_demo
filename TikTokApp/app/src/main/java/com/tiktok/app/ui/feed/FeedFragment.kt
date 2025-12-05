package com.tiktok.app.ui.feed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tiktok.app.R
import com.tiktok.app.TikTokApplication
import com.tiktok.app.databinding.FragmentFeedBinding
import com.tiktok.app.ui.video.VideoDetailActivity
import com.tiktok.app.viewmodel.FeedViewModel

class FeedFragment : Fragment() {
    
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: VideoFeedAdapter
    private var category: String = "recommend"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY) ?: "recommend"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
        
        // 加载数据
        viewModel.loadVideos(category)
    }
    
    private fun setupViewModel() {
        val app = requireActivity().application as TikTokApplication
        viewModel = ViewModelProvider(
            this,
            app.viewModelFactory
        )[FeedViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = VideoFeedAdapter { video, position, coverImageView ->
            // 跳转到视频详情页，添加转场动画
            val intent = Intent(requireContext(), VideoDetailActivity::class.java).apply {
                putExtra(VideoDetailActivity.EXTRA_VIDEO_ID, video.id)
                putExtra(VideoDetailActivity.EXTRA_VIDEO_POSITION, position)
                putExtra(VideoDetailActivity.EXTRA_CATEGORY, category)
            }
            
            // 使用共享元素转场动画
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                coverImageView,
                "video_cover_${video.id}"
            )
            startActivity(intent, options.toBundle())
        }
        
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@FeedFragment.adapter
            setHasFixedSize(true)
            
            // 滚动监听（加载更多）
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    if (!recyclerView.canScrollVertically(1)) {
                        // 滚动到底部，加载更多
                        viewModel.loadMore()
                    }
                }
            })
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            // 设置刷新动画颜色（TikTok红+粉+黄渐变）
            setColorSchemeResources(
                R.color.tiktok_red,
                R.color.tiktok_pink,
                android.R.color.holo_orange_light,
                R.color.tiktok_blue
            )
            // 设置背景颜色
            setProgressBackgroundColorSchemeResource(android.R.color.white)
            // 设置刷新动画的起始和结束位置
            setProgressViewOffset(false, 0, 150)
            // 设置刷新监听
            setOnRefreshListener {
                viewModel.refresh(category)
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.videoList.observe(viewLifecycleOwner) { videos ->
            adapter.submitList(videos)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 首次加载显示中心进度条
            if (adapter.itemCount == 0) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            } else {
                // 加载更多时显示底部提示
                binding.loadingFooter.root.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_CATEGORY = "category"
        
        fun newInstance(category: String): FeedFragment {
            return FeedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }
}
