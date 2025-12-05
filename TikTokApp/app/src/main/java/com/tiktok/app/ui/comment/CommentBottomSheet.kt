package com.tiktok.app.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tiktok.app.TikTokApplication
import com.tiktok.app.databinding.BottomSheetCommentBinding
import com.tiktok.app.viewmodel.CommentViewModel

class CommentBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetCommentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CommentViewModel
    private lateinit var adapter: CommentAdapter
    private var videoId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoId = arguments?.getString(ARG_VIDEO_ID) ?: ""
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCommentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置BottomSheet初始高度为屏幕的70%
        setupBottomSheetHeight()
        
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // 加载评论
        viewModel.loadComments(videoId, true)
    }
    
    private fun setupBottomSheetHeight() {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val peekHeight = (screenHeight * 0.7).toInt() // 初始高度为屏幕70%
        
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as? com.google.android.material.bottomsheet.BottomSheetDialog)?.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.layoutParams?.height = peekHeight
            bottomSheet?.requestLayout()
            
            // 设置BottomSheet行为
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
            behavior.peekHeight = peekHeight
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    
    private fun setupViewModel() {
        val app = requireActivity().application as TikTokApplication
        viewModel = ViewModelProvider(
            this,
            app.viewModelFactory
        )[CommentViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = CommentAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CommentBottomSheet.adapter
            
            // 确保RecyclerView可见和可滚动
            isNestedScrollingEnabled = true
            
            // 调试信息
            post {
                android.util.Log.d("CommentBottomSheet", "RecyclerView高度: $height, 宽度: $width")
                android.util.Log.d("CommentBottomSheet", "RecyclerView可见性: $visibility")
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.postComment(videoId, content)
            } else {
                Toast.makeText(requireContext(), "请输入评论内容", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.commentList.observe(viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
            binding.tvCommentCount.text = "${comments.size}条评论"
            
            // 调试日志
            android.util.Log.d("CommentBottomSheet", "评论列表更新: ${comments.size}条评论")
            
            // 如果有评论，滚动到顶部显示最新评论
            if (comments.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // 可以显示loading状态
        }
        
        viewModel.postSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.etComment.text?.clear()
                hideKeyboard()
                Toast.makeText(requireContext(), "评论成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "评论失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_VIDEO_ID = "video_id"
        
        fun newInstance(videoId: String): CommentBottomSheet {
            return CommentBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIDEO_ID, videoId)
                }
            }
        }
    }
}
