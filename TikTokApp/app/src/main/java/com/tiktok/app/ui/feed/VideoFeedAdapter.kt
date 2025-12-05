package com.tiktok.app.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiktok.app.data.model.VideoItem
import com.tiktok.app.databinding.ItemVideoFeedBinding

class VideoFeedAdapter(
    private val onItemClick: (VideoItem, Int, android.view.View) -> Unit
) : RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder>() {
    
    private val videoList = mutableListOf<VideoItem>()
    
    fun submitList(newList: List<VideoItem>) {
        val diffCallback = VideoDiffCallback(videoList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        videoList.clear()
        videoList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding, onItemClick)
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position])
    }
    
    override fun getItemCount(): Int = videoList.size
    
    class VideoViewHolder(
        private val binding: ItemVideoFeedBinding,
        private val onItemClick: (VideoItem, Int, android.view.View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(video: VideoItem) {
            binding.apply {
                // 设置图片高度（瀑布流效果）
                val ratio = video.height.toFloat() / video.width
                val calculatedHeight = (root.width * ratio).toInt()
                ivCover.layoutParams.height = calculatedHeight
                
                // 加载封面图
                Glide.with(ivCover.context)
                    .load(video.coverUrl)
                    .centerCrop()
                    .into(ivCover)
                
                // 设置标题和统计数据
                tvTitle.text = video.title
                tvLikeCount.text = formatCount(video.likeCount)
                tvCommentCount.text = formatCount(video.commentCount)
                
                // 点击事件
                root.setOnClickListener {
                    onItemClick(video, bindingAdapterPosition, ivCover)
                }
                
                // 设置共享元素转场名称
                ivCover.transitionName = "video_cover_${video.id}"
            }
        }
        
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> String.format("%.1fw", count / 10000.0)
                count >= 1000 -> String.format("%.1fk", count / 1000.0)
                else -> count.toString()
            }
        }
    }
    
    class VideoDiffCallback(
        private val oldList: List<VideoItem>,
        private val newList: List<VideoItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
