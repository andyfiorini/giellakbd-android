package com.android.inputmethod.ui.personaldictionary.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.personaldictionary.blacklist.adapter.BlacklistWordEvent
import com.android.inputmethod.ui.personaldictionary.blacklist.adapter.BlacklistWordViewHolder
import com.android.inputmethod.ui.personaldictionary.blacklistworddialog.BlacklistWordDialogNavArg
import com.android.inputmethod.usecases.BlacklistUseCase
import com.android.inputmethod.usecases.ChangeBlacklistUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_personal_blacklist.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase


class BlacklistFragment : Fragment(), BlacklistView {
    private lateinit var rvBlacklist: RecyclerView
    private lateinit var disposable: Disposable

    private lateinit var presenter: BlacklistPresenter

    private val factory = BlacklistWordViewHolder.BlacklistWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private val navArgs by navArgs<BlacklistFragmentArgs>()
    override val languageId by lazy { navArgs.blacklistNavArg.languageId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val blacklistUseCase = BlacklistUseCase(database)
        val removeWordUseCase = RemoveWordUseCase(database)
        val blacklistWordUseCase = ChangeBlacklistUseCase(database)
        presenter = BlacklistPresenter(this, blacklistUseCase, removeWordUseCase, blacklistWordUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personal_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvBlacklist = rv_blacklist_words
        rvBlacklist.layoutManager = LinearLayoutManager(context!!)
        rvBlacklist.adapter = adapter

        fab_blacklist_addword.setOnClickListener {
            navigateToBlacklistWordDialogFragment(languageId)
        }
    }

    override fun onResume() {
        super.onResume()
        disposable = presenter.states.observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun navigateToBlacklistWordDialogFragment(languageId: Long) {
        findNavController().navigate(BlacklistFragmentDirections.actionBlacklistFragmentToBlacklistWordDialogFragment(
                BlacklistWordDialogNavArg(languageId)
        ))
    }

    override fun render(viewState: BlacklistViewState) {
        adapter.update(viewState.blacklist)
        g_blacklist_empty.isInvisible = viewState.blacklist.isNotEmpty()
        g_blacklist_empty.requestLayout()
    }

    override fun events(): Observable<BlacklistEvent> {
        return adapter.events().map {
            when (it) {
                is BlacklistWordEvent.RemoveEvent -> {
                    BlacklistEvent.OnRemoveEvent(it.wordId)
                }
                is BlacklistWordEvent.AllowEvent -> {
                    BlacklistEvent.OnAllowEvent(it.wordId)
                }
            }
        }
    }
}
