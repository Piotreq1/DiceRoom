package com.example.diceroom.profile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentProfileBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Utils


class ProfileFragment : Fragment() {
    private lateinit var bind: FragmentProfileBinding
    private val utils = Utils()

    interface OnNavigateToGameListListener {
        fun navigateToGameList()
    }

    private var navigateToGameListListener: OnNavigateToGameListListener? = null

    fun setOnNavigateToGameListListener(listener: OnNavigateToGameListListener) {
        navigateToGameListListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = FragmentProfileBinding.inflate(inflater, container, false)

        val authManager = AuthManager()
        val userManager = UserManager()
        val currentUserId = authManager.getCurrentUser()?.uid

        if (currentUserId != null) {
            userManager.getUserById(currentUserId) { user ->
                if (user != null) {
                    bind.nicknameTextView.text = user.nickname
                    this.context?.let {
                        utils.downloadImageFromFirebaseStorage(it, user.avatar) { isSuccess, file ->
                            if (isSuccess) {
                                bind.profileImageView.setImageURI(Uri.fromFile(file))
                            }
                        }
                    }
                }
            }
        }
        val menu: Menu = bind.navView.menu

        for (i in 0 until menu.size()) {
            val menuItem: MenuItem = menu.getItem(i)
            val customView: View = inflater.inflate(R.layout.arrow_icon_layout, null, false)
            val icon: ImageView = customView.findViewById(R.id.icon)
            icon.setImageResource(R.drawable.right_arrow_64)
            val iconSizeInDp = 23
            val iconSizeInPx = (iconSizeInDp * resources.displayMetrics.density).toInt()
            icon.layoutParams.width = iconSizeInPx
            icon.layoutParams.height = iconSizeInPx
            icon.requestLayout()

            menuItem.actionView = customView

            menuItem.setOnMenuItemClickListener {
                when (menuItem.itemId) {
                    R.id.meetingsItem -> {
                        // TODO: Meetings implementation
                        this.context?.let { it1 -> utils.showToast(it1, "Not implemented yet!") }
                        true
                    }

                    R.id.editProfileItem -> {
                       findNavController().navigate(R.id.action_mainMenuFragment_to_profileConfigFragment)
                        true
                    }

                    R.id.tutorialItem -> {
                        // TODO PASS SOMETHING TO NOT SHOW A SKIP
                        findNavController().navigate(R.id.action_mainMenuFragment_to_tutorialFragment)
                        true
                    }

                    R.id.notificationItem -> {
                        // TODO: Notifications settings - new window / popup screen
                        true
                    }

                    R.id.changePasswordItem -> {
                        findNavController().navigate(R.id.action_mainMenuFragment_to_changePasswordFragment)
                        true
                    }
                    R.id.favouritesItem -> {
                        if (currentUserId != null) {
                            val sharedPreferences = requireContext().getSharedPreferences("gameListPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isFavourites", true)
                            editor.apply()

                            // TODO: NAVIGATE TO GAME FRAGMENT
                        }
                        true
                    }

                    R.id.logoutItem -> {
                        authManager.logout()
                        // TODO HANDLE BACKPRESSING
                        findNavController().navigate(R.id.action_mainMenuFragment_to_loginFragment)
                        true
                    }

                    else -> false
                }
            }
        }

        return bind.root
    }
}