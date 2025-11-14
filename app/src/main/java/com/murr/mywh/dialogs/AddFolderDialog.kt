package com.murr.mywh.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.murr.mywh.R
import com.murr.mywh.databinding.DialogAddFolderBinding

class AddFolderDialog(
    private val initialName: String = "",
    private val initialDescription: String = "",
    private val isSubFolder: Boolean = false,
    private val onFolderAdded: (name: String, description: String) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddFolderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddFolderBinding.inflate(LayoutInflater.from(context))

        // Set initial values if editing
        binding.etFolderName.setText(initialName)
        binding.etFolderDescription.setText(initialDescription)

        val title = when {
            initialName.isNotEmpty() -> R.string.edit_folder
            isSubFolder -> R.string.add_subfolder
            else -> R.string.add_folder
        }

        val buttonText = if (initialName.isEmpty()) R.string.ok else R.string.ok

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(buttonText) { _, _ ->
                val name = binding.etFolderName.text.toString()
                val description = binding.etFolderDescription.text.toString()
                if (name.isNotBlank()) {
                    onFolderAdded(name, description)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

