package com.android.inputmethod.ui.personaldictionary.dictionary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
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

    private lateinit var presenter: DictionaryPresenter

    private val factory = DictionaryWordViewHolder.DictionaryWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private val args by navArgs<DictionaryFragmentArgs>()
    override val languageId by lazy { args.dictionaryNavArg.languageId }

    private lateinit var swipeActionCallback: SwipeActionCallback
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val dictionaryUseCase = DictionaryUseCase(database)
        val removeWordUseCase = SoftDeleteWordUseCase(database)
        val blacklistWordUseCase = SetBlacklistUseCase(database)
        presenter = DictionaryPresenter(this, dictionaryUseCase, removeWordUseCase, blacklistWordUseCase)

        Log.d("swipeActionCallback", "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Log.d("swipeActionCallback", "onCreateView")
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

        val paint = TextPaint().apply {
            color = Color.WHITE
            isAntiAlias = true
            val textSizePixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
            textSize = textSizePixel
        }

        swipeActionCallback = SwipeActionCallback(
                SwipeConf(
                        left = SwipeActionConf(
                                resources.getDrawable(R.drawable.vd_blacklist, activity?.theme),
                                resources.getString(R.string.block_word),
                                paint,
                                ColorDrawable(ContextCompat.getColor(context!!, R.color.colorBlock))
                        ),
                        right = SwipeActionConf(
                                resources.getDrawable(R.drawable.vd_delete, activity?.theme),
                                resources.getString(R.string.delete_word),
                                paint,
                                ColorDrawable(ContextCompat.getColor(context!!, R.color.colorDelete))
                        )
                )
        )
        val ith = ItemTouchHelper(swipeActionCallback)
        ith.attachToRecyclerView(rvDictionary)
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)

        events().subscribe(events)
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
        Log.d("RenderSnackBar", "$viewState")
        when (viewState) {
            is SnackbarViewState.WordRemoved -> {
                snackbar.setText("Word '${viewState.word}' was removed.")
                snackbar.setAction(R.string.snackbar_undo) { undoRemove(viewState.wordId) }
                snackbar.show()
            }
            is SnackbarViewState.RemoveFailed -> {
                snackbar.setText("Failed to remove word, ${viewState.wordException}")
                adapter.notifyDataSetChanged()
                snackbar.show()
            }
            is SnackbarViewState.Hidden -> {
                snackbar.dismiss()
            }
            is SnackbarViewState.WordBlacklisted -> {
                snackbar.setText("Word '${viewState.word}' was blacklisted.")
                snackbar.setAction(R.string.snackbar_undo) { undoBlacklist(viewState.wordId) }
                snackbar.show()
            }
            is SnackbarViewState.BlacklistFailed -> {
                snackbar.setText("Failed to remove word, ${viewState.blacklistException}")
                adapter.notifyDataSetChanged()
                snackbar.show()
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

    override val events: PublishSubject<DictionaryEvent> = PublishSubject.create()

    private fun events(): Observable<DictionaryEvent> {
        return Observable.merge(
                adapter.events().map {
                    when (it) {
                        is DictionaryWordEvent.PressEvent -> {
                            DictionaryEvent.OnWordSelected(it.wordId, it.word)
                        }
                    }
                },
                swipeActionCallback.swipes().map {
                    when (it) {
                        is SwipeEvent.SwipeLeft -> {
                            val word = adapter.items[it.viewHolder.adapterPosition]
                            DictionaryEvent.OnBlacklistEvent(word.wordId, word.word)
                        }
                        is SwipeEvent.SwipeRight -> {
                            val word = adapter.items[it.viewHolder.adapterPosition]
                            DictionaryEvent.OnRemoveEvent(word.wordId, word.word)
                        }
                    }
                }
        )
    }
}
