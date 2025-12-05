package com.tiktok.app.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.edit

/**
 * 头像管理器 - 负责头像的持久化存储和读取
 */
object AvatarManager {
    
    private const val PREFS_NAME = "avatar_prefs"
    private const val KEY_AVATAR_URI = "avatar_uri"
    
    /**
     * 保存头像URI
     */
    fun saveAvatarUri(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_AVATAR_URI, uri.toString())
        }
    }
    
    /**
     * 获取保存的头像URI
     */
    fun getSavedAvatarUri(context: Context): Uri? {
        val uriString = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AVATAR_URI, null)
        return uriString?.let { Uri.parse(it) }
    }
    
    /**
     * 清除头像
     */
    fun clearAvatar(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_AVATAR_URI)
        }
    }
    
    /**
     * 检查是否有保存的头像
     */
    fun hasAvatar(context: Context): Boolean {
        return getSavedAvatarUri(context) != null
    }
}
