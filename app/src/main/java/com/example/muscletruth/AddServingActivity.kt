package com.example.muscletruth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.data.api.models.Product
import com.example.muscletruth.data.api.models.Serving
import com.example.muscletruth.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddServingActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private var products = mutableListOf<Product.ProductBase>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var searchView: SearchView
    private lateinit var addProductLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_serving)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        productsList = findViewById<RecyclerView>(R.id.products_rv)
        setupList()
        loadData()

        searchView = findViewById<SearchView>(R.id.products_sv)
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter?.filter(newText)
                return true
            }
        })

        addProductLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val addedProductTitle = result.data?.getStringExtra("productTitle")
                if(addedProductTitle != null){
                    loadData()
                    lifecycleScope.launch {
                        delay(500)
                        searchView.setQuery(addedProductTitle, true)
                    }
                }
            }
        }

        val addButton = findViewById<Button>(R.id.add_serving_btn_add_product)
        addButton.setOnClickListener{
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData(){
        lifecycleScope.launch {
            try{
                products = withContext(Dispatchers.IO){
                    userRepository.getProducts()
                }
                adapter.submitList(products)
                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter({ product ->
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_amount, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Введите количество продукта:")
                .setView(dialogView)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отменить", null)
                .create()

            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val amount = dialogView.findViewById<EditText>(R.id.dialog_product_amount_et_amount)
                amount.addTextChangedListener{
                    if(amount.length() > 4){
                        amount.setText(amount.text.dropLast(1))
                        amount.setSelection(amount.text.length)
                    }
                    else if(amount.length() == 0){
                        amount.error = "Введите кол-во продукта!"
                    }
                }
                button.setOnClickListener {
                    //9999 max
                    if(amount.length() > 0 && amount.length() <= 4){
                        val intAmount = amount.text.toString().toInt()
                        if(intAmount <= 0){
                            amount.error = "Кол-во продукта должно быть больше 0!"
                        }
                        else if(intAmount > 9999){
                            amount.error = "Кол-во продукта должно быть меньше 9999!"
                        }
                        else{
                            val resultIntent = Intent()
                            val serving = Serving.ServingItem(productID =product.id!!, productAmount = intAmount)
                            resultIntent.putExtra("serving", serving)
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                    else{
                        amount.error = "Введите корректное кол-во продукта!"
                    }
                }
            }

            dialog.show()
        }, this)
        adapter.items = mutableListOf()
        productsList.adapter = adapter
    }
}