package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyReminderScheduler
import com.example.data.GamePreferencesRepository
import com.example.data.SoundHapticManager
import com.example.domain.engine.ChallengeCodec
import com.example.domain.engine.CodeBreakerPuzzle
import com.example.domain.engine.CodeGuessFeedback
import com.example.domain.engine.DeterministicRandom
import com.example.domain.engine.MemoryGridPuzzle
import com.example.domain.engine.NumberChainPuzzle
import com.example.domain.engine.OddShapePuzzle
import com.example.domain.engine.Puzzle
import com.example.domain.engine.PuzzleEngine
import com.example.domain.engine.QuickCalcPuzzle
import com.example.domain.engine.RivalRoundPlan
import com.example.domain.engine.RivalSimulator
import com.example.domain.engine.WordTwistPuzzle
import com.example.domain.model.AppLanguage
import com.example.domain.model.FriendChallengeData
import com.example.domain.model.GameMode
import com.example.domain.model.MatchResult
import com.example.domain.model.MatchVerdict
import com.example.domain.model.PlayPreference
import com.example.domain.model.PuzzleType
import com.example.domain.model.RankTier
import com.example.domain.model.RivalId
import com.example.domain.model.RoundOutcome
import com.example.domain.model.TOTAL_CAMPAIGN_LEVELS
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

sealed interface AppScreen {
    data object Splash : AppScreen
    data object Onboarding : AppScreen
    data object Home : AppScreen
    data object LevelSelect : AppScreen
    data object DailyPuzzle : AppScreen
    data object DuelLobby : AppScreen
    data object PracticePicker : AppScreen
    data object PuzzlePlay : AppScreen
    data object Result : AppScreen
    data object BrainProfile : AppScreen
    data object FriendChallenge : AppScreen
    data object Settings : AppScreen
}

data class ActiveMatchState(
    val mode: GameMode,
    val puzzles: List<Puzzle>,
    val currentRoundIndex: Int = 0,
    val countdownValue: Int = 3, // 3, 2, 1, 0 (0 = live)
    val elapsedRoundMs: Long = 0L,
    val remainingRoundMs: Long = 0L,
    val playerScore: Int = 0,
    val rivalScore: Int = 0,
    val rivalId: RivalId? = null,
    val friendChallenge: FriendChallengeData? = null,
    val dailyEpochDay: Long? = null,
    val rivalPlan: RivalRoundPlan? = null,
    val rivalHasSolvedCurrentRound: Boolean = false,
    val roundsCompleted: List<RoundOutcome> = emptyList(),
    val hintUsedThisRound: Boolean = false,
    val eliminatedOptionIndices: Set<Int> = emptySet(),
    val revealedCodePegIndex: Int? = null,
    val revealedWordPrefix: String? = null,
    val memoryPreviewActive: Boolean = false,
    val memoryPreviewRemainingMs: Long = 0L,
    val codeBreakerHistory: List<CodeGuessFeedback> = emptyList(),
    val roundFeedback: RoundFeedbackBanner? = null,
    val isTransitioningRound: Boolean = false,
    val practiceFilterType: PuzzleType? = null,
    val levelNumber: Int? = null
) {
    val currentPuzzle: Puzzle? get() = puzzles.getOrNull(currentRoundIndex)
    val isUntimed: Boolean get() = mode == GameMode.PRACTICE
}

data class RoundFeedbackBanner(
    val isCorrect: Boolean,
    val pointsAwarded: Int,
    val messageIndex: Int,
    val isTimeUp: Boolean = false
)

data class MindfeudUiState(
    val screen: AppScreen = AppScreen.Splash,
    val profileLoaded: Boolean = false,
    val profile: UserProfile = UserProfile(),
    val selectedRival: RivalId = RivalId.QUICK_QASIM,
    val activeMatch: ActiveMatchState? = null,
    val lastResult: MatchResult? = null,
    val pendingDeepLinkChallenge: FriendChallengeData? = null,
    val day3PromptDismissed: Boolean = false,
    val onboardingSolved: Boolean = false
) {
    val effectiveEpochDay: Long
        get() {
            val deviceDay = System.currentTimeMillis() / 86_400_000L
            return max(deviceDay, profile.maxSeenEpochDay)
        }
}

class MindfeudViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    val soundHaptic = SoundHapticManager(application.applicationContext)

    private val _uiState = MutableStateFlow(MindfeudUiState())
    val uiState: StateFlow<MindfeudUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var splashDone = false

    init {
        viewModelScope.launch {
            val deviceDay = System.currentTimeMillis() / 86_400_000L
            repository.syncCurrentDay(deviceDay)
        }

        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                _uiState.update { current ->
                    val nextScreen = if (splashDone && current.screen == AppScreen.Splash) {
                        resolvePostSplashScreen(profile, current.pendingDeepLinkChallenge)
                    } else {
                        current.screen
                    }
                    current.copy(
                        profileLoaded = true,
                        profile = profile,
                        screen = nextScreen
                    )
                }
            }
        }

        // 1.5s splash screen per Section 4 & 6
        viewModelScope.launch {
            delay(1500L)
            splashDone = true
            _uiState.update { current ->
                if (current.screen == AppScreen.Splash && current.profileLoaded) {
                    current.copy(screen = resolvePostSplashScreen(current.profile, current.pendingDeepLinkChallenge))
                } else {
                    current
                }
            }
        }
    }

    private fun resolvePostSplashScreen(
        profile: UserProfile,
        deepLink: FriendChallengeData?
    ): AppScreen {
        if (deepLink != null) return AppScreen.FriendChallenge
        return AppScreen.Home
    }

    fun handleIncomingDeepLink(uriString: String?) {
        if (uriString.isNullOrBlank()) return
        val parsed = ChallengeCodec.parseInput(uriString) ?: return
        _uiState.update { state ->
            val targetScreen = if (state.screen == AppScreen.Splash || !state.profile.onboardingCompleted) {
                state.screen
            } else {
                AppScreen.FriendChallenge
            }
            state.copy(
                pendingDeepLinkChallenge = parsed,
                screen = targetScreen
            )
        }
    }

    fun consumePendingDeepLink() {
        _uiState.update { it.copy(pendingDeepLinkChallenge = null) }
    }

    // Navigation helpers
    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.PuzzlePlay) {
            stopMatchTimer()
        }
        _uiState.update { it.copy(screen = screen) }
    }

    fun navigateHome() {
        stopMatchTimer()
        _uiState.update { it.copy(screen = AppScreen.Home, activeMatch = null) }
    }

    // Onboarding
    fun onOnboardingPuzzleSolved() {
        val settings = _uiState.value.profile.settings
        soundHaptic.playWinFanfare(settings.soundEnabled, settings.hapticsEnabled)
        _uiState.update { it.copy(onboardingSolved = true) }
    }

    fun completeOnboarding(preference: PlayPreference) {
        viewModelScope.launch {
            val effectiveDay = _uiState.value.effectiveEpochDay
            repository.completeOnboarding(preference, effectiveDay)
            val destination = if (_uiState.value.pendingDeepLinkChallenge != null) {
                AppScreen.FriendChallenge
            } else {
                AppScreen.Home
            }
            _uiState.update { it.copy(screen = destination) }
        }
    }

    fun dismissDay3Prompt() {
        _uiState.update { it.copy(day3PromptDismissed = true) }
    }

    // Rival selection
    fun selectRival(rival: RivalId) {
        val settings = _uiState.value.profile.settings
        soundHaptic.playTap(settings.soundEnabled, settings.hapticsEnabled)
        _uiState.update { it.copy(selectedRival = rival) }
    }

    fun selectRandomUnlockedRival() {
        val profile = _uiState.value.profile
        val unlocked = RivalId.entries.filter { profile.isRivalUnlocked(it) }
        val pick = unlocked.randomOrNull() ?: RivalId.QUICK_QASIM
        selectRival(pick)
    }

    // Start Matches
    fun startDuel(rival: RivalId = _uiState.value.selectedRival) {
        val profile = _uiState.value.profile
        val effectiveRival = if (profile.isRivalUnlocked(rival)) rival else RivalId.QUICK_QASIM
        val seed = System.currentTimeMillis()
        val puzzles = PuzzleEngine.generateDuelSet(
            seed = seed,
            rank = profile.rank,
            hardMode = profile.settings.hardModeEnabled
        )
        launchMatch(
            ActiveMatchState(
                mode = GameMode.DUEL,
                puzzles = puzzles,
                countdownValue = 3,
                rivalId = effectiveRival
            )
        )
    }

    fun startDailyPuzzle() {
        val state = _uiState.value
        val day = state.effectiveEpochDay
        val puzzles = PuzzleEngine.generateDailySet(
            epochDay = day,
            hardMode = state.profile.settings.hardModeEnabled
        )
        launchMatch(
            ActiveMatchState(
                mode = GameMode.DAILY,
                puzzles = puzzles,
                countdownValue = 3,
                dailyEpochDay = day
            )
        )
    }

    fun startCreateFriendChallenge() {
        val code = ChallengeCodec.generateCode()
        val seed = ChallengeCodec.codeToSeed(code)
        val challenge = FriendChallengeData(
            code = code,
            seed = seed,
            challengerName = _uiState.value.profile.nickname,
            targetScore = null
        )
        val puzzles = PuzzleEngine.generateFriendChallengeSet(seed)
        launchMatch(
            ActiveMatchState(
                mode = GameMode.FRIEND_CHALLENGE,
                puzzles = puzzles,
                countdownValue = 3,
                friendChallenge = challenge
            )
        )
    }

    fun startAcceptFriendChallenge(challenge: FriendChallengeData) {
        val puzzles = PuzzleEngine.generateFriendChallengeSet(challenge.seed)
        launchMatch(
            ActiveMatchState(
                mode = GameMode.FRIEND_CHALLENGE,
                puzzles = puzzles,
                countdownValue = 3,
                friendChallenge = challenge
            )
        )
    }

    fun startPractice(filterType: PuzzleType?) {
        val rng = DeterministicRandom(System.currentTimeMillis())
        val diff = if (_uiState.value.profile.settings.hardModeEnabled) 3 else 1
        val puzzles = if (filterType != null) {
            List(5) { idx ->
                PuzzleEngine.generatePuzzle(
                    type = filterType,
                    rng = DeterministicRandom(rng.nextLong() xor idx.toLong()),
                    difficulty = diff,
                    id = "practice_${filterType.name}_$idx"
                )
            }
        } else {
            val all = rng.shuffle(PuzzleType.entries).take(5)
            all.mapIndexed { idx, type ->
                PuzzleEngine.generatePuzzle(
                    type = type,
                    rng = DeterministicRandom(rng.nextLong() xor (idx * 17L)),
                    difficulty = diff,
                    id = "practice_mix_$idx"
                )
            }
        }
        launchMatch(
            ActiveMatchState(
                mode = GameMode.PRACTICE,
                puzzles = puzzles,
                countdownValue = 0, // Instant start in practice
                practiceFilterType = filterType
            )
        )
    }

    fun startCampaignLevel(levelNumber: Int) {
        val profile = _uiState.value.profile
        val clamped = levelNumber.coerceIn(1, profile.highestUnlockedLevel.coerceAtLeast(1))
        val puzzles = PuzzleEngine.generateCampaignLevelSet(clamped)
        soundHaptic.playTap(profile.settings.soundEnabled, profile.settings.hapticsEnabled)
        launchMatch(
            ActiveMatchState(
                mode = GameMode.CAMPAIGN_LEVEL,
                puzzles = puzzles,
                countdownValue = 3,
                levelNumber = clamped
            )
        )
    }

    fun rematch() {
        val last = _uiState.value.lastResult ?: return
        when (last.mode) {
            GameMode.CAMPAIGN_LEVEL -> {
                val lvl = last.levelNumber ?: 1
                if (last.starsEarned >= 1 && lvl < TOTAL_CAMPAIGN_LEVELS) {
                    startCampaignLevel(lvl + 1)
                } else {
                    startCampaignLevel(lvl)
                }
            }
            GameMode.DUEL -> startDuel(last.rivalId ?: _uiState.value.selectedRival)
            GameMode.FRIEND_CHALLENGE -> {
                val fc = last.friendChallenge
                if (fc != null && fc.targetScore != null) {
                    startAcceptFriendChallenge(fc)
                } else {
                    startCreateFriendChallenge()
                }
            }
            GameMode.PRACTICE -> startPractice(_uiState.value.activeMatch?.practiceFilterType)
            GameMode.DAILY -> startDuel()
            GameMode.ONBOARDING -> navigateHome()
        }
    }

    private fun launchMatch(initial: ActiveMatchState) {
        stopMatchTimer()
        _uiState.update {
            it.copy(
                screen = AppScreen.PuzzlePlay,
                activeMatch = initial
            )
        }
        startRoundOrCountdown()
    }

    private fun startRoundOrCountdown() {
        stopMatchTimer()
        timerJob = viewModelScope.launch {
            val match = _uiState.value.activeMatch ?: return@launch
            val settings = _uiState.value.profile.settings

            if (match.countdownValue > 0) {
                for (tick in match.countdownValue downTo 1) {
                    _uiState.update { s ->
                        s.copy(activeMatch = s.activeMatch?.copy(countdownValue = tick))
                    }
                    soundHaptic.playCountdownTick(settings.soundEnabled, settings.hapticsEnabled)
                    delay(550L)
                }
                _uiState.update { s ->
                    s.copy(activeMatch = s.activeMatch?.copy(countdownValue = 0))
                }
            }

            beginCurrentRoundLoop()
        }
    }

    private suspend fun beginCurrentRoundLoop() {
        val state = _uiState.value
        val match = state.activeMatch ?: return
        val puzzle = match.currentPuzzle ?: return

        val rivalPlan = match.rivalId?.let { rivalId ->
            RivalSimulator.planRound(
                rival = rivalId,
                puzzle = puzzle,
                playerRank = state.profile.rank,
                consecutiveLosses = state.profile.consecutiveLosses,
                rng = DeterministicRandom(System.currentTimeMillis() xor match.currentRoundIndex.toLong())
            )
        }

        val isMemory = puzzle is MemoryGridPuzzle
        val previewMs = if (puzzle is MemoryGridPuzzle) puzzle.previewDurationMs else 0L
        val limitMs = puzzle.timeLimitSec * 1000L

        _uiState.update { s ->
            s.copy(
                activeMatch = s.activeMatch?.copy(
                    elapsedRoundMs = 0L,
                    remainingRoundMs = limitMs,
                    rivalPlan = rivalPlan,
                    rivalHasSolvedCurrentRound = false,
                    hintUsedThisRound = false,
                    eliminatedOptionIndices = emptySet(),
                    revealedCodePegIndex = null,
                    revealedWordPrefix = null,
                    memoryPreviewActive = isMemory,
                    memoryPreviewRemainingMs = previewMs,
                    codeBreakerHistory = emptyList(),
                    roundFeedback = null,
                    isTransitioningRound = false
                )
            )
        }

        // Memory preview phase (3 seconds) before recall timer starts
        if (isMemory && previewMs > 0L) {
            var remainingPreview = previewMs
            while (remainingPreview > 0L) {
                delay(100L)
                remainingPreview = (remainingPreview - 100L).coerceAtLeast(0L)
                _uiState.update { s ->
                    s.copy(
                        activeMatch = s.activeMatch?.copy(
                            memoryPreviewRemainingMs = remainingPreview,
                            memoryPreviewActive = remainingPreview > 0L
                        )
                    )
                }
            }
        }

        // Active round ticker
        var elapsed = 0L
        while (true) {
            delay(100L)
            val currentMatch = _uiState.value.activeMatch ?: return
            if (currentMatch.isTransitioningRound) return

            elapsed += 100L
            val plan = currentMatch.rivalPlan
            val rivalJustSolved = plan != null &&
                !currentMatch.rivalHasSolvedCurrentRound &&
                elapsed >= plan.solveDelayMs

            val newRivalScore = if (rivalJustSolved) {
                currentMatch.rivalScore + plan!!.points
            } else {
                currentMatch.rivalScore
            }

            if (currentMatch.isUntimed) {
                _uiState.update { s ->
                    s.copy(
                        activeMatch = s.activeMatch?.copy(
                            elapsedRoundMs = elapsed
                        )
                    )
                }
            } else {
                val remaining = (limitMs - elapsed).coerceAtLeast(0L)
                _uiState.update { s ->
                    s.copy(
                        activeMatch = s.activeMatch?.copy(
                            elapsedRoundMs = elapsed,
                            remainingRoundMs = remaining,
                            rivalHasSolvedCurrentRound = currentMatch.rivalHasSolvedCurrentRound || rivalJustSolved,
                            rivalScore = newRivalScore
                        )
                    )
                }
                if (remaining <= 0L) {
                    finishCurrentRound(isCorrect = false, wordLength = 0, isTimeUp = true)
                    return
                }
            }
        }
    }

    // Player answer handlers for all 6 puzzle types
    fun submitNumberChainAnswer(optionValue: Int) {
        val match = _uiState.value.activeMatch ?: return
        if (match.isTransitioningRound || match.countdownValue > 0) return
        val puzzle = match.currentPuzzle as? NumberChainPuzzle ?: return
        finishCurrentRound(isCorrect = optionValue == puzzle.correctAnswer)
    }

    fun submitOddShapeAnswer(tileIndex: Int) {
        val match = _uiState.value.activeMatch ?: return
        if (match.isTransitioningRound || match.countdownValue > 0) return
        val puzzle = match.currentPuzzle as? OddShapePuzzle ?: return
        finishCurrentRound(isCorrect = tileIndex == puzzle.oddIndex)
    }

    fun submitQuickCalcAnswer(selectedCardIds: List<Int>) {
        val match = _uiState.value.activeMatch ?: return
        if (match.isTransitioningRound || match.countdownValue > 0) return
        val puzzle = match.currentPuzzle as? QuickCalcPuzzle ?: return
        val ok = PuzzleEngine.isQuickCalcCorrect(puzzle, selectedCardIds)
        finishCurrentRound(isCorrect = ok)
    }

    fun submitWordTwistAnswer(word: String): Boolean {
        val match = _uiState.value.activeMatch ?: return false
        if (match.isTransitioningRound || match.countdownValue > 0) return false
        val puzzle = match.currentPuzzle as? WordTwistPuzzle ?: return false
        val valid = PuzzleEngine.isValidWordTwistSubmission(puzzle, word)
        if (!valid) {
            val settings = _uiState.value.profile.settings
            soundHaptic.playWrong(settings.soundEnabled, settings.hapticsEnabled)
            return false
        }
        finishCurrentRound(isCorrect = true, wordLength = word.trim().length)
        return true
    }

    fun submitCodeBreakerGuess(guess: List<Int>) {
        val match = _uiState.value.activeMatch ?: return
        if (match.isTransitioningRound || match.countdownValue > 0) return
        val puzzle = match.currentPuzzle as? CodeBreakerPuzzle ?: return
        if (guess.size != 4) return

        val feedback = PuzzleEngine.evaluateCodeGuess(puzzle.secretCode, guess)
        val updatedHistory = match.codeBreakerHistory + feedback
        _uiState.update { s ->
            s.copy(activeMatch = s.activeMatch?.copy(codeBreakerHistory = updatedHistory))
        }

        val settings = _uiState.value.profile.settings
        if (feedback.exactMatches == 4) {
            finishCurrentRound(isCorrect = true)
        } else if (updatedHistory.size >= puzzle.maxAttempts) {
            finishCurrentRound(isCorrect = false)
        } else {
            soundHaptic.playTap(settings.soundEnabled, settings.hapticsEnabled)
        }
    }

    fun submitMemoryGridSelection(selectedCells: Set<Int>) {
        val match = _uiState.value.activeMatch ?: return
        if (match.isTransitioningRound || match.countdownValue > 0 || match.memoryPreviewActive) return
        val puzzle = match.currentPuzzle as? MemoryGridPuzzle ?: return
        val isCorrect = selectedCells == puzzle.targetCells
        finishCurrentRound(isCorrect = isCorrect)
    }

    // Limited Hint (3 free/day per Section 4 & 5)
    fun useHint() {
        val state = _uiState.value
        val match = state.activeMatch ?: return
        val puzzle = match.currentPuzzle ?: return
        if (match.hintUsedThisRound || match.isTransitioningRound || match.countdownValue > 0) return

        viewModelScope.launch {
            val consumed = if (match.mode == GameMode.PRACTICE) {
                true
            } else {
                repository.consumeHint(state.effectiveEpochDay)
            }
            if (!consumed) return@launch

            val settings = state.profile.settings
            soundHaptic.playTap(settings.soundEnabled, settings.hapticsEnabled)

            when (puzzle) {
                is NumberChainPuzzle -> {
                    val wrongIndices = puzzle.options.indices.filter { puzzle.options[it] != puzzle.correctAnswer }
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                eliminatedOptionIndices = wrongIndices.take(2).toSet()
                            )
                        )
                    }
                }
                is OddShapePuzzle -> {
                    val wrongIndices = puzzle.tiles.indices.filter { it != puzzle.oddIndex }.take(2).toSet()
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                eliminatedOptionIndices = wrongIndices
                            )
                        )
                    }
                }
                is CodeBreakerPuzzle -> {
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                revealedCodePegIndex = 0
                            )
                        )
                    }
                }
                is QuickCalcPuzzle -> {
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                revealedCodePegIndex = puzzle.solutionCardIds.firstOrNull()
                            )
                        )
                    }
                }
                is WordTwistPuzzle -> {
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                revealedWordPrefix = puzzle.anchorWord.take(2)
                            )
                        )
                    }
                }
                is MemoryGridPuzzle -> {
                    // Brief 1.4s peek at the pattern
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                hintUsedThisRound = true,
                                memoryPreviewActive = true,
                                memoryPreviewRemainingMs = 1400L
                            )
                        )
                    }
                    delay(1400L)
                    _uiState.update { s ->
                        s.copy(
                            activeMatch = s.activeMatch?.copy(
                                memoryPreviewActive = false,
                                memoryPreviewRemainingMs = 0L
                            )
                        )
                    }
                }
            }
        }
    }

    private fun finishCurrentRound(isCorrect: Boolean, wordLength: Int = 0, isTimeUp: Boolean = false) {
        stopMatchTimer()
        val state = _uiState.value
        val match = state.activeMatch ?: return
        val puzzle = match.currentPuzzle ?: return
        val settings = state.profile.settings

        if (isCorrect) {
            soundHaptic.playCorrect(settings.soundEnabled, settings.hapticsEnabled)
        } else {
            soundHaptic.playWrong(settings.soundEnabled, settings.hapticsEnabled)
        }

        val playerPoints = PuzzleEngine.calculatePoints(
            puzzle = puzzle,
            isCorrect = isCorrect,
            elapsedMs = match.elapsedRoundMs,
            wordLength = wordLength
        )

        // Ensure rival round score is counted if player finished faster than rival
        val plan = match.rivalPlan
        val rivalRoundPoints = plan?.points ?: 0
        val updatedRivalScore = if (plan != null && !match.rivalHasSolvedCurrentRound) {
            match.rivalScore + rivalRoundPoints
        } else {
            match.rivalScore
        }

        val outcome = RoundOutcome(
            roundIndex = match.currentRoundIndex,
            puzzleType = puzzle.type,
            playerCorrect = isCorrect,
            playerPoints = playerPoints,
            playerTimeMs = match.elapsedRoundMs,
            rivalCorrect = plan?.willBeCorrect ?: false,
            rivalPoints = rivalRoundPoints,
            rivalTimeMs = plan?.solveDelayMs ?: 0L
        )

        val updatedMatch = match.copy(
            playerScore = match.playerScore + playerPoints,
            rivalScore = updatedRivalScore,
            rivalHasSolvedCurrentRound = true,
            roundsCompleted = match.roundsCompleted + outcome,
            isTransitioningRound = true,
            roundFeedback = RoundFeedbackBanner(
                isCorrect = isCorrect,
                pointsAwarded = playerPoints,
                messageIndex = match.currentRoundIndex % 3,
                isTimeUp = isTimeUp
            )
        )

        _uiState.update { it.copy(activeMatch = updatedMatch) }

        timerJob = viewModelScope.launch {
            delay(1050L)
            val nextIdx = updatedMatch.currentRoundIndex + 1
            if (nextIdx < updatedMatch.puzzles.size) {
                _uiState.update { s ->
                    s.copy(
                        activeMatch = updatedMatch.copy(
                            currentRoundIndex = nextIdx,
                            roundFeedback = null,
                            isTransitioningRound = false
                        )
                    )
                }
                beginCurrentRoundLoop()
            } else {
                completeMatch(updatedMatch)
            }
        }
    }

    private suspend fun completeMatch(match: ActiveMatchState) {
        val profile = _uiState.value.profile
        val previousRank = profile.rank

        val opponentScore = when (match.mode) {
            GameMode.DUEL -> match.rivalScore
            GameMode.FRIEND_CHALLENGE -> match.friendChallenge?.targetScore ?: 0
            else -> 0
        }

        val correctCount = match.roundsCompleted.count { it.playerCorrect }
        val starsEarned = if (match.mode == GameMode.CAMPAIGN_LEVEL) {
            PuzzleEngine.calculateLevelStars(correctCount, match.roundsCompleted.size)
        } else {
            0
        }

        val verdict = when (match.mode) {
            GameMode.CAMPAIGN_LEVEL -> if (starsEarned >= 1) MatchVerdict.WIN else MatchVerdict.LOSS
            GameMode.DUEL -> when {
                match.playerScore > opponentScore -> MatchVerdict.WIN
                match.playerScore == opponentScore -> MatchVerdict.DRAW
                else -> MatchVerdict.LOSS
            }
            GameMode.FRIEND_CHALLENGE -> {
                val target = match.friendChallenge?.targetScore
                if (target == null) MatchVerdict.COMPLETED
                else when {
                    match.playerScore > target -> MatchVerdict.WIN
                    match.playerScore == target -> MatchVerdict.DRAW
                    else -> MatchVerdict.LOSS
                }
            }
            GameMode.DAILY, GameMode.PRACTICE, GameMode.ONBOARDING -> MatchVerdict.COMPLETED
        }

        val xpEarned = PuzzleEngine.calculateXpEarned(
            correctCount = correctCount,
            totalRounds = match.roundsCompleted.size,
            wonMatch = verdict == MatchVerdict.WIN || verdict == MatchVerdict.COMPLETED,
            isDaily = match.mode == GameMode.DAILY,
            isPractice = match.mode == GameMode.PRACTICE
        )

        val effects = repository.recordMatchResult(
            mode = match.mode,
            verdict = verdict,
            playerScore = match.playerScore,
            xpEarned = xpEarned,
            rounds = match.roundsCompleted,
            deviceEpochDay = System.currentTimeMillis() / 86_400_000L,
            levelNumber = match.levelNumber,
            starsEarned = starsEarned
        )

        val newRank = RankTier.fromXp(effects.newXp)
        if (verdict == MatchVerdict.WIN || verdict == MatchVerdict.COMPLETED) {
            soundHaptic.playWinFanfare(profile.settings.soundEnabled, profile.settings.hapticsEnabled)
        }

        val finalChallenge = match.friendChallenge?.let { fc ->
            if (fc.targetScore == null) fc.copy(targetScore = match.playerScore) else fc
        }

        val result = MatchResult(
            mode = match.mode,
            verdict = verdict,
            playerScore = match.playerScore,
            rivalScore = opponentScore,
            rivalId = match.rivalId,
            friendChallenge = finalChallenge,
            dailyEpochDay = match.dailyEpochDay,
            rounds = match.roundsCompleted,
            xpEarned = xpEarned,
            previousRank = previousRank,
            newRank = newRank,
            streakAfter = effects.newStreak,
            firstSessionBadgeUnlocked = effects.firstSessionBadgeUnlocked,
            day3FreezeGiftUnlocked = effects.day3FreezeGiftUnlocked,
            levelNumber = match.levelNumber,
            starsEarned = starsEarned,
            unlockedNextLevel = effects.unlockedNextLevel
        )

        _uiState.update {
            it.copy(
                screen = AppScreen.Result,
                lastResult = result,
                activeMatch = match
            )
        }
    }

    private fun stopMatchTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Settings updates
    fun updateNickname(name: String) {
        viewModelScope.launch { repository.updateNickname(name) }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(language)
            if (_uiState.value.profile.settings.reminderEnabled) {
                DailyReminderScheduler.scheduleDailyReminder(getApplication(), language)
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateSoundEnabled(enabled) }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.updateHapticsEnabled(enabled) }
    }

    fun updateReduceMotion(enabled: Boolean) {
        viewModelScope.launch { repository.updateReduceMotion(enabled) }
    }

    fun updateHardMode(enabled: Boolean) {
        viewModelScope.launch { repository.updateHardMode(enabled) }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminderEnabled(enabled)
            val app = getApplication<Application>()
            if (enabled) {
                DailyReminderScheduler.scheduleDailyReminder(app, _uiState.value.profile.settings.language)
            } else {
                DailyReminderScheduler.cancelDailyReminder(app)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMatchTimer()
        soundHaptic.release()
    }
}
