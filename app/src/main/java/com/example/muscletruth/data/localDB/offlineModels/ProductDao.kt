package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products WHERE (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%')")
    suspend fun getProducts(searchQuery: String? = null): MutableList<Product>

    @Query("SELECT * FROM products WHERE server_id = :productID")
    suspend fun getProduct(productID: Int): Product?

    @Query("SELECT * FROM products WHERE server_id = -1")
    suspend fun getProductsForSync(): List<Product>
}