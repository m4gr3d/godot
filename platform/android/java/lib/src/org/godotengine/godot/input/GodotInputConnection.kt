package org.godotengine.godot.input

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputContentInfo
import androidx.annotation.RequiresApi

class GodotInputConnection(targetView: View) : BaseInputConnection(targetView, false) {

	companion object {
		private val TAG = GodotInputConnection::class.java.simpleName
	}

	override fun sendKeyEvent(event: KeyEvent?): Boolean {
		Log.d(TAG, "sendKeyEvent($event)")
		return super.sendKeyEvent(event)
	}

	override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? {
		Log.d(TAG, "getTextBeforeCursor($n, $flags)")
		return super.getTextBeforeCursor(n, flags)
	}

	override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? {
		Log.d(TAG, "getTextAfterCursor($n, $flags)")
		return super.getTextAfterCursor(n, flags)
	}

	override fun getSelectedText(flags: Int): CharSequence? {
		Log.d(TAG, "getSelectedText($flags)")
		return super.getSelectedText(flags)
	}

	override fun getCursorCapsMode(reqModes: Int): Int {
		Log.d(TAG, "getCursorCapsMode($reqModes)")
		return super.getCursorCapsMode(reqModes)
	}

	override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? {
		Log.d(TAG, "getExtractedText($request, $flags)")
		return super.getExtractedText(request, flags)
	}

	override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
		Log.d(TAG, "deleteSurroundingText($beforeLength, $afterLength)")
		return super.deleteSurroundingText(beforeLength, afterLength)
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
		Log.d(TAG, "deleteSurroundingTextInCodePoints($beforeLength, $afterLength)")
		return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
	}

	override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
		Log.d(TAG, "setComposingText($text, $newCursorPosition)")
		return super.setComposingText(text, newCursorPosition)
	}

	override fun setComposingRegion(start: Int, end: Int): Boolean {
		Log.d(TAG, "setComposingRegion($start, $end)")
		return super.setComposingRegion(start, end)
	}

	override fun finishComposingText(): Boolean {
		Log.d(TAG, "finishComposingText()")
		return super.finishComposingText()
	}

	override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
		Log.d(TAG, "commitText($text, $newCursorPosition)")
		return super.commitText(text, newCursorPosition)
	}

	override fun commitCompletion(text: CompletionInfo?): Boolean {
		Log.d(TAG, "commitCompletion($text)")
		return super.commitCompletion(text)
	}

	override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
		Log.d(TAG, "commitCorrection($correctionInfo)")
		return super.commitCorrection(correctionInfo)
	}

	override fun setSelection(start: Int, end: Int): Boolean {
		Log.d(TAG, "setSelection($start, $end)")
		return super.setSelection(start, end)
	}

	override fun performEditorAction(editorAction: Int): Boolean {
		Log.d(TAG, "performEditorAction($editorAction)")
		return super.performEditorAction(editorAction)
	}

	override fun performContextMenuAction(id: Int): Boolean {
		Log.d(TAG, "performContextMenuAction($id)")
		return super.performContextMenuAction(id)
	}

	override fun beginBatchEdit(): Boolean {
		Log.d(TAG, "beginBatchEdit()")
		return super.beginBatchEdit()
	}

	override fun endBatchEdit(): Boolean {
		Log.d(TAG, "endBatchEdit()")
		return super.endBatchEdit()
	}

	override fun clearMetaKeyStates(states: Int): Boolean {
		Log.d(TAG, "clearMetaKeyStates($states)")
		return super.clearMetaKeyStates(states)
	}

	override fun reportFullscreenMode(enabled: Boolean): Boolean {
		Log.d(TAG, "reportFullscreenMode($enabled)")
		return super.reportFullscreenMode(enabled)
	}

	override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
		Log.d(TAG, "performPrivateCommand($action, $data)")
		return super.performPrivateCommand(action, data)
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
		Log.d(TAG, "requestCursorUpdates($cursorUpdateMode)")
		return super.requestCursorUpdates(cursorUpdateMode)
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun getHandler(): Handler? {
		Log.d(TAG, "getHandler()")
		return super.getHandler()
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override fun closeConnection() {
		Log.d(TAG, "closeConnection()")
		return super.closeConnection()
	}

	@RequiresApi(Build.VERSION_CODES.N_MR1)
	override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean {
		Log.d(TAG, "commitContent($inputContentInfo, $flags, $opts)")
		return super.commitContent(inputContentInfo, flags, opts)
	}
}
