package com.signalzero.app.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

interface VoiceSosListener {
    fun onVoicePhraseDetected(phrase: String)
}

class VoiceSosDetector(
    private val context: Context,
    var listener: VoiceSosListener? = null
) : RecognitionListener {

    private val tag = "SignalZero_Voice"
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    var isTestMode = false
    private var lastTriggerTime = 0L
    private val cooldownMs = 6000L

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(onDetected: (String) -> Unit) {
        listener = object : VoiceSosListener {
            override fun onVoicePhraseDetected(phrase: String) {
                onDetected(phrase)
            }
        }
        startListening("en-US")
    }

    fun startListening(languageCode: String = "en-US") {
        if (!isAvailable() || isListening) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@VoiceSosDetector)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Log.i(tag, "Voice emergency listener started for language: $languageCode")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognizer: ${e.message}")
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(tag, "Error destroying speech recognizer: ${e.message}")
        }
        speechRecognizer = null
        isListening = false
        Log.i(tag, "Voice emergency listener stopped.")
    }

    override fun onResults(results: Bundle?) {
        processSpeechBundle(results)
        // Restart listening if still active
        if (isListening) {
            speechRecognizer?.destroy()
            startListening()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        processSpeechBundle(partialResults)
    }

    private fun processSpeechBundle(bundle: Bundle?) {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < cooldownMs) return

        for (text in matches) {
            val lower = text.lowercase().trim()
            if (lower.contains("help me") || lower.contains("मदद करो") || lower.contains("bachao") || lower.contains("बचाओ")) {
                lastTriggerTime = now
                Log.i(tag, "Emergency voice phrase recognized: $text")
                listener?.onVoicePhraseDetected(text)
                break
            }
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {
        // Recover and restart if appropriate
        if (isListening && error != SpeechRecognizer.ERROR_NO_MATCH) {
            speechRecognizer?.destroy()
            startListening()
        }
    }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
