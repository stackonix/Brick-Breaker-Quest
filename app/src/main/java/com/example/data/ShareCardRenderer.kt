package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.domain.model.BrainSkills
import com.example.domain.model.MatchResult
import com.example.domain.model.MatchVerdict
import com.example.domain.model.RankTier
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates crisp White Game Theme PNG share cards (1080x1350) with zero personal data
 * and invokes the standard Android Share Sheet.
 */
object ShareCardRenderer {

    fun shareResultCard(
        context: Context,
        nickname: String,
        result: MatchResult,
        rankLabel: String,
        verdictTitle: String,
        shareCaption: String
    ) {
        val bitmap = renderResultBitmap(nickname, result, rankLabel, verdictTitle)
        shareBitmap(context, bitmap, "mindfeud_result.png", shareCaption)
    }

    fun shareBrainProfileCard(
        context: Context,
        nickname: String,
        rank: RankTier,
        rankLabel: String,
        streak: Int,
        skills: BrainSkills,
        skillLabels: List<String>,
        shareCaption: String
    ) {
        val bitmap = renderBrainProfileBitmap(nickname, rank, rankLabel, streak, skills, skillLabels)
        shareBitmap(context, bitmap, "mindfeud_brain_profile.png", shareCaption)
    }

    fun shareTextInvite(context: Context, title: String, message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String, caption: String) {
        runCatching {
            val dir = File(context.cacheDir, "cards").apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Mindfeud Card"))
        }.onFailure {
            shareTextInvite(context, "Mindfeud", caption)
        }
    }

    private fun renderResultBitmap(
        nickname: String,
        result: MatchResult,
        rankLabel: String,
        verdictTitle: String
    ): Bitmap {
        val w = 1080
        val h = 1350
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#F2F5FF"), Color.parseColor("#E9EEFF")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val accentColor = when (result.verdict) {
            MatchVerdict.WIN, MatchVerdict.COMPLETED -> Color.parseColor("#047857")
            MatchVerdict.DRAW -> Color.parseColor("#5B3FD0")
            MatchVerdict.LOSS -> Color.parseColor("#BE123C")
        }

        // Tactile 3D lip shadow
        val lipRect = RectF(72f, 108f, w - 72f, h - 84f)
        val lipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#C3CCEE") }
        canvas.drawRoundRect(lipRect, 48f, 48f, lipPaint)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.parseColor("#5B3FD0")
        }

        val mainCard = RectF(72f, 96f, w - 72f, h - 96f)
        canvas.drawRoundRect(mainCard, 48f, 48f, cardPaint)
        canvas.drawRoundRect(mainCard, 48f, 48f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B3FD0")
            textSize = 46f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MINDFEUD • DAILY LOGIC DUEL", w / 2f, 195f, titlePaint)

        val verdictPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 86f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(verdictTitle.uppercase(), w / 2f, 320f, verdictPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F5684")
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$nickname  •  $rankLabel", w / 2f, 395f, subPaint)

        // Big score box
        val scoreBox = RectF(130f, 460f, w - 130f, 750f)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F0F3FF") }
        canvas.drawRoundRect(scoreBox, 36f, 36f, boxPaint)

        val bigScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#14172F")
            textSize = 136f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("${result.playerScore}", w / 2f, 625f, bigScorePaint)
        canvas.drawText(
            "ACCURACY ${result.accuracyPercent}%  •  STREAK ${result.streakAfter}d",
            w / 2f,
            705f,
            subPaint.apply { textSize = 34f }
        )

        // Round status pills
        val pillStartX = (w - (result.rounds.size * 116f - 26f)) / 2f
        result.rounds.forEachIndexed { idx, round ->
            val left = pillStartX + idx * 116f
            val rect = RectF(left, 825f, left + 90f, 915f)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (round.playerCorrect) Color.parseColor("#10B981") else Color.parseColor("#F43F5E")
            }
            canvas.drawRoundRect(rect, 22f, 22f, p)
            val symPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 48f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(if (round.playerCorrect) "✓" else "✕", rect.centerX(), rect.centerY() + 17f, symPaint)
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B45309")
            textSize = 42f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val codeLine = result.friendChallenge?.code?.let { "CHALLENGE CODE: $it" }
            ?: "CAN YOU BEAT MY SCORE IN 60 SEC?"
        canvas.drawText(codeLine, w / 2f, 1080f, footerPaint)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F5684")
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Offline Logic Duels • No Ads • Pure Skill", w / 2f, 1170f, tagPaint)
        return bmp
    }

    private fun renderBrainProfileBitmap(
        nickname: String,
        rank: RankTier,
        rankLabel: String,
        streak: Int,
        skills: BrainSkills,
        skillLabels: List<String>
    ): Bitmap {
        val w = 1080
        val h = 1350
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.WHITE, Color.parseColor("#F0F3FF")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val borderColor = if (rank == RankTier.LEGEND) Color.parseColor("#F59E0B") else Color.parseColor("#5B3FD0")
        val frameRect = RectF(64f, 64f, w - 64f, h - 64f)
        val frameBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = borderColor
        }
        canvas.drawRoundRect(frameRect, 48f, 48f, frameBg)
        canvas.drawRoundRect(frameRect, 48f, 48f, framePaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B3FD0")
            textSize = 44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MINDFEUD BRAIN PROFILE", w / 2f, 165f, headerPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#14172F")
            textSize = 74f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(nickname, w / 2f, 260f, namePaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B45309")
            textSize = 38f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$rankLabel  •  ${streak}d Streak  •  AVG ${skills.average()}", w / 2f, 330f, metaPaint)

        val cx = w / 2f
        val cy = 720f
        val radius = 270f
        val values = listOf(skills.logic, skills.speed, skills.memory, skills.pattern, skills.word)

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#D5DCF5")
        }
        for (ring in 1..4) {
            val r = radius * (ring / 4f)
            val path = Path()
            for (i in 0 until 5) {
                val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            canvas.drawPath(path, gridPaint)
        }

        val dataPath = Path()
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#14172F")
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        for (i in 0 until 5) {
            val angle = -PI / 2.0 + i * (2.0 * PI / 5.0)
            val norm = (values[i].coerceIn(10, 100)) / 100f
            val x = cx + (radius * norm * cos(angle)).toFloat()
            val y = cy + (radius * norm * sin(angle)).toFloat()
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)

            val lx = cx + ((radius + 62f) * cos(angle)).toFloat()
            val ly = cy + ((radius + 54f) * sin(angle)).toFloat()
            val label = skillLabels.getOrNull(i) ?: ""
            canvas.drawText("$label ${values[i]}", lx, ly, labelPaint)
        }
        dataPath.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 91, 63, 208)
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B3FD0")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawPath(dataPath, fillPaint)
        canvas.drawPath(dataPath, strokePaint)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F5684")
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Mindfeud • 60-Second Daily Logic Duels", w / 2f, 1210f, footerPaint)
        return bmp
    }
}
