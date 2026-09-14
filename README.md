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

## Default admin login

A default admin account (`admin` / seeded password) is created on first run.
See `src/main/java/com/contextenglish/seed/DataSeeder.java` for the seeded
credentials, and change the password after your first login.

**Change this password immediately after your first login** (Profile page →
Change password). This account is created automatically the first time the
app starts against an empty database.

---

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

## Deploying to Render

1. Push this project to a **private** GitHub repository (do not make it
   public while `application-local.properties` history could ever have been
   committed — it won't be, since it's gitignored from the start here, but
   always double check `git status` before your first commit).
2. In Render, create a new **Web Service** from that repo, environment: **Docker**.
3. Render will build using the included `Dockerfile` automatically.
4. Under the service's **Environment** tab, add the following variables —
   type the real values directly into Render's dashboard, never into a file
   that gets committed:

| Key | Value |
|---|---|
| `SUPABASE_DB_URL` | `jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres` |
| `SUPABASE_DB_USERNAME` | `postgres.cvyddsbdynqnzutyfnsc` |
| `SUPABASE_DB_PASSWORD` | *(your DB password — from Supabase Settings → Database)* |
| `SUPABASE_URL` | `https://cvyddsbdynqnzutyfnsc.supabase.co` |
| `SUPABASE_PUBLISHABLE_KEY` | `sb_publishable_rBmiXlp1Bp2SJqgh0Ixn0A_iu4SxEbO` |
| `SUPABASE_SECRET_KEY` | *(your secret key — from Supabase Settings → API Keys)* |

The DB host, username, project URL, and publishable key aren't secret (the
publishable key is designed to be public — same idea as a Stripe publishable
key), so they're safe to keep in this table. The DB password and the secret
key are the two values that must never sit in a committed file — Render's
Environment tab is the only place they should be typed.

> **Why the pooler URL, not the "Direct connection" one:** Supabase's direct
> host (`db.<ref>.supabase.co`) only resolves over IPv6. Render's network
> path isn't guaranteed to support that, so use the Session Pooler host
> instead (same one used in local dev) — it's IPv4-compatible. Note the
> username also changes to `postgres.<project-ref>` for the pooler.

Render automatically provides a `PORT` variable, which `application.properties`
already reads (`server.port=${PORT:8080}`) — you don't need to set that
yourself.

5. Deploy. Render will run the multi-stage Docker build and start the app.

> **Security note:** the Supabase credentials above are the same ones you
> shared during setup. Treat them the same way you'd treat any database
> password — don't paste them into public places (public repos, public chat
> tools, etc). If you ever suspect they've leaked, rotate the database
> password and regenerate the API keys from your Supabase project's
> Settings → Database / Settings → API Keys pages.

### A note on Java 26 / Spring Boot 4.1

This project intentionally matches the same Java 26 + Spring Boot 4.1 stack
used in your original "Restaurant" reference project, per your request. Both
are genuinely released, current versions as of this build. Because they're
very recent, if you ever hit a dependency or Docker base-image resolution
issue on Render that seems environment-related rather than code-related, it's
worth checking whether a newer patch release fixes it — this stack sits at
the leading edge and gets less battle-testing than the Java 21 LTS + Spring
Boot 3.x combination.

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

## How the evaluation engine works (no external NLP)

Every target word/phrase (`TargetItem`) carries admin-curated
`AcceptableAnswer` rows tagged `EXACT`, `SYNONYM`, `KEYWORD`, or `CATEGORY`,
plus `DistractorOption` rows for multiple-choice display. Free-text answers
are normalized (lowercased, punctuation stripped, light suffix-stemming) and
checked against those rows in order — exact/synonym matches score full
credit, keyword-contains matches score partial credit. Multiple-choice and
multi-select modes score directly against which options were marked
`required` vs. distractors. See `EvaluationService.java` for the full logic.

## Known limitations / good next steps

- No password-reset flow (only in-app change-password) — add if needed.
- The daily "quick practice" feed pulls from `MICRO`-tagged passages only;
  add more of those via the admin panel to make it richer.
- Word-level highlighting matches by exact surface text (case-insensitive,
  first occurrence only per passage) — very overlapping target phrases in
  the same passage aren't specially handled.
- No automated tests are included beyond the placeholder test scaffolding
  Spring Initializr generates — worth adding unit tests for
  `EvaluationService` and `AdaptiveDifficultyService` if this becomes a
  larger project.
