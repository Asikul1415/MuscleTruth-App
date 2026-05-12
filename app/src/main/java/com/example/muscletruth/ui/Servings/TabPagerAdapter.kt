package com.example.muscletruth.ui.Servings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = listOf(ProductsListFragment(), FavouriteProductsFragment(), RecentProductsFragment())
    private val titles = listOf("Общий список", "Избранные", "Недавно использованные")

    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
    fun getTabTitle(position: Int): String = titles[position]
}