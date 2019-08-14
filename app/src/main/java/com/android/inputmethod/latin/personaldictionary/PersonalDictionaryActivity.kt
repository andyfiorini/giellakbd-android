package com.android.inputmethod.latin.personaldictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_personal_dictionary.*
import kotlinx.android.synthetic.main.fragment_personal_dictionary.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import android.content.Context
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.inputmethod.latin.R
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dictionary_item.view.*


class PersonalDictionaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_dictionary)
        val host = f_dictionary_navhost as NavHostFragment

        setSupportActionBar(tl_dictionary)
        NavigationUI.setupActionBarWithNavController(this, host.navController)
    }
}

