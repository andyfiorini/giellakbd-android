package com.android.inputmethod.ui.personaldictionary.dictionary

import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
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
import com.android.inputmethod.usecases.AddWordUseCase
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.HasWordUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_add_word.view.*
import kotlinx.android.synthetic.main.fragment_personal_dictionary.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase


class DictionaryFragment : Fragment(), DictionaryView {
    private lateinit var rvDictionary: RecyclerView
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var dictionaryUseCase: DictionaryUseCase
    private lateinit var removeWordUseCase: RemoveWordUseCase
    private lateinit var addWordUseCase: AddWordUseCase
    private lateinit var hasWordUseCase: HasWordUseCase
    private lateinit var presenter: DictionaryPresenter

    private val factory = DictionaryWordViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        database = PersonalDictionaryDatabase.getInstance(context!!)
        dictionaryUseCase = DictionaryUseCase(database)
        removeWordUseCase = RemoveWordUseCase(database)
        addWordUseCase = AddWordUseCase(database)
        hasWordUseCase = HasWordUseCase(database)
        presenter = DictionaryPresenter(this, dictionaryUseCase, removeWordUseCase, addWordUseCase, hasWordUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personal_dictionary, container, false)
    }

    private lateinit var builder: AlertDialog.Builder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDictionary = rv_personaldict_words
        rvDictionary.layoutManager = LinearLayoutManager(context!!)
        rvDictionary.adapter = adapter

        fab_personaldict_addword.setOnClickListener {
            showAddWordDialog()
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
        showAddWordDialog()
    }

    override fun navigateToUploadDictionary() {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToUploadFragment())
    }

    override fun render(viewState: DictionaryViewState) {
        adapter.update(viewState.dictionary)
        g_personaldict_empty.isInvisible = viewState.dictionary.isNotEmpty()

        dialog?.let {
            it.findViewById<TextInputLayout>(R.id.til_addword)?.error = viewState.alertError
            it.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = viewState.alertError == null
        }
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
        return Observable.merge<DictionaryEvent>(
                adapter.events().map {
                    when (it) {
                        is DictionaryWordEvent.OnClickPressEvent -> {
                            DictionaryEvent.OnWordSelected(it.wordId, it.word)
                        }
                        is DictionaryWordEvent.OnClickRemoveEvent -> {
                            DictionaryEvent.OnRemoveEvent(it.wordId)
                        }
                    }

                },
                inputDialogEvents
        )
    }


    private val inputDialogEvents = PublishSubject.create<DictionaryEvent.DialogEvent>()

    private fun showAddWordDialog() {
        builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.add_word_dialog_title)

        val viewInflated: View = LayoutInflater.from(context).inflate(R.layout.dialog_add_word, view as ViewGroup?, false)
        builder.setView(viewInflated)


        viewInflated.tiet_addword.textChanges().skipInitialValue().map { DictionaryEvent.DialogEvent.OnDialogInput(it.toString()) }.subscribe(inputDialogEvents)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val newWord = viewInflated.tiet_addword.text.toString()
            inputDialogEvents.onNext(DictionaryEvent.DialogEvent.OnDialogAddWordEvent(newWord))
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.setOnDismissListener {
            dialog = null
            inputDialogEvents.onNext(DictionaryEvent.DialogEvent.OnDialogInputDismiss)
        }

        dialog = builder.show()
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
    }

    private fun dismissDialog() {
        dialog?.dismiss()
    }


}
