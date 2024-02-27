package com.example.pizza_pro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pizza_pro.R
import com.example.pizza_pro.adapter.HistoryAdapter
import com.example.pizza_pro.database.MyViewModel
import com.example.pizza_pro.databinding.FragmentHistoryBinding
import com.example.pizza_pro.item.MyContext
import com.example.pizza_pro.utils.Util
import kotlinx.coroutines.runBlocking
import java.util.*

@Suppress("DEPRECATION")
class HistoryFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var myViewModel: MyViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewModel = ViewModelProvider(this)[MyViewModel::class.java]
        val myContext =
            MyContext(myViewModel, requireActivity(), layoutInflater, binding.clHistory, "users")
        historyAdapter = HistoryAdapter(myContext)
        binding.rvOrders.adapter = historyAdapter

        listOf(
            binding.btnClose,
            binding.btnClear,
            binding.btnSwap,
            binding.ivSearch,
            binding.ivCross
        ).forEach { it.setOnClickListener(this) }

        binding.etSearchBar.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) updateHistory() }

        updateHistory()
    }

    // saves data in case of rotating screen or exiting app
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("regex", binding.etSearchBar.text.toString())
    }

    // restores data from the saved state
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            binding.etSearchBar.setText(savedInstanceState.getString("regex").toString())
            updateHistory()
        }
    }

    // handles on click methods
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> requireFragmentManager().popBackStack()
            R.id.btn_clear -> createHistoryAlertDialog()
            R.id.btn_swap -> {
                Util.swapTextViews(binding.tvSelected, binding.tvUnselected)
                updateHistory()
            }
            R.id.iv_search -> updateHistory()
            R.id.iv_cross -> {
                binding.etSearchBar.setText("")
                updateHistory()
            }
        }
    }

    // creates an alert dialog for placing an order
    private fun createHistoryAlertDialog() {
        var runnable = { }
        val type = binding.tvSelected.text.toString().toLowerCase(Locale.ROOT)

        if (type == "users") {
            runnable = { runBlocking { myViewModel.clearAllUsers() } }
        } else if (type == "orders") {
            runnable = { runBlocking { myViewModel.clearAllOrders() } }
        }

        Util.createAlertDialog(
            requireActivity(),
            "clear_history",
            runnable,
            layoutInflater,
            binding.clHistory
        )
    }

    // updates history fragment
    private fun updateHistory() {
        val type = binding.tvSelected.text.toString().toLowerCase(Locale.ROOT)
        val regex = binding.etSearchBar.text.toString()

        val myContext =
            MyContext(myViewModel, requireActivity(), layoutInflater, binding.clHistory, type)
        historyAdapter = HistoryAdapter(myContext)
        binding.rvOrders.adapter = historyAdapter

        runBlocking {
            if (type == "users") {
                myViewModel.getFilteredUsers(regex.trim())
                myViewModel.users.observe(viewLifecycleOwner) { newUsers ->
                    val filteredUsers: MutableList<Any> = newUsers.toMutableList()
                    historyAdapter.initItems(filteredUsers)
                }
            } else if (type == "orders") {
                myViewModel.getFilteredOrders(regex.trim())
                myViewModel.orders.observe(viewLifecycleOwner) { newOrders ->
                    val filteredOrders: MutableList<Any> = newOrders.toMutableList()
                    historyAdapter.initItems(filteredOrders)
                }
            }
        }

        binding.etSearchBar.clearFocus()
    }
}