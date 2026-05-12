package com.example.muscletruth.ui.Servings

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muscletruth.R
import com.example.muscletruth.data.models.Product
import com.example.muscletruth.data.models.Serving
import com.example.muscletruth.data.repository.ProductRepository
import com.example.muscletruth.ui.Products.AddProductActivity
import com.example.muscletruth.ui.Products.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentProductsFragment : Fragment() {
    private var products = mutableListOf<Product>()
    private lateinit var productsList: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var searchInput: EditText
    private lateinit var addProductLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productsList = view.findViewById<RecyclerView>(R.id.fgt_recent_products_products_rv)
        setupList()
        loadData()

        searchInput = view.findViewById<EditText>(R.id.search_input)
        val clearButton = view.findViewById<ImageView>(R.id.clear_button)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter?.filter(s.toString())

                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearButton.setOnClickListener {
            searchInput.text.clear()
            adapter.filter?.filter("")
        }

        addProductLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val addedProductTitle = result.data?.getStringExtra("productTitle")
                if(addedProductTitle != null){
                    loadData()
                    lifecycleScope.launch {
                        delay(500)
                        searchInput.setText(addedProductTitle)
                    }
                }
            }
        }

        val addButton = view.findViewById<Button>(R.id.fgt_recent_products_btn_add)
        addButton.setOnClickListener{
            val intent = Intent(requireContext(), AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }

        val backButton = view.findViewById<Button>(R.id.fgt_recent_products_btn_back)
        backButton.setOnClickListener {
            requireActivity().setResult(RESULT_CANCELED)
            requireActivity().finish()
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
                    ProductRepository.getRecentProducts().map {it-> ProductRepository.getProduct(it.productServerID, it.productLocalID)!!}.toMutableList()
                }
                Log.d("APP_DEBUG!", "RECENT $products")
                adapter.submitList(products)
                adapter.notifyDataSetChanged()
            }
            catch(e: Exception){

            }
        }
    }

    private fun setupList() {
        productsList.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter({ product ->
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_product_amount, null)
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 1).toInt()
            val height = (displayMetrics.heightPixels * 0.55).toInt()

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Введите количество продукта:")
                .setView(dialogView)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отменить", null)
                .create()


            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                val productTitleField = dialog.findViewById<TextView>(R.id.dg_product_amount_tv_product_title)
                productTitleField?.text = product.title

                val amount = dialogView.findViewById<EditText>(R.id.dialog_product_amount_et_amount)
                val proteinsField = dialogView.findViewById<TextView>(R.id.dg_product_amount_tv_proteins_val)
                val carbsField = dialogView.findViewById<TextView>(R.id.dg_product_amount_tv_carbs_val)
                val fatsField = dialogView.findViewById<TextView>(R.id.dg_product_amount_tv_fats_val)
                val caloriesField = dialogView.findViewById<TextView>(R.id.dg_product_amount_tv_calories_val)

                amount.addTextChangedListener {
                    if (amount.length() > 4) {
                        amount.setText(amount.text.dropLast(1))
                        amount.setSelection(amount.text.length)
                    } else if (amount.length() == 0) {
                        amount.error = "Введите кол-во продукта!"
                    }
                    else{
                        val productAmount = amount.text.toString().toDouble()
                        val proteinsAmount = product.proteins * (productAmount / 100)
                        val carbsAmount = product.carbs * (productAmount / 100)
                        val fatsAmount = product.fats * (productAmount / 100)
                        val caloriesAmount = proteinsAmount * 4 + carbsAmount * 4 + fatsAmount * 9

                        proteinsField.text = "%.2f".format(proteinsAmount)
                        carbsField.text = "%.2f".format(carbsAmount)
                        fatsField.text = "%.2f".format(fatsAmount)
                        caloriesField.text = "%.2f".format(caloriesAmount)
                    }
                }
                button.setOnClickListener {
                    //9999 max
                    if (amount.length() > 0 && amount.length() <= 4) {
                        val intAmount = amount.text.toString().toInt()
                        if (intAmount <= 0) {
                            amount.error = "Кол-во продукта должно быть больше 0!"
                        } else if (intAmount > 9999) {
                            amount.error = "Кол-во продукта должно быть меньше 9999!"
                        } else {
                            val resultIntent = Intent()
                            val serving = Serving(
                                productID = product.serverID,
                                localProductID = product.localID,
                                productAmount = intAmount
                            )
                            resultIntent.putExtra("serving", serving)
                            requireActivity().setResult(RESULT_OK, resultIntent)
                            requireActivity().finish()
                        }
                    } else {
                        amount.error = "Введите корректное кол-во продукта!"
                    }
                }
            }

            dialog.show()
            dialog.window?.setLayout(width, height)
        }, requireContext())
        adapter.items = mutableListOf()
        productsList.adapter = adapter
    }
}