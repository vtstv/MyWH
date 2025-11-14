package com.murr.mywh

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.murr.mywh.database.AppDatabase
import com.murr.mywh.database.entities.Folder
import com.murr.mywh.database.entities.Product
import com.murr.mywh.database.entities.Storage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadStorage() = runBlocking {
        val storage = Storage(name = "Test Storage", description = "Test Description")
        val storageId = db.storageDao().insertStorage(storage)

        val storages = db.storageDao().getAllStorages().first()
        assertEquals(1, storages.size)
        assertEquals("Test Storage", storages[0].name)
    }

    @Test
    fun insertAndReadFolder() = runBlocking {
        val storage = Storage(name = "Test Storage")
        val storageId = db.storageDao().insertStorage(storage)

        val folder = Folder(name = "Test Folder", storageId = storageId)
        val folderId = db.folderDao().insertFolder(folder)

        val folders = db.folderDao().getFoldersByStorage(storageId, 10, 0).first()
        assertEquals(1, folders.size)
        assertEquals("Test Folder", folders[0].name)
    }

    @Test
    fun insertAndReadProduct() = runBlocking {
        val storage = Storage(name = "Test Storage")
        val storageId = db.storageDao().insertStorage(storage)

        val folder = Folder(name = "Test Folder", storageId = storageId)
        val folderId = db.folderDao().insertFolder(folder)

        val product = Product(
            name = "Test Product",
            description = "Test Description",
            folderId = folderId
        )
        val productId = db.productDao().insertProduct(product)

        val products = db.productDao().getProductsByFolder(folderId, 10, 0).first()
        assertEquals(1, products.size)
        assertEquals("Test Product", products[0].name)
    }

    @Test
    fun markProduct() = runBlocking {
        val storage = Storage(name = "Test Storage")
        val storageId = db.storageDao().insertStorage(storage)

        val folder = Folder(name = "Test Folder", storageId = storageId)
        val folderId = db.folderDao().insertFolder(folder)

        val product = Product(
            name = "Test Product",
            description = "Test Description",
            folderId = folderId,
            isMarked = false
        )
        val productId = db.productDao().insertProduct(product)

        val retrievedProduct = db.productDao().getProductById(productId)
        assertNotNull(retrievedProduct)
        assertFalse(retrievedProduct!!.isMarked)

        retrievedProduct.isMarked = true
        db.productDao().updateProduct(retrievedProduct)

        val markedProducts = db.productDao().getMarkedProducts().first()
        assertEquals(1, markedProducts.size)
        assertTrue(markedProducts[0].isMarked)
    }

    @Test
    fun deleteProductCascade() = runBlocking {
        val storage = Storage(name = "Test Storage")
        val storageId = db.storageDao().insertStorage(storage)

        val folder = Folder(name = "Test Folder", storageId = storageId)
        val folderId = db.folderDao().insertFolder(folder)

        val product = Product(
            name = "Test Product",
            description = "Test Description",
            folderId = folderId
        )
        db.productDao().insertProduct(product)

        val folderToDelete = db.folderDao().getFoldersByStorage(storageId, 10, 0).first()[0]
        db.folderDao().deleteFolder(folderToDelete)

        val products = db.productDao().getProductsByFolder(folderId, 10, 0).first()
        assertEquals(0, products.size)
    }
}

