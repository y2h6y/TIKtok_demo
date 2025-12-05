package com.tiktok.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktok.app.data.model.VideoItem
import com.tiktok.app.data.repository.VideoRepository
import kotlinx.coroutines.launch

class VideoDetailViewModel(
    private val repository: VideoRepository
) : ViewModel() {
    
    private val _currentVideo = MutableLiveData<VideoItem>()
    val currentVideo: LiveData<VideoItem> = _currentVideo
    
    private val _isPlaying = MutableLiveData<Boolean>(true)
    val isPlaying: LiveData<Boolean> = _isPlaying
    
    /**
     * 设置当前视频
     */
    fun setCurrentVideo(video: VideoItem) {
        _currentVideo.value = video
    }
    
    /**
     * 切换播放状态
     */
    fun togglePlayState() {
        _isPlaying.value = !(_isPlaying.value ?: true)
    }
    
    /**
     * 点赞/取消点赞
     */
    fun toggleLike() {
        val video = _currentVideo.value ?: return
        viewModelScope.launch {
            try {
                if (video.isLiked) {
                    repository.unlikeVideo(video.id)
                    _currentVideo.value = video.copy(
                        isLiked = false,
                        likeCount = maxOf(0, video.likeCount - 1)
                    )
                } else {
                    repository.likeVideo(video.id)
                    _currentVideo.value = video.copy(
                        isLiked = true,
                        likeCount = video.likeCount + 1
                    )
                }
            } catch (e: Exception) {
                // 暂时忽略错误，实际项目应该显示Toast
            }
        }
    }
}
