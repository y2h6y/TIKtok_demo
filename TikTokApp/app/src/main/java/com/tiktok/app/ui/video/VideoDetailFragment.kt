package com.tiktok.app.ui.video

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.bumptech.glide.Glide
import com.tiktok.app.R
import com.tiktok.app.TikTokApplication
import com.tiktok.app.data.model.VideoItem
import com.tiktok.app.databinding.ItemVideoDetailBinding
import com.tiktok.app.ui.comment.CommentBottomSheet
import com.tiktok.app.utils.VideoPlayerManager
import com.tiktok.app.viewmodel.VideoDetailViewModel

class VideoDetailFragment : Fragment() {
    
    private var _binding: ItemVideoDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: VideoDetailViewModel
    private var player: ExoPlayer? = null
    private lateinit var video: VideoItem
    private lateinit var gestureDetector: GestureDetectorCompat
    private var musicDiscAnimator: ObjectAnimator? = null
    private var playerListener: Player.Listener? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        video = arguments?.getSerializable(ARG_VIDEO) as VideoItem
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupUI()
        setupPlayer()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        val app = requireActivity().application as TikTokApplication
        viewModel = ViewModelProvider(
            this,
            app.viewModelFactory
        )[VideoDetailViewModel::class.java]
        
        viewModel.setCurrentVideo(video)
    }
    
    private fun setupUI() {
        binding.apply {
            // 加载作者头像
            Glide.with(requireContext())
                .load(video.authorAvatar)
                .circleCrop()
                .into(ivAuthorAvatar)
            
            // 设置信息
            tvAuthorName.text = "@${video.authorName}"
            tvDescription.text = video.description
            tvLikeCount.text = formatCount(video.likeCount)
            tvCommentCount.text = formatCount(video.commentCount)
            tvShareCount.text = formatCount(video.shareCount)
            
            // 设置点赞状态
            updateLikeButton(video.isLiked)
            
            // 点击头像进入作者主页（暂未实现）
            ivAuthorAvatar.setOnClickListener {
                Toast.makeText(requireContext(), "查看作者主页：${video.authorName}", Toast.LENGTH_SHORT).show()
                // TODO: 跳转到作者个人主页
            }
        }
    }
    
    private fun setupPlayer() {
        player = VideoPlayerManager.getPlayer(requireContext())
        
        // Remove old connection
        binding.playerView.player = null
        
        // Connect player to PlayerView
        binding.playerView.player = player
        binding.playerView.setKeepScreenOn(true)
        binding.playerView.transitionName = "video_cover_${video.id}"
        
        // 添加播放器监听器
        playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // 检查 Fragment 是否还存活
                if (_binding == null) return
                
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _binding?.progressLoading?.visibility = View.VISIBLE
                        _binding?.tvError?.visibility = View.GONE
                    }
                    Player.STATE_READY -> {
                        _binding?.progressLoading?.visibility = View.GONE
                        _binding?.tvError?.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        _binding?.progressLoading?.visibility = View.GONE
                    }
                }
            }
            
            
            override fun onPlayerError(error: PlaybackException) {
                // 检查 Fragment 是否还存活
                if (_binding == null) return
                
                // 播放错误
                _binding?.progressLoading?.visibility = View.GONE
                _binding?.tvError?.visibility = View.VISIBLE
                android.util.Log.e("VideoDetailFragment", "播放错误: ${error.message}", error)
                
                // 检查 Context 是否还可用
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "视频加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        player?.addListener(playerListener!!)
        
        // 错误提示点击重试
        binding.tvError.setOnClickListener {
            loadVideo()
        }
        
        // 直接加载视频
        loadVideo()
    }
    
    private fun loadVideo() {
        // 检查 binding 是否可用
        if (_binding == null) return
        
        _binding?.progressLoading?.visibility = View.VISIBLE
        _binding?.tvError?.visibility = View.GONE
        
        try {
            val mediaItem = MediaItem.fromUri(video.videoUrl)
            player?.apply {
                stop()
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                volume = 1.0f
            }
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailFragment", "Load video failed", e)
            _binding?.progressLoading?.visibility = View.GONE
            _binding?.tvError?.visibility = View.VISIBLE
        }
    }
    
    private fun setupClickListeners() {
        // 手势检测器（双击点赞）
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 双击点赞
                if (!video.isLiked) {
                    viewModel.toggleLike()
                }
                playDoubleTapLikeAnimation()
                return true
            }
            
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // 单击暂停/播放
                viewModel.togglePlayState()
                return true
            }
        })
        
        binding.apply {
            // 视频区域手势处理
            playerView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
            
            // 点赞按钮
            llLike.setOnClickListener {
                viewModel.toggleLike()
            }
            
            // 评论按钮
            llComment.setOnClickListener {
                showCommentSheet()
            }
            
            // 分享按钮
            llShare.setOnClickListener {
                // TODO: 实现分享功能
            }
            
            // 刷新按钮
            btnRefresh.setOnClickListener {
                refreshVideo()
            }
        }
        
        // 启动音乐转盘旋转动画
        startMusicDiscRotation()
    }
    
    /**
     * 刷新视频 - 重新加载当前视频
     */
    private fun refreshVideo() {
        // 旋转动画
        binding.btnRefresh.animate()
            .rotation(binding.btnRefresh.rotation + 360f)
            .setDuration(500)
            .start()
        
        // 重新加载视频
        loadVideo()
        
        Toast.makeText(requireContext(), "正在刷新...", Toast.LENGTH_SHORT).show()
    }
    
    private fun observeViewModel() {
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                player?.play()
                binding.ivPauseIcon.visibility = View.GONE
                musicDiscAnimator?.resume()
            } else {
                player?.pause()
                binding.ivPauseIcon.visibility = View.VISIBLE
                musicDiscAnimator?.pause()
            }
        }
        
        viewModel.currentVideo.observe(viewLifecycleOwner) { video ->
            binding.tvLikeCount.text = formatCount(video.likeCount)
            updateLikeButton(video.isLiked)
        }
    }
    
    private fun updateLikeButton(isLiked: Boolean) {
        if (isLiked) {
            binding.ivLike.setImageResource(R.drawable.ic_like_filled)
            binding.ivLike.setColorFilter(
                resources.getColor(R.color.tiktok_red, null),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.ivLike.setImageResource(R.drawable.ic_like_outline)
            binding.ivLike.setColorFilter(
                resources.getColor(R.color.white, null),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }
    
    private fun showCommentSheet() {
        val commentSheet = CommentBottomSheet.newInstance(video.id)
        commentSheet.show(childFragmentManager, "CommentBottomSheet")
    }
    
    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> String.format("%.1fw", count / 10000.0)
            count >= 1000 -> String.format("%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }
    
    /**
     * 双击点赞动画
     */
    private fun playDoubleTapLikeAnimation() {
        binding.ivDoubleTapLike.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            
            // 缩放动画
            val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1.2f, 1f)
            val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f, 0f)
            
            val animSet = AnimatorSet()
            animSet.playTogether(scaleX, scaleY, alpha)
            animSet.duration = 800
            animSet.interpolator = AccelerateDecelerateInterpolator()
            
            animSet.start()
            
            // 动画结束后隐藏
            postDelayed({
                visibility = View.GONE
            }, 800)
        }
    }
    
    /**
     * 启动音乐转盘旋转动画
     */
    private fun startMusicDiscRotation() {
        musicDiscAnimator = ObjectAnimator.ofFloat(binding.ivMusicDisc, "rotation", 0f, 360f).apply {
            duration = 10000 // 10秒转一圈
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            start()
        }
    }
    
    /**
     * 停止音乐转盘旋转
     */
    private fun stopMusicDiscRotation() {
        musicDiscAnimator?.cancel()
        musicDiscAnimator = null
    }
    
    override fun onResume() {
        super.onResume()
        // Reconnect player to current Fragment's PlayerView
        binding.playerView.player = null
        binding.playerView.player = player
        
        // 如果已经有视频，继续播放
        if (player?.currentMediaItem != null) {
            player?.play()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Disconnect player
        binding.playerView.player = null
        player?.pause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // 移除播放器监听器，防止内存泄漏
        playerListener?.let { player?.removeListener(it) }
        playerListener = null
        
        // 停止播放器
        player?.stop()
        binding.playerView.player = null
        
        // 停止动画
        stopMusicDiscRotation()
        
        _binding = null
    }
    
    companion object {
        private const val ARG_VIDEO = "video"
        
        fun newInstance(video: VideoItem): VideoDetailFragment {
            return VideoDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_VIDEO, video)
                }
            }
        }
    }
}
