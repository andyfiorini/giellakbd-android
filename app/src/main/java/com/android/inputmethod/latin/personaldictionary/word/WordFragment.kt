package com.android.inputmethod.latin.personaldictionary.word

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.latin.personaldictionary.WordContextUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dictionary_item_context.view.*
import kotlinx.android.synthetic.main.fragment_word.view.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.dictionary.personal.WordContext

class WordFragment : Fragment(), WordView {

    private lateinit var rvDictionary: RecyclerView
    private lateinit var adapter: WordRecyclerAdapter
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var useCase: WordContextUseCase
    private lateinit var presenter: WordPresenter

    private val args by navArgs<WordFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PersonalDictionaryDatabase.getInstance(context!!)

        useCase = WordContextUseCase(database)

        presenter = WordPresenter(useCase, args.wordNavArg.wordId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_word, container, false)

        rvDictionary = view.rv_personaldict_wordcontext
        rvDictionary.layoutManager = LinearLayoutManager(context!!)
        adapter = WordRecyclerAdapter(context!!)

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
        Log.d("WordFragment", "Rendering: $viewState")
        adapter.update(viewState.contexts)
    }
}

class WordRecyclerAdapter(context: Context) : RecyclerView.Adapter<WordRecyclerAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null
    private var data = mutableListOf<WordContext>()

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.dictionary_item_context, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wordContext = data[position]
        holder.tvPrev.text = wordContext.prevWord
        holder.tvNext.text = wordContext.nextWord
    }

    // total number of rows
    override fun getItemCount(): Int {
        return data.size
    }

    fun update(newData: List<WordContext>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var tvPrev: TextView = itemView.tv_dictitem_prev
        var tvNext: TextView = itemView.tv_dictitem_next

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    internal fun getItem(id: Int): WordContext {
        return data[id]
    }

    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}

