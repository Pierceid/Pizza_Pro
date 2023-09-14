package com.example.pizza_pro.fragment

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.pizza_pro.R
import com.example.pizza_pro.databinding.FragmentFeedbackBinding
import com.example.pizza_pro.options.Gender
import com.example.pizza_pro.options.Satisfaction
import com.example.pizza_pro.utils.Util

@Suppress("DEPRECATION")
class FeedbackFragment : Fragment(), OnClickListener {

    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var navController: NavController
    private lateinit var satisfaction: Satisfaction
    private lateinit var thoughts: String
    private var followUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedbackBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        navController = Navigation.findNavController(view)

        binding.btnSend.setOnClickListener(this)
        binding.btnDiscard.setOnClickListener(this)
        binding.btnCart.setOnClickListener(this)
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
                Util.navigateToFragment(requireFragmentManager(), HistoryFragment())
                true
            }
            R.id.mi_aboutApp -> {
                Util.navigateToFragment(requireFragmentManager(), AboutAppFragment())
                true
            }
            R.id.mi_logOut -> {
                Util.removeAdditionalFragment(requireFragmentManager())
                navController.navigate(R.id.action_feedbackFragment_to_introFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // saves data in case of rotating screen or exiting app
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding = FragmentFeedbackBinding.inflate(layoutInflater)
        getInput()
        outState.putSerializable("satisfaction", satisfaction)
        outState.putString("thoughts", thoughts)
        outState.putBoolean("followUp", followUp)
    }

    // restores data from the saved state
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            Util.checkSatisfactionRadioButton(
                savedInstanceState.getSerializable("satisfaction") as Satisfaction,
                binding.rgSatisfactionOptions
            )
            binding.etThoughts.setText(savedInstanceState.getString("thoughts").toString())
            binding.scFollowUp.isChecked = savedInstanceState.getBoolean("followUp")
        }
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
            R.id.btn_send -> {
                onAttach(requireContext())
                createSendPopUpWindow()
                onDetach()
            }
            R.id.btn_discard -> clearInput()
            R.id.btn_cart -> requireActivity().onBackPressed()
        }
    }

    // creates pop up window for "send" action
    private fun createSendPopUpWindow() {
        Util.createPopUpWindow(
            getString(R.string.sent_successfully), layoutInflater, binding.clFeedback
        )
        val runnable = { clearInput() }
        Util.getHandler(runnable)
    }

    // updates feedback fragment
    private fun updateFeedback() {
        Util.checkSatisfactionRadioButton(satisfaction, binding.rgSatisfactionOptions)
        binding.etThoughts.setText(thoughts)
        binding.scFollowUp.isChecked = followUp
    }

    // assigns values to attributes
    private fun getInput() {
        satisfaction = Util.getSatisfactionFromRadioGroup(binding.rgSatisfactionOptions)
        thoughts = binding.etThoughts.text.toString()
        followUp = binding.scFollowUp.isChecked
    }

    // resets values to attributes
    private fun clearInput() {
        satisfaction = Satisfaction.GREAT
        thoughts = ""
        followUp = false
        updateFeedback()
    }
}