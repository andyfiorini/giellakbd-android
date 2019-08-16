package com.android.inputmethod.ui.personaldictionary.upload

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.inputmethod.latin.R
import com.android.inputmethod.ui.personaldictionary.dictionary.*
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordEvent
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.ui.personaldictionary.word.WordNavArg
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class UploadFragment : Fragment(), UploadView {
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var dictionaryUseCase: DictionaryUseCase
    private lateinit var presenter: UploadPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PersonalDictionaryDatabase.getInstance(context!!)
        dictionaryUseCase = DictionaryUseCase(database)
        presenter = UploadPresenter(this, dictionaryUseCase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_personal_upload, container, false)
    }


    override fun onResume() {
        super.onResume()
        disposable = presenter.start().observeOn(AndroidSchedulers.mainThread()).subscribe(::render)
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun render(viewState: UploadViewState) {
    }

    override fun events(): Observable<UploadEvent> {
        return Observable.empty()
    }
}
