package com.example.pizza_pro.fragment

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pizza_pro.R
import com.example.pizza_pro.adapter.OrderAdapter
import com.example.pizza_pro.database.OrderViewModel
import com.example.pizza_pro.databinding.FragmentHistoryBinding
import com.example.pizza_pro.utils.Util

@Suppress("DEPRECATION")
class HistoryFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrderAdapter()
        binding.rvOrders.adapter = adapter
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        orderViewModel.allOrders.observe(viewLifecycleOwner) { newOrders ->
            adapter.initOrders(newOrders)
        }

        binding.btnClose.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    // handles on click methods
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_close -> requireFragmentManager().popBackStack()
            R.id.btn_clear -> {
                onAttach(requireContext())
                createClearPopUpWindow()
                onDetach()
            }
        }
    }

    // creates pop up window for "clear" action
    private fun createClearPopUpWindow() {
        Util.createPopUpWindow(
            getString(R.string.database_has_been_cleared), layoutInflater, binding.clHistory
        )
        val runnable = { orderViewModel.clearAllOrders() }
        Util.getHandler(runnable)
    }
}