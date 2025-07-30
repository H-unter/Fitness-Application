package com.example.fitnessapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SetEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetEntry(entry: SetEntryEntity)

    @Update
    suspend fun updateSetEntry(entry: SetEntryEntity)

    @Query("DELETE FROM SetEntry WHERE setEntryId = :entryId")
    suspend fun deleteSetEntryById(entryId: Int)

    @Query("SELECT * FROM SetEntry WHERE setGroupId = :setGroupId ORDER BY setIndex ASC")
    suspend fun getEntriesForGroup(setGroupId: Int): List<SetEntryEntity>
}
