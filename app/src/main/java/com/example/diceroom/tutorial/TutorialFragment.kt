package com.example.diceroom.tutorial

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.diceroom.R
import com.example.diceroom.utils.Constants.Companion.IS_FIRST_RUN_PREFS
import com.example.diceroom.utils.ViewPagerAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class TutorialFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutorial, container, false)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        val skipBtn: MaterialButton = view.findViewById(R.id.skip_to_login_mb)

        val sharedPreferences = requireActivity().getSharedPreferences(IS_FIRST_RUN_PREFS, Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean(IS_FIRST_RUN_PREFS, true)

        if (!isFirstRun) {
            skipBtn.visibility = GONE
        }

        val fragmentList = arrayListOf(
            WelcomeTutorial(),
            FirstTutorial(),
            SecondTutorial(),
            ThirdTutorial()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )
        val viewPager = view.findViewById<ViewPager2>(R.id.tutorial_pager)
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
        skipBtn.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences(IS_FIRST_RUN_PREFS, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(IS_FIRST_RUN_PREFS, false)
            editor.apply()
            findNavController().navigate(R.id.action_tutorialFragment_to_selectLogin)
        }
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        return view
    }

}