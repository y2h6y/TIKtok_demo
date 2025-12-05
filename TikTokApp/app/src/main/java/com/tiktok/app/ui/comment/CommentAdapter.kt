package com.tiktok.app.ui.comment

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiktok.app.data.model.Comment
import com.tiktok.app.databinding.ItemCommentBinding

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    
    private val commentList = mutableListOf<Comment>()
    
    fun submitList(newList: List<Comment>) {
        commentList.clear()
        commentList.addAll(newList)
        notifyDataSetChanged()
    }
    
    fun addComment(comment: Comment) {
        commentList.add(0, comment)
        notifyItemInserted(0)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentList[position])
    }
    
    override fun getItemCount(): Int = commentList.size
    
    class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(comment: Comment) {
            binding.apply {
                // 加载头像
                Glide.with(ivAvatar.context)
                    .load(comment.avatarUrl)
                    .circleCrop()
                    .into(ivAvatar)
                
                // 设置用户名和内容
                tvUserName.text = comment.userName
                tvContent.text = comment.content
                
                // 设置时间（相对时间）
                tvTime.text = getRelativeTime(comment.timestamp)
            }
        }
        
        private fun getRelativeTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "刚刚"
                diff < 3600000 -> "${diff / 60000}分钟前"
                diff < 86400000 -> "${diff / 3600000}小时前"
                diff < 604800000 -> "${diff / 86400000}天前"
                else -> DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    now,
                    DateUtils.DAY_IN_MILLIS
                ).toString()
            }
        }
    }
}
