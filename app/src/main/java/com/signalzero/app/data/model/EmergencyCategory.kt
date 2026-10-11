package com.signalzero.app.data.model

import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization

enum class EmergencyCategory(
    val id: String,
    val translationKey: String,
    val templateKey: String,
    val severityLevel: Int // 1 (highest) to 4
) {
    MEDICAL("MEDICAL", "cat_medical", "template_medical", 1),
    FIRE("FIRE", "cat_fire", "template_fire", 1),
    FLOOD("FLOOD", "cat_flood", "template_flood", 2),
    EARTHQUAKE("EARTHQUAKE", "cat_earthquake", "template_earthquake", 1),
    TRAPPED("TRAPPED", "cat_trapped", "template_trapped", 1),
    PERSONAL_SAFETY("PERSONAL_SAFETY", "cat_personal_safety", "template_personal_safety", 2),
    OTHER("OTHER", "cat_other", "template_other", 3);

    fun getDisplayName(language: AppLanguage): String = Localization.get(translationKey, language)
    fun getTemplateMessage(language: AppLanguage): String = Localization.get(templateKey, language)

    companion object {
        fun fromString(value: String): EmergencyCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.id.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
