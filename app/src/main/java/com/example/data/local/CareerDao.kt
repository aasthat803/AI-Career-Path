package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Query("SELECT * FROM career_roadmaps ORDER BY createdAt DESC")
    fun getAllRoadmaps(): Flow<List<CareerRoadmapEntity>>

    @Query("SELECT * FROM career_roadmaps WHERE id = :id")
    fun getRoadmapById(id: Long): Flow<CareerRoadmapEntity?>

    @Query("SELECT * FROM career_roadmaps WHERE isBookmarked = 1 ORDER BY createdAt DESC")
    fun getBookmarkedRoadmaps(): Flow<List<CareerRoadmapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: CareerRoadmapEntity): Long

    @Update
    suspend fun updateRoadmap(roadmap: CareerRoadmapEntity)

    @Query("UPDATE career_roadmaps SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, isBookmarked: Boolean)

    @Query("UPDATE career_roadmaps SET completedItemIdsJson = :completedJson WHERE id = :id")
    suspend fun updateCompletedItems(id: Long, completedJson: String)

    @Query("DELETE FROM career_roadmaps WHERE id = :id")
    suspend fun deleteRoadmapById(id: Long)
}
