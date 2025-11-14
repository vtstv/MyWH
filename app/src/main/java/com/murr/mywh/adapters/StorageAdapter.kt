package com.murr.mywh.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.murr.mywh.database.entities.Storage
import com.murr.mywh.databinding.ItemStorageBinding

class StorageAdapter(
    private val onStorageClick: (Storage) -> Unit
) : ListAdapter<Storage, StorageAdapter.StorageViewHolder>(StorageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        val binding = ItemStorageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StorageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StorageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StorageViewHolder(
        private val binding: ItemStorageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(storage: Storage) {
            binding.tvStorageName.text = storage.name
            binding.tvStorageDescription.text = storage.description
            binding.root.setOnClickListener { onStorageClick(storage) }
        }
    }

    private class StorageDiffCallback : DiffUtil.ItemCallback<Storage>() {
        override fun areItemsTheSame(oldItem: Storage, newItem: Storage) = 
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Storage, newItem: Storage) = 
            oldItem == newItem
    }
}
