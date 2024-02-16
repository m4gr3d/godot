package org.godotengine.godot.input;

import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;

import java.util.Locale;

/**
 * Enum must be kept up-to-date with DisplayServer::VirtualKeyboardType
 */
public enum VirtualKeyboardType {
	KEYBOARD_TYPE_DEFAULT,
	KEYBOARD_TYPE_MULTILINE,
	KEYBOARD_TYPE_NUMBER,
	KEYBOARD_TYPE_NUMBER_DECIMAL,
	KEYBOARD_TYPE_PHONE,
	KEYBOARD_TYPE_EMAIL_ADDRESS,
	KEYBOARD_TYPE_PASSWORD,
	KEYBOARD_TYPE_URL;

	/**
	 * Returns the Android input type corresponding to the given {@link VirtualKeyboardType} type.
	 */
	public int toAndroidInputType() {
		final int inputType;
		String acceptCharacters = null;
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
				acceptCharacters = "0123456789,.- ";
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

		if (!TextUtils.isEmpty(acceptCharacters)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				//				edit.setKeyListener(DigitsKeyListener.getInstance(Locale.getDefault()));
			} else {
				//				edit.setKeyListener(DigitsKeyListener.getInstance(acceptCharacters));
			}
		}

		return inputType;
	}
}
