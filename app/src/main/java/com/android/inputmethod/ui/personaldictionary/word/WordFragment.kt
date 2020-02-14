package com.android.inputmethod.ui.personaldictionary.word

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewHolder
import com.android.inputmethod.usecases.WordContextUseCase
import com.android.inputmethod.usecases.WordUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_word.*
import kotlinx.android.synthetic.main.fragment_word.view.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class WordFragment : Fragment(), WordView {

    private lateinit var rvDictionary: RecyclerView
    private lateinit var disposable: Disposable
    private val factory = WordContextViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var wordContextUseCase: WordContextUseCase
    private lateinit var wordUseCase: WordUseCase
    private lateinit var presenter: WordPresenter

    private val args by navArgs<WordFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PersonalDictionaryDatabase.getInstance(context!!)

        wordContextUseCase = WordContextUseCase(database)
        wordUseCase = WordUseCase(database)

        presenter = WordPresenter(args.wordNavArg.wordId, wordContextUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_word, container, false)

        rvDictionary = view.rv_word_wordcontexts
        rvDictionary.layoutManager = LinearLayoutManager(context!!)
        rvDictionary.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        disposable = presenter.start().observeOn(AndroidSchedulers.mainThread()).subscribe(::render)

    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun events(): Observable<WordEvent> {
        return Observable.empty()
    }

    override fun render(viewState: WordViewState) {
        adapter.update(viewState.contexts)
        g_word_empty.isInvisible = viewState.contexts.isNotEmpty()

    }
}

