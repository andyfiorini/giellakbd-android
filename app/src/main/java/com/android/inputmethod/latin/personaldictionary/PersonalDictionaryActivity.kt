package com.android.inputmethod.latin.personaldictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_personal_dictionary.*
import kotlinx.android.synthetic.main.content_personal_dictionary.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import android.content.Context
import android.view.View
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dictionary_item.view.*


class PersonalDictionaryActivity : AppCompatActivity() {
    private lateinit var rvDictionary: RecyclerView
    private lateinit var adapter: MyRecyclerViewAdapter

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.android.inputmethod.latin.R.layout.activity_personal_dictionary)
        setSupportActionBar(toolbar)


        rvDictionary = rv_personaldict_words
        rvDictionary.layoutManager = LinearLayoutManager(this)
        adapter = MyRecyclerViewAdapter(this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        rvDictionary.adapter = adapter


    }


    override fun onResume() {
        super.onResume()

        disposable = PersonalDictionaryDatabase.getInstance(this)
                .dictionaryDao()
                .dictionaryF()
                .subscribe {
                    val words = it.map { it.word }
                    adapter.update(words)
        }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }
}
class MyRecyclerViewAdapter(context: Context) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null
    private var data = mutableListOf<String>()

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(com.android.inputmethod.latin.R.layout.dictionary_item, parent, false)
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

