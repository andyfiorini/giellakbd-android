package no.divvun.dictionary.personal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single

@Dao
interface CandidatesDao {
    @Query("SELECT * FROM Candidates WHERE word=:word")
    fun findCandidate(word: String): Single<Candidate>

    @Query("SELECT COUNT(1) FROM Candidates WHERE word=:word")
    fun containsCandidate(word: String): Int

    @Query("SELECT COUNT(word) FROM Candidates WHERE word=:word")
    fun isCandidate(word: String): Int

    @Insert
    fun insertCandidate(candidate: Candidate)

    @Query("DELETE FROM Candidates WHERE word = :word")
    fun removeCandidate(word: String): Int
}