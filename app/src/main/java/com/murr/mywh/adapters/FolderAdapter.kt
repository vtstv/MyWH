package com.murr.mywh.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.databinding.ItemFolderBinding
import java.text.SimpleDateFormat
import java.util.*

class FolderAdapter(
    private val onFolderClick: (Folder) -> Unit,
    private val onFolderLongClick: ((Folder) -> Unit)? = null,
    private val storageMap: Map<Long, String> = emptyMap()
) : ListAdapter<Folder, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val selectedIds = mutableSetOf<Long>()

    fun setSelectedIds(ids: Set<Long>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FolderViewHolder(
        private val binding: ItemFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(folder: Folder) {
            binding.tvFolderName.text = folder.name

            // Display storage name
            val storageName = storageMap[folder.storageId] ?: ""
            if (storageName.isNotEmpty()) {
                binding.tvStorageName.text = storageName
                binding.tvStorageName.visibility = View.VISIBLE
            } else {
                binding.tvStorageName.visibility = View.GONE
            }

            // Display description if present
            if (folder.description.isNotEmpty()) {
                binding.tvFolderDescription.text = folder.description
                binding.tvFolderDescription.visibility = View.VISIBLE
            } else {
                binding.tvFolderDescription.visibility = View.GONE
            }

            // Display creation date (mandatory)
            binding.tvCreatedAt.text = dateFormat.format(Date(folder.createdAt))
            binding.tvCreatedAt.visibility = View.VISIBLE

            // Display modification date if different from creation date
            if (folder.updatedAt != folder.createdAt) {
                binding.tvUpdatedAt.text = dateFormat.format(Date(folder.updatedAt))
                binding.tvUpdatedAt.visibility = View.VISIBLE
            } else {
                binding.tvUpdatedAt.visibility = View.GONE
            }

            // Display favorite icon
            binding.ivMarked.visibility = if (folder.isMarked) View.VISIBLE else View.GONE

            // Highlight selected folders
            val isSelected = selectedIds.contains(folder.id)
            if (isSelected) {
                binding.cardFolder.strokeWidth = 4
                binding.cardFolder.strokeColor = binding.root.context.getColor(android.R.color.holo_blue_light)
                binding.cardFolder.setCardBackgroundColor(binding.root.context.getColor(android.R.color.holo_blue_light).and(0x20FFFFFF.toInt()))
            } else {
                binding.cardFolder.strokeWidth = 0
                binding.cardFolder.setCardBackgroundColor(binding.root.context.getColor(android.R.color.transparent))
            }

            binding.root.setOnClickListener { onFolderClick(folder) }
            onFolderLongClick?.let { callback ->
                binding.root.setOnLongClickListener {
                    callback(folder)
                    true
                }
            }
        }
    }

    private class FolderDiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Folder, newItem: Folder) = 
            oldItem == newItem
    }
}
