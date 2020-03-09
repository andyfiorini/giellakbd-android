package com.android.inputmethod.ui.personaldictionary.dictionary

import android.os.Bundle
import android.view.*
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.components.recycleradapter.*
import com.android.inputmethod.ui.personaldictionary.addworddialog.AddWordDialogNavArg
import com.android.inputmethod.ui.personaldictionary.blacklist.BlacklistNavArg
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordEvent
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewHolder
import com.android.inputmethod.ui.personaldictionary.upload.UploadNavArg
import com.android.inputmethod.ui.personaldictionary.word.WordNavArg
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.SetBlacklistUseCase
import com.android.inputmethod.usecases.SoftDeleteWordUseCase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_personal_dictionary.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class DictionaryFragment : Fragment(), DictionaryView {
    private lateinit var rvDictionary: RecyclerView
    private lateinit var disposable: Disposable
    private lateinit var viewDisposable: Disposable

    private lateinit var presenter: DictionaryPresenter

    private val factory = DictionaryWordViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private val args by navArgs<DictionaryFragmentArgs>()
    override val languageId by lazy { args.dictionaryNavArg.languageId }

    override val events: PublishSubject<DictionaryEvent> = PublishSubject.create()

    private lateinit var swipeCallback: SwipeCallback
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val dictionaryUseCase = DictionaryUseCase(database)
        val removeWordUseCase = SoftDeleteWordUseCase(database)
        val blacklistWordUseCase = SetBlacklistUseCase(database)
        presenter = DictionaryPresenter(this, dictionaryUseCase, removeWordUseCase, blacklistWordUseCase)
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
            navigateToAddWordDialogFragment(languageId)
        }

        swipeCallback = SwipeCallback(SwipeDirection.LEFT to R.layout.swipe_left_block, SwipeDirection.RIGHT to R.layout.swipe_right_delete)
        swipeCallback.attachTo(rvDictionary)

        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)

        viewDisposable = events().subscribe { events.onNext(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDisposable.dispose()
    }

    override fun onResume() {
        super.onResume()
        disposable = presenter.states.observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
        snackbar.dismiss()
    }

    override fun navigateToWordFragment(wordId: Long, word: String) {
        findNavController().navigate(
                DictionaryFragmentDirections.actionDictionaryFragmentToWordFragment(word, WordNavArg(wordId, word))
        )
    }

    override fun navigateToAddWordDialogFragment(languageId: Long) {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToAddWordDialogFragment(
                AddWordDialogNavArg(languageId)
        ))
    }

    override fun navigateToUploadDictionary(languageId: Long) {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToUploadFragment(UploadNavArg(languageId)))
    }


    override fun navigateToBlacklistFragment(languageId: Long) {
        findNavController().navigate(DictionaryFragmentDirections.actionDictionaryFragmentToBlacklistFragment(BlacklistNavArg(languageId)))
    }


    override fun render(viewState: DictionaryViewState) {
        adapter.update(viewState.dictionary)
        g_personaldict_empty.isInvisible = viewState.dictionary.isNotEmpty()
        g_personaldict_empty.requestLayout()
        renderSnackbar(viewState.snackbar)
    }

    private fun renderSnackbar(viewState: SnackbarViewState) {
        when (viewState) {
            is SnackbarViewState.WordRemoved -> {
                snackbar.setText(getString(R.string.snackbar_delete_word, viewState.word))
                snackbar.setAction(R.string.snackbar_undo) { undoRemove(viewState.wordId) }
                snackbar.show()
            }
            is SnackbarViewState.RemoveFailed -> {
                snackbar.setText(getString(R.string.snackbar_delete_word_failed, viewState.wordException))
                snackbar.setAction(null, null)
                snackbar.show()
                adapter.notifyDataSetChanged()
            }
            is SnackbarViewState.Hidden -> {
                snackbar.dismiss()
            }
            is SnackbarViewState.WordBlacklisted -> {
                snackbar.setText(getString(R.string.snackbar_block_word, viewState.word))
                snackbar.setAction(R.string.snackbar_undo) { undoBlacklist(viewState.wordId) }
                snackbar.show()
            }
            is SnackbarViewState.BlacklistFailed -> {
                snackbar.setText(getString(R.string.snackbar_block_word_failed, viewState.blacklistException))
                snackbar.setAction(null, null)
                snackbar.show()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun undoRemove(wordId: Long) {
        events.onNext(DictionaryEvent.OnUndoRemoveEvent(wordId))
    }

    private fun undoBlacklist(wordId: Long) {
        events.onNext(DictionaryEvent.OnUndoBlacklistEvent(wordId))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dictionary_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.fragment_upload -> {
                navigateToUploadDictionary(languageId)
            }
            R.id.fragment_blacklist -> {
                navigateToBlacklistFragment(languageId)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun events(): Observable<DictionaryEvent> {
        return Observable.merge(
                adapter.events().map {
                    when (it) {
                        is DictionaryWordEvent.PressEvent -> {
                            DictionaryEvent.OnWordSelected(it.wordId, it.word)
                        }
                    }
                },
                swipeCallback.swipes().flatMap {
                    when (it.direction) {
                        SwipeDirection.LEFT -> {
                            val word = adapter.items[it.viewHolder.adapterPosition]
                            Observable.just(DictionaryEvent.OnBlacklistEvent(word.wordId, word.word))
                        }
                        SwipeDirection.RIGHT -> {
                            val word = adapter.items[it.viewHolder.adapterPosition]
                            Observable.just(DictionaryEvent.OnRemoveEvent(word.wordId, word.word))
                        }
                        else -> Observable.empty()
                    }
                }
        )
    }
}
