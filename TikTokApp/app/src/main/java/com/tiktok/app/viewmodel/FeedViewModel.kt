package com.tiktok.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktok.app.data.model.VideoItem
import com.tiktok.app.data.repository.VideoRepository
import com.tiktok.app.data.mock.MockDataGenerator
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: VideoRepository
) : ViewModel() {
    
    private val _videoList = MutableLiveData<List<VideoItem>>()
    val videoList: LiveData<List<VideoItem>> = _videoList
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private var currentCategory = "recommend"
    private var currentPage = 0
    private var isLoadingMore = false
    private var hasMore = true
    
    /**
     * 加载视频列表
     */
    fun loadVideos(category: String = currentCategory, refresh: Boolean = false) {
        if (isLoadingMore && !refresh) return
        if (!hasMore && !refresh) return
        
        currentCategory = category
        if (refresh) {
            currentPage = 0
            hasMore = true
            _isRefreshing.value = true
        } else {
            _isLoading.value = true
        }
        
        isLoadingMore = true
        
        viewModelScope.launch {
            try {
                // 使用Mock数据
                val mockVideos = MockDataGenerator.generateVideos(
                    category = category,
                    count = 20,
                    startIndex = currentPage * 20
                )
                
                val currentList = if (refresh) emptyList() else (_videoList.value ?: emptyList())
                _videoList.value = currentList + mockVideos
                
                currentPage++
                hasMore = currentPage < 5 // 模拟最多5页数据
                
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "加载失败"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
                isLoadingMore = false
            }
        }
    }
    
    /**
     * 刷新
     */
    fun refresh(category: String = currentCategory) {
        loadVideos(category, true)
    }
    
    /**
     * 加载更多
     */
    fun loadMore() {
        loadVideos(currentCategory, false)
    }
}
