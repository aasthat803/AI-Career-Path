package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CareerRoadmapEntity
import com.example.data.model.CareerOverview
import com.example.data.model.CareerRoadmap
import com.example.data.model.LearningResource
import com.example.data.model.RecommendedProject
import com.example.data.model.RoadmapPhase
import com.example.data.repository.DefaultRoadmaps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Career Path", appName)
    }

    @Test
    fun `test career roadmap progress calculation`() {
        val roadmap = DefaultRoadmaps.createAiEngineerRoadmap()
        assertTrue(roadmap.phases.isNotEmpty())
        assertTrue(roadmap.resources.isNotEmpty())
        assertTrue(roadmap.projects.isNotEmpty())
        assertTrue(roadmap.totalSkillsCount > 0)
        assertEquals(0, roadmap.completedSkillsCount)

        // Mark 1 skill done
        val phase = roadmap.phases.first()
        val skill = phase.technicalSkills.first()
        val updatedRoadmap = roadmap.copy(
            completedItemIds = setOf("${phase.id}_tech_$skill")
        )
        assertEquals(1, updatedRoadmap.completedSkillsCount)
        assertTrue(updatedRoadmap.progressPercent > 0f)
    }

    @Test
    fun `test entity serialization roundtrip`() {
        val roadmap = DefaultRoadmaps.createAiEngineerRoadmap()
        val entity = CareerRoadmapEntity.fromDomain(roadmap)
        val domain = entity.toDomain()

        assertEquals(roadmap.targetRole, domain.targetRole)
        assertEquals(roadmap.phases.size, domain.phases.size)
        assertEquals(roadmap.resources.size, domain.resources.size)
        assertEquals(roadmap.projects.size, domain.projects.size)
        assertEquals(roadmap.careerOverview.feasibilityRating, domain.careerOverview.feasibilityRating)
    }
}
