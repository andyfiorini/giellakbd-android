package com.android.inputmethod.ui.personaldictionary.dictionary

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordEvent
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewHolder
import com.android.inputmethod.ui.personaldictionary.word.WordNavArg
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_personal_dictionary.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase


class DictionaryFragment : Fragment(), DictionaryView {
    private lateinit var rvDictionary: RecyclerView
    private lateinit var disposable: Disposable

    private lateinit var presenter: DictionaryPresenter

    private val factory = DictionaryWordViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val dictionaryUseCase = DictionaryUseCase(database)
        val removeWordUseCase = RemoveWordUseCase(database)
        presenter = DictionaryPresenter(this, dictionaryUseCase, removeWordUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personal_dictionary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDictionary = rv_personaldict_words
        rvDictionary.layoutManager = LinearLayoutManager(context!!)
        rvDictionary.adapter = adapter

        fab_personaldict_addword.setOnClickListener {
            navigateToAddWordDialogFragment()
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

    override fun navigateToWordFragment(wordId: Long, word: String) {
        findNavController().navigate(
                DictionaryFragmentDirections.actionDictionaryFragmentToWordFragment(word, WordNavArg(wordId, word))
        )
    }

    override fun navigateToAddWordDialogFragment() {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToAddWordDialogFragment())
    }

    override fun navigateToUploadDictionary() {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToUploadFragment())
    }

    override fun render(viewState: DictionaryViewState) {
        adapter.update(viewState.dictionary)
        g_personaldict_empty.isInvisible = viewState.dictionary.isNotEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dictionary_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_upload -> {
                navigateToUploadDictionary()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun events(): Observable<DictionaryEvent> {
        return adapter.events().map {
            when (it) {
                is DictionaryWordEvent.OnClickPressEvent -> {
                    DictionaryEvent.OnWordSelected(it.wordId, it.word)
                }
                is DictionaryWordEvent.OnClickRemoveEvent -> {
                    DictionaryEvent.OnRemoveEvent(it.wordId)
                }
            }

        }
    }

}
