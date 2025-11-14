package com.murr.mywh.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.murr.mywh.R
import com.murr.mywh.databinding.DialogAddStorageBinding

class AddStorageDialog(
    private val initialName: String = "",
    private val initialDescription: String = "",
    private val onStorageAdded: (name: String, description: String) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddStorageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddStorageBinding.inflate(LayoutInflater.from(context))

        // Set initial values if editing
        binding.etStorageName.setText(initialName)
        binding.etStorageDescription.setText(initialDescription)

        val title = if (initialName.isEmpty()) R.string.add_storage else R.string.edit_storage
        val buttonText = if (initialName.isEmpty()) R.string.ok else R.string.ok

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton(buttonText) { _, _ ->
                val name = binding.etStorageName.text.toString()
                val description = binding.etStorageDescription.text.toString()
                if (name.isNotBlank()) {
                    onStorageAdded(name, description)
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

