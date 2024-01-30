package com.example.diceroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.diceroom.databinding.FragmentMainMenuBinding
import com.example.diceroom.games.GamesListFragment
import com.example.diceroom.meetings.MeetingFragment
import com.example.diceroom.profile.ProfileFragment
import com.example.diceroom.utils.Constants.Companion.CURRENT_ITEM_KEY
import com.example.diceroom.utils.ViewPagerAdapter


class MainMenuFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bind: FragmentMainMenuBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = arguments
        val currentItem = args?.getInt(CURRENT_ITEM_KEY)

        bind = FragmentMainMenuBinding.inflate(layoutInflater)

        viewPager = bind.mainMenuPager

        val fragments = arrayListOf(
            GamesListFragment(), MeetingFragment(), ProfileFragment()
        )

        val adapter = ViewPagerAdapter(
            fragments,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        viewPager.adapter = adapter
        viewPager.currentItem = currentItem ?: 1
        bind.bottomNav.menu.getItem(viewPager.currentItem).isChecked = true

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bind.bottomNav.menu.getItem(position).isChecked = true
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().moveTaskToBack(true)
        }

        bind.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.gameItem -> viewPager.currentItem = 0
                R.id.meetingItem -> viewPager.currentItem = 1
                R.id.profileItem -> viewPager.currentItem = 2
            }
            true
        }

        return bind.root
    }
}