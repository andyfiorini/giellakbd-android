package com.android.inputmethod.ui.personaldictionary.upload

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.android.inputmethod.latin.R
import com.android.inputmethod.usecases.JsonDictionaryUseCase
import com.google.gson.Gson
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_personal_upload.*
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class UploadFragment : Fragment(), UploadView {
    private lateinit var disposable: Disposable

    private lateinit var database: PersonalDictionaryDatabase
    private lateinit var jsonDictionaryUseCase: JsonDictionaryUseCase
    private lateinit var presenter: UploadPresenter
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = PersonalDictionaryDatabase.getInstance(context!!)

        jsonDictionaryUseCase = JsonDictionaryUseCase(database, gson)
        presenter = UploadPresenter(this, jsonDictionaryUseCase)
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
        return b_upload_upload.clicks().map { UploadEvent.OnUploadPressed }
    }
}
