package com.example.dailydose

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditProfileFragment : Fragment() {

    private lateinit var editFname: EditText
    private lateinit var editLname: EditText
    private lateinit var editEmail: EditText
    private lateinit var buttonUpdate: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var profileImage: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageUri: Uri
    private var selectedImageUrl: String? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 1002
        private const val GALLERY_REQUEST_CODE = 1001
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            profileImage.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        editFname = view.findViewById(R.id.textFname)
        editLname = view.findViewById(R.id.textLname)
        buttonUpdate = view.findViewById(R.id.buttonEdit)
        buttonBack = view.findViewById(R.id.buttonBack)
        profileImage = view.findViewById(R.id.profileImage)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserData()

        profileImage.setOnClickListener {
            showImageSourceDialog()
        }

        buttonUpdate.setOnClickListener {
            updateUserData()
        }

        buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            imageUri = createImageUri()
            takePictureLauncher.launch(imageUri)
        }
    }

    private fun createImageUri(): Uri {
        val image = File(requireContext().filesDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileProvider", image)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                imageUri = it
                profileImage.setImageURI(imageUri)
            }
        }
    }

    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fname = document.getString("Fname")
                        val lname = document.getString("Lname")
                        selectedImageUrl = document.getString("profileImage")

                        editFname.setText(fname)
                        editLname.setText(lname)

                        if (selectedImageUrl != null) {
                            Glide.with(this)
                                .load(selectedImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .into(profileImage)
                        } else {
                            profileImage.setImageResource(R.drawable.default_profile)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageAndSaveData(imageUri: Uri, onComplete: (String?) -> Unit) {
        val storageRef = storage.reference.child("profile_images/${firebaseAuth.currentUser?.uid}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    private fun updateUserData() {
        val userId = firebaseAuth.currentUser?.uid
        val fname = editFname.text.toString().trim()
        val lname = editLname.text.toString().trim()

        if (userId != null && fname.isNotEmpty() && lname.isNotEmpty()) {
            val updatedUser: MutableMap<String, Any> = hashMapOf(
                "Fname" to fname,
                "Lname" to lname
            )

            if (::imageUri.isInitialized) {
                uploadImageAndSaveData(imageUri) { imageUrl ->
                    if (imageUrl != null) {
                        updatedUser["profileImage"] = imageUrl
                        saveToFirestore(userId, updatedUser)
                    } else {
                        Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                saveToFirestore(userId, updatedUser)
            }
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToFirestore(userId: String, updatedUser: Map<String, Any>) {
        firestore.collection("users").document(userId)
            .update(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show()
            }
    }
}
