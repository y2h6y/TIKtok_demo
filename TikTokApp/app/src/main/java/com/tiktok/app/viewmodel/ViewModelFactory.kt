package com.tiktok.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tiktok.app.data.repository.CommentRepository
import com.tiktok.app.data.repository.VideoRepository

class ViewModelFactory(
    private val videoRepository: VideoRepository,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(videoRepository) as T
            }
            modelClass.isAssignableFrom(VideoDetailViewModel::class.java) -> {
                VideoDetailViewModel(videoRepository) as T
            }
            modelClass.isAssignableFrom(CommentViewModel::class.java) -> {
                CommentViewModel(commentRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
