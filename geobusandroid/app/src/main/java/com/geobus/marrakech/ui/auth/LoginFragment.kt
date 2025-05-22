package com.geobus.marrakech.ui.auth

import android.os.Bundle
import com.geobus.marrakech.model.AuthResponse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.geobus.marrakech.R
import com.geobus.marrakech.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment pour la connexion des utilisateurs - AMÉLIORÉ
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
     * Observateur pour les données du ViewModel - AMÉLIORÉ
     */
    private fun observeViewModel() {
        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading
        }

        // Observer les erreurs avec traitement spécifique
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let { message ->
                showErrorMessage(message)
                viewModel.clearError()
            }
        }

        // Observer le type d'erreur pour un traitement spécifique
        viewModel.errorType.observe(viewLifecycleOwner) { errorType ->
            errorType?.let { type ->
                handleSpecificError(type)
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
     * Affiche un message d'erreur avec style approprié
     */
    private fun showErrorMessage(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

        // Personnaliser l'apparence selon le type d'erreur
        when {
            message.contains("incorrect") || message.contains("invalide") -> {
                // Erreur d'authentification - rouge
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            message.contains("connexion") || message.contains("réseau") -> {
                // Erreur réseau - orange/warning
                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.warning
                    )
                )
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            message.contains("serveur") -> {
                // Erreur serveur - info
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.info))
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            else -> {
                // Erreur générale - couleur par défaut
                snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        snackbar.show()
    }

    /**
     * Gère les erreurs spécifiques selon leur type
     */
    private fun handleSpecificError(errorType: String) {
        when (errorType) {
            AuthResponse.ERROR_INVALID_CREDENTIALS,
            AuthResponse.ERROR_WRONG_PASSWORD -> {
                // Mettre en évidence les champs d'identifiants
                binding.tilEmail.error = " " // Espace pour déclencher l'état d'erreur
                binding.tilPassword.error = " "

                // Effacer les erreurs après 3 secondes
                binding.root.postDelayed({
                    binding.tilEmail.error = null
                    binding.tilPassword.error = null
                }, 3000)
            }

            AuthResponse.ERROR_USER_NOT_FOUND -> {
                // Mettre en évidence le champ username
                binding.tilEmail.error = "Utilisateur non trouvé"

                binding.root.postDelayed({
                    binding.tilEmail.error = null
                }, 3000)
            }

            AuthResponse.ERROR_NETWORK -> {
                // Afficher un message de reconnexion avec couleur warning
                val snackbar = Snackbar.make(
                    binding.root,
                    "Vérifiez votre connexion internet",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Réessayer") {
                        if (viewModel.getFailedAttempts() > 0) {
                            login() // Réessayer automatiquement
                        }
                    }

                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.warning
                    )
                )
                snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.primary
                    )
                )
                snackbar.show()
            }
        }
    }

    /**
     * Connexion de l'utilisateur avec validation améliorée
     */
    private fun login() {
        val username = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Réinitialiser les erreurs précédentes
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validation des champs
        var isValid = true

        if (username.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            isValid = false
        } else if (username.length < 3) {
            binding.tilEmail.error = "Le nom d'utilisateur doit contenir au moins 3 caractères"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
            isValid = false
        }

        if (!isValid) {
            return
        }

        // Vérifier si trop de tentatives échouées
        if (viewModel.getFailedAttempts() >= 3) {
            val snackbar = Snackbar.make(
                binding.root,
                "Trop de tentatives échouées. Attendez quelques minutes ou vérifiez vos identifiants.",
                Snackbar.LENGTH_LONG
            )
                .setAction("Réinitialiser") {
                    viewModel.resetFailedAttempts()
                }

            // Style pour l'avertissement de sécurité
            snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error))
            snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            snackbar.show()
            return
        }

        // Lancer la connexion
        viewModel.login(username, password)
    }
}