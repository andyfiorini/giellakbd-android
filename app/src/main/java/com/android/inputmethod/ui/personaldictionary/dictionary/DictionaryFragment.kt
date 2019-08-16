package com.android.inputmethod.ui.personaldictionary.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.personaldictionary.DictionaryUseCase
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordEvent
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewHolder
import com.android.inputmethod.ui.personaldictionary.word.WordNavArg
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_personal_dictionary.view.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class DictionaryFragment : Fragment(), DictionaryView {
    private lateinit var rvDictionary: RecyclerView
    //    private lateinit var adapter: DictionaryRecyclerAdapter
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var dictionaryUseCase: DictionaryUseCase
    private lateinit var removeWordUseCase: RemoveWordUseCase
    private lateinit var presenter: DictionaryPresenter

    private val factory = DictionaryWordViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = PersonalDictionaryDatabase.getInstance(context!!)
        dictionaryUseCase = DictionaryUseCase(database)
        removeWordUseCase = RemoveWordUseCase(database)
        presenter = DictionaryPresenter(this, dictionaryUseCase, removeWordUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_personal_dictionary, container, false)

        rvDictionary = view.rv_personaldict_words
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

    override fun navigateToWordFragment(wordId: Long) {
        findNavController().navigate(
                DictionaryFragmentDirections.actionDictionaryFragmentToWordFragment(WordNavArg(wordId))
        )
    }

    override fun render(viewState: DictionaryViewState) {
        adapter.update(viewState.dictionary)
    }

    override fun events(): Observable<DictionaryEvent> {
        return adapter.events().map {
            when (it) {
                is DictionaryWordEvent.OnClickPressEvent -> {
                    DictionaryEvent.OnWordSelected(it.wordId)
                }
                is DictionaryWordEvent.OnClickRemoveEvent ->{
                    DictionaryEvent.OnRemoveEvent(it.wordId)
                }
            }
        }
    }
}
