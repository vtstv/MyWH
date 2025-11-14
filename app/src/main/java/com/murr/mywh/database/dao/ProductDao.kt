package com.murr.mywh.database.dao

import androidx.room.*
import com.murr.mywh.database.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE folderId = :folderId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getProductsByFolder(folderId: Long, limit: Int, offset: Int): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE folderId = :folderId AND name LIKE '%' || :query || '%'")
    fun searchProductsInFolder(folderId: Long, query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isMarked = 1 ORDER BY name ASC")
    fun getMarkedProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Long): Product?

    @Insert
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isMarked = 1")
    suspend fun getMarkedProductCount(): Int
}
