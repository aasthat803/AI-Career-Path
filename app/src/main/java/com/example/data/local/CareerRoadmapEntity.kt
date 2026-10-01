package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CareerOverview
import com.example.data.model.CareerRoadmap
import com.example.data.model.LearningResource
import com.example.data.model.RecommendedProject
import com.example.data.model.RoadmapPhase
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "career_roadmaps")
data class CareerRoadmapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetRole: String,
    val userSkills: String,
    val userInterests: String,
    val overviewJson: String,
    val phasesJson: String,
    val resourcesJson: String,
    val projectsJson: String,
    val counselorAdvice: String,
    val rawMarkdown: String,
    val completedItemIdsJson: String = "[]",
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): CareerRoadmap {
        val overview = parseOverview(overviewJson)
        val phases = parsePhases(phasesJson)
        val resources = parseResources(resourcesJson)
        val projects = parseProjects(projectsJson)
        val completedSet = parseCompletedSet(completedItemIdsJson)

        return CareerRoadmap(
            id = id,
            targetRole = targetRole,
            userSkills = userSkills,
            userInterests = userInterests,
            careerOverview = overview,
            phases = phases,
            resources = resources,
            projects = projects,
            counselorAdvice = counselorAdvice,
            rawMarkdown = rawMarkdown,
            completedItemIds = completedSet,
            isBookmarked = isBookmarked,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(roadmap: CareerRoadmap): CareerRoadmapEntity {
            return CareerRoadmapEntity(
                id = roadmap.id,
                targetRole = roadmap.targetRole,
                userSkills = roadmap.userSkills,
                userInterests = roadmap.userInterests,
                overviewJson = serializeOverview(roadmap.careerOverview),
                phasesJson = serializePhases(roadmap.phases),
                resourcesJson = serializeResources(roadmap.resources),
                projectsJson = serializeProjects(roadmap.projects),
                counselorAdvice = roadmap.counselorAdvice,
                rawMarkdown = roadmap.rawMarkdown,
                completedItemIdsJson = serializeCompletedSet(roadmap.completedItemIds),
                isBookmarked = roadmap.isBookmarked,
                createdAt = roadmap.createdAt
            )
        }

        private fun serializeOverview(overview: CareerOverview): String {
            val json = JSONObject()
            json.put("alignmentSummary", overview.alignmentSummary)
            json.put("feasibilityRating", overview.feasibilityRating)
            json.put("potentialGrowth", overview.potentialGrowth)
            json.put("estimatedDuration", overview.estimatedDuration)
            json.put("targetRoleDescription", overview.targetRoleDescription)
            return json.toString()
        }

        private fun parseOverview(jsonStr: String): CareerOverview {
            return try {
                val json = JSONObject(jsonStr)
                CareerOverview(
                    alignmentSummary = json.optString("alignmentSummary"),
                    feasibilityRating = json.optString("feasibilityRating", "High Feasibility"),
                    potentialGrowth = json.optString("potentialGrowth"),
                    estimatedDuration = json.optString("estimatedDuration", "3-6 Months"),
                    targetRoleDescription = json.optString("targetRoleDescription")
                )
            } catch (e: Exception) {
                CareerOverview(
                    alignmentSummary = "Overview not available",
                    feasibilityRating = "Moderate",
                    potentialGrowth = "Growing industry",
                    estimatedDuration = "6 Months",
                    targetRoleDescription = ""
                )
            }
        }

        private fun serializePhases(phases: List<RoadmapPhase>): String {
            val array = JSONArray()
            for (p in phases) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("phaseNumber", p.phaseNumber)
                obj.put("phaseTitle", p.phaseTitle)
                obj.put("durationWeeks", p.durationWeeks)
                obj.put("objective", p.objective)
                obj.put("technicalSkills", JSONArray(p.technicalSkills))
                obj.put("softSkills", JSONArray(p.softSkills))
                obj.put("milestone", p.milestone)
                array.put(obj)
            }
            return array.toString()
        }

        private fun parsePhases(jsonStr: String): List<RoadmapPhase> {
            val list = mutableListOf<RoadmapPhase>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val techArray = obj.optJSONArray("technicalSkills")
                    val softArray = obj.optJSONArray("softSkills")

                    val techList = mutableListOf<String>()
                    if (techArray != null) {
                        for (j in 0 until techArray.length()) techList.add(techArray.getString(j))
                    }

                    val softList = mutableListOf<String>()
                    if (softArray != null) {
                        for (j in 0 until softArray.length()) softList.add(softArray.getString(j))
                    }

                    list.add(
                        RoadmapPhase(
                            id = obj.optString("id", "phase_${i + 1}"),
                            phaseNumber = obj.optInt("phaseNumber", i + 1),
                            phaseTitle = obj.optString("phaseTitle", "Phase ${i + 1}"),
                            durationWeeks = obj.optString("durationWeeks", "4-6 Weeks"),
                            objective = obj.optString("objective", ""),
                            technicalSkills = techList,
                            softSkills = softList,
                            milestone = obj.optString("milestone", "")
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        private fun serializeResources(resources: List<LearningResource>): String {
            val array = JSONArray()
            for (r in resources) {
                val obj = JSONObject()
                obj.put("title", r.title)
                obj.put("platform", r.platform)
                obj.put("isFree", r.isFree)
                obj.put("type", r.type)
                obj.put("linkOrQuery", r.linkOrQuery)
                obj.put("description", r.description)
                obj.put("bestFor", r.bestFor)
                array.put(obj)
            }
            return array.toString()
        }

        private fun parseResources(jsonStr: String): List<LearningResource> {
            val list = mutableListOf<LearningResource>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LearningResource(
                            title = obj.optString("title"),
                            platform = obj.optString("platform"),
                            isFree = obj.optBoolean("isFree", true),
                            type = obj.optString("type", "Course"),
                            linkOrQuery = obj.optString("linkOrQuery"),
                            description = obj.optString("description"),
                            bestFor = obj.optString("bestFor")
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        private fun serializeProjects(projects: List<RecommendedProject>): String {
            val array = JSONArray()
            for (p in projects) {
                val obj = JSONObject()
                obj.put("title", p.title)
                obj.put("difficulty", p.difficulty)
                obj.put("description", p.description)
                obj.put("techStack", JSONArray(p.techStack))
                obj.put("portfolioImpact", p.portfolioImpact)
                obj.put("keyFeatures", JSONArray(p.keyFeatures))
                array.put(obj)
            }
            return array.toString()
        }

        private fun parseProjects(jsonStr: String): List<RecommendedProject> {
            val list = mutableListOf<RecommendedProject>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val techArray = obj.optJSONArray("techStack")
                    val featuresArray = obj.optJSONArray("keyFeatures")

                    val techList = mutableListOf<String>()
                    if (techArray != null) {
                        for (j in 0 until techArray.length()) techList.add(techArray.getString(j))
                    }

                    val featList = mutableListOf<String>()
                    if (featuresArray != null) {
                        for (j in 0 until featuresArray.length()) featList.add(featuresArray.getString(j))
                    }

                    list.add(
                        RecommendedProject(
                            title = obj.optString("title"),
                            difficulty = obj.optString("difficulty", "Intermediate"),
                            description = obj.optString("description"),
                            techStack = techList,
                            portfolioImpact = obj.optString("portfolioImpact"),
                            keyFeatures = featList
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        private fun serializeCompletedSet(set: Set<String>): String {
            val array = JSONArray()
            for (item in set) array.put(item)
            return array.toString()
        }

        private fun parseCompletedSet(jsonStr: String): Set<String> {
            val set = mutableSetOf<String>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    set.add(array.getString(i))
                }
            } catch (_: Exception) {}
            return set
        }
    }
}
