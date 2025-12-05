package com.tiktok.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 头像上传辅助类
 * 支持：相机拍照、相册选择、图片裁剪（使用 uCrop）
 */
class AvatarUploadHelper(private val activity: Activity) {
    
    private var currentPhotoUri: Uri? = null
    
    companion object {
        const val REQUEST_CAMERA = 1001
        const val REQUEST_GALLERY = 1002
        const val REQUEST_CROP = 1003
        
        // 需要的权限
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
    
    /**
     * 检查权限
     */
    fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取需要的权限列表
     */
    fun getRequiredPermissions(): Array<String> = REQUIRED_PERMISSIONS
    
    /**
     * 从相机拍照
     */
    fun openCamera(launcher: ActivityResultLauncher<Intent>): Uri? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // 创建临时文件
        val photoFile = createImageFile()
        photoFile?.let {
            currentPhotoUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                it
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            launcher.launch(intent)
            return currentPhotoUri
        }
        
        return null
    }
    
    /**
     * 从相册选择
     */
    fun openGallery(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        launcher.launch(intent)
    }
    
    /**
     * 使用 uCrop 裁剪图片（带可视化界面）
     */
    fun cropImageWithUCrop(sourceUri: Uri): Intent {
        // 创建输出文件
        val cropFile = createImageFile()
        val destinationUri = Uri.fromFile(cropFile)
        
        // 配置 uCrop
        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setCircleDimmedLayer(true) // 圆形遮罩
            setShowCropGrid(false) // 不显示网格
            setShowCropFrame(false) // 不显示边框
            
            // 设置颜色（让按钮更明显）
            setStatusBarColor(activity.getColor(android.R.color.black))
            setToolbarColor(activity.getColor(android.R.color.black))
            setActiveControlsWidgetColor(activity.getColor(android.R.color.holo_blue_light))
            setToolbarWidgetColor(activity.getColor(android.R.color.white)) // 按钮为白色
            
            // 设置标题
            setToolbarTitle("裁剪头像")
            
            // 保留底部控制栏（有旋转、翻转等功能）
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false) // 禁止自由裁剪，保持1:1
        }
        
        return UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // 1:1 比例
            .withMaxResultSize(800, 800) // 最大尺寸
            .withOptions(options)
            .getIntent(activity)
    }
    
    /**
     * 手动裁剪图片（简化版）
     * 将图片等比缩放并裁剪为正方形
     */
    fun manualCropImage(sourceUri: Uri, outputSize: Int = 300): Bitmap? {
        try {
            val inputStream = activity.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            return cropToSquare(bitmap, outputSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * 将bitmap裁剪为正方形
     */
    private fun cropToSquare(bitmap: Bitmap, size: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val minSize = minOf(width, height)
        
        // 裁剪为正方形
        val x = (width - minSize) / 2
        val y = (height - minSize) / 2
        val squareBitmap = Bitmap.createBitmap(bitmap, x, y, minSize, minSize)
        
        // 缩放到目标尺寸
        return Bitmap.createScaledBitmap(squareBitmap, size, size, true)
    }
    
    /**
     * 保存bitmap到文件
     */
    fun saveBitmapToFile(bitmap: Bitmap): File? {
        val file = createImageFile()
        file?.let {
            try {
                val fos = FileOutputStream(it)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                return it
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
    
    /**
     * 创建临时图片文件
     */
    private fun createImageFile(): File? {
        return try {
            val storageDir = activity.getExternalFilesDir("avatars")
            storageDir?.mkdirs()
            File.createTempFile(
                "avatar_${System.currentTimeMillis()}_",
                ".jpg",
                storageDir
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 清理临时文件
     */
    fun cleanup() {
        currentPhotoUri = null
    }
}
