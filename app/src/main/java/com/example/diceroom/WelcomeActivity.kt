package com.example.diceroom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.diceroom.authentication.SelectLoginActivity
import com.example.diceroom.tutorial.FirstTutorial
import com.example.diceroom.tutorial.SecondTutorial
import com.example.diceroom.tutorial.ThirdTutorial
import com.example.diceroom.tutorial.ViewPagerAdapter
import com.example.diceroom.tutorial.WelcomeTutorial
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.FirebaseApp


class WelcomeActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
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
        setContentView(R.layout.tutorial_activity)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        viewPager = findViewById(R.id.tutorial_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val skipBtn: MaterialButton = findViewById(R.id.skip_to_login_mb)

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val intent = intent

        if (!isFirstRun && !intent.hasExtra("isFirst")) {
            startActivity(Intent(this, SelectLoginActivity::class.java))
        }

        if (intent.hasExtra("isFirst")) {
            val isFirst = intent.getBooleanExtra("isFirst", true)
            if (isFirst) {
                FirebaseApp.initializeApp(this)
                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                skipBtn.setOnClickListener {
                    startActivity(Intent(this, SelectLoginActivity::class.java))
                }
            } else {
                skipBtn.visibility = INVISIBLE
            }
        } else {
            FirebaseApp.initializeApp(this)
            skipBtn.setOnClickListener {
                startActivity(Intent(this, SelectLoginActivity::class.java))
            }
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 3) {
                    skipBtn.text = "Login now!"
                    skipBtn.backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 0, 0))
                    skipBtn.setTextColor(Color.WHITE)
                } else {
                    skipBtn.text = "Skip to login"
                    skipBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    skipBtn.setTextColor(Color.BLACK)
                }
            }
        })

        val fragments: ArrayList<Fragment> = arrayListOf(
            WelcomeTutorial(),
            FirstTutorial(),
            SecondTutorial(),
            ThirdTutorial()
        )

        val adapter = ViewPagerAdapter(fragments, this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }
}