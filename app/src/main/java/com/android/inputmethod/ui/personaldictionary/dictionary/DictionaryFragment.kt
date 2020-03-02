package com.android.inputmethod.ui.personaldictionary.dictionary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextPaint
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
import com.android.inputmethod.ui.components.recycleradapter.EventAdapter
import com.android.inputmethod.ui.components.recycleradapter.SwipeActionCallback
import com.android.inputmethod.ui.components.recycleradapter.SwipeActionConf
import com.android.inputmethod.ui.components.recycleradapter.SwipeConf
import com.android.inputmethod.ui.personaldictionary.addworddialog.AddWordDialogNavArg
import com.android.inputmethod.ui.personaldictionary.blacklist.BlacklistNavArg
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordEvent
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewHolder
import com.android.inputmethod.ui.personaldictionary.upload.UploadNavArg
import com.android.inputmethod.ui.personaldictionary.word.WordNavArg
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import com.android.inputmethod.usecases.SetBlacklistUseCase
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

    private val args by navArgs<DictionaryFragmentArgs>()
    override val languageId by lazy { args.dictionaryNavArg.languageId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val database = PersonalDictionaryDatabase.getInstance(context!!)
        val dictionaryUseCase = DictionaryUseCase(database)
        val removeWordUseCase = RemoveWordUseCase(database)
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

        val paint = TextPaint().apply {
            color = Color.WHITE
            isAntiAlias = true
            val textSizePixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, resources.displayMetrics)
            textSize = textSizePixel
        }

        val ith = ItemTouchHelper(
                SwipeActionCallback(
                        SwipeConf(
                                SwipeActionConf(
                                        resources.getDrawable(R.drawable.vd_delete, activity?.theme),
                                        resources.getString(R.string.delete_word),
                                        paint,
                                        ColorDrawable(ContextCompat.getColor(context!!, R.color.colorDelete))
                                ),
                                SwipeActionConf(
                                        resources.getDrawable(R.drawable.vd_blacklist, activity?.theme),
                                        "Block",
                                        paint,
                                        ColorDrawable(ContextCompat.getColor(context!!, R.color.colorBlock))
                                )
                        )
                )
        )
        ith.attachToRecyclerView(rvDictionary)
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

    override fun events(): Observable<DictionaryEvent> {
        return adapter.events().map {
            when (it) {
                is DictionaryWordEvent.PressEvent -> {
                    DictionaryEvent.OnWordSelected(it.wordId, it.word)
                }
                is DictionaryWordEvent.RemoveEvent -> {
                    DictionaryEvent.OnRemoveEvent(it.wordId)
                }
                is DictionaryWordEvent.BlacklistEvent -> {
                    DictionaryEvent.OnBlacklistEvent(it.wordId)
                }
            }
        }
    }
}
