package com.example.ui.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.domain.model.AppLanguage
import com.example.domain.model.PuzzleType
import com.example.domain.model.RankTier
import com.example.domain.model.RivalId
import com.example.domain.model.SkillType

@Immutable
data class AppStrings(
    val appName: String,
    val tagline: String,
    val offlineReady: String,

    // Onboarding
    val onboardingTapPrompt: String,
    val onboardingHintStep: String,
    val onboardingWinTitle: String,
    val onboardingWinSubtitle: String,
    val onboardingPrefQuestion: String,
    val prefDaily: String,
    val prefDuel: String,
    val prefBoth: String,
    val continueCta: String,

    // Home
    val dailyPuzzleTitle: String,
    val dailyPuzzleSubtitle: String,
    val dailyCompletedBadge: String,
    val dailyPlayNow: String,
    val dailyShareResult: String,
    val dailyPracticeInstead: String,
    val duelPrimaryCta: String,
    val duelSubtitle: String,
    val friendChallengeTitle: String,
    val friendChallengeSub: String,
    val practiceTitle: String,
    val practiceSub: String,
    val brainProfileTitle: String,
    val brainProfileSub: String,
    val streakLabel: String,
    val freezesLabel: String,
    val hintsTodayLabel: String,
    val healthyBreakBannerTitle: String,
    val healthyBreakBannerBody: String,
    val day3PromptTitle: String,
    val day3PromptBody: String,
    val enableReminderCta: String,
    val maybeLaterCta: String,

    // Duel Lobby
    val duelLobbyTitle: String,
    val chooseRival: String,
    val randomRival: String,
    val startDuelCta: String,
    val rivalLockedAt: String,
    val mercyActiveNote: String,
    val rivalSpeedFast: String,
    val rivalSpeedCalm: String,
    val rivalSpeedBalanced: String,
    val rivalStrongIn: String,

    // Practice
    val practiceScreenTitle: String,
    val practiceScreenSub: String,
    val practiceMixAll: String,
    val untimedBadge: String,

    // Puzzle Screen
    val roundOf: String,
    val youLabel: String,
    val rivalSolved: String,
    val rivalThinking: String,
    val hintBtn: String,
    val noHintsLeft: String,
    val getReady: String,
    val goText: String,
    val leaveDialogTitle: String,
    val leaveDialogBody: String,
    val keepPlayingCta: String,
    val leaveMatchCta: String,
    val correctToast: String,
    val wrongGentle1: String,
    val wrongGentle2: String,
    val wrongGentle3: String,
    val timeUpGentle: String,

    // Puzzle-specific instructions
    val numChainPrompt: String,
    val codeBreakerPrompt: String,
    val codeBreakerExact: String,
    val codeBreakerColor: String,
    val codeBreakerSubmit: String,
    val codeBreakerClear: String,
    val oddShapePrompt: String,
    val quickCalcPrompt: String,
    val quickCalcStart: String,
    val quickCalcTarget: String,
    val quickCalcCurrent: String,
    val quickCalcUndo: String,
    val wordTwistPrompt: String,
    val wordTwistSubmit: String,
    val wordTwistNotValid: String,
    val wordTwistShuffle: String,
    val memoryMemorize: String,
    val memoryRecall: String,
    val memoryRemaining: String,

    // Result Screen
    val verdictWin: String,
    val verdictDraw: String,
    val verdictLoss: String,
    val verdictDailyDone: String,
    val verdictPracticeDone: String,
    val xpEarnedLabel: String,
    val accuracyLabel: String,
    val avgSpeedLabel: String,
    val rankUpBanner: String,
    val firstBadgeBanner: String,
    val day3GiftBanner: String,
    val rematchCta: String,
    val shareCardCta: String,
    val homeCta: String,
    val beatFriendScore: String,
    val friendTargetScore: String,

    // Brain Profile
    val brainScorecardTitle: String,
    val avgSkillLabel: String,
    val duelsPlayedLabel: String,
    val winsLabel: String,
    val bestStreakLabel: String,
    val badgesLabel: String,
    val skillTrendUp: String,
    val skillTrendSteady: String,
    val shareProfileCta: String,
    val rankRoadTitle: String,

    // Friend Challenge
    val friendScreenTitle: String,
    val createChallengeTab: String,
    val joinChallengeTab: String,
    val createChallengeDesc: String,
    val playAndCreateCta: String,
    val shareInviteCta: String,
    val enterCodeLabel: String,
    val enterCodeHint: String,
    val startChallengeCta: String,
    val invalidCodeError: String,
    val noServerNote: String,

    // Settings
    val settingsTitle: String,
    val nicknameLabel: String,
    val nicknameHint: String,
    val saveCta: String,
    val languageLabel: String,
    val themeLabel: String,
    val themeDark: String,
    val themeLight: String,
    val themeSystem: String,
    val soundLabel: String,
    val hapticsLabel: String,
    val reduceMotionLabel: String,
    val dailyReminderLabel: String,
    val dailyReminderSub: String,
    val hardModeLabel: String,
    val hardModeLockedSub: String,
    val hardModeUnlockedSub: String,
    val privacyPolicyTitle: String,
    val privacyPolicyBody: String,
    val closeCta: String,
    val backContentDesc: String,
    val settingsContentDesc: String,

    // Bottom Navigation
    val navHome: String,
    val navDailyPuzzle: String,
    val navDuelMode: String,
    val navProfile: String,

    // Daily Logic Grid Component
    val dailyGridTitle: String,
    val dailyGridSubtitle: String,
    val dailyGridSolvedBadge: String,
    val dailyGridConflictHint: String,
    val dailyGridClear: String,
    val dailyGridReset: String,
    val dailyGridSolvedBanner: String,
    val difficultyEasy: String,
    val difficultyMedium: String,
    val difficultyHard: String,
    val cluesLabel: String,

    // Campaign / Level Adventure
    val startGameCta: String,
    val homeHeroHeadline: String,
    val homeHeroSub: String,
    val selectLevelTitle: String,
    val levelWord: String,
    val playNowBadge: String,
    val starsLabel: String,
    val levelsUnlockedLabel: String,
    val nextLevelCta: String,
    val retryLevelCta: String,
    val levelsMapCta: String,
    val levelCompleteTitle: String,
    val levelRetryTitle: String,
    val nextLevelUnlockedBanner: String,
    val moreModesLabel: String
) {
    fun chapterTitle(chapterIndex: Int, isHindi: Boolean): String = if (isHindi) {
        when (chapterIndex) {
            0 -> "वर्ल्ड 1 • स्पार्क मीडो"
            1 -> "वर्ल्ड 2 • लॉजिक पीक"
            2 -> "वर्ल्ड 3 • साइफ़र वैली"
            else -> "वर्ल्ड 4 • मास्टरमाइंड किला"
        }
    } else {
        when (chapterIndex) {
            0 -> "World 1 • Spark Meadow"
            1 -> "World 2 • Logic Peak"
            2 -> "World 3 • Cipher Valley"
            else -> "World 4 • Mastermind Citadel"
        }
    }
    fun rankName(rank: RankTier, isHindi: Boolean): String = if (isHindi) {
        when (rank) {
            RankTier.ROOKIE -> "रूकी"
            RankTier.THINKER -> "थिंकर"
            RankTier.STRATEGIST -> "स्ट्रैटेजिस्ट"
            RankTier.MASTERMIND -> "मास्टरमाइंड"
            RankTier.LEGEND -> "लीजेंड"
        }
    } else {
        when (rank) {
            RankTier.ROOKIE -> "Rookie"
            RankTier.THINKER -> "Thinker"
            RankTier.STRATEGIST -> "Strategist"
            RankTier.MASTERMIND -> "Mastermind"
            RankTier.LEGEND -> "Legend"
        }
    }

    fun rankUnlockSummary(rank: RankTier, isHindi: Boolean): String = if (isHindi) {
        when (rank) {
            RankTier.ROOKIE -> "बेसिक पज़ल्स + 2 राइवल्स"
            RankTier.THINKER -> "कोड ब्रेकर + काव्या अनलॉक"
            RankTier.STRATEGIST -> "स्पेशल एरीना स्टाइल"
            RankTier.MASTERMIND -> "हार्ड मोड + बॉस भावना"
            RankTier.LEGEND -> "लीजेंड बैज और गोल्ड फ्रेम"
        }
    } else {
        when (rank) {
            RankTier.ROOKIE -> "Core puzzles + 2 Rivals"
            RankTier.THINKER -> "Code Breaker + Calm Kavya"
            RankTier.STRATEGIST -> "Arena accents & mastery"
            RankTier.MASTERMIND -> "Hard Mode + Boss Bhavna"
            RankTier.LEGEND -> "Legend badge & gold frame"
        }
    }

    fun skillName(skill: SkillType, isHindi: Boolean): String = if (isHindi) {
        when (skill) {
            SkillType.LOGIC -> "तर्क"
            SkillType.SPEED -> "रफ़्तार"
            SkillType.MEMORY -> "याददाश्त"
            SkillType.PATTERN -> "पैटर्न"
            SkillType.WORD -> "शब्द"
        }
    } else {
        when (skill) {
            SkillType.LOGIC -> "Logic"
            SkillType.SPEED -> "Speed"
            SkillType.MEMORY -> "Memory"
            SkillType.PATTERN -> "Pattern"
            SkillType.WORD -> "Word"
        }
    }

    fun puzzleName(type: PuzzleType, isHindi: Boolean): String = if (isHindi) {
        when (type) {
            PuzzleType.NUMBER_CHAIN -> "नंबर चेन"
            PuzzleType.CODE_BREAKER -> "कोड ब्रेकर"
            PuzzleType.ODD_SHAPE -> "ऑड शेप"
            PuzzleType.QUICK_CALC -> "क्विक कैल्क"
            PuzzleType.WORD_TWIST -> "वर्ड ट्विस्ट"
            PuzzleType.MEMORY_GRID -> "मेमोरी ग्रिड"
        }
    } else {
        when (type) {
            PuzzleType.NUMBER_CHAIN -> "Number Chain"
            PuzzleType.CODE_BREAKER -> "Code Breaker"
            PuzzleType.ODD_SHAPE -> "Odd Shape"
            PuzzleType.QUICK_CALC -> "Quick Calc"
            PuzzleType.WORD_TWIST -> "Word Twist"
            PuzzleType.MEMORY_GRID -> "Memory Grid"
        }
    }

    fun rivalName(rival: RivalId, isHindi: Boolean): String = if (isHindi) {
        when (rival) {
            RivalId.QUICK_QASIM -> "क्विक क़ासिम"
            RivalId.PATTERN_PRANAV -> "पैटर्न प्रणव"
            RivalId.CALM_KAVYA -> "काम काव्या"
            RivalId.BOSS_BHAVNA -> "बॉस भावना"
        }
    } else {
        when (rival) {
            RivalId.QUICK_QASIM -> "Quick Qasim"
            RivalId.PATTERN_PRANAV -> "Pattern Pranav"
            RivalId.CALM_KAVYA -> "Calm Kavya"
            RivalId.BOSS_BHAVNA -> "Boss Bhavna"
        }
    }

    fun rivalBio(rival: RivalId, isHindi: Boolean): String = if (isHindi) {
        when (rival) {
            RivalId.QUICK_QASIM -> "बहुत तेज़, पर कभी-कभी जल्दी में चूक जाता है"
            RivalId.PATTERN_PRANAV -> "पैटर्न में माहिर, शब्द पज़ल में थोड़ा धीमा"
            RivalId.CALM_KAVYA -> "शांत और सटीक, बहुत कम गलती करती है"
            RivalId.BOSS_BHAVNA -> "हर स्किल में संतुलित — सबसे बड़ी चुनौती"
        }
    } else {
        when (rival) {
            RivalId.QUICK_QASIM -> "Lightning fast, occasionally slips in a rush"
            RivalId.PATTERN_PRANAV -> "Sharp at patterns, slightly slower on words"
            RivalId.CALM_KAVYA -> "Measured pace with razor-sharp accuracy"
            RivalId.BOSS_BHAVNA -> "Balanced across all 5 skills — ultimate test"
        }
    }
}

val EnglishStrings = AppStrings(
    appName = "Mindfeud",
    tagline = "Daily Logic Duels • 60s",
    offlineReady = "100% Offline • Pure Skill",

    onboardingTapPrompt = "Find the next number in the chain",
    onboardingHintStep = "Each step adds +4. Tap the answer!",
    onboardingWinTitle = "That was your first duel!",
    onboardingWinSubtitle = "Fast, clean, and sharp. +50 XP unlocked.",
    onboardingPrefQuestion = "How do you like to play?",
    prefDaily = "Daily Puzzle",
    prefDuel = "60s Duels",
    prefBoth = "Both!",
    continueCta = "Let's Play",

    dailyPuzzleTitle = "Daily Puzzle",
    dailyPuzzleSubtitle = "Same 5-puzzle set for everyone today",
    dailyCompletedBadge = "Done Today",
    dailyPlayNow = "Play Daily",
    dailyShareResult = "Share Daily Score",
    dailyPracticeInstead = "Practice More",
    duelPrimaryCta = "Start 60s Duel",
    duelSubtitle = "Race AI rivals across 5 rapid logic rounds",
    friendChallengeTitle = "Friend Challenge",
    friendChallengeSub = "Same 5 puzzles via offline code",
    practiceTitle = "Practice",
    practiceSub = "Untimed, all 6 puzzle types",
    brainProfileTitle = "Brain Profile",
    brainProfileSub = "5-skill scorecard & rank",
    streakLabel = "Streak",
    freezesLabel = "Freezes",
    hintsTodayLabel = "Hints today",
    healthyBreakBannerTitle = "20 duels today — impressive!",
    healthyBreakBannerBody = "Enough for today? Give your mind a quick breather and come back fresh.",
    day3PromptTitle = "3-Day Streak Habit!",
    day3PromptBody = "You earned +1 Streak Freeze! Want 1 calm daily reminder at 7:30 PM?",
    enableReminderCta = "Turn On",
    maybeLaterCta = "Not Now",

    duelLobbyTitle = "60s Duel Lobby",
    chooseRival = "Pick Your Rival",
    randomRival = "Random Rival",
    startDuelCta = "Start Duel",
    rivalLockedAt = "Unlocks at",
    mercyActiveNote = "Balanced matchmaking active — rivals eased slightly for a fair bounce-back.",
    rivalSpeedFast = "Fast",
    rivalSpeedCalm = "Calm",
    rivalSpeedBalanced = "Elite",
    rivalStrongIn = "Strong:",

    practiceScreenTitle = "Practice Arena",
    practiceScreenSub = "No timer, no rank pressure — master any puzzle type.",
    practiceMixAll = "5-Puzzle Mix",
    untimedBadge = "Untimed",

    roundOf = "Round",
    youLabel = "You",
    rivalSolved = "Solved!",
    rivalThinking = "Thinking...",
    hintBtn = "Hint",
    noHintsLeft = "No hints left today",
    getReady = "Get Ready",
    goText = "GO!",
    leaveDialogTitle = "Leave this match?",
    leaveDialogBody = "Your current round progress won't be saved.",
    keepPlayingCta = "Keep Playing",
    leaveMatchCta = "Leave",
    correctToast = "Sharp!",
    wrongGentle1 = "Close one! Next round is yours.",
    wrongGentle2 = "Tricky one — shake it off!",
    wrongGentle3 = "Almost had it — keep rolling!",
    timeUpGentle = "Time's up! Onto the next one.",

    numChainPrompt = "What number comes next?",
    codeBreakerPrompt = "Crack the 4-peg secret code",
    codeBreakerExact = "Exact",
    codeBreakerColor = "Color",
    codeBreakerSubmit = "Check",
    codeBreakerClear = "Clear",
    oddShapePrompt = "Tap the one shape that breaks the rule",
    quickCalcPrompt = "Pick 3 cards in order to hit the target",
    quickCalcStart = "Start",
    quickCalcTarget = "Target",
    quickCalcCurrent = "Now",
    quickCalcUndo = "Undo",
    wordTwistPrompt = "Build the longest word (3–6 letters)",
    wordTwistSubmit = "Submit",
    wordTwistNotValid = "Try another valid 3+ letter word",
    wordTwistShuffle = "Shuffle",
    memoryMemorize = "Memorize the glowing tiles...",
    memoryRecall = "Now tap the exact same tiles!",
    memoryRemaining = "tiles left",

    verdictWin = "Victory!",
    verdictDraw = "Dead Heat!",
    verdictLoss = "Good Fight!",
    verdictDailyDone = "Daily Complete!",
    verdictPracticeDone = "Practice Done!",
    xpEarnedLabel = "XP Earned",
    accuracyLabel = "Accuracy",
    avgSpeedLabel = "Avg Time",
    rankUpBanner = "RANK UP!",
    firstBadgeBanner = "First Session Badge Unlocked!",
    day3GiftBanner = "+1 Free Streak Freeze Gift!",
    rematchCta = "Rematch",
    shareCardCta = "Share Card",
    homeCta = "Home",
    beatFriendScore = "Beat your friend's target!",
    friendTargetScore = "Friend's Score",

    brainScorecardTitle = "Brain Profile",
    avgSkillLabel = "Overall Index",
    duelsPlayedLabel = "Duels",
    winsLabel = "Wins",
    bestStreakLabel = "Best Streak",
    badgesLabel = "Badges",
    skillTrendUp = "Sharper than your starting baseline!",
    skillTrendSteady = "Play daily duels to level up all 5 skills.",
    shareProfileCta = "Share Brain Profile",
    rankRoadTitle = "Rank Journey",

    friendScreenTitle = "Friend Challenge",
    createChallengeTab = "Create",
    joinChallengeTab = "Accept Code",
    createChallengeDesc = "Play a 5-puzzle duel to lock in your score, then share the offline code or link with a friend.",
    playAndCreateCta = "Play & Generate Code",
    shareInviteCta = "Share Challenge Invite",
    enterCodeLabel = "Friend's Code or Link",
    enterCodeHint = "Paste MF-XXXXXX or mindfeud:// link",
    startChallengeCta = "Play Same 5 Puzzles",
    invalidCodeError = "Please enter a valid 6-character code like MF-7K4P92.",
    noServerNote = "Works 100% offline — share links contain only the puzzle code and score, zero personal data.",

    settingsTitle = "Settings",
    nicknameLabel = "Display Nickname (Optional)",
    nicknameHint = "Used on share cards only",
    saveCta = "Save",
    languageLabel = "Language / भाषा",
    themeLabel = "Appearance",
    themeDark = "Dark",
    themeLight = "White",
    themeSystem = "System",
    soundLabel = "Sound Effects",
    hapticsLabel = "Haptic Vibration",
    reduceMotionLabel = "Reduce Motion",
    dailyReminderLabel = "Daily Reminder (Max 1/day)",
    dailyReminderSub = "Gentle ping at 7:30 PM — never at night",
    hardModeLabel = "Hard Mode Puzzles",
    hardModeLockedSub = "Unlocks at Mastermind rank (4,000 XP)",
    hardModeUnlockedSub = "Tougher sequences, 4×4 grids & deeper codes",
    privacyPolicyTitle = "Privacy & 15+ Safety Promise",
    privacyPolicyBody = "• 100% Offline Local Storage: Your streak, rank, and Brain Profile stay on your phone.\n• Zero Personal Data: No login, email, phone number, birthday, or location is collected.\n• Safe for 15+ Players: No open chat, no DMs, no ads, no tracking, and no real-money prizes or betting.\n• Clean Share Cards: Shared links only carry a puzzle seed code, score, and optional filtered nickname.",
    closeCta = "Close",
    backContentDesc = "Go back",
    settingsContentDesc = "Open settings",

    navHome = "Play",
    navDailyPuzzle = "Daily",
    navDuelMode = "Duel",
    navProfile = "Profile",

    dailyGridTitle = "Daily Logic Grid",
    dailyGridSubtitle = "Fill 1–4 with no repeats in any row, column, or 2×2 box",
    dailyGridSolvedBadge = "Solved!",
    dailyGridConflictHint = "Duplicate in row, column, or box",
    dailyGridClear = "Clear",
    dailyGridReset = "Reset",
    dailyGridSolvedBanner = "Logic Grid Solved!",
    difficultyEasy = "Easy",
    difficultyMedium = "Medium",
    difficultyHard = "Hard",
    cluesLabel = "clues",

    startGameCta = "START GAME",
    homeHeroHeadline = "Logic Adventure",
    homeHeroSub = "20 brain-teasing levels • Collect 3 stars on every stage",
    selectLevelTitle = "Select Level",
    levelWord = "Level",
    playNowBadge = "PLAY",
    starsLabel = "Stars",
    levelsUnlockedLabel = "Unlocked",
    nextLevelCta = "Next Level",
    retryLevelCta = "Retry Level",
    levelsMapCta = "Levels",
    levelCompleteTitle = "Level Complete!",
    levelRetryTitle = "Almost There!",
    nextLevelUnlockedBanner = "New Level Unlocked!",
    moreModesLabel = "More Game Modes"
)

val HindiStrings = AppStrings(
    appName = "Mindfeud",
    tagline = "डेली लॉजिक ड्यूल • 60 सेकंड",
    offlineReady = "100% ऑफ़लाइन • शुद्ध स्किल",

    onboardingTapPrompt = "इस क्रम में अगला नंबर कौन-सा है?",
    onboardingHintStep = "हर बार +4 जुड़ रहा है — सही जवाब चुनें!",
    onboardingWinTitle = "ये था आपका पहला ड्यूल!",
    onboardingWinSubtitle = "बहुत बढ़िया! +50 XP मिले।",
    onboardingPrefQuestion = "कैसे खेलना पसंद है?",
    prefDaily = "डेली पज़ल",
    prefDuel = "60 सेकंड ड्यूल",
    prefBoth = "दोनों!",
    continueCta = "खेलना शुरू करें",

    dailyPuzzleTitle = "डेली पज़ल",
    dailyPuzzleSubtitle = "आज सबके लिए एक जैसे 5 पज़ल",
    dailyCompletedBadge = "आज पूरा हुआ",
    dailyPlayNow = "आज का पज़ल खेलें",
    dailyShareResult = "स्कोर शेयर करें",
    dailyPracticeInstead = "और अभ्यास करें",
    duelPrimaryCta = "60 सेकंड ड्यूल खेलें",
    duelSubtitle = "5 तेज़ लॉजिक राउंड में AI राइवल से मुकाबला",
    friendChallengeTitle = "दोस्त को चैलेंज",
    friendChallengeSub = "ऑफ़लाइन कोड से वही 5 पज़ल",
    practiceTitle = "अभ्यास",
    practiceSub = "बिना टाइमर, सभी 6 पज़ल",
    brainProfileTitle = "ब्रेन प्रोफ़ाइल",
    brainProfileSub = "5 स्किल्स का स्कोरकार्ड",
    streakLabel = "स्ट्रीक",
    freezesLabel = "फ़्रीज़",
    hintsTodayLabel = "आज के हिंट",
    healthyBreakBannerTitle = "आज 20 ड्यूल पूरे — शानदार!",
    healthyBreakBannerBody = "आज के लिए काफ़ी? थोड़ा आराम करें और कल फिर तरोताज़ा खेलें।",
    day3PromptTitle = "3 दिन की स्ट्रीक!",
    day3PromptBody = "आपको +1 स्ट्रीक फ़्रीज़ मिला! क्या रोज़ शाम 7:30 पर एक रिमाइंडर चाहिए?",
    enableReminderCta = "चालू करें",
    maybeLaterCta = "अभी नहीं",

    duelLobbyTitle = "60 सेकंड ड्यूल लॉबी",
    chooseRival = "अपना राइवल चुनें",
    randomRival = "रैंडम राइवल",
    startDuelCta = "ड्यूल शुरू करें",
    rivalLockedAt = "अनलॉक रैंक:",
    mercyActiveNote = "फेयर बैलेंस चालू — लगातार हार के बाद राइवल थोड़ा आसान किया गया है।",
    rivalSpeedFast = "तेज़",
    rivalSpeedCalm = "शांत",
    rivalSpeedBalanced = "एलीट",
    rivalStrongIn = "मज़बूत:",

    practiceScreenTitle = "अभ्यास एरीना",
    practiceScreenSub = "कोई टाइमर या रैंक का दबाव नहीं — कोई भी पज़ल सीखें।",
    practiceMixAll = "5-पज़ल मिक्स",
    untimedBadge = "बिना टाइमर",

    roundOf = "राउंड",
    youLabel = "आप",
    rivalSolved = "हल किया!",
    rivalThinking = "सोच रहा है...",
    hintBtn = "हिंट",
    noHintsLeft = "आज के हिंट ख़त्म",
    getReady = "तैयार हो जाइए",
    goText = "शुरू!",
    leaveDialogTitle = "मैच छोड़ना है?",
    leaveDialogBody = "अभी के राउंड की प्रगति सेव नहीं होगी।",
    keepPlayingCta = "खेलते रहो",
    leaveMatchCta = "बाहर निकलें",
    correctToast = "बढ़िया!",
    wrongGentle1 = "बस थोड़ा सा चूके! अगला राउंड आपका।",
    wrongGentle2 = "थोड़ा पेचीदा था — आगे बढ़ते हैं!",
    wrongGentle3 = "लगभग सही था — लगे रहो!",
    timeUpGentle = "समय पूरा! अगले पज़ल पर चलते हैं।",

    numChainPrompt = "अगला नंबर कौन-सा आएगा?",
    codeBreakerPrompt = "4-रंग का गुप्त कोड पहचानें",
    codeBreakerExact = "सटीक",
    codeBreakerColor = "सिर्फ़ रंग",
    codeBreakerSubmit = "जाँचें",
    codeBreakerClear = "हटाएं",
    oddShapePrompt = "जो आकृति सबसे अलग है उस पर टैप करें",
    quickCalcPrompt = "टारगेट तक पहुँचने के लिए 3 कार्ड चुनें",
    quickCalcStart = "शुरू",
    quickCalcTarget = "लक्ष्य",
    quickCalcCurrent = "अभी",
    quickCalcUndo = "वापस",
    wordTwistPrompt = "सबसे लंबा अंग्रेज़ी शब्द बनाएं (3–6 अक्षर)",
    wordTwistSubmit = "सबमिट",
    wordTwistNotValid = "कोई सही 3+ अक्षर वाला शब्द आज़माएं",
    wordTwistShuffle = "बदलें",
    memoryMemorize = "चमकते खानों को याद रखें...",
    memoryRecall = "अब वही खाने टैप करें!",
    memoryRemaining = "बाकी",

    verdictWin = "जीत गए!",
    verdictDraw = "बराबरी!",
    verdictLoss = "अच्छी टक्कर!",
    verdictDailyDone = "डेली पज़ल पूरा!",
    verdictPracticeDone = "अभ्यास पूरा!",
    xpEarnedLabel = "XP मिले",
    accuracyLabel = "सटीकता",
    avgSpeedLabel = "औसत समय",
    rankUpBanner = "नया रैंक अनलॉक!",
    firstBadgeBanner = "पहला सेशन बैज मिला!",
    day3GiftBanner = "+1 फ्री स्ट्रीक फ़्रीज़ गिफ्ट!",
    rematchCta = "फिर से खेलें",
    shareCardCta = "कार्ड शेयर करें",
    homeCta = "होम",
    beatFriendScore = "दोस्त का स्कोर तोड़ने की चुनौती!",
    friendTargetScore = "दोस्त का स्कोर",

    brainScorecardTitle = "ब्रेन प्रोफ़ाइल",
    avgSkillLabel = "कुल स्कोर",
    duelsPlayedLabel = "ड्यूल",
    winsLabel = "जीत",
    bestStreakLabel = "बेस्ट स्ट्रीक",
    badgesLabel = "बैज",
    skillTrendUp = "शुरुआत से ज़्यादा तेज़ और सटीक!",
    skillTrendSteady = "सभी 5 स्किल्स बढ़ाने के लिए रोज़ खेलें।",
    shareProfileCta = "प्रोफ़ाइल शेयर करें",
    rankRoadTitle = "रैंक की यात्रा",

    friendScreenTitle = "दोस्त को चैलेंज",
    createChallengeTab = "चैलेंज बनाएं",
    joinChallengeTab = "कोड डालें",
    createChallengeDesc = "5 पज़ल खेलकर अपना स्कोर सेट करें, फिर ऑफ़लाइन कोड या लिंक दोस्त को भेजें।",
    playAndCreateCta = "खेलें और कोड बनाएं",
    shareInviteCta = "चैलेंज लिंक भेजें",
    enterCodeLabel = "दोस्त का कोड या लिंक",
    enterCodeHint = "MF-XXXXXX या लिंक पेस्ट करें",
    startChallengeCta = "वही 5 पज़ल खेलें",
    invalidCodeError = "कृपया सही 6-अक्षर का कोड डालें (जैसे MF-7K4P92)।",
    noServerNote = "100% ऑफ़लाइन चलता है — लिंक में सिर्फ़ पज़ल कोड और स्कोर होता है, कोई निजी जानकारी नहीं।",

    settingsTitle = "सेटिंग्स",
    nicknameLabel = "निकनेम (वैकल्पिक)",
    nicknameHint = "सिर्फ़ शेयर कार्ड पर दिखेगा",
    saveCta = "सेव करें",
    languageLabel = "भाषा / Language",
    themeLabel = "थीम",
    themeDark = "डार्क",
    themeLight = "सफ़ेद",
    themeSystem = "सिस्टम",
    soundLabel = "साउंड इफ़ेक्ट्स",
    hapticsLabel = "वाइब्रेशन (हैप्टिक्स)",
    reduceMotionLabel = "कम एनिमेशन (Reduce Motion)",
    dailyReminderLabel = "डेली रिमाइंडर (दिन में max 1)",
    dailyReminderSub = "शाम 7:30 पर शांत रिमाइंडर — रात में कभी नहीं",
    hardModeLabel = "हार्ड मोड पज़ल्स",
    hardModeLockedSub = "मास्टरमाइंड रैंक (4,000 XP) पर खुलेगा",
    hardModeUnlockedSub = "कठिन पैटर्न, 4×4 ग्रिड और चुनौतीपूर्ण कोड",
    privacyPolicyTitle = "प्राइवेसी और 15+ सेफ़्टी वादा",
    privacyPolicyBody = "• 100% ऑफ़लाइन लोकल स्टोरेज: आपकी स्ट्रीक, रैंक और ब्रेन प्रोफ़ाइल आपके फ़ोन में ही रहती है।\n• कोई निजी डेटा नहीं: कोई लॉगिन, ईमेल, फ़ोन नंबर, जन्मतिथि या लोकेशन नहीं लिया जाता।\n• 15+ प्लेयर्स के लिए सुरक्षित: कोई अनजान चैट, विज्ञापन, ट्रैकिंग या पैसे/जुए का खेल नहीं।\n• सुरक्षित शेयर कार्ड: लिंक में केवल पज़ल कोड, स्कोर और आपका चुना हुआ निकनेम होता है।",
    closeCta = "बंद करें",
    backContentDesc = "वापस जाएं",
    settingsContentDesc = "सेटिंग्स खोलें",

    navHome = "खेलें",
    navDailyPuzzle = "डेली",
    navDuelMode = "ड्यूल",
    navProfile = "प्रोफ़ाइल",

    dailyGridTitle = "डेली लॉजिक ग्रिड",
    dailyGridSubtitle = "हर पंक्ति, कॉलम और 2×2 बॉक्स में 1–4 बिना दोहराए भरें",
    dailyGridSolvedBadge = "हल हुआ!",
    dailyGridConflictHint = "पंक्ति, कॉलम या बॉक्स में नंबर दोहराया गया है",
    dailyGridClear = "हटाएं",
    dailyGridReset = "रीसेट",
    dailyGridSolvedBanner = "लॉजिक ग्रिड हल हो गया!",
    difficultyEasy = "आसान",
    difficultyMedium = "मध्यम",
    difficultyHard = "कठिन",
    cluesLabel = "क्लूज़",

    startGameCta = "गेम शुरू करें",
    homeHeroHeadline = "लॉजिक एडवेंचर",
    homeHeroSub = "20 मज़ेदार लेवल्स • हर लेवल पर 3 स्टार जीतें",
    selectLevelTitle = "लेवल चुनें",
    levelWord = "लेवल",
    playNowBadge = "खेलें",
    starsLabel = "स्टार्स",
    levelsUnlockedLabel = "अनलॉक",
    nextLevelCta = "अगला लेवल",
    retryLevelCta = "फिर से खेलें",
    levelsMapCta = "लेवल्स",
    levelCompleteTitle = "लेवल पूरा हुआ!",
    levelRetryTitle = "फिर कोशिश करें!",
    nextLevelUnlockedBanner = "नया लेवल अनलॉक हुआ!",
    moreModesLabel = "अन्य गेम मोड्स"
)

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.HINDI -> HindiStrings
}

val LocalStrings = staticCompositionLocalOf { EnglishStrings }
