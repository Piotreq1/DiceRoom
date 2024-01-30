package com.example.diceroom.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.google.android.material.button.MaterialButton

class SelectLoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_login, container, false)

        val loginButton: MaterialButton = view.findViewById(R.id.goToLoginButton)
        val signUpButton: MaterialButton = view.findViewById(R.id.goToSignUpButton)

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectLogin_to_loginFragment)
        }

        signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_selectLogin_to_registerFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }
        return view
    }
}
