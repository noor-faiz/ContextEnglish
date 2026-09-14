# ContextEnglish

A passive, contextual English-learning web application built with Java 26,
Spring Boot 4.1, Thymeleaf, and PostgreSQL (via Supabase). Learners read short
passages with difficult words/idioms highlighted, and infer meaning from
context instead of memorizing definitions — answering by multiple choice,
free text, or multi-select, with instant explanations (optionally in Bangla
or another native language).

---

## What's already wired up

- Registration/login (Spring Security, session-based), roles: LEARNER / ADMIN
- Onboarding: preferences + a short placement test that assigns a starting level
- The core contextual learning loop: highlighted passages, click-to-answer,
  rule-based evaluation engine (exact / synonym / keyword / category matching —
  no external NLP), instant feedback with native-language explanations
- Adaptive difficulty that nudges the learner's level after each session
- Progress dashboard, vocabulary tracker, weak-area breakdown, achievements
- A daily "quick practice" feed (short MICRO passages)
- Full admin console: create/edit passages and their target items/answers,
  manage users
- Sample data seeded automatically on first run: 7 story passages + 2 quick-
  feed cards across all three levels, a 10-question placement test, 6
  achievements, and one admin account


## Running locally

You need JDK 26 and Maven installed (or just use the Dockerfile — see below).

Your real Supabase credentials are already filled in at
`src/main/resources/application-local.properties`, which is loaded
automatically (`spring.profiles.active=local` is the default). **This file is
listed in `.gitignore` — never commit it or push it to a public repository.**

```bash
mvn spring-boot:run
```

The app will start on **http://localhost:8080**. On first run, Hibernate will
auto-create all tables in your Supabase Postgres database (`ddl-auto=update`)
and the seed data described above will be inserted.

### Running with Docker locally

```bash
docker build -t contextenglish .
docker run -p 8080:8080 \
  -e SUPABASE_DB_URL="jdbc:postgresql://db.cvyddsbdynqnzutyfnsc.supabase.co:5432/postgres" \
  -e SUPABASE_DB_USERNAME="postgres" \
  -e SUPABASE_DB_PASSWORD="#12345678SeuCse#" \
  contextenglish
```

(`application-local.properties` is excluded from the Docker build context via
`.dockerignore`, so when running via Docker you always need to pass these as
environment variables — same as you will on Render.)

---
## Project structure

```
src/main/java/com/contextenglish/
├── entity/          JPA entities (User, Passage, TargetItem, AcceptableAnswer, ...)
├── entity/enums/     Role, LevelType, ContentType, ItemType, AnswerMode, AnswerType, AnswerStatus, AchievementCriteria
├── repository/       Spring Data JPA repositories
├── service/          Business logic — EvaluationService is the answer-matching engine,
│                     ContentSelectionService picks passages, AdaptiveDifficultyService
│                     adjusts level, ProgressService ties it all together
├── controller/web/   Thymeleaf page controllers (+ web/admin for the admin console)
├── controller/api/   JSON REST endpoints consumed by the frontend JS
├── dto/              Request/response objects
├── security/         Spring Security wiring (custom UserDetailsService, login success handler)
├── config/           SecurityConfig, WebConfig
├── exception/        Custom exceptions + centralized API error handling
├── seed/             DataSeeder — populates demo content on first run
└── util/             TextNormalizer (evaluation engine helper), ScoreCalculator

src/main/resources/
├── templates/        Thymeleaf views (fragments/ for navbar, footer)
├── static/css/       Design system (style.css)
├── static/js/        session.js (learning loop), admin-content.js (content editor)
└── static/images/    logo.svg
```
  the same passage aren't specially handled.
- No automated tests are included beyond the placeholder test scaffolding
  Spring Initializr generates — worth adding unit tests for
  `EvaluationService` and `AdaptiveDifficultyService` if this becomes a
  larger project.
