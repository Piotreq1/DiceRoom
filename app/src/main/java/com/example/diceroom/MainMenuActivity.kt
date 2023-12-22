package com.example.diceroom

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.diceroom.databinding.MainMenuActivityBinding
import com.example.diceroom.tutorial.ViewPagerAdapter


class MainMenuActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    private lateinit var bind: MainMenuActivityBinding
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewPager.currentItem == 0) {
                finish()
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

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)/* if (position == 3) {
                     skipBtn.text = "Login now!"
                     skipBtn.backgroundTintList = ColorStateList.valueOf(Color.rgb(99, 228, 142))
                 } else {
                     skipBtn.text = "Skip to login"
                     skipBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                 }*/
            }
        })

        val fragments: ArrayList<Fragment> = arrayListOf(
            GamesListFragment(), MeetingFragment(), ProfileFragment()
        )

        val adapter = ViewPagerAdapter(fragments, this)
        viewPager.adapter = adapter
        viewPager.currentItem = 1
        bind.bottomNav.menu.getItem(viewPager.currentItem).isChecked = true

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bind.bottomNav.menu.getItem(position).isChecked = true
            }
        })

        bind.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.gameField -> viewPager.currentItem = 0
                R.id.meetingField -> viewPager.currentItem = 1
                R.id.profileField -> viewPager.currentItem = 2
            }
            true
        }
    }
}