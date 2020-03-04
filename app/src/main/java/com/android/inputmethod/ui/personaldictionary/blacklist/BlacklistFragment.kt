package com.android.inputmethod.ui.personaldictionary.blacklist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextPaint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.android.inputmethod.ui.personaldictionary.blacklist.adapter.BlacklistWordViewHolder
import com.android.inputmethod.ui.personaldictionary.blacklistworddialog.BlacklistWordDialogNavArg
import com.android.inputmethod.usecases.BlacklistUseCase
import com.android.inputmethod.usecases.SetBlacklistUseCase
import com.android.inputmethod.usecases.SoftDeleteWordUseCase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_personal_blacklist.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase


class BlacklistFragment : Fragment(), BlacklistView {
    private lateinit var rvBlacklist: RecyclerView
    private lateinit var disposable: Disposable
    private lateinit var viewDisposable: Disposable

    private lateinit var presenter: BlacklistPresenter

    private val factory = BlacklistWordViewHolder.BlacklistWordViewHolderFactory
    private val adapter = EventAdapter(factory)

    private val navArgs by navArgs<BlacklistFragmentArgs>()
    override val languageId by lazy { navArgs.blacklistNavArg.languageId }

    override val events = PublishSubject.create<BlacklistEvent>()

    private lateinit var swipeActionCallback: SwipeActionCallback
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val blacklistUseCase = BlacklistUseCase(database)
        val removeWordUseCase = SoftDeleteWordUseCase(database)
        val blacklistWordUseCase = SetBlacklistUseCase(database)
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

        val paint = TextPaint().apply {
            color = Color.WHITE
            isAntiAlias = true
            val textSizePixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
            textSize = textSizePixel
        }
        swipeActionCallback = SwipeActionCallback(
                SwipeConf(
                        left = SwipeActionConf(
                                resources.getDrawable(R.drawable.vd_allow, activity?.theme),
                                resources.getString(R.string.allow_word),
                                paint,
                                ColorDrawable(ContextCompat.getColor(context!!, R.color.colorAllow))
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
        ith.attachToRecyclerView(rvBlacklist)
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
        renderSnackbar(viewState.snackbar)
    }

    private fun renderSnackbar(viewState: BlacklistSnackbarViewState) {
        when (viewState) {
            is BlacklistSnackbarViewState.WordRemoved -> {
                snackbar.setText("Word '${viewState.word}' was removed.")
                snackbar.setAction(R.string.snackbar_undo) { undoRemove(viewState.wordId) }
                snackbar.show()
            }
            is BlacklistSnackbarViewState.RemoveFailed -> {
                snackbar.setText("Failed to remove word, ${viewState.wordException}")
                snackbar.setAction(null, null)
                snackbar.show()
                adapter.notifyDataSetChanged()
            }
            is BlacklistSnackbarViewState.Hidden -> {
                snackbar.dismiss()
            }
            is BlacklistSnackbarViewState.WordAllowed -> {
                snackbar.setText("Word '${viewState.word}' was allowed.")
                snackbar.setAction(R.string.snackbar_undo) { undoAllow(viewState.wordId) }
                snackbar.show()
            }
            is BlacklistSnackbarViewState.AllowFailed -> {
                snackbar.setText("Failed to remove word, ${viewState.blacklistException}")
                snackbar.setAction(null, null)
                snackbar.show()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun undoRemove(wordId: Long) {
        events.onNext(BlacklistEvent.OnUndoRemove(wordId))
    }

    private fun undoAllow(wordId: Long) {
        events.onNext(BlacklistEvent.OnUndoAllow(wordId))
    }

    private fun events(): Observable<BlacklistEvent> {
        return swipeActionCallback.swipes().map {
            when (it) {
                is SwipeEvent.SwipeLeft -> {
                    val word = adapter.items[it.viewHolder.adapterPosition]
                    BlacklistEvent.OnAllowEvent(word.wordId, word.word)
                }
                is SwipeEvent.SwipeRight -> {
                    val word = adapter.items[it.viewHolder.adapterPosition]
                    BlacklistEvent.OnRemoveEvent(word.wordId, word.word)
                }
            }
        }
    }
}
