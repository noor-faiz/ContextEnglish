package com.contextenglish.seed;

import com.contextenglish.dto.request.AcceptableAnswerRequest;
import com.contextenglish.dto.request.PassageCreateRequest;
import com.contextenglish.dto.request.TargetItemRequest;
import com.contextenglish.entity.Achievement;
import com.contextenglish.entity.AssessmentQuestion;
import com.contextenglish.entity.User;
import com.contextenglish.entity.UserPreference;
import com.contextenglish.entity.UserProgress;
import com.contextenglish.entity.enums.AchievementCriteria;
import com.contextenglish.entity.enums.AnswerMode;
import com.contextenglish.entity.enums.AnswerType;
import com.contextenglish.entity.enums.ContentType;
import com.contextenglish.entity.enums.ItemType;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.entity.enums.Role;
import com.contextenglish.repository.AchievementRepository;
import com.contextenglish.repository.AssessmentQuestionRepository;
import com.contextenglish.repository.PassageRepository;
import com.contextenglish.repository.UserPreferenceRepository;
import com.contextenglish.repository.UserProgressRepository;
import com.contextenglish.repository.UserRepository;
import com.contextenglish.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds a small but complete demo data set on first run against an empty
 * database: one admin account, a placement-test question bank, a handful of
 * achievements, and sample passages spanning every level, item type, and
 * answer mode so the app is fully explorable immediately after deployment.
 * Runs once — every check below is guarded by "if the table is already
 * populated, skip it" so it's safe to leave in place permanently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserProgressRepository userProgressRepository;
    private final PasswordEncoder passwordEncoder;
    private final AchievementRepository achievementRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final PassageRepository passageRepository;
    private final AdminContentService adminContentService;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdminUser();
        seedAchievements();
        seedAssessmentQuestions();
        seedPassages();
    }

    private void seedAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@contextenglish.local");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        admin.setCurrentLevel(LevelType.ADVANCED);
        admin.setOnboardingComplete(true);
        userRepository.save(admin);

        UserPreference pref = new UserPreference();
        pref.setUser(admin);
        userPreferenceRepository.save(pref);

        UserProgress progress = new UserProgress();
        progress.setUser(admin);
        userProgressRepository.save(progress);

        log.info("Seeded default admin account -> username: admin / password: Admin@123 (please change this after first login)");
    }

    private void seedAchievements() {
        if (achievementRepository.count() > 0) {
            return;
        }
        List<Achievement> achievements = List.of(
                achievement("first_session", "First Steps", "Complete your first learning session",
                        AchievementCriteria.FIRST_SESSION, null, "🎉"),
                achievement("streak_3", "3-Day Streak", "Practice 3 days in a row",
                        AchievementCriteria.STREAK_DAYS, 3, "🔥"),
                achievement("streak_7", "Week Warrior", "Practice 7 days in a row",
                        AchievementCriteria.STREAK_DAYS, 7, "⭐"),
                achievement("level_up", "Leveling Up", "Advance beyond Beginner level",
                        AchievementCriteria.LEVEL_UP, null, "📈"),
                achievement("words_10", "Word Collector", "Master 10 words or phrases",
                        AchievementCriteria.WORDS_LEARNED, 10, "📚"),
                achievement("sessions_10", "Dedicated Learner", "Complete 10 learning sessions",
                        AchievementCriteria.SESSIONS_COMPLETED, 10, "💪")
        );
        achievementRepository.saveAll(achievements);
    }

    private Achievement achievement(String code, String title, String desc,
                                     AchievementCriteria type, Integer value, String icon) {
        Achievement a = new Achievement();
        a.setCode(code);
        a.setTitle(title);
        a.setDescription(desc);
        a.setCriteriaType(type);
        a.setCriteriaValue(value);
        a.setIcon(icon);
        return a;
    }

    private void seedAssessmentQuestions() {
        if (assessmentQuestionRepository.count() > 0) {
            return;
        }
        List<AssessmentQuestion> questions = List.of(
                question("Choose the correct word: 'She ___ to school every day.'",
                        "go", "goes", "going", "gone", 2, LevelType.BEGINNER),
                question("What is the opposite of 'happy'?",
                        "joyful", "sad", "excited", "calm", 2, LevelType.BEGINNER),
                question("Choose the correct sentence.",
                        "He don't like coffee.", "He doesn't like coffee.", "He not like coffee.", "He no like coffee.", 2, LevelType.BEGINNER),
                question("What does 'huge' mean?",
                        "very small", "very big", "very fast", "very slow", 2, LevelType.BEGINNER),
                question("Choose the word that means 'to look at something quickly'.",
                        "glance", "stare", "ignore", "sleep", 1, LevelType.INTERMEDIATE),
                question("'She was on the fence about the decision' means she was...",
                        "very confident", "undecided", "very angry", "asleep", 2, LevelType.INTERMEDIATE),
                question("Choose the best synonym for 'reluctant'.",
                        "eager", "unwilling", "certain", "curious", 2, LevelType.INTERMEDIATE),
                question("'The negotiations reached an impasse' means they...",
                        "succeeded completely", "reached a deadlock", "started fresh", "were cancelled", 2, LevelType.ADVANCED),
                question("Choose the closest meaning of 'ubiquitous'.",
                        "rare", "found everywhere", "expensive", "forbidden", 2, LevelType.ADVANCED),
                question("'Her argument was cogent' means it was...",
                        "confusing", "clear and convincing", "very long", "offensive", 2, LevelType.ADVANCED)
        );
        assessmentQuestionRepository.saveAll(questions);
    }

    private AssessmentQuestion question(String text, String o1, String o2, String o3, String o4,
                                         int correct, LevelType level) {
        AssessmentQuestion q = new AssessmentQuestion();
        q.setQuestionText(text);
        q.setOption1(o1);
        q.setOption2(o2);
        q.setOption3(o3);
        q.setOption4(o4);
        q.setCorrectOption(correct);
        q.setDifficultyLevel(level);
        return q;
    }

    private void seedPassages() {
        if (passageRepository.count() > 0) {
            return;
        }

        // ---------------------------------------------------------- BEGINNER

        adminContentService.createPassage(passage(
                "A Tired Traveler",
                "Maria arrived at the airport after a long flight. She felt exhausted and just wanted to rest. "
                        + "Her suitcase was heavy, so she asked a stranger for help. He was very kind and carried "
                        + "it to the taxi for her.",
                LevelType.BEGINNER, "travel", ContentType.STORY,
                List.of(
                        item("exhausted", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("very tired", AnswerType.EXACT, true)),
                                List.of("very excited", "very hungry", "very confused"),
                                "Exhausted means extremely tired. The clue is 'long flight' and 'wanted to rest'.",
                                "'Exhausted' মানে অত্যন্ত ক্লান্ত। ইঙ্গিতটি হলো দীর্ঘ ফ্লাইট এবং বিশ্রাম নিতে চাওয়া।"),
                        item("kind", ItemType.WORD, AnswerMode.FREE_TEXT, "Think about how the stranger treated Maria.",
                                List.of(accept("friendly", AnswerType.EXACT, true),
                                        accept("nice", AnswerType.SYNONYM, true),
                                        accept("helpful", AnswerType.SYNONYM, true),
                                        accept("good person", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Kind' describes someone who is friendly and helpful toward others.",
                                "'Kind' মানে বন্ধুত্বপূর্ণ এবং অন্যের প্রতি সহায়ক।"),
                        item("heavy", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("weighs a lot", AnswerType.EXACT, true)),
                                List.of("very small", "very colorful", "very cheap"),
                                "'Heavy' means something weighs a lot — that's why she needed help carrying it.",
                                "'Heavy' মানে ভারী, অনেক ওজনযুক্ত।")
                )
        ));

        adminContentService.createPassage(passage(
                "The New Job",
                "Tom started his first job on Monday. He was nervous because everything was new to him. "
                        + "His manager was patient and explained each task slowly. By Friday, Tom felt more "
                        + "confident and even helped a new colleague.",
                LevelType.BEGINNER, "work", ContentType.STORY,
                List.of(
                        item("nervous", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("worried or anxious", AnswerType.EXACT, true)),
                                List.of("very happy", "very tired", "very angry"),
                                "'Nervous' means feeling worried or anxious, often before something new.", null),
                        item("patient", ItemType.WORD, AnswerMode.FREE_TEXT, "Think about how the manager treated Tom's mistakes.",
                                List.of(accept("calm and understanding", AnswerType.EXACT, true),
                                        accept("tolerant", AnswerType.SYNONYM, true),
                                        accept("does not get angry", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Patient' describes someone who stays calm and doesn't get frustrated easily.", null),
                        item("confident", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("sure of yourself", AnswerType.EXACT, true)),
                                List.of("very confused", "very sleepy", "very shy"),
                                "'Confident' means feeling sure of your own abilities.", null)
                )
        ));

        adminContentService.createPassage(passage(
                "Weekend Plans",
                "On Saturday, Ana and her friends decided to relax. They went to the park, had a picnic, and "
                        + "later watched a movie at home. It was a calm and enjoyable day.",
                LevelType.BEGINNER, "daily life", ContentType.STORY,
                List.of(
                        item("relax", ItemType.WORD, AnswerMode.MULTI_SELECT, "More than one option correctly describes 'relax'.",
                                List.of(accept("rest", AnswerType.EXACT, true),
                                        accept("unwind", AnswerType.SYNONYM, true)),
                                List.of("work hard", "argue", "hurry"),
                                "'Relax' means to rest and become less tense — both 'rest' and 'unwind' capture this meaning.",
                                null),
                        item("calm", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("peaceful", AnswerType.EXACT, true)),
                                List.of("very loud", "very busy", "very expensive"),
                                "'Calm' means peaceful and without stress.", null)
                )
        ));

        // ------------------------------------------------------- INTERMEDIATE

        adminContentService.createPassage(passage(
                "A Difficult Decision",
                "After weighing the pros and cons, Sarah finally made up her mind to accept the job offer in "
                        + "another city. It was a bittersweet moment — exciting for her career, but she would "
                        + "miss her family dearly.",
                LevelType.INTERMEDIATE, "career", ContentType.STORY,
                List.of(
                        item("made up her mind", ItemType.IDIOM, AnswerMode.MCQ, null,
                                List.of(accept("decided", AnswerType.EXACT, true)),
                                List.of("changed her mind", "forgot everything", "asked for advice"),
                                "'Made up her mind' is an idiom meaning she reached a final decision.", null),
                        item("bittersweet", ItemType.WORD, AnswerMode.FREE_TEXT, "Notice the sentence explains both a positive and a negative side.",
                                List.of(accept("both happy and sad", AnswerType.EXACT, true),
                                        accept("mixed feelings", AnswerType.SYNONYM, true),
                                        accept("happy and sad", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Bittersweet' describes something that brings both happiness and sadness at the same time.", null),
                        item("weighing the pros and cons", ItemType.IDIOM, AnswerMode.MCQ, null,
                                List.of(accept("carefully considering advantages and disadvantages", AnswerType.EXACT, true)),
                                List.of("measuring physical weight", "arguing with someone", "ignoring the problem"),
                                "This idiom means carefully thinking about the good and bad points before deciding.", null)
                )
        ));

        adminContentService.createPassage(passage(
                "Office Miscommunication",
                "The email was ambiguous, so half the team assumed the deadline was Friday while others thought "
                        + "it was Monday. To avoid further confusion, the manager decided to clarify the "
                        + "instructions in a follow-up meeting.",
                LevelType.INTERMEDIATE, "work", ContentType.STORY,
                List.of(
                        item("ambiguous", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("unclear or open to more than one interpretation", AnswerType.EXACT, true)),
                                List.of("very detailed", "very short", "very rude"),
                                "'Ambiguous' means something can be understood in more than one way.", null),
                        item("clarify", ItemType.WORD, AnswerMode.FREE_TEXT, null,
                                List.of(accept("make clear", AnswerType.EXACT, true),
                                        accept("explain", AnswerType.SYNONYM, true),
                                        accept("make it clear", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Clarify' means to make something easier to understand.", null),
                        item("assumed", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("believed something without checking", AnswerType.EXACT, true)),
                                List.of("confirmed with proof", "wrote down", "forgot completely"),
                                "'Assumed' means to believe something is true without being certain.", null)
                )
        ));

        // ----------------------------------------------------------- ADVANCED

        adminContentService.createPassage(passage(
                "The Whistleblower",
                "Despite the potential repercussions, she chose to disclose the company's fraudulent practices "
                        + "to the authorities. Her colleagues were divided: some praised her integrity, while "
                        + "others accused her of being sanctimonious.",
                LevelType.ADVANCED, "ethics", ContentType.STORY,
                List.of(
                        item("repercussions", ItemType.WORD, AnswerMode.FREE_TEXT, null,
                                List.of(accept("consequences", AnswerType.EXACT, true),
                                        accept("aftermath", AnswerType.SYNONYM, true),
                                        accept("results of an action", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Repercussions' are the often negative consequences that follow an action.", null),
                        item("disclose", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("reveal or make known", AnswerType.EXACT, true)),
                                List.of("hide completely", "destroy", "ignore"),
                                "'Disclose' means to reveal information that was previously secret.", null),
                        item("sanctimonious", ItemType.WORD, AnswerMode.MCQ, "This word has a negative, critical tone.",
                                List.of(accept("pretending to be morally superior", AnswerType.EXACT, true)),
                                List.of("extremely generous", "very shy", "highly skilled"),
                                "'Sanctimonious' describes someone who acts as if they are morally better than others, often insincerely.", null),
                        item("integrity", ItemType.WORD, AnswerMode.FREE_TEXT, null,
                                List.of(accept("honesty", AnswerType.EXACT, true),
                                        accept("moral uprightness", AnswerType.SYNONYM, true),
                                        accept("strong moral principles", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Integrity' means having strong moral principles and being honest.", null)
                )
        ));

        adminContentService.createPassage(passage(
                "Climate Negotiations",
                "Negotiators reached a tentative agreement after weeks of deadlock, though critics argue the "
                        + "accord is little more than a symbolic gesture unless nations follow through with "
                        + "concrete action.",
                LevelType.ADVANCED, "current affairs", ContentType.STORY,
                List.of(
                        item("tentative", ItemType.WORD, AnswerMode.MCQ, null,
                                List.of(accept("not yet certain or final", AnswerType.EXACT, true)),
                                List.of("completely final", "very strong", "publicly announced"),
                                "'Tentative' means not yet fixed or certain, subject to change.", null),
                        item("deadlock", ItemType.WORD, AnswerMode.FREE_TEXT, null,
                                List.of(accept("a situation where no progress can be made", AnswerType.EXACT, true),
                                        accept("stalemate", AnswerType.SYNONYM, true),
                                        accept("no progress", AnswerType.KEYWORD, true)),
                                List.of(),
                                "'Deadlock' describes a standstill where opposing sides cannot move forward.", null),
                        item("symbolic gesture", ItemType.IDIOM, AnswerMode.MULTI_SELECT, "Two options together best capture this idea.",
                                List.of(accept("an action mainly for show", AnswerType.EXACT, true),
                                        accept("not practically effective alone", AnswerType.EXACT, true)),
                                List.of("a binding legal contract", "a scientific measurement", "a financial investment"),
                                "A 'symbolic gesture' represents an idea or intention but may not, by itself, produce real practical change.", null)
                )
        ));

        // -------------------------------------------------------- MICRO FEED

        adminContentService.createPassage(passage(
                "Text Message",
                "Hey! Running late, stuck in traffic. Be there in 10 mins, hang tight!",
                LevelType.BEGINNER, "daily life", ContentType.MICRO,
                List.of(
                        item("hang tight", ItemType.IDIOM, AnswerMode.MCQ, null,
                                List.of(accept("wait patiently", AnswerType.EXACT, true)),
                                List.of("leave immediately", "get angry", "call the police"),
                                "'Hang tight' is an informal way of saying 'please wait patiently'.", null)
                )
        ));

        adminContentService.createPassage(passage(
                "Product Review",
                "This blender is a game changer — it's fast, powerful, and surprisingly quiet for the price.",
                LevelType.INTERMEDIATE, "shopping", ContentType.MICRO,
                List.of(
                        item("game changer", ItemType.IDIOM, AnswerMode.MCQ, null,
                                List.of(accept("something that changes a situation significantly", AnswerType.EXACT, true)),
                                List.of("a broken product", "an average product", "a type of board game"),
                                "A 'game changer' is something that has a major, transformative impact.", null)
                )
        ));

        log.info("Seeded {} sample passages", passageRepository.count());
    }

    // ---------------------------------------------------------------- helpers

    private PassageCreateRequest passage(String title, String text, LevelType level, String topic,
                                          ContentType contentType, List<TargetItemRequest> items) {
        PassageCreateRequest req = new PassageCreateRequest();
        req.setTitle(title);
        req.setContentText(text);
        req.setLevel(level);
        req.setTopic(topic);
        req.setContentType(contentType);
        req.setTargetItems(items);
        return req;
    }

    private TargetItemRequest item(String surfaceText, ItemType itemType, AnswerMode answerMode, String hint,
                                    List<AcceptableAnswerRequest> answers, List<String> distractors,
                                    String explanationText, String explanationNative) {
        TargetItemRequest req = new TargetItemRequest();
        req.setSurfaceText(surfaceText);
        req.setItemType(itemType);
        req.setAnswerMode(answerMode);
        req.setHintText(hint);
        req.setAcceptableAnswers(answers);
        req.setDistractorOptions(distractors);
        req.setExplanationText(explanationText);
        req.setExplanationNative(explanationNative);
        return req;
    }

    private AcceptableAnswerRequest accept(String text, AnswerType type, boolean required) {
        AcceptableAnswerRequest req = new AcceptableAnswerRequest();
        req.setAnswerText(text);
        req.setAnswerType(type);
        req.setRequired(required);
        req.setPointsWeight(1.0);
        return req;
    }
}
