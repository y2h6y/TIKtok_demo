package com.tiktok.app.ui.profile

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.tiktok.app.databinding.FragmentProfilePageBinding
import com.tiktok.app.utils.AvatarManager
import com.tiktok.app.utils.AvatarUploadHelper
import com.yalantis.ucrop.UCrop

/**
 * 个人中心Fragment - 在底部导航的"我的"Tab中显示
 */
class ProfilePageFragment : Fragment() {
    
    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!
    
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
            Toast.makeText(requireContext(), "需要相机和存储权限", Toast.LENGTH_SHORT).show()
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
                        // 持久化保存头像URI
                        android.util.Log.d("ProfilePageFragment", "[cropLauncher] 保存头像: $currentAvatarUri")
                        AvatarManager.saveAvatarUri(requireContext(), currentAvatarUri!!)
                        updateAvatarUI(currentAvatarUri!!)
                        Toast.makeText(requireContext(), "头像已更新 ✅", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            UCrop.RESULT_ERROR -> {
                result.data?.let { intent ->
                    val cropError = UCrop.getError(intent)
                    android.util.Log.e("ProfilePageFragment", "[cropLauncher] 裁剪失败: ${cropError?.message}")
                    Toast.makeText(requireContext(), "裁剪失败: ${cropError?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        avatarHelper = AvatarUploadHelper(requireActivity())
        setupClickListeners()
        loadSavedAvatar()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次返回时重新加载头像
        loadSavedAvatar()
    }
    
    private fun setupClickListeners() {
        // 点击上传头像按钮
        binding.btnUploadAvatar.setOnClickListener {
            if (avatarHelper.checkPermissions()) {
                showAvatarPickerDialog()
            } else {
                requestPermissions()
            }
        }
        
        // 点击头像也可以上传
        binding.ivAvatar.setOnClickListener {
            binding.btnUploadAvatar.performClick()
        }
    }
    
    private fun requestPermissions() {
        permissionLauncher.launch(avatarHelper.getRequiredPermissions())
    }
    
    private fun showAvatarPickerDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(requireContext())
            .setTitle("选择头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                }
            }
            .show()
    }
    
    private fun takePhoto() {
        tempImageUri = avatarHelper.openCamera(cameraLauncher)
    }
    
    private fun pickFromGallery() {
        avatarHelper.openGallery(galleryLauncher)
    }
    
    private fun cropImage(uri: Uri) {
        try {
            val cropIntent = avatarHelper.cropImageWithUCrop(uri)
            cropLauncher.launch(cropIntent)
        } catch (e: Exception) {
            android.util.Log.e("ProfilePageFragment", "[cropImage] 裁剪功能初始化失败", e)
            Toast.makeText(requireContext(), "裁剪功能初始化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadSavedAvatar() {
        try {
            val savedUri = AvatarManager.getSavedAvatarUri(requireContext())
            android.util.Log.d("ProfilePageFragment", "加载头像: $savedUri")
            
            if (savedUri != null && savedUri.toString().isNotEmpty()) {
                // 加载保存的头像
                currentAvatarUri = savedUri
                updateAvatarUI(savedUri)
            } else {
                // 加载默认头像
                android.util.Log.d("ProfilePageFragment", "使用默认头像")
                Glide.with(this)
                    .load("https://picsum.photos/200")
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .into(binding.ivAvatar)
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfilePageFragment", "加载头像失败", e)
            // 出错时显示默认图标
            binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
    }
    
    private fun updateAvatarUI(uri: Uri) {
        try {
            android.util.Log.d("ProfilePageFragment", "更新头像UI: $uri")
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_myplaces)
                .error(android.R.drawable.ic_menu_myplaces)
                .into(binding.ivAvatar)
        } catch (e: Exception) {
            android.util.Log.e("ProfilePageFragment", "更新头像UI失败", e)
            binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = ProfilePageFragment()
    }
}
