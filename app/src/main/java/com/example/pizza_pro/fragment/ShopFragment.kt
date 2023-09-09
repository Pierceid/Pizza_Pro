package com.example.pizza_pro.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.pizza_pro.R
import com.example.pizza_pro.adapter.PizzaAdapter
import com.example.pizza_pro.data.DataSource
import com.example.pizza_pro.databinding.FragmentShopBinding
import com.example.pizza_pro.item.Pizza
import com.example.pizza_pro.options.Gender
import com.example.pizza_pro.utils.Util

@Suppress("DEPRECATION")
class ShopFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentShopBinding
    private lateinit var navController: NavController
    private lateinit var pizzas: MutableList<Pizza>
    private lateinit var adapter: PizzaAdapter
    private lateinit var data: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PizzaAdapter(childFragmentManager, DataSource().loadData())
        pizzas = adapter.getPizzas()
        val changedPizzas =
            (requireArguments().getParcelableArrayList<Pizza>("orderedItems") as? MutableList<Pizza>)
                ?: mutableListOf()
        Util.updatePizzas(pizzas, changedPizzas)
        data = (requireArguments().getString("data")) ?: ""
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.btnHome.setOnClickListener(this)
        binding.btnCart.setOnClickListener(this)
        binding.etSearchBar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateShop()
        }
        updateShop()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_settings, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_profile -> {
                val bundle = bundleOf(
                    "name" to requireArguments().getString("name").toString(),
                    "email" to requireArguments().getString("email").toString(),
                    "password" to requireArguments().getString("password").toString(),
                    "location" to requireArguments().getString("location").toString(),
                    "gender" to requireArguments().getSerializable("gender") as Gender
                )
                Util.navigateToFragment(requireFragmentManager(), ProfileFragment(), bundle)
                true
            }
            R.id.mi_history -> {
                Util.navigateToFragment(requireFragmentManager(), HistoryFragment(data))
                true
            }
            R.id.mi_aboutApp -> {
                Util.navigateToFragment(requireFragmentManager(), AboutAppFragment())
                true
            }
            R.id.mi_logOut -> {
                navController.navigate(R.id.action_shopFragment_to_introFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // saves data in case of rotating screen or exiting app
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("list", pizzas as ArrayList<out Parcelable>)
    }

    // restores data from the saved state
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            val savedPizzas =
                savedInstanceState.getParcelableArrayList<Pizza>("list") as MutableList<Pizza>
            adapter.initPizzas(savedPizzas)
            updateShop()
        }
    }

    // handles on click methods
    override fun onClick(v: View?) {
        val bundle = bundleOf(
            "name" to requireArguments().getString("name").toString(),
            "email" to requireArguments().getString("email").toString(),
            "password" to requireArguments().getString("password").toString(),
            "location" to requireArguments().getString("location").toString(),
            "gender" to requireArguments().getSerializable("gender") as Gender,
            "selectedItems" to adapter.getSelectedPizzas() as ArrayList<out Parcelable>,
            "data" to data
        )
        when (v!!.id) {
            R.id.btn_home -> navController.navigate(R.id.action_shopFragment_to_introFragment)
            R.id.btn_cart -> navController.navigate(
                R.id.action_shopFragment_to_cartFragment, bundle
            )
        }
    }

    // updates shop fragment
    private fun updateShop() {
        adapter = PizzaAdapter(childFragmentManager, pizzas)

        val regex = binding.etSearchBar.text.toString()
        if (regex.isNotEmpty()) {
            val filteredPizzas = adapter.getFilteredPizzas(regex)
            Util.updatePizzas(pizzas, filteredPizzas)
            adapter = PizzaAdapter(childFragmentManager, filteredPizzas)
        }

        binding.rvPizzas.adapter = adapter
    }
}