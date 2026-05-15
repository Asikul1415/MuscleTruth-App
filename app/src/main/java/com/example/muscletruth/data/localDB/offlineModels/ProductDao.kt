package com.example.muscletruth.data.localDB.offlineModels

import androidx.room.*
import com.example.muscletruth.data.models.FavouriteProduct
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.models.RecentServing

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentProduct(recentProduct: RecentServing)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavouriteProduct(favouriteProduct: FavouriteProduct)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAllFavourites(products: List<FavouriteProduct>)

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAllRecent(products: List<RecentServing>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Delete
    suspend fun deleteFavouriteProduct(favouriteProduct: FavouriteProduct)

    @Delete
    suspend fun deleteRecentProduct(productsHistory: RecentServing)

    @Query("SELECT * FROM products WHERE (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%')")
    suspend fun getProducts(searchQuery: String? = null): MutableList<Product>

    @Query("SELECT * FROM products WHERE server_id = :productID")
    suspend fun getServerProduct(productID: Int): Product?

    @Query("SELECT * FROM products WHERE local_id = :productID")
    suspend fun getLocalProduct(productID: String): Product?

    @Query("SELECT * FROM products WHERE server_id = -1")
    suspend fun getProductsForSync(): List<Product>

    @Query("SELECT * FROM favourite_products")
    suspend fun getFavouriteProducts(): List<FavouriteProduct>

    @Query("SELECT * FROM favourite_products WHERE product_server_id = :productID OR product_local_id = :localProductID")
    suspend fun getFavouriteProduct(productID:Int, localProductID: String?): FavouriteProduct
}