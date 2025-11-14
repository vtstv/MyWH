package com.murr.mywh.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.murr.mywh.databinding.DialogAddProductBinding

class AddProductDialog(
    private val onProductAdded: (name: String, description: String) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddProductBinding.inflate(LayoutInflater.from(context))

        return AlertDialog.Builder(requireContext())
            .setTitle("Add Product")
            .setView(binding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = binding.etProductName.text.toString()
                val description = binding.etProductDescription.text.toString()
                if (name.isNotBlank()) {
                    onProductAdded(name, description)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

