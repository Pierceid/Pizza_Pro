package com.example.pizza_pro.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.pizza_pro.R
import com.example.pizza_pro.adapter.MyAdapter
import com.example.pizza_pro.data.DataSource
import com.example.pizza_pro.databinding.FragmentShopBinding
import com.example.pizza_pro.item.MyContext
import com.example.pizza_pro.item.Pizza
import com.example.pizza_pro.options.Gender
import com.example.pizza_pro.utils.MyMenuProvider
import kotlinx.coroutines.runBlocking

@Suppress("DEPRECATION")
class ShopFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentShopBinding
    private lateinit var navController: NavController
    private lateinit var pizzas: MutableList<Pizza>
    private lateinit var adapter: MyAdapter<Pizza>

    private var menuProvider: MenuProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val isLocked: Boolean
        val changedPizzas: MutableList<Pizza>

        requireArguments().let {
            changedPizzas = it.getParcelableArrayList<Pizza>("orderedItems") as MutableList<Pizza>
            isLocked = it.getBoolean("isLocked")
        }

        pizzas = DataSource().loadData()
        updatePizzas(pizzas, changedPizzas)

        if (isLocked) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        navController = Navigation.findNavController(view)
        menuProvider =
            MyMenuProvider(requireActivity(), this, requireFragmentManager(), navController)
        requireActivity().addMenuProvider(menuProvider!!)

        listOf(
            binding.btnCart,
            binding.ivSearch,
            binding.ivCross,
            binding.ivBanner,
            binding.topAppBar
        ).forEach { it.setOnClickListener(this) }
        binding.etSearchBar.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) updateShop() }

        updateShop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider!!)
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
            pizzas = savedInstanceState.getParcelableArrayList<Pizza>("list") as MutableList<Pizza>
            updateShop()
        }
    }

    // handles on click methods
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_cart -> {
                binding.etSearchBar.setText("")
                updateShop()
                val bundle = bundleOf(
                    "name" to requireArguments().getString("name").toString(),
                    "email" to requireArguments().getString("email").toString(),
                    "password" to requireArguments().getString("password").toString(),
                    "location" to requireArguments().getString("location").toString(),
                    "gender" to requireArguments().getSerializable("gender") as Gender,
                    "orderedItems" to adapter.getSelectedPizzas() as ArrayList<out Parcelable>,
                    "isLocked" to (requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                )
                navController.navigate(R.id.action_shopFragment_to_cartFragment, bundle)
            }
            R.id.iv_search, R.id.iv_banner, R.id.topAppBar -> updateShop()
            R.id.iv_cross -> {
                binding.etSearchBar.setText("")
                updateShop()
            }
        }
    }

    // updates shop fragment
    private fun updateShop() {
        val regex = binding.etSearchBar.text.toString()
        val myContext = MyContext(
            "pizzas", requireActivity(), layoutInflater, fragmentManager = requireFragmentManager()
        )
        adapter = MyAdapter(myContext)

        runBlocking {
            if (regex.trim().isNotEmpty()) {
                val filteredPizzas = adapter.getFilteredPizzas(regex.trim())
                updatePizzas(pizzas, filteredPizzas)
                adapter.initItems(filteredPizzas)
            } else {
                adapter.initItems(pizzas)
            }
        }

        binding.rvPizzas.adapter = adapter
        binding.etSearchBar.clearFocus()
    }


    // updates main list of pizzas based on other list of pizzas
    private fun updatePizzas(mainList: MutableList<Pizza>, otherList: MutableList<Pizza>) {
        if (otherList.isNotEmpty()) {
            otherList.forEach { otherPizza ->
                mainList.find { it.name == otherPizza.name }?.count = otherPizza.count
            }
        }
    }
}