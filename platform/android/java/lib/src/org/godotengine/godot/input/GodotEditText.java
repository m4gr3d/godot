/**************************************************************************/
/*  GodotEditText.java                                                    */
/**************************************************************************/
/*                         This file is part of:                          */
/*                             GODOT ENGINE                               */
/*                        https://godotengine.org                         */
/**************************************************************************/
/* Copyright (c) 2014-present Godot Engine contributors (see AUTHORS.md). */
/* Copyright (c) 2007-2014 Juan Linietsky, Ariel Manzur.                  */
/*                                                                        */
/* Permission is hereby granted, free of charge, to any person obtaining  */
/* a copy of this software and associated documentation files (the        */
/* "Software"), to deal in the Software without restriction, including    */
/* without limitation the rights to use, copy, modify, merge, publish,    */
/* distribute, sublicense, and/or sell copies of the Software, and to     */
/* permit persons to whom the Software is furnished to do so, subject to  */
/* the following conditions:                                              */
/*                                                                        */
/* The above copyright notice and this permission notice shall be         */
/* included in all copies or substantial portions of the Software.        */
/*                                                                        */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. */
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY   */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,   */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE      */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                 */
/**************************************************************************/

package org.godotengine.godot.input;

import org.godotengine.godot.*;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.Locale;

public class GodotEditText extends EditText {
	private static final String TAG = GodotEditText.class.getSimpleName();
	private static final InputFilter[] NO_INPUT_FILTERS = new InputFilter[0];

	// Enum must be kept up-to-date with DisplayServer::VirtualKeyboardType
	public enum VirtualKeyboardType {
		KEYBOARD_TYPE_DEFAULT,
		KEYBOARD_TYPE_MULTILINE,
		KEYBOARD_TYPE_NUMBER,
		KEYBOARD_TYPE_NUMBER_DECIMAL,
		KEYBOARD_TYPE_PHONE,
		KEYBOARD_TYPE_EMAIL_ADDRESS,
		KEYBOARD_TYPE_PASSWORD,
		KEYBOARD_TYPE_URL;

		int toAndroidInputType() {
			final int inputType;
			switch (this) {
				case KEYBOARD_TYPE_DEFAULT:
				default:
					inputType = InputType.TYPE_CLASS_TEXT;
					break;
				case KEYBOARD_TYPE_MULTILINE:
					inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
					break;
				case KEYBOARD_TYPE_NUMBER:
					inputType = InputType.TYPE_CLASS_NUMBER;
					break;
				case KEYBOARD_TYPE_NUMBER_DECIMAL:
					inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL;
					break;
				case KEYBOARD_TYPE_PHONE:
					inputType = InputType.TYPE_CLASS_PHONE;
					break;
				case KEYBOARD_TYPE_EMAIL_ADDRESS:
					inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
					break;
				case KEYBOARD_TYPE_PASSWORD:
					inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
					break;
				case KEYBOARD_TYPE_URL:
					inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
					break;
			}
			return inputType;
		}
	}

	// ===========================================================
	// Fields
	// ===========================================================
	private GodotRenderView mRenderView;
	private @Nullable GodotTextInputWrapper mInputWrapper;
	private VirtualKeyboardType mKeyboardType = VirtualKeyboardType.KEYBOARD_TYPE_DEFAULT;

	// ===========================================================
	// Constructors
	// ===========================================================
	public GodotEditText(final Context context) {
		super(context);
		initView();
	}

	public GodotEditText(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public GodotEditText(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	protected void initView() {
		setPadding(0, 0, 0, 0);
		setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
	}

	public VirtualKeyboardType getKeyboardType() {
		return mKeyboardType;
	}

	private void updateMaxInputLength(int maxInputLength) {
		InputFilter[] filters = new InputFilter[1];
		filters[0] = new InputFilter.LengthFilter(maxInputLength);
		setFilters(filters);
	}

	private void disableMaxInputLength() {
		setFilters(NO_INPUT_FILTERS);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setView(final GodotRenderView view) {
		mRenderView = view;
		if (mInputWrapper == null) {
			mInputWrapper = new GodotTextInputWrapper(mRenderView, this);
		}
		setOnEditorActionListener(mInputWrapper);
		view.getView().requestFocus();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		Log.d(TAG, "onCreateInputConnection()");
		InputConnection baseInputConnection = super.onCreateInputConnection(outAttrs);
		return new GodotInputConnectionWrapper(baseInputConnection);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
		Log.d(TAG, "onKeyDown(" + keyCode + ", " + keyEvent);

		// When a hardware keyboard is connected, all key events come through so we can route them
		// directly to the engine.
		// This is not the case when using a soft keyboard, requiring extra processing from this class.
		if (hasHardwareKeyboard()) {
			return mRenderView.getInputHandler().onKeyDown(keyCode, keyEvent);
		}

		if (needHandlingInGodot(keyCode, keyEvent) && mRenderView.getInputHandler().onKeyDown(keyCode, keyEvent)) {
			return true;
		} else {
			return super.onKeyDown(keyCode, keyEvent);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		Log.d(TAG, "onKeyUp(" + keyCode + ", " + keyEvent);
		// When a hardware keyboard is connected, all key events come through so we can route them
		// directly to the engine.
		// This is not the case when using a soft keyboard, requiring extra processing from this class.
		if (hasHardwareKeyboard()) {
			return mRenderView.getInputHandler().onKeyUp(keyCode, keyEvent);
		}

		if (needHandlingInGodot(keyCode, keyEvent) && mRenderView.getInputHandler().onKeyUp(keyCode, keyEvent)) {
			return true;
		} else {
			return super.onKeyUp(keyCode, keyEvent);
		}
	}

	private boolean needHandlingInGodot(int keyCode, KeyEvent keyEvent) {
		boolean isArrowKey = keyCode == KeyEvent.KEYCODE_DPAD_UP ||
			keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
			keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
			keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;

		boolean isModifiedKey = !(keyEvent.isCtrlPressed() &&
			(
				keyCode == KeyEvent.KEYCODE_A || // Select all combo
					keyCode == KeyEvent.KEYCODE_Z || // undo combo
					keyCode == KeyEvent.KEYCODE_X || // cut combo
					keyCode == KeyEvent.KEYCODE_C || // copy combo
					keyCode == KeyEvent.KEYCODE_V || // paste combo
					keyCode == KeyEvent.KEYCODE_Y    // redo combo
			)) &&
			(keyEvent.isAltPressed() || keyEvent.isCtrlPressed() || keyEvent.isSymPressed() ||
				keyEvent.isFunctionPressed() || keyEvent.isMetaPressed());

		boolean isBackKey = keyCode == KeyEvent.KEYCODE_ESCAPE ||
			keyCode == KeyEvent.KEYCODE_BACK;

		return keyCode == KeyEvent.KEYCODE_TAB || KeyEvent.isModifierKey(keyCode) || isBackKey || isArrowKey || isModifiedKey;
	}

	boolean hasHardwareKeyboard() {
		Configuration config = getResources().getConfiguration();
		boolean hasHardwareKeyboardConfig = config.keyboard != Configuration.KEYBOARD_NOKEYS &&
				config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
		if (hasHardwareKeyboardConfig) {
			return true;
		}

		return mRenderView.getInputHandler().hasHardwareKeyboard();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	/**
	 * Show the soft keyboard
	 * <p>
	 * Must be called from the UI thread.
	 * @param existingText
	 * @param keyboardType
	 * @param maxInputLength
	 * @param cursorStart
	 * @param cursorEnd
	 */
	public void showKeyboard(String existingText, VirtualKeyboardType keyboardType, int maxInputLength, int cursorStart, int cursorEnd) {
		if (hasHardwareKeyboard()) {
			return;
		}

		if (!requestFocus() || mInputWrapper == null) {
			return;
		}

		removeTextChangedListener(mInputWrapper);

		this.mKeyboardType = keyboardType;
		boolean disableInputLimit = maxInputLength <= 0;

		String originText;
		if (cursorStart == -1) { // cursor position not given
			originText = existingText;
			cursorStart = 0;
		} else if (cursorEnd == -1) { // not text selection
			originText = existingText.substring(0, cursorStart);
			maxInputLength = maxInputLength - (existingText.length() - cursorStart);
		} else {
			originText = existingText.substring(0, cursorEnd);
			maxInputLength = maxInputLength - (existingText.length() - cursorEnd);
		}

		if (disableInputLimit) {
			disableMaxInputLength();
		} else {
			updateMaxInputLength(maxInputLength);
		}

		setText(originText);
		setSelection(Math.min(cursorStart, length()));
		if (cursorEnd >= cursorStart) {
			extendSelection(Math.min(cursorEnd, length()));
			mInputWrapper.setSelection(true);
		} else {
			mInputWrapper.setSelection(false);
		}

		setInputType(keyboardType.toAndroidInputType());
		if (keyboardType == VirtualKeyboardType.KEYBOARD_TYPE_NUMBER_DECIMAL) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				setKeyListener(DigitsKeyListener.getInstance(Locale.getDefault()));
			} else {
				setKeyListener(DigitsKeyListener.getInstance("0123456789,.- "));
			}
		}

		addTextChangedListener(mInputWrapper);
		final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(this, 0);
	}

	/**
	 * Hide the soft keyboard.
	 * <p>
	 * Must be called from the UI thread.
	 */
	public void hideKeyboard() {
		if (hasHardwareKeyboard()) {
			return;
		}

		if (mInputWrapper != null) {
			removeTextChangedListener(mInputWrapper);
		}
		final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
		mRenderView.getView().requestFocus();
	}
}
