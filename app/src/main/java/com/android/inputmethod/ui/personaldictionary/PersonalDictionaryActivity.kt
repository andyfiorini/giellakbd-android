package com.android.inputmethod.ui.personaldictionary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_personal_dictionary.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.android.inputmethod.latin.R


class PersonalDictionaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_dictionary)
        val host = f_dictionary_navhost as NavHostFragment

        setSupportActionBar(tl_dictionary)
        NavigationUI.setupActionBarWithNavController(this, host.navController)
    }
}

