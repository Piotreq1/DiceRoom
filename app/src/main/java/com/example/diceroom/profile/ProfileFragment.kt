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
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.diceroom.R
import com.example.diceroom.databinding.FragmentProfileBinding
import com.example.diceroom.managers.AuthManager
import com.example.diceroom.managers.UserManager
import com.example.diceroom.utils.Constants.Companion.CURRENT_ITEM_KEY
import com.example.diceroom.utils.Constants.Companion.FAVOURITES_KEY
import com.example.diceroom.utils.Constants.Companion.GAMES_PREFS
import com.example.diceroom.utils.Constants.Companion.MEET_PREFS
import com.example.diceroom.utils.Constants.Companion.USER_MEETINGS_KEY
import com.example.diceroom.utils.Utils


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
                        val sharedPreferences = requireContext().getSharedPreferences(
                            MEET_PREFS, Context.MODE_PRIVATE
                        )
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(USER_MEETINGS_KEY, true)
                        editor.apply()
                        val args = Bundle()
                        args.putInt(CURRENT_ITEM_KEY, 1)

                        findNavController().navigate(R.id.mainMenuFragment, args)
                        true
                    }

                    R.id.editProfileItem -> {
                        findNavController().navigate(R.id.action_mainMenuFragment_to_profileConfigFragment)
                        true
                    }

                    R.id.tutorialItem -> {
                        findNavController().navigate(R.id.action_mainMenuFragment_to_tutorialFragment)
                        true
                    }

                    R.id.notificationItem -> {
                        showNotificationSettingsPopup()
                        true
                    }

                    R.id.changePasswordItem -> {
                        findNavController().navigate(R.id.action_mainMenuFragment_to_changePasswordFragment)
                        true
                    }

                    R.id.favouritesItem -> {
                        if (currentUserId != null) {
                            val sharedPreferences = requireContext().getSharedPreferences(
                                GAMES_PREFS, Context.MODE_PRIVATE
                            )
                            val editor = sharedPreferences.edit()
                            editor.putBoolean(FAVOURITES_KEY, true)
                            editor.apply()
                            val args = Bundle()
                            args.putInt(CURRENT_ITEM_KEY, 0)

                            findNavController().navigate(R.id.mainMenuFragment, args)
                        }
                        true
                    }

                    R.id.logoutItem -> {
                        authManager.logout()
                        findNavController().popBackStack(R.id.loginFragment, false)
                        true
                    }

                    else -> false
                }
            }
        }

        return bind.root
    }

    private fun showNotificationSettingsPopup() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Notification Settings")

        val view = layoutInflater.inflate(R.layout.notification_settings_layout, null)

        val switchNotification = view.findViewById<SwitchCompat>(R.id.switchNotification)
        val numberPickerHours = view.findViewById<NumberPicker>(R.id.numberPickerHours)

        numberPickerHours.minValue = 1
        numberPickerHours.maxValue = 24
        builder.setView(view)

        builder.setPositiveButton("Save") { dialog, _ ->
            val enableNotifications = switchNotification.isChecked
            val hoursBefore = numberPickerHours.value

            utils.showToast(requireContext(), "$enableNotifications $hoursBefore")

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

}