package com.signalzero.app

import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.DeliveryStatus
import com.signalzero.app.data.model.EmergencyCategory
import org.junit.Assert.*
import org.junit.Test

class LocalizationAndMessageTest {

    @Test
    fun testAllSevenEmergencyCategoriesHaveEnglishAndHindiTemplates() {
        val categories = EmergencyCategory.values()
        assertEquals("Must support exactly 7 emergency categories", 7, categories.size)

        for (category in categories) {
            val enName = category.getDisplayName(AppLanguage.ENGLISH)
            val hiName = category.getDisplayName(AppLanguage.HINDI)

            assertFalse("English category name must not be blank for $category", enName.isBlank())
            assertFalse("Hindi category name must not be blank for $category", hiName.isBlank())
            assertNotEquals("Hindi category name must differ from English for $category", enName, hiName)

            val enMsg = category.getTemplateMessage(AppLanguage.ENGLISH)
            val hiMsg = category.getTemplateMessage(AppLanguage.HINDI)

            assertFalse("English template must not be blank for $category", enMsg.isBlank())
            assertFalse("Hindi template must not be blank for $category", hiMsg.isBlank())
            assertTrue("English message must contain meaningful SOS text", enMsg.length > 15)
            assertTrue("Hindi message must contain meaningful SOS text", hiMsg.length > 15)
        }
    }

    @Test
    fun testDeliveryStatusTranslationsAreComplete() {
        val statuses = DeliveryStatus.values()
        for (status in statuses) {
            val enLabel = status.getDisplayName(AppLanguage.ENGLISH)
            val hiLabel = status.getDisplayName(AppLanguage.HINDI)

            assertFalse("English label must not be blank for $status", enLabel.isBlank())
            assertFalse("Hindi label must not be blank for $status", hiLabel.isBlank())
        }
    }
}
