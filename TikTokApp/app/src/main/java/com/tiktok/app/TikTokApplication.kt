package com.tiktok.app

import android.app.Application
import com.tiktok.app.data.api.RetrofitClient
import com.tiktok.app.data.db.AppDatabase
import com.tiktok.app.data.repository.CommentRepository
import com.tiktok.app.data.repository.VideoRepository
import com.tiktok.app.viewmodel.ViewModelFactory

class TikTokApplication : Application() {
    
    // 数据库
    private val database by lazy { AppDatabase.getInstance(this) }
    
    // Repository
    private val videoRepository by lazy {
        VideoRepository(
            RetrofitClient.apiService,
            database.videoDao()
        )
    }
    
    private val commentRepository by lazy {
        CommentRepository(
            RetrofitClient.apiService,
            database.commentDao()
        )
    }
    
    // ViewModel Factory
    val viewModelFactory by lazy {
        ViewModelFactory(videoRepository, commentRepository)
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}
