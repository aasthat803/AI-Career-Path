package com.example.data.model

data class RoadmapPhase(
    val id: String,
    val phaseNumber: Int,
    val phaseTitle: String,
    val durationWeeks: String,
    val objective: String,
    val technicalSkills: List<String>,
    val softSkills: List<String>,
    val milestone: String
)

data class LearningResource(
    val title: String,
    val platform: String,
    val isFree: Boolean,
    val type: String, // Course, YouTube, Documentation, GitHub, Book
    val linkOrQuery: String,
    val description: String,
    val bestFor: String
)

data class RecommendedProject(
    val title: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val description: String,
    val techStack: List<String>,
    val portfolioImpact: String,
    val keyFeatures: List<String>
)

data class CareerOverview(
    val alignmentSummary: String,
    val feasibilityRating: String, // e.g. "High Feasibility (75% Skill Match)"
    val potentialGrowth: String,
    val estimatedDuration: String,
    val targetRoleDescription: String
)

data class CareerRoadmap(
    val id: Long = 0,
    val targetRole: String,
    val userSkills: String,
    val userInterests: String,
    val careerOverview: CareerOverview,
    val phases: List<RoadmapPhase>,
    val resources: List<LearningResource>,
    val projects: List<RecommendedProject>,
    val counselorAdvice: String,
    val rawMarkdown: String,
    val completedItemIds: Set<String> = emptySet(),
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalSkillsCount: Int
        get() = phases.sumOf { it.technicalSkills.size + it.softSkills.size }

    val completedSkillsCount: Int
        get() = phases.sumOf { phase ->
            phase.technicalSkills.count { completedItemIds.contains("${phase.id}_tech_$it") } +
            phase.softSkills.count { completedItemIds.contains("${phase.id}_soft_$it") }
        }

    val progressPercent: Float
        get() = if (totalSkillsCount > 0) completedSkillsCount.toFloat() / totalSkillsCount else 0f
}
