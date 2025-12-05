package com.tiktok.app.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.tiktok.app.databinding.ActivityProfileEditBinding
import com.tiktok.app.utils.AvatarUploadHelper
import com.yalantis.ucrop.UCrop

/**
 * 个人资料编辑页面
 * 演示头像上传功能的使用
 */
class ProfileEditActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var avatarHelper: AvatarUploadHelper
    
    private var currentAvatarUri: Uri? = null
    private var tempImageUri: Uri? = null
    
    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            showAvatarPickerDialog()
        } else {
            Toast.makeText(this, "需要相机和存储权限", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 相机拍照
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            tempImageUri?.let { uri ->
                cropImage(uri)
            }
        }
    }
    
    // 相册选择
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                tempImageUri = uri
                cropImage(uri)
            }
        }
    }
    
    // 图片裁剪 (uCrop)
    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                result.data?.let { intent ->
                    val resultUri = UCrop.getOutput(intent)
                    if (resultUri != null) {
                        currentAvatarUri = resultUri
                        updateAvatarUI(resultUri)
                        Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            UCrop.RESULT_ERROR -> {
                result.data?.let { intent ->
                    val cropError = UCrop.getError(intent)
                    Toast.makeText(this, "裁剪失败: ${cropError?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        avatarHelper = AvatarUploadHelper(this)
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // 显示当前头像
        currentAvatarUri?.let {
            updateAvatarUI(it)
        }
    }
    
    private fun setupClickListeners() {
        // 点击头像上传
        binding.ivAvatar.setOnClickListener {
            if (avatarHelper.checkPermissions()) {
                showAvatarPickerDialog()
            } else {
                requestPermissions()
            }
        }
    }
    
    /**
     * 请求权限
     */
    private fun requestPermissions() {
        permissionLauncher.launch(avatarHelper.getRequiredPermissions())
    }
    
    /**
     * 显示头像选择对话框
     */
    private fun showAvatarPickerDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(this)
            .setTitle("选择头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                }
            }
            .show()
    }
    
    /**
     * 拍照
     */
    private fun takePhoto() {
        tempImageUri = avatarHelper.openCamera(cameraLauncher)
    }
    
    /**
     * 从相册选择
     */
    private fun pickFromGallery() {
        avatarHelper.openGallery(galleryLauncher)
    }
    
    /**
     * 裁剪图片 (使用 uCrop)
     */
    private fun cropImage(uri: Uri) {
        try {
            val cropIntent = avatarHelper.cropImageWithUCrop(uri)
            cropLauncher.launch(cropIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "裁剪功能初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新头像显示
     */
    private fun updateAvatarUI(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.ivAvatar)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        avatarHelper.cleanup()
    }
}
