package com.tiktok.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktok.app.data.model.Comment
import com.tiktok.app.data.repository.CommentRepository
import com.tiktok.app.data.mock.MockDataGenerator
import kotlinx.coroutines.launch

class CommentViewModel(
    private val repository: CommentRepository
) : ViewModel() {
    
    private val _commentList = MutableLiveData<List<Comment>>()
    val commentList: LiveData<List<Comment>> = _commentList
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _postSuccess = MutableLiveData<Boolean>()
    val postSuccess: LiveData<Boolean> = _postSuccess
    
    private var currentVideoId: String? = null
    private var currentPage = 0
    
    /**
     * 加载评论列表
     */
    fun loadComments(videoId: String, refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            currentVideoId = videoId
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // 使用Mock数据
                val mockComments = MockDataGenerator.generateComments(videoId, 20)
                
                val currentList = if (refresh) emptyList() else (_commentList.value ?: emptyList())
                _commentList.value = currentList + mockComments
                
                currentPage++
            } catch (e: Exception) {
                // 错误处理
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 发布评论
     */
    fun postComment(videoId: String, content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                val result = repository.postComment(videoId, content)
                if (result.isSuccess) {
                    // 添加到列表顶部
                    val currentList = _commentList.value ?: emptyList()
                    _commentList.value = listOf(result.getOrNull()!!) + currentList
                    _postSuccess.value = true
                } else {
                    _postSuccess.value = false
                }
            } catch (e: Exception) {
                _postSuccess.value = false
            }
        }
    }
}
