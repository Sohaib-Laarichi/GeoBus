package com.geobus.marrakech.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment pour la connexion des utilisateurs
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser le ViewModel
        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        // Configurer les boutons
        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Observer les données
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observateur pour les données du ViewModel
     */
    private fun observeViewModel() {
        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        // Observer les erreurs
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observer l'utilisateur connecté
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Naviguer vers la carte
                findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
            }
        }
    }

    /**
     * Connexion de l'utilisateur
     */
    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validation des champs
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            return
        } else {
            binding.tilPassword.error = null
        }

        // Connexion
        viewModel.login(email, password)
    }
}
