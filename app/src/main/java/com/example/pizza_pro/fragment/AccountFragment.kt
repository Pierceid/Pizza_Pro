package com.example.pizza_pro.fragment

import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.pizza_pro.R
import com.example.pizza_pro.database.Order
import com.example.pizza_pro.database.OrderViewModel
import com.example.pizza_pro.databinding.FragmentAccountBinding
import com.example.pizza_pro.options.Gender
import com.example.pizza_pro.utils.Util
import kotlinx.coroutines.runBlocking

@Suppress("DEPRECATION")
class AccountFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentAccountBinding
    private lateinit var navController: NavController
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var location: String
    private lateinit var gender: Gender
    private var isPasswordVisible = false
    private var isRegistering: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        navController = Navigation.findNavController(view)
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

        val buttons = listOf(
            binding.btnSwap,
            binding.btnEye,
            binding.rbMale,
            binding.rbFemale,
            binding.rbOther,
            binding.btnCancel,
            binding.btnNext
        )
        for (button in buttons) button.setOnClickListener(this)

        val inputs = listOf(
            binding.inputName, binding.inputEmail, binding.inputPassword, binding.inputLocation
        )
        for (input in inputs) input.setOnFocusChangeListener { _, _ -> checkInput() }
    }

    // handles on click methods
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_swap -> {
                val inputs = listOf(
                    binding.inputName, binding.inputEmail, binding.inputPassword, binding.inputLocation
                )
                for (input in inputs) input.clearFocus()

                isRegistering = !isRegistering
                updateAccount(newIsRegistering = isRegistering, swapSelection = true)
            }
            R.id.btn_eye -> {
                isPasswordVisible = !isPasswordVisible
                updateAccount(newIsPasswordVisible = isPasswordVisible)
            }
            R.id.rb_male, R.id.rb_female, R.id.rb_other -> updateAccount()
            R.id.btn_next -> {
                if (checkInput()) {
                    val bundle = bundleOf(
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "location" to location,
                        "gender" to gender
                    )
                    navController.navigate(R.id.action_accountFragment_to_shopFragment, bundle)
                }
            }
            R.id.btn_cancel -> navController.navigate(R.id.action_accountFragment_to_introFragment)
        }
    }

    // saves data in case of rotating screen or exiting app
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getInput()
        outState.putString("name", name)
        outState.putString("email", email)
        outState.putString("password", password)
        outState.putString("location", location)
        outState.putSerializable("gender", gender)
        outState.putBoolean("isPasswordVisible", isPasswordVisible)
        outState.putBoolean("isRegistering", isRegistering)
    }

    // restores data from the saved state
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            binding.inputName.setText(savedInstanceState.getString("name").toString())
            binding.inputEmail.setText(savedInstanceState.getString("email").toString())
            binding.inputPassword.setText(savedInstanceState.getString("password").toString())
            binding.inputLocation.setText(savedInstanceState.getString("location").toString())
            updateAccount(
                savedInstanceState.getBoolean("isPasswordVisible"),
                savedInstanceState.getSerializable("gender") as Gender,
                savedInstanceState.getBoolean("isRegistering")
            )
        }
    }

    // updates account fragment
    private fun updateAccount(
        newIsPasswordVisible: Boolean = Util.getVisibilityOfPassword(binding.btnEye, requireContext()),
        newGender: Gender = Util.getGenderFromRadioGroup(binding.rgGenderOptions),
        newIsRegistering: Boolean = Util.getIsRegistering(binding.tvSelected, requireActivity()),
        swapSelection: Boolean = false
    ) {
        isPasswordVisible = newIsPasswordVisible
        Util.changeVisibilityOfPassword(isPasswordVisible, binding.inputPassword, binding.btnEye)
        gender = newGender
        Util.checkGenderRadioButton(gender, binding.rgGenderOptions)
        isRegistering = newIsRegistering

        if (swapSelection) {
            Util.changeVisibilityOfAccountFields(
                isRegistering, binding.inputNameLayout, binding.rgGenderOptions
            )
            Util.swapTextViews(binding.tvSelected, binding.tvUnselected)
        }
    }

    // validates user's input
    private fun checkInput(): Boolean {
        getInput()
        if (email.isNotEmpty()) {
            runBlocking { orderViewModel.getUserOrder(email) }
        }
        val order = orderViewModel.userOrder

        return if (isRegistering) validateRegistration(order) else validateLogin(order)
    }

    // validates input when registering
    private fun validateRegistration(order: Order?): Boolean {
        val validEmail = (order == null && email.isNotEmpty())
        val validName = name.isNotEmpty()
        val validPassword = password.length > 5
        val validLocation = location.isNotEmpty()

        binding.inputEmail.error = if (!validEmail) getString(R.string.invalid_email) else null
        binding.inputName.error = if (!validName) getString(R.string.invalid_username) else null
        binding.inputPassword.error = if (!validPassword) getString(R.string.short_password) else null
        binding.inputLocation.error = if (!validLocation) getString(R.string.invalid_location) else null

        return validName && validEmail && validPassword && validLocation
    }

    // validates input when logging in
    private fun validateLogin(order: Order?): Boolean {
        val validEmail = (order != null && email == order.userInfo.email)
        val validPassword = (order != null && password == order.userInfo.password)
        val validLocation = location.isNotEmpty()

        binding.inputEmail.error = if (!validEmail) getString(R.string.invalid_email) else null
        binding.inputPassword.error = if (!validPassword) getString(R.string.invalid_password) else null
        binding.inputLocation.error = if (!validLocation) getString(R.string.invalid_location) else null

        if (order != null) {
            name = order.userInfo.name
            gender = order.userInfo.gender
        }

        return validEmail && validPassword && validLocation
    }

    // assigns values to attributes
    private fun getInput() {
        name = binding.inputName.text.toString()
        email = binding.inputEmail.text.toString()
        password = binding.inputPassword.text.toString()
        location = binding.inputLocation.text.toString()
        gender = Util.getGenderFromRadioGroup(binding.rgGenderOptions)
        isPasswordVisible = Util.getVisibilityOfPassword(binding.btnEye, requireContext())
        isRegistering = Util.getIsRegistering(binding.tvSelected, requireActivity())
    }
}