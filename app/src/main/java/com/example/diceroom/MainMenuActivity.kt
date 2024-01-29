package com.example.diceroom

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.diceroom.databinding.MainMenuActivityBinding
import com.example.diceroom.games.GamesListFragment
import com.example.diceroom.meetings.MeetingFragment
import com.example.diceroom.profile.ProfileFragment
import com.example.diceroom.utils.ViewPagerAdapter


class MainMenuActivity : AppCompatActivity(), ProfileFragment.OnNavigateToGameListListener {
    lateinit var viewPager: ViewPager2
    private lateinit var bind: MainMenuActivityBinding
    private var lastBackPressedTime: Long = 0
    private val interval = 1000

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewPager.currentItem == 0) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressedTime < interval) {
                    moveTaskToBack(true)
                } else {
                    lastBackPressedTime = currentTime
                    viewPager.currentItem = viewPager.currentItem - 1
                }
            } else {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = MainMenuActivityBinding.inflate(layoutInflater)
        setContentView(bind.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        viewPager = bind.mainMenuPager

        val fragments: ArrayList<Fragment> = arrayListOf(
            GamesListFragment(), MeetingFragment(), ProfileFragment()
        )

        val adapter = ViewPagerAdapter(fragments, this)
        viewPager.adapter = adapter
        viewPager.currentItem = intent.getIntExtra("currentItem", 1)
        bind.bottomNav.menu.getItem(viewPager.currentItem).isChecked = true

        val profileFragment = fragments[2] as ProfileFragment
        profileFragment.setOnNavigateToGameListListener(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bind.bottomNav.menu.getItem(position).isChecked = true
            }
        })

        bind.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.gameItem -> viewPager.currentItem = 0
                R.id.meetingItem -> viewPager.currentItem = 1
                R.id.profileItem -> viewPager.currentItem = 2
            }
            true
        }
    }

    override fun navigateToGameList() {
        viewPager.currentItem = 0
    }
}