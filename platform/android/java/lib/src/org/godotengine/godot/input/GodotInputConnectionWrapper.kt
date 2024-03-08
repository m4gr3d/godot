package org.godotengine.godot.input

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import androidx.annotation.RequiresApi

/**
 * Godot [InputConnection] implementation used to route IME input events to the native engine.
 */
class GodotInputConnectionWrapper(private val wrappedInputConnection: InputConnection) : InputConnection {

	companion object {
		private val TAG = "FHK" //GodotInputConnection::class.java.simpleName
	}

	override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? {
		Log.d(TAG, "getTextBeforeCursor($n, $flags)")
		return wrappedInputConnection.getTextBeforeCursor(n, flags)
	}

	override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? {
		Log.d(TAG, "getTextAfterCursor($n, $flags)")
		return wrappedInputConnection.getTextAfterCursor(n, flags)
	}

	override fun getSelectedText(flags: Int): CharSequence {
		Log.d(TAG, "getSelectedText($flags)")
		return wrappedInputConnection.getSelectedText(flags)
	}

	override fun getCursorCapsMode(reqModes: Int): Int {
		Log.d(TAG, "getCursorCapsMode($reqModes)")
		return wrappedInputConnection.getCursorCapsMode(reqModes)
	}

	override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText {
		Log.d(TAG, "getExtractedText($request, $flags)")
		return wrappedInputConnection.getExtractedText(request, flags)
	}

	override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
		Log.d(TAG, "deleteSurroundingText($beforeLength, $afterLength)")
		return wrappedInputConnection.deleteSurroundingText(beforeLength, afterLength)
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
		Log.d(TAG, "deleteSurroundingTextInCodePoints($beforeLength, $afterLength)")
		return wrappedInputConnection.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
	}

	override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
		Log.d(TAG, "setComposingText($text, $newCursorPosition)")
		return wrappedInputConnection.setComposingText(text, newCursorPosition)
	}

	override fun setComposingRegion(start: Int, end: Int): Boolean {
		Log.d(TAG, "setComposingRegion($start, $end)")
		return wrappedInputConnection.setComposingRegion(start, end)
	}

	override fun finishComposingText(): Boolean {
		Log.d(TAG, "finishComposingText()")
		return wrappedInputConnection.finishComposingText()
	}

	override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
		Log.d(TAG, "commitText($text, $newCursorPosition)")
		return wrappedInputConnection.commitText(text, newCursorPosition)
	}

	override fun commitCompletion(text: CompletionInfo?): Boolean {
		Log.d(TAG, "commitCompletion($text)")
		return wrappedInputConnection.commitCompletion(text)
	}

	override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
		Log.d(TAG, "commitCorrection($correctionInfo)")
		return wrappedInputConnection.commitCorrection(correctionInfo)
	}

	override fun setSelection(start: Int, end: Int): Boolean {
		Log.d(TAG, "setSelection($start, $end)")
		return wrappedInputConnection.setSelection(start, end)
	}

	override fun performEditorAction(editorAction: Int): Boolean {
		Log.d(TAG, "performEditorAction($editorAction)")
		return wrappedInputConnection.performEditorAction(editorAction)
	}

	override fun performContextMenuAction(id: Int): Boolean {
		Log.d(TAG, "performContextMenuAction($id)")
		return wrappedInputConnection.performContextMenuAction(id)
	}

	override fun beginBatchEdit(): Boolean {
		Log.d(TAG, "beginBatchEdit()")
		return wrappedInputConnection.beginBatchEdit()
	}

	override fun endBatchEdit(): Boolean {
		Log.d(TAG, "endBatchEdit()")
		return wrappedInputConnection.endBatchEdit()
	}

	override fun sendKeyEvent(event: KeyEvent?): Boolean {
		Log.d(TAG, "sendKeyEvent($event)")
		return wrappedInputConnection.sendKeyEvent(event)
	}

	override fun clearMetaKeyStates(states: Int): Boolean {
		Log.d(TAG, "clearMetaKeyStates($states)")
		return wrappedInputConnection.clearMetaKeyStates(states)
	}

	override fun reportFullscreenMode(enabled: Boolean): Boolean {
		Log.d(TAG, "reportFullscreenMode($enabled)")
		return wrappedInputConnection.reportFullscreenMode(enabled)
	}

	override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
		Log.d(TAG, "performPrivateCommand($action, $data)")
		return wrappedInputConnection.performPrivateCommand(action, data)
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
		Log.d(TAG, "requestCursorUpdates($cursorUpdateMode)")
		return wrappedInputConnection.requestCursorUpdates(cursorUpdateMode)
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun getHandler(): Handler? {
		Log.d(TAG, "getHandler()")
		return wrappedInputConnection.getHandler()
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun closeConnection() {
		Log.d(TAG, "closeConnection()")
		return wrappedInputConnection.closeConnection()
	}

	@RequiresApi(Build.VERSION_CODES.N_MR1)
	override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean {
		Log.d(TAG, "commitContent($inputContentInfo, $flags, $opts)")
		return wrappedInputConnection.commitContent(inputContentInfo, flags, opts)
	}
}
