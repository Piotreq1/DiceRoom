package com.example.diceroom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.diceroom.authentication.AuthManager
import com.example.diceroom.authentication.ChangePasswordActivity
import com.example.diceroom.authentication.ProfileConfigActivity
import com.example.diceroom.authentication.SelectLoginActivity
import com.example.diceroom.databinding.FragmentProfileBinding
import com.example.diceroom.models.UserManager


class ProfileFragment : Fragment() {
    private lateinit var bind: FragmentProfileBinding
    private val utils = Utils()
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
                        val intent = Intent(requireContext(), ProfileConfigActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.tutorialItem -> {
                        val intent = Intent(requireContext(), WelcomeActivity::class.java)
                        intent.putExtra("isFirst", false)
                        startActivity(intent)
                        true
                    }

                    R.id.notificationItem -> {
                        // TODO: Notifications settings - new window / popup screen
                        true
                    }

                    R.id.changePasswordItem -> {
                        val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.logoutItem -> {
                        authManager.logout()
                        val intent = Intent(requireContext(), SelectLoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }
        }

        return bind.root
    }
}