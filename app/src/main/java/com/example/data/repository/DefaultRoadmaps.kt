package com.example.data.repository

import com.example.data.model.CareerOverview
import com.example.data.model.CareerRoadmap
import com.example.data.model.LearningResource
import com.example.data.model.RecommendedProject
import com.example.data.model.RoadmapPhase

object DefaultRoadmaps {

    val sampleRoadmaps: List<CareerRoadmap> = listOf(
        createAiEngineerRoadmap(),
        createCloudArchitectRoadmap(),
        createUiUxEngineerRoadmap()
    )

    fun createAiEngineerRoadmap(): CareerRoadmap {
        val phases = listOf(
            RoadmapPhase(
                id = "ai_eng_p1",
                phaseNumber = 1,
                phaseTitle = "Phase 1: Foundations of Programming & Data",
                durationWeeks = "6-8 Weeks",
                objective = "Master core Python programming, vector math, and essential data manipulation libraries.",
                technicalSkills = listOf(
                    "Python OOP & Data Structures",
                    "NumPy & Vectorized Math",
                    "Pandas Data Wrangling",
                    "Git & GitHub Version Control",
                    "Linear Algebra & Statistics Basics"
                ),
                softSkills = listOf(
                    "Algorithmic Thinking",
                    "Problem Deconstruction",
                    "Clear Technical Documentation"
                ),
                milestone = "Build an automated exploratory data analysis CLI tool with visual reporting."
            ),
            RoadmapPhase(
                id = "ai_eng_p2",
                phaseNumber = 2,
                phaseTitle = "Phase 2: Classical Machine Learning & Neural Networks",
                durationWeeks = "8-10 Weeks",
                objective = "Train predictive models, understand loss functions, and transition to deep learning with PyTorch.",
                technicalSkills = listOf(
                    "Scikit-Learn (Classification, Regression, Trees)",
                    "PyTorch Tensors & Training Loops",
                    "Feature Engineering & Cross Validation",
                    "Model Evaluation Metrics (F1, AUC-ROC)",
                    "SQL & Relational Databases"
                ),
                softSkills = listOf(
                    "Analytical Storytelling",
                    "Experimental Rigor",
                    "Prioritization of Business Impact"
                ),
                milestone = "Deploy a machine learning API service that predicts customer churn with 85%+ accuracy."
            ),
            RoadmapPhase(
                id = "ai_eng_p3",
                phaseNumber = 3,
                phaseTitle = "Phase 3: Generative AI, LLMs & Production Deployment",
                durationWeeks = "8-12 Weeks",
                objective = "Build production-grade GenAI applications using LangChain, RAG, embeddings, and FastAPI.",
                technicalSkills = listOf(
                    "Retrieval Augmented Generation (RAG) Architecture",
                    "Vector Databases (Pinecone, ChromaDB)",
                    "FastAPI & Docker Containerization",
                    "Gemini API & Prompt Engineering",
                    "Model Monitoring & Latency Optimization"
                ),
                softSkills = listOf(
                    "Product Empathy & User Feedback",
                    "Collaboration with Product Managers",
                    "Continuous Learning & Adaptation"
                ),
                milestone = "Deploy a full-stack AI multimodal knowledge assistant on cloud infrastructure."
            )
        )

        val resources = listOf(
            LearningResource(
                title = "DeepLearning.AI Machine Learning Specialization",
                platform = "Coursera",
                isFree = false,
                type = "Course",
                linkOrQuery = "https://www.coursera.org/specializations/machine-learning-introduction",
                description = "Led by Andrew Ng, covers modern machine learning algorithms and best practices.",
                bestFor = "Core ML intuition and mathematical grounding"
            ),
            LearningResource(
                title = "Fast.ai Practical Deep Learning for Coders",
                platform = "Official Site / YouTube",
                isFree = true,
                type = "Course",
                linkOrQuery = "https://course.fast.ai",
                description = "Top-down, hands-on deep learning course using PyTorch with zero fluff.",
                bestFor = "Fast practical neural network development"
            ),
            LearningResource(
                title = "StatQuest with Josh Starmer",
                platform = "YouTube",
                isFree = true,
                type = "YouTube",
                linkOrQuery = "https://www.youtube.com/@statquest",
                description = "Crystal-clear visual breakdowns of ML algorithms, trees, and neural nets.",
                bestFor = "Demystifying complex ML concepts"
            ),
            LearningResource(
                title = "Hugging Face NLP & Deep Learning Course",
                platform = "Official Documentation",
                isFree = true,
                type = "Documentation",
                linkOrQuery = "https://huggingface.co/learn",
                description = "Comprehensive tutorials on transformers, tokenizers, fine-tuning, and pipelines.",
                bestFor = "Modern Generative AI and Transformer architectures"
            ),
            LearningResource(
                title = "Full Stack LLM BootCamp GitHub Repositories",
                platform = "GitHub",
                isFree = true,
                type = "GitHub",
                linkOrQuery = "https://github.com/full-stack-deep-learning",
                description = "Production patterns for deploying and serving modern LLM systems.",
                bestFor = "Real-world engineering and architecture"
            )
        )

        val projects = listOf(
            RecommendedProject(
                title = "Multimodal Enterprise Knowledge Assistant (RAG)",
                difficulty = "Advanced",
                description = "An enterprise document assistant that ingests PDFs, indexes embeddings in a vector database, and generates contextual answers with citation links.",
                techStack = listOf("Python", "FastAPI", "Gemini API", "ChromaDB", "Docker"),
                portfolioImpact = "High hiring signal for Generative AI and modern software engineering roles.",
                keyFeatures = listOf(
                    "Hybrid search combining semantic vectors and keyword ranking",
                    "Source attribution and page-level quote verification",
                    "Asynchronous streaming response delivery"
                )
            ),
            RecommendedProject(
                title = "Real-Time Customer Sentiment & Intent Classifier",
                difficulty = "Intermediate",
                description = "An automated telemetry pipeline that ingests customer support queries, tags intent, scores urgency, and generates suggested replies.",
                techStack = listOf("Python", "Scikit-Learn", "FastAPI", "PostgreSQL", "Streamlit"),
                portfolioImpact = "Demonstrates business ROI and practical ML pipeline engineering.",
                keyFeatures = listOf(
                    "Interactive dashboard for support agents",
                    "Automated classification confidence metrics",
                    "CSV batch export and live webhook integration"
                )
            ),
            RecommendedProject(
                title = "Intelligent Expense Auditor & OCR Extractor",
                difficulty = "Beginner-Intermediate",
                description = "Receipt parser that extracts dates, line items, and totals from image receipts using vision AI and flags anomalies.",
                techStack = listOf("Python", "Gemini Vision", "SQLite", "FastAPI"),
                portfolioImpact = "Shows end-to-end integration of multimodal AI with clean database design.",
                keyFeatures = listOf(
                    "Automatic categorization into standard tax buckets",
                    "Duplicate receipt detection algorithm",
                    "Clean REST API endpoints"
                )
            )
        )

        val markdown = """
# AI Career Roadmap: AI & Machine Learning Engineer

## 1. Career Overview & Feasibility
- **Alignment:** Strong alignment! Your analytical foundation and coding curiosity provide an ideal starting pad for modern AI engineering. Transferable skills in logic and problem decomposition will accelerate your progress through data pipelines and model evaluation.
- **Growth & Outlook:** The AI sector is experiencing explosive growth (+35% annual job demand globally). High demand spans autonomous agents, enterprise LLM integrations, and intelligent automation.

---

## 2. Phase-wise Learning Path (Roadmap)

### Phase 1: Foundations of Programming & Data (6-8 Weeks)
- **Technical Skills:** Python OOP, NumPy, Pandas, Git/GitHub, Linear Algebra & Statistics Basics.
- **Soft Skills:** Algorithmic thinking, problem deconstruction, clean technical documentation.
- **Milestone Project:** Build an automated exploratory data analysis CLI tool with visual reporting.

### Phase 2: Classical Machine Learning & Neural Networks (8-10 Weeks)
- **Technical Skills:** Scikit-Learn (Regression, Trees), PyTorch Tensors & Training Loops, Feature Engineering, SQL.
- **Soft Skills:** Experimental rigor, analytical storytelling, prioritizing business impact.
- **Milestone Project:** Deploy a customer churn prediction API service with 85%+ accuracy.

### Phase 3: Generative AI, LLMs & Production Deployment (8-12 Weeks)
- **Technical Skills:** Retrieval-Augmented Generation (RAG), Vector Databases (ChromaDB), FastAPI, Docker, Gemini API.
- **Soft Skills:** Product empathy, cross-functional collaboration, continuous adaptation.
- **Milestone Project:** Deploy a full-stack AI multimodal knowledge assistant on cloud infrastructure.

---

## 3. Top Curated Learning Resources
- **Coursera - DeepLearning.AI ML Specialization** (Paid / Audit Free): Core ML intuition and mathematical grounding.
- **Fast.ai Practical Deep Learning for Coders** (Free): Hands-on PyTorch engineering.
- **StatQuest with Josh Starmer** (Free on YouTube): Crystal-clear visual breakdowns of ML algorithms.
- **Hugging Face Official Course** (Free Docs): Deep dive into transformers and tokenizers.
- **Full Stack Deep Learning** (Free GitHub): Production engineering and deployment patterns.

---

## 4. Recommended Projects
1. **Multimodal Enterprise Knowledge Assistant (RAG):** Fast ingestion of company docs with semantic search and citations.
2. **Customer Sentiment & Intent Classifier:** Live telemetry service tagging incoming support tickets.
3. **Intelligent Expense Auditor & OCR Extractor:** Multimodal receipt understanding and ledger entry.

*Counselor Note:* Focus on shipping working software early rather than getting stuck in math theory. Employers hire engineers who can solve problems end-to-end!
        """.trimIndent()

        return CareerRoadmap(
            id = 1,
            targetRole = "AI & Machine Learning Engineer",
            userSkills = "Python basics, problem solving, analytical mindset",
            userInterests = "Artificial Intelligence, Automation, Deep Learning",
            careerOverview = CareerOverview(
                alignmentSummary = "Strong alignment. Your analytical background and coding interest provide the foundational building blocks for modern AI engineering.",
                feasibilityRating = "High Feasibility (85% Match)",
                potentialGrowth = "35%+ YoY growth; exceptional demand for LLM application builders and ML engineers.",
                estimatedDuration = "6-9 Months",
                targetRoleDescription = "Build, fine-tune, and deploy intelligent algorithms and Generative AI systems that automate complex decisions."
            ),
            phases = phases,
            resources = resources,
            projects = projects,
            counselorAdvice = "Focus on shipping working applications early. Build hands-on projects, push clean code to GitHub, and learn how to connect APIs to real business problems!",
            rawMarkdown = markdown,
            isBookmarked = true
        )
    }

    fun createCloudArchitectRoadmap(): CareerRoadmap {
        val phases = listOf(
            RoadmapPhase(
                id = "cloud_p1",
                phaseNumber = 1,
                phaseTitle = "Phase 1: Cloud & Networking Foundations",
                durationWeeks = "6-8 Weeks",
                objective = "Grasp TCP/IP, DNS, Linux systems administration, and core cloud primitives.",
                technicalSkills = listOf("Linux CLI & Bash Scripting", "Networking (CIDR, Subnets, VPC, DNS)", "Cloud Core (Compute, Object Storage, IAM)", "Git"),
                softSkills = listOf("Systemic Thinking", "Root-Cause Analysis"),
                milestone = "Deploy a hardened, multi-tier web application across public and private subnets."
            ),
            RoadmapPhase(
                id = "cloud_p2",
                phaseNumber = 2,
                phaseTitle = "Phase 2: Infrastructure as Code & Containers",
                durationWeeks = "8-10 Weeks",
                objective = "Automate infrastructure deployment and container orchestration.",
                technicalSkills = listOf("Terraform / OpenTofu", "Docker & Multi-stage builds", "Kubernetes Basics", "CI/CD Pipelines (GitHub Actions)"),
                softSkills = listOf("Reliability Mindset", "Cost-Conscious Architecture"),
                milestone = "Automate a zero-downtime blue/green deployment pipeline managed completely by Terraform."
            ),
            RoadmapPhase(
                id = "cloud_p3",
                phaseNumber = 3,
                phaseTitle = "Phase 3: High Availability, Security & Multi-Cloud",
                durationWeeks = "8-12 Weeks",
                objective = "Design fault-tolerant, secure, and disaster-recoverable architectures.",
                technicalSkills = listOf("Distributed Systems Design", "Disaster Recovery & Replication", "Cloud Security (Zero Trust, KMS)", "Cost Optimization & FinOps"),
                softSkills = listOf("Stakeholder Negotiation", "Risk Assessment"),
                milestone = "Deliver an end-to-end multi-region resilient architecture whitepaper and reference repo."
            )
        )

        return CareerRoadmap(
            id = 2,
            targetRole = "Cloud Solutions Architect",
            userSkills = "Basic IT knowledge, scripting, business analysis",
            userInterests = "Scalable systems, Cloud computing, Infrastructure",
            careerOverview = CareerOverview(
                alignmentSummary = "Excellent synergy. Your background in IT workflows and structured problem-solving translates smoothly into designing scalable cloud systems.",
                feasibilityRating = "High Feasibility (80% Match)",
                potentialGrowth = "Crucial enterprise demand as organizations modernize workloads to cloud-native platforms.",
                estimatedDuration = "6-8 Months",
                targetRoleDescription = "Design secure, resilient, and cost-effective cloud infrastructures that power modern digital enterprises."
            ),
            phases = phases,
            resources = listOf(
                LearningResource("AWS / Google Cloud Certified Architect Training", "Coursera / A Cloud Guru", false, "Course", "https://cloud.google.com/learn", "Comprehensive certification path.", "Architecture best practices"),
                LearningResource("TechWorld with Nana", "YouTube", true, "YouTube", "https://www.youtube.com/@TechWorldwithNana", "Top-tier DevOps and Kubernetes tutorials.", "Docker, Kubernetes, and CI/CD"),
                LearningResource("The System Design Primer", "GitHub", true, "GitHub", "https://github.com/donnemartin/system-design-primer", "Industry-standard guide for large scale distributed systems.", "Scalability and high availability")
            ),
            projects = listOf(
                RecommendedProject("Automated Multi-Region Terraform Infrastructure", "Intermediate", "Complete IaC repository provisioning VPCs, load balancers, and databases with automated failover.", listOf("Terraform", "AWS/GCP", "Bash"), "Proves IaC automation and production readiness.", listOf("Zero manual provisioning", "Automated state locking", "Secret management via KMS")),
                RecommendedProject("Kubernetes Microservices Observability Suite", "Advanced", "Deploy and instrument a microservices cluster with Prometheus, Grafana, and distributed tracing.", listOf("Kubernetes", "Prometheus", "Grafana", "Docker"), "Demonstrates production observability and troubleshooting.", listOf("Auto-scaling rules based on traffic", "Custom SLA alert dashboard", "Health check probes"))
            ),
            counselorAdvice = "Certifications get you interviews, but working Terraform scripts on GitHub get you job offers. Focus on hands-on infrastructure as code!",
            rawMarkdown = "# Cloud Solutions Architect Career Roadmap\n\nAssess your readiness, automate infrastructure with Terraform, and master cloud security.",
            isBookmarked = false
        )
    }

    fun createUiUxEngineerRoadmap(): CareerRoadmap {
        val phases = listOf(
            RoadmapPhase(
                id = "uiux_p1",
                phaseNumber = 1,
                phaseTitle = "Phase 1: Design Systems & User Psychology",
                durationWeeks = "4-6 Weeks",
                objective = "Learn design principles, wireframing, color theory, typography, and Figma auto-layout.",
                technicalSkills = listOf("Figma Advanced (Components, Auto-Layout)", "Material Design 3 Guidelines", "Wireframing & Prototyping", "Design Accessibility (WCAG 2.1)"),
                softSkills = listOf("Empathy & Active Listening", "User Interviewing"),
                milestone = "Complete an audited interactive design system for a mobile app with WCAG compliance."
            ),
            RoadmapPhase(
                id = "uiux_p2",
                phaseNumber = 2,
                phaseTitle = "Phase 2: Modern Frontend & Interactive Prototyping",
                durationWeeks = "6-8 Weeks",
                objective = "Bring designs to life using modern declarative UI frameworks like Jetpack Compose or React.",
                technicalSkills = listOf("Jetpack Compose / React UI", "Design Tokens & Theming", "Micro-interactions & Animations", "Responsive & Adaptive Layouts"),
                softSkills = listOf("Cross-Functional Translation", "Design Critique & Feedback"),
                milestone = "Code a pixel-perfect, accessible component library published as an open source package."
            ),
            RoadmapPhase(
                id = "uiux_p3",
                phaseNumber = 3,
                phaseTitle = "Phase 3: Usability Testing, Analytics & Portfolio",
                durationWeeks = "6-8 Weeks",
                objective = "Conduct rigorous user testing, iterate based on product telemetry, and assemble a portfolio.",
                technicalSkills = listOf("Usability Testing Frameworks", "Conversion Rate Optimization (CRO)", "Interactive Web Portfolio", "Design Handoff Documentation"),
                softSkills = listOf("Product Strategy Pitching", "Storytelling & Case Study Presentation"),
                milestone = "Publish a polished 3-case-study portfolio demonstrating quantifiable user impact."
            )
        )

        return CareerRoadmap(
            id = 3,
            targetRole = "UI/UX & Creative Frontend Technologist",
            userSkills = "Graphic design, visual aesthetics, HTML/CSS basics",
            userInterests = "Product design, user experience, creative technology",
            careerOverview = CareerOverview(
                alignmentSummary = "Outstanding natural fit! Your visual eye and creative sense give you a distinctive edge over pure coders.",
                feasibilityRating = "High Feasibility (90% Match)",
                potentialGrowth = "High demand for hybrid 'Design Engineers' who bridge the gap between Figma and production code.",
                estimatedDuration = "4-6 Months",
                targetRoleDescription = "Craft intuitive user journeys and engineer responsive, delightful digital interfaces."
            ),
            phases = phases,
            resources = listOf(
                LearningResource("Google UX Design Professional Certificate", "Coursera", false, "Course", "https://www.coursera.org/professional-certificates/google-ux-design", "Foundational UX methodology from Google experts.", "UX research & wireframing"),
                LearningResource("Figma YouTube Channel & Learn Hub", "YouTube", true, "YouTube", "https://www.youtube.com/@Figma", "Official tutorials on auto-layout, design systems, and variables.", "Design tool mastery"),
                LearningResource("Refactoring UI by Adam Wathan & Steve Schoger", "Book / Web", false, "Book", "https://www.refactoringui.com", "Practical design tactics for developers.", "Visual hierarchy and polish")
            ),
            projects = listOf(
                RecommendedProject("FinTech Mobile App Redesign & Case Study", "Intermediate", "Complete redesign of a legacy banking app with user research, persona creation, and Figma prototype.", listOf("Figma", "User Research", "Prototyping"), "Shows end-to-end design thinking and user empathy.", listOf("Before & After heuristic evaluation", "High-fidelity interactive prototype", "Design system documentation")),
                RecommendedProject("Accessible Open-Source Component Library", "Intermediate-Advanced", "A production-ready declarative UI component kit adhering to Material 3 and WCAG AA standards.", listOf("Jetpack Compose", "Kotlin", "Accessibility"), "Demonstrates rare hybrid design + engineering chops.", listOf("Light & Dark mode tokens", "Screen reader TalkBack optimized", "Interactive demo app"))
            ),
            counselorAdvice = "Document your process! Hiring managers care 10x more about *why* you made a design decision and how you tested it than just pretty screenshots.",
            rawMarkdown = "# UI/UX Engineer Career Roadmap\n\nBridge the gap between design and engineering with user-centric thinking.",
            isBookmarked = false
        )
    }
}
