package com.example.droiddevs

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText

class DeletableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onDeleteKeyListener: (() -> Unit)? = null

    /**
     * Catches hardware keyboard events.
     */
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (text.toString().isEmpty()) {
                onDeleteKeyListener?.invoke()
                return true // Consume the event
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    /**
     * Catches soft (on-screen) keyboard events by wrapping the InputConnection.
     * This handles multiple methods that soft keyboards might use for delete operations.
     */
    override fun onCreateInputConnection(outAttrs: android.view.inputmethod.EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(ic, true) {

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                // Handle the most common soft keyboard delete method
                if (beforeLength == 1 && afterLength == 0 && text.toString().isEmpty()) {
                    onDeleteKeyListener?.invoke()
                    return true // Consume the soft keyboard's backspace
                }
                return super.deleteSurroundingText(beforeLength, afterLength)
            }

            override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
                // Handle Unicode code point deletion (for some keyboards)
                if (beforeLength == 1 && afterLength == 0 && text.toString().isEmpty()) {
                    onDeleteKeyListener?.invoke()
                    return true
                }
                return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
            }

            override fun sendKeyEvent(event: KeyEvent): Boolean {
                // Handle key events sent through InputConnection
                if (event.keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    text.toString().isEmpty()) {
                    onDeleteKeyListener?.invoke()
                    return true
                }
                return super.sendKeyEvent(event)
            }

            override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
                // Handle backspace during text composition (some IMEs)
                if (text?.isEmpty() == true && this@DeletableEditText.text.toString().isEmpty()) {
                    onDeleteKeyListener?.invoke()
                    return true
                }
                return super.setComposingText(text, newCursorPosition)
            }

            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                // Additional safety check for empty text commits
                if (text?.isEmpty() == true && this@DeletableEditText.text.toString().isEmpty()) {
                    // This might be a delete operation, but we need to be careful
                    // Only trigger if we're sure it's a delete, not just an empty commit
                    return super.commitText(text, newCursorPosition)
                }
                return super.commitText(text, newCursorPosition)
            }
        }
    }
}