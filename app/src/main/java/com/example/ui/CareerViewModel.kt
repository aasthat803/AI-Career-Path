package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.CareerRoadmap
import com.example.data.repository.CareerRepository
import com.example.data.repository.DefaultRoadmaps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CareerUiState(
    val userSkills: String = "",
    val userInterests: String = "",
    val targetRole: String = "",
    val experienceLevel: String = "Beginner / Switcher",
    val weeklyHours: Int = 10,
    val isGenerating: Boolean = false,
    val activeRoadmap: CareerRoadmap? = null,
    val selectedTab: Int = 0, // 0 = Build, 1 = Active Roadmap, 2 = Saved Roadmaps, 3 = Presets
    val userMessage: String? = null,
    val customApiKey: String = "",
    val showApiKeyDialog: Boolean = false
)

class CareerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CareerRepository = CareerRepository(
        AppDatabase.getInstance(application).careerDao()
    )

    private val _uiState = MutableStateFlow(CareerUiState())
    val uiState: StateFlow<CareerUiState> = _uiState.asStateFlow()

    val savedRoadmaps: StateFlow<List<CareerRoadmap>> = repository.getSavedRoadmaps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-seed sample roadmaps if database is brand new so user has immediate rich samples
        viewModelScope.launch {
            savedRoadmaps.collect { list ->
                if (list.isEmpty()) {
                    DefaultRoadmaps.sampleRoadmaps.forEach { sample ->
                        repository.saveRoadmap(sample)
                    }
                }
            }
        }
    }

    fun updateUserSkills(skills: String) {
        _uiState.update { it.copy(userSkills = skills) }
    }

    fun updateUserInterests(interests: String) {
        _uiState.update { it.copy(userInterests = interests) }
    }

    fun updateTargetRole(role: String) {
        _uiState.update { it.copy(targetRole = role) }
    }

    fun updateExperienceLevel(level: String) {
        _uiState.update { it.copy(experienceLevel = level) }
    }

    fun updateWeeklyHours(hours: Int) {
        _uiState.update { it.copy(weeklyHours = hours) }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun updateCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun addQuickSkill(skill: String) {
        val current = _uiState.value.userSkills.trim()
        val updated = if (current.isEmpty()) skill else "$current, $skill"
        _uiState.update { it.copy(userSkills = updated) }
    }

    fun addQuickInterest(interest: String) {
        val current = _uiState.value.userInterests.trim()
        val updated = if (current.isEmpty()) interest else "$current, $interest"
        _uiState.update { it.copy(userInterests = updated) }
    }

    fun applyPreset(role: String, skills: String, interests: String) {
        _uiState.update {
            it.copy(
                targetRole = role,
                userSkills = skills,
                userInterests = interests,
                selectedTab = 0
            )
        }
    }

    fun selectRoadmap(roadmap: CareerRoadmap) {
        _uiState.update {
            it.copy(
                activeRoadmap = roadmap,
                selectedTab = 1
            )
        }
    }

    fun generateRoadmap() {
        val skills = _uiState.value.userSkills.trim()
        val interests = _uiState.value.userInterests.trim()
        val role = _uiState.value.targetRole.trim()

        if (role.isEmpty()) {
            _uiState.update { it.copy(userMessage = "Please enter your target goal or dream role.") }
            return
        }

        _uiState.update { it.copy(isGenerating = true, userMessage = null) }

        viewModelScope.launch {
            val result = repository.generateRoadmap(
                userSkills = if (skills.isEmpty()) "Foundational curiosity & general computer literacy" else skills,
                userInterests = if (interests.isEmpty()) "Modern technology, impactful software" else interests,
                targetRole = role,
                customApiKey = _uiState.value.customApiKey
            )

            result.onSuccess { roadmap ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        activeRoadmap = roadmap,
                        selectedTab = 1,
                        userMessage = "Career Roadmap generated successfully!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        userMessage = "Generation notice: ${err.message}"
                    )
                }
            }
        }
    }

    fun toggleBookmark(roadmap: CareerRoadmap) {
        viewModelScope.launch {
            repository.toggleBookmark(roadmap.id, roadmap.isBookmarked)
            _uiState.update { state ->
                if (state.activeRoadmap?.id == roadmap.id) {
                    state.copy(activeRoadmap = state.activeRoadmap.copy(isBookmarked = !roadmap.isBookmarked))
                } else {
                    state
                }
            }
        }
    }

    fun toggleSkillCompletion(phaseId: String, skillName: String, isTech: Boolean) {
        val active = _uiState.value.activeRoadmap ?: return
        val itemKey = "${phaseId}_${if (isTech) "tech" else "soft"}_$skillName"

        viewModelScope.launch {
            val updatedSet = repository.toggleSkillCompletion(
                roadmapId = active.id,
                skillKey = itemKey,
                currentCompletedItems = active.completedItemIds
            )
            _uiState.update { state ->
                state.copy(
                    activeRoadmap = state.activeRoadmap?.copy(completedItemIds = updatedSet)
                )
            }
        }
    }

    fun deleteRoadmap(id: Long) {
        viewModelScope.launch {
            repository.deleteRoadmap(id)
            _uiState.update { state ->
                val nextActive = if (state.activeRoadmap?.id == id) null else state.activeRoadmap
                val nextTab = if (state.activeRoadmap?.id == id) 2 else state.selectedTab
                state.copy(activeRoadmap = nextActive, selectedTab = nextTab)
            }
        }
    }

    fun isApiKeyActive(): Boolean {
        return _uiState.value.customApiKey.isNotBlank() ||
               (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
    }
}
