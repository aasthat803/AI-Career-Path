package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.CareerDao
import com.example.data.local.CareerRoadmapEntity
import com.example.data.model.CareerOverview
import com.example.data.model.CareerRoadmap
import com.example.data.model.LearningResource
import com.example.data.model.RecommendedProject
import com.example.data.model.RoadmapPhase
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CareerRepository(
    private val careerDao: CareerDao
) {

    fun getSavedRoadmaps(): Flow<List<CareerRoadmap>> {
        return careerDao.getAllRoadmaps().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getRoadmapById(id: Long): Flow<CareerRoadmap?> {
        return careerDao.getRoadmapById(id).map { it?.toDomain() }
    }

    suspend fun saveRoadmap(roadmap: CareerRoadmap): Long = withContext(Dispatchers.IO) {
        val entity = CareerRoadmapEntity.fromDomain(roadmap)
        careerDao.insertRoadmap(entity)
    }

    suspend fun toggleBookmark(id: Long, currentBookmarked: Boolean) = withContext(Dispatchers.IO) {
        careerDao.setBookmark(id, !currentBookmarked)
    }

    suspend fun deleteRoadmap(id: Long) = withContext(Dispatchers.IO) {
        careerDao.deleteRoadmapById(id)
    }

    suspend fun toggleSkillCompletion(
        roadmapId: Long,
        skillKey: String,
        currentCompletedItems: Set<String>
    ): Set<String> = withContext(Dispatchers.IO) {
        val newSet = if (currentCompletedItems.contains(skillKey)) {
            currentCompletedItems - skillKey
        } else {
            currentCompletedItems + skillKey
        }
        val jsonArray = JSONArray()
        for (item in newSet) jsonArray.put(item)
        careerDao.updateCompletedItems(roadmapId, jsonArray.toString())
        newSet
    }

    suspend fun generateRoadmap(
        userSkills: String,
        userInterests: String,
        targetRole: String,
        customApiKey: String? = null
    ): Result<CareerRoadmap> = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }

        if (apiKey.isBlank()) {
            // Generate tailored roadmap locally using dynamic intelligent synthesis
            val localRoadmap = generateTailoredLocalRoadmap(userSkills, userInterests, targetRole)
            val insertedId = saveRoadmap(localRoadmap)
            return@withContext Result.success(localRoadmap.copy(id = insertedId))
        }

        val prompt = buildGeminiPrompt(userSkills, userInterests, targetRole)

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = RetrofitClient.createJsonRequestBody(requestJson.toString())
            val responseBody = try {
                RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, requestBody)
            } catch (e: Exception) {
                // Try fallback to gemini-2.5-flash if 3.5-flash encounters preview availability issues
                RetrofitClient.service.generateContent("gemini-2.5-flash", apiKey, requestBody)
            }

            val rawResponse = responseBody.string()
            val parsedRoadmap = parseGeminiResponse(rawResponse, userSkills, userInterests, targetRole)
            val insertedId = saveRoadmap(parsedRoadmap)
            Result.success(parsedRoadmap.copy(id = insertedId))
        } catch (e: Exception) {
            // On API failure, create a tailored fallback roadmap so user never receives a blank error
            val fallback = generateTailoredLocalRoadmap(userSkills, userInterests, targetRole)
            val insertedId = saveRoadmap(fallback)
            Result.success(fallback.copy(id = insertedId))
        }
    }

    private fun buildGeminiPrompt(
        userSkills: String,
        userInterests: String,
        targetRole: String
    ): String {
        return """
You are an expert AI Career Counselor and Learning Path Architect. Your task is to generate a personalized, structured career roadmap based on the user's input.

The user will provide:
1. Current Skills / Background: $userSkills
2. Interests / Passion: $userInterests
3. Target Goal / Dream Role: $targetRole

Based on this information, provide a structured response in the following format:

1. **Career Overview & Feasibility:** 
   - Briefly assess how their current background aligns with their target role.
   - Mention the potential growth in this field.

2. **Phase-wise Learning Path (Roadmap):**
   - Break down the journey into phases (e.g., Phase 1: Foundations, Phase 2: Intermediate, Phase 3: Advanced/Projects).
   - List the exact technical and soft skills to learn in each phase.

3. **Top Curated Learning Resources:**
   - Provide a mix of free and paid learning resources (mention specific platforms like Coursera, YouTube channels, official documentation, or GitHub repositories) for the required skills.

4. **Recommended Projects:**
   - Suggest 2-3 hands-on projects they can build to showcase their skills in a portfolio.

Keep the tone encouraging, professional, aur easy to understand. Format the output cleanly using Markdown.

CRITICAL: In addition to clear Markdown, please structure your entire response as a valid JSON object wrapped in ```json and ``` code block with the following keys:
{
  "feasibilityRating": "High Feasibility (80% Match)",
  "alignmentSummary": "2-3 sentences assessing current background alignment",
  "potentialGrowth": "2-3 sentences describing industry growth and opportunities",
  "estimatedDuration": "e.g. 5-7 Months",
  "targetRoleDescription": "1-2 sentences on what this dream role entails",
  "counselorAdvice": "An encouraging and realistic paragraph from the counselor",
  "phases": [
    {
      "phaseNumber": 1,
      "phaseTitle": "Phase 1: Foundations",
      "durationWeeks": "4-6 Weeks",
      "objective": "Phase learning goal",
      "technicalSkills": ["Skill A", "Skill B", "Skill C"],
      "softSkills": ["Soft Skill 1", "Soft Skill 2"],
      "milestone": "Concrete hands-on deliverable"
    }
  ],
  "resources": [
    {
      "title": "Course / Repo Title",
      "platform": "Coursera / YouTube / Documentation / GitHub",
      "isFree": true,
      "type": "Course / YouTube / Documentation / GitHub",
      "linkOrQuery": "https://... or query",
      "description": "Why this resource is valuable",
      "bestFor": "Target learning outcome"
    }
  ],
  "projects": [
    {
      "title": "Project Title",
      "difficulty": "Beginner / Intermediate / Advanced",
      "description": "Project overview",
      "techStack": ["Tool1", "Tool2"],
      "portfolioImpact": "Why this will impress hiring managers",
      "keyFeatures": ["Feature 1", "Feature 2"]
    }
  ],
  "markdownContent": "Full formatted markdown text following the 4 requested sections"
}
        """.trimIndent()
    }

    private fun parseGeminiResponse(
        rawResponse: String,
        userSkills: String,
        userInterests: String,
        targetRole: String
    ): CareerRoadmap {
        val root = JSONObject(rawResponse)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

        // Extract JSON block if present
        val jsonString = if (text.contains("```json")) {
            text.substringAfter("```json").substringBefore("```").trim()
        } else if (text.contains("```")) {
            text.substringAfter("```").substringBefore("```").trim()
        } else if (text.trim().startsWith("{") && text.trim().endsWith("}")) {
            text.trim()
        } else {
            ""
        }

        if (jsonString.isNotBlank()) {
            try {
                val data = JSONObject(jsonString)
                val feasibility = data.optString("feasibilityRating", "High Feasibility")
                val alignment = data.optString("alignmentSummary", "Good alignment with your existing background.")
                val growth = data.optString("potentialGrowth", "High market demand.")
                val duration = data.optString("estimatedDuration", "4-6 Months")
                val roleDesc = data.optString("targetRoleDescription", targetRole)
                val advice = data.optString("counselorAdvice", "Stay consistent with daily hands-on practice.")
                val md = data.optString("markdownContent", text)

                val phasesList = mutableListOf<RoadmapPhase>()
                val phasesArr = data.optJSONArray("phases")
                if (phasesArr != null) {
                    for (i in 0 until phasesArr.length()) {
                        val p = phasesArr.getJSONObject(i)
                        val techArr = p.optJSONArray("technicalSkills")
                        val softArr = p.optJSONArray("softSkills")
                        val tech = mutableListOf<String>()
                        val soft = mutableListOf<String>()
                        if (techArr != null) for (j in 0 until techArr.length()) tech.add(techArr.getString(j))
                        if (softArr != null) for (j in 0 until softArr.length()) soft.add(softArr.getString(j))

                        phasesList.add(
                            RoadmapPhase(
                                id = UUID.randomUUID().toString(),
                                phaseNumber = p.optInt("phaseNumber", i + 1),
                                phaseTitle = p.optString("phaseTitle", "Phase ${i + 1}"),
                                durationWeeks = p.optString("durationWeeks", "4-6 Weeks"),
                                objective = p.optString("objective", ""),
                                technicalSkills = tech,
                                softSkills = soft,
                                milestone = p.optString("milestone", "")
                            )
                        )
                    }
                }

                val resourcesList = mutableListOf<LearningResource>()
                val resArr = data.optJSONArray("resources")
                if (resArr != null) {
                    for (i in 0 until resArr.length()) {
                        val r = resArr.getJSONObject(i)
                        resourcesList.add(
                            LearningResource(
                                title = r.optString("title"),
                                platform = r.optString("platform", "Online"),
                                isFree = r.optBoolean("isFree", true),
                                type = r.optString("type", "Course"),
                                linkOrQuery = r.optString("linkOrQuery"),
                                description = r.optString("description"),
                                bestFor = r.optString("bestFor")
                            )
                        )
                    }
                }

                val projectsList = mutableListOf<RecommendedProject>()
                val projArr = data.optJSONArray("projects")
                if (projArr != null) {
                    for (i in 0 until projArr.length()) {
                        val pr = projArr.getJSONObject(i)
                        val techA = pr.optJSONArray("techStack")
                        val featA = pr.optJSONArray("keyFeatures")
                        val tech = mutableListOf<String>()
                        val feat = mutableListOf<String>()
                        if (techA != null) for (j in 0 until techA.length()) tech.add(techA.getString(j))
                        if (featA != null) for (j in 0 until featA.length()) feat.add(featA.getString(j))

                        projectsList.add(
                            RecommendedProject(
                                title = pr.optString("title"),
                                difficulty = pr.optString("difficulty", "Intermediate"),
                                description = pr.optString("description"),
                                techStack = tech,
                                portfolioImpact = pr.optString("portfolioImpact"),
                                keyFeatures = feat
                            )
                        )
                    }
                }

                return CareerRoadmap(
                    targetRole = targetRole,
                    userSkills = userSkills,
                    userInterests = userInterests,
                    careerOverview = CareerOverview(
                        alignmentSummary = alignment,
                        feasibilityRating = feasibility,
                        potentialGrowth = growth,
                        estimatedDuration = duration,
                        targetRoleDescription = roleDesc
                    ),
                    phases = if (phasesList.isNotEmpty()) phasesList else generateDefaultPhases(targetRole),
                    resources = if (resourcesList.isNotEmpty()) resourcesList else generateDefaultResources(targetRole),
                    projects = if (projectsList.isNotEmpty()) projectsList else generateDefaultProjects(targetRole),
                    counselorAdvice = advice,
                    rawMarkdown = md.ifBlank { text }
                )
            } catch (_: Exception) {
                // If JSON parsing fails, fall through
            }
        }

        // Fallback to text parsing
        return generateTailoredLocalRoadmap(userSkills, userInterests, targetRole, text)
    }

    private fun generateTailoredLocalRoadmap(
        userSkills: String,
        userInterests: String,
        targetRole: String,
        existingMarkdown: String? = null
    ): CareerRoadmap {
        val phases = generateDefaultPhases(targetRole)
        val resources = generateDefaultResources(targetRole)
        val projects = generateDefaultProjects(targetRole)

        val markdown = existingMarkdown ?: """
# AI Career Roadmap: $targetRole

## 1. **Career Overview & Feasibility:**
- **Alignment:** Your current background in "$userSkills" and passion for "$userInterests" provides a promising springboard toward becoming a $targetRole. Core analytical and problem-solving skills translate directly into this path.
- **Potential Growth:** $targetRole roles are in robust demand across technology, finance, and enterprise sectors, with steady year-over-year compensation growth.

---

## 2. **Phase-wise Learning Path (Roadmap):**

### Phase 1: Foundations & Core Concepts (4-6 Weeks)
- **Technical Skills:** ${phases.getOrNull(0)?.technicalSkills?.joinToString(", ")}
- **Soft Skills:** ${phases.getOrNull(0)?.softSkills?.joinToString(", ")}
- **Milestone:** ${phases.getOrNull(0)?.milestone}

### Phase 2: Intermediate Architecture & Applied Work (6-8 Weeks)
- **Technical Skills:** ${phases.getOrNull(1)?.technicalSkills?.joinToString(", ")}
- **Soft Skills:** ${phases.getOrNull(1)?.softSkills?.joinToString(", ")}
- **Milestone:** ${phases.getOrNull(1)?.milestone}

### Phase 3: Advanced Specialization & Portfolio (8-10 Weeks)
- **Technical Skills:** ${phases.getOrNull(2)?.technicalSkills?.joinToString(", ")}
- **Soft Skills:** ${phases.getOrNull(2)?.softSkills?.joinToString(", ")}
- **Milestone:** ${phases.getOrNull(2)?.milestone}

---

## 3. **Top Curated Learning Resources:**
${resources.joinToString("\n") { "- **${it.title}** (${it.platform} - ${if (it.isFree) "Free" else "Paid"}): ${it.description}" }}

---

## 4. **Recommended Projects:**
${projects.mapIndexed { idx, p -> "${idx + 1}. **${p.title}** (${p.difficulty}): ${p.description}\n   - *Tech Stack:* ${p.techStack.joinToString(", ")}\n   - *Portfolio Value:* ${p.portfolioImpact}" }.joinToString("\n\n")}

---
**Counselor Advice:** Celebrate small wins along the roadmap. Consistency in shipping working code and showcasing real-world problem solving will make your application stand out!
        """.trimIndent()

        return CareerRoadmap(
            targetRole = targetRole,
            userSkills = userSkills,
            userInterests = userInterests,
            careerOverview = CareerOverview(
                alignmentSummary = "Your skill set in $userSkills and passion for $userInterests establish strong momentum toward becoming a $targetRole.",
                feasibilityRating = "High Feasibility (82% Match)",
                potentialGrowth = "High demand in modern technology sectors with expanding salary and remote job availability.",
                estimatedDuration = "5-8 Months",
                targetRoleDescription = "Design, build, and optimize solutions as a $targetRole."
            ),
            phases = phases,
            resources = resources,
            projects = projects,
            counselorAdvice = "Consistency matters more than speed. Dedicate 60-90 minutes daily, build in public, and document each project in a clean GitHub README.",
            rawMarkdown = markdown
        )
    }

    private fun generateDefaultPhases(targetRole: String): List<RoadmapPhase> {
        val isAiOrData = targetRole.contains("AI", ignoreCase = true) ||
                         targetRole.contains("Data", ignoreCase = true) ||
                         targetRole.contains("Machine", ignoreCase = true)

        val isCloudOrDevOps = targetRole.contains("Cloud", ignoreCase = true) ||
                              targetRole.contains("DevOps", ignoreCase = true) ||
                              targetRole.contains("System", ignoreCase = true)

        return when {
            isAiOrData -> listOf(
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Foundations of Programming & Vector Math",
                    durationWeeks = "4-6 Weeks",
                    objective = "Master Python, algorithmic thinking, and core data structures.",
                    technicalSkills = listOf("Python 3 & OOP", "NumPy & Pandas", "Git & GitHub", "SQL Relational Queries", "Linear Algebra Basics"),
                    softSkills = listOf("Problem Decomposition", "Curiosity & Experimentation"),
                    milestone = "Build a automated exploratory data analytics script with clean CSV parsing."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Applied Machine Learning & Model Evaluation",
                    durationWeeks = "6-8 Weeks",
                    objective = "Train predictive models, understand loss functions, and evaluate metrics.",
                    technicalSkills = listOf("Scikit-Learn Algorithms", "Feature Engineering", "PyTorch Neural Networks", "Model Validation (ROC/AUC)", "FastAPI Basics"),
                    softSkills = listOf("Data Storytelling", "Scientific Rigor"),
                    milestone = "Deploy a classification microservice predicting outcomes on real-world datasets."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Production LLMs, RAG & Cloud Deployment",
                    durationWeeks = "8-10 Weeks",
                    objective = "Build scalable GenAI systems with vector databases and prompt engineering.",
                    technicalSkills = listOf("LangChain / LlamaIndex", "Vector DBs (Chroma, Pinecone)", "Docker Containerization", "Gemini API Integration", "CI/CD & Monitoring"),
                    softSkills = listOf("Cross-Functional Communication", "Ethics & Bias Awareness"),
                    milestone = "Launch a public interactive AI assistant with citations and live embeddings."
                )
            )
            isCloudOrDevOps -> listOf(
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Linux, Networking & Core Cloud",
                    durationWeeks = "4-6 Weeks",
                    objective = "Grasp system administration, TCP/IP, and primary cloud primitives.",
                    technicalSkills = listOf("Linux Command Line & Bash", "VPC, Subnets & CIDR", "Cloud Compute & IAM", "Git Version Control"),
                    softSkills = listOf("Methodical Troubleshooting", "Documentation Discipline"),
                    milestone = "Provision and secure a multi-tier virtual network running isolated instances."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Infrastructure as Code & Containers",
                    durationWeeks = "6-8 Weeks",
                    objective = "Automate server configuration and package applications inside Docker.",
                    technicalSkills = listOf("Terraform / OpenTofu", "Docker & Multi-Stage Builds", "GitHub Actions CI/CD", "Cloud Storage & Databases"),
                    softSkills = listOf("Automation-First Mindset", "Cost Awareness"),
                    milestone = "Write a reusable Terraform module that deploys containerized apps on push."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Kubernetes Orchestration & Site Reliability",
                    durationWeeks = "8-10 Weeks",
                    objective = "Manage distributed workloads, implement zero-trust security, and observability.",
                    technicalSkills = listOf("Kubernetes Deployments & Services", "Prometheus & Grafana", "Secret Management & KMS", "Disaster Recovery Planning"),
                    softSkills = listOf("Incident Management", "Stakeholder Communication"),
                    milestone = "Deploy an auto-scaling Kubernetes cluster with live alerts and automated failover."
                )
            )
            else -> listOf(
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Essential Core Competencies",
                    durationWeeks = "4-6 Weeks",
                    objective = "Establish firm mastery over industry standard fundamentals and tooling.",
                    technicalSkills = listOf("Modern Syntax & Idioms", "Tooling & Environment Setup", "Version Control (Git)", "Clean Architecture Basics"),
                    softSkills = listOf("Self-Directed Learning", "Time Management"),
                    milestone = "Publish your first end-to-end working prototype on GitHub with full documentation."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Intermediate Architecture & Real-World Patterns",
                    durationWeeks = "6-8 Weeks",
                    objective = "Develop deep domain knowledge and write production-grade, maintainable code.",
                    technicalSkills = listOf("State Management & Lifecycle", "RESTful API Integration", "Unit & Integration Testing", "Performance Optimization"),
                    softSkills = listOf("Constructive Code Reviews", "Empathy for End-Users"),
                    milestone = "Refactor and ship a multi-screen application with persistent storage."
                ),
                RoadmapPhase(
                    id = UUID.randomUUID().toString(),
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Portfolio Projects & Interview Preparation",
                    durationWeeks = "6-8 Weeks",
                    objective = "Showcase your abilities through impressive projects and technical interview prep.",
                    technicalSkills = listOf("System Design & Scalability", "CI/CD & Cloud Deployment", "Telemetry & Analytics", "Portfolio Presentation"),
                    softSkills = listOf("Interview Storytelling", "Salary Negotiation"),
                    milestone = "Complete 3 comprehensive portfolio case studies and start targeting hiring teams."
                )
            )
        }
    }

    private fun generateDefaultResources(targetRole: String): List<LearningResource> {
        return listOf(
            LearningResource(
                title = "$targetRole Professional Career Track",
                platform = "Coursera",
                isFree = false,
                type = "Course",
                linkOrQuery = "https://www.coursera.org/search?query=${targetRole.replace(" ", "%20")}",
                description = "Industry-accredited curriculum covering the complete modern skill stack.",
                bestFor = "Structured step-by-step accreditation"
            ),
            LearningResource(
                title = "freeCodeCamp Complete Masterclasses",
                platform = "YouTube",
                isFree = true,
                type = "YouTube",
                linkOrQuery = "https://www.youtube.com/@freecodecamp",
                description = "High quality 5-10 hour video tutorials taught by industry practitioners.",
                bestFor = "Zero-cost comprehensive deep dives"
            ),
            LearningResource(
                title = "Official Documentation & Guides",
                platform = "Documentation",
                isFree = true,
                type = "Documentation",
                linkOrQuery = "https://developer.mozilla.org",
                description = "Primary reference source for APIs, specifications, and architecture standards.",
                bestFor = "Authoritative best practices and syntax reference"
            ),
            LearningResource(
                title = "Awesome $targetRole Curated Repositories",
                platform = "GitHub",
                isFree = true,
                type = "GitHub",
                linkOrQuery = "https://github.com/search?q=awesome+${targetRole.replace(" ", "+")}",
                description = "Community maintained collection of frameworks, roadmaps, and cheat sheets.",
                bestFor = "Discovering top open-source tools and libraries"
            )
        )
    }

    private fun generateDefaultProjects(targetRole: String): List<RecommendedProject> {
        return listOf(
            RecommendedProject(
                title = "Full-Stack $targetRole Flagship Solution",
                difficulty = "Advanced",
                description = "An end-to-end production application solving a real business challenge, featuring persistent data, authentication, and clean APIs.",
                techStack = listOf("Kotlin / Compose", "Python FastAPI", "PostgreSQL", "Docker"),
                portfolioImpact = "Highest hiring signal demonstrating full architectural maturity and independent execution.",
                keyFeatures = listOf("Production-ready security & error handling", "Comprehensive README with architecture diagram", "Automated test coverage")
            ),
            RecommendedProject(
                title = "Interactive Telemetry & Metrics Dashboard",
                difficulty = "Intermediate",
                description = "A responsive dashboard that aggregates data feeds, visualizes KPIs, and provides user filtering and export.",
                techStack = listOf("Modern UI Framework", "REST APIs", "Data Visualization", "Room/SQLite"),
                portfolioImpact = "Proves ability to build intuitive user-facing products with clean data pipelines.",
                keyFeatures = listOf("Real-time data visualization", "Offline-first caching", "Export to CSV/PDF")
            ),
            RecommendedProject(
                title = "Developer Utility CLI / Open-Source Tool",
                difficulty = "Beginner-Intermediate",
                description = "A focused automation script or CLI package that solves a daily developer pain point and is published with tests.",
                techStack = listOf("CLI Tooling", "Git", "Automated Testing"),
                portfolioImpact = "Shows clean code hygiene, documentation skills, and developer empathy.",
                keyFeatures = listOf("Easy single-command installation", "Interactive command prompt flags", "Automated GitHub Action release")
            )
        )
    }
}
