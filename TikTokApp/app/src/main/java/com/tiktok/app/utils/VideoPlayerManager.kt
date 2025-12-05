package com.tiktok.app.utils

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * 视频播放器管理器 - 单例模式
 * 统一管理ExoPlayer实例，防止内存泄漏
 */
object VideoPlayerManager {
    
    private var player: ExoPlayer? = null
    
    /**
     * 获取播放器实例
     */
    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            // 配置音频属性
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()
            
            player = ExoPlayer.Builder(context.applicationContext)
                .build()
                .apply {
                    // 设置音频属性
                    setAudioAttributes(audioAttributes, true)
                    // 单曲循环
                    repeatMode = Player.REPEAT_MODE_ONE
                    // 设置音量
                    volume = 1.0f
                }
        }
        return player!!
    }
    
    /**
     * 释放播放器
     */
    fun release() {
        player?.release()
        player = null
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        player?.pause()
    }
    
    /**
     * 继续播放
     */
    fun play() {
        player?.play()
    }
}
