package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var products = emptyList<Product.ProductBase>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_products)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        productsList = findViewById<RecyclerView>(R.id.products_rv)
        setupList()
        loadData()
    }

    private fun loadData(){
        lifecycleScope.launch {
            try{
                products = withContext(Dispatchers.IO){
                    userRepository.getProducts()
                }
                adapter.items = products
                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter { product ->
            val resultIntent = Intent()
            val serving = Serving.ServingItem(productID =product.id!!, productAmount = 250)
            resultIntent.putExtra("serving", serving)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        adapter.items = emptyList()
        productsList.adapter = adapter
    }
}