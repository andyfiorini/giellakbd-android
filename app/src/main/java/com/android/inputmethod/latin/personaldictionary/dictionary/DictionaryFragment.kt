package com.android.inputmethod.latin.personaldictionary.dictionary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.latin.personaldictionary.DictionaryUseCase
import com.android.inputmethod.latin.personaldictionary.word.WordNavArg
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dictionary_item.view.*
import kotlinx.android.synthetic.main.fragment_personal_dictionary.*
import kotlinx.android.synthetic.main.fragment_personal_dictionary.view.*
import kotlinx.android.synthetic.main.fragment_personal_dictionary.view.fab
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class DictionaryFragment : Fragment(), DictionaryView {
    private lateinit var rvDictionary: RecyclerView
    private lateinit var adapter: DictionaryRecyclerAdapter
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var useCase: DictionaryUseCase
    private lateinit var presenter: DictionaryPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = PersonalDictionaryDatabase.getInstance(context!!)
        useCase = DictionaryUseCase(database)
        presenter = DictionaryPresenter(this, useCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_personal_dictionary, container, false)

        rvDictionary = view.rv_personaldict_words
        rvDictionary.layoutManager = LinearLayoutManager(context!!)
        adapter = DictionaryRecyclerAdapter(context!!)

        rvDictionary.adapter = adapter
        return view
    }


    override fun onResume() {
        super.onResume()
        disposable = presenter.start().subscribeOn(AndroidSchedulers.mainThread()).subscribe(::render)
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
        val words = viewState.dictionary.map { it.word }
        adapter.update(words)
    }

    override fun events(): Observable<DictionaryEvent> {
        return fab.clicks().flatMap { useCase.execute().take(1).map {
            Log.d("DictionaryFragment", "Word: ${it.first().word} with id: ${it.first().wordId}")
            it.first().wordId
        } }.map { DictionaryEvent.OnWordSelected(it) }
    }

}

class DictionaryRecyclerAdapter(context: Context) : RecyclerView.Adapter<DictionaryRecyclerAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null
    private var data = mutableListOf<String>()

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.dictionary_item, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = data[position]
        holder.myTextView.text = animal
    }

    // total number of rows
    override fun getItemCount(): Int {
        return data.size
    }

    fun update(newData: List<String>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var myTextView: TextView = itemView.tv_dictitem_word

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    internal fun getItem(id: Int): String {
        return data[id]
    }

    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}

