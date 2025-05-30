package com.example.dailydose

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dailydose.AddFragment.Companion
import com.example.dailydose.databinding.FragmentAddBinding
import com.example.dailydose.databinding.FragmentEditJournalBinding
import java.io.File


class EditJournalFragment : Fragment() {

    private lateinit var binding: FragmentEditJournalBinding
    private lateinit var imageUri: Uri
    private var id: String? = null
    private var title: String? = null
    private var selectedImageUrl: String? = null
    private var mood: String? = null
    private var content: String? = null

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
        private const val CAMERA_REQUEST_CODE = 1002
        private const val DEFAULT_IMAGE = "android.resource://com.example.dailydose/drawable/default_image" // Adjust the package name and drawable name
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.uploadimageview.setImageURI(imageUri)
            selectedImageUrl = imageUri.toString()
        } else {
            binding.uploadimageview.setImageURI(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            id = it.getString("id")
            title = it.getString("title")
            selectedImageUrl = it.getString("imageUrl")
            binding.uploadimageview.setImageURI(selectedImageUrl?.toUri())
            mood = it.getString("mood")
            content = it.getString("content")
        }

        binding.editTextTitle.setText(title)
        binding.moodButtonGroup.buttons.forEach { button ->
            if (button.text == mood) {
                binding.moodButtonGroup.selectButtonWithAnimation(button)
                binding.moodButtonGroup.selectButton(button)

            }
        }



        binding.uploadimageview.setOnClickListener {
            openGallery()
        }

        binding.cameraimageview.setOnClickListener {
            requestCameraPermission()
        }


        binding.buttonNext.setOnClickListener {
            val journalTitle = binding.editTextTitle.text.toString()

            if (journalTitle.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a journal title", Toast.LENGTH_SHORT).show()
            } else {
                val selectedButtons = binding.moodButtonGroup.selectedButtons
                if (selectedButtons.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select your mood", Toast.LENGTH_SHORT).show()
                } else {
                    val moodToSend = selectedButtons.first().text

                    // Set selectedImageUrl to default image if no image is selected
                    val imageToSend = selectedImageUrl ?: DEFAULT_IMAGE

                    // Prepare to navigate to AddFragmentTwo
                    val bundle = Bundle().apply {
                        putString("id", id)
                        putString("journalTitle", journalTitle)
                        putString("selectedImageUrl", imageToSend)
                        putString("selectedMood", moodToSend)
                        putString("content", content)
                    }

                    // Navigate to AddFragmentTwo
                    findNavController().navigate(R.id.action_editJournalFragment_to_editJournalFragmentTwo, bundle)
                }
            }
        }
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
            val imageUri = data?.data
            selectedImageUrl = imageUri?.toString()
            binding.uploadimageview.setImageURI(imageUri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageUri = createImageUri()
                takePictureLauncher.launch(imageUri)
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    }

}