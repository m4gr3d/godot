/**************************************************************************/
/*  SubWindowManager.kt                                                   */
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


package org.godotengine.godot.utils

import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import org.godotengine.godot.Godot
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Godot sub windows.
 */
internal object SubWindowManager {
	private val TAG = SubWindowManager::class.java.simpleName

	internal const val MAIN_WINDOW_ID = 0
	internal const val INVALID_WINDOW_ID = -1

	/**
	 * Must match the enum in 'servers/display_server.h#WindowMode'
	 */
	enum class WindowMode {
		WINDOWED,
		MINIMIZED,
		MAXIMIZED,
		FULLSCREEN,
		EXCLUSIVE_FULLSCREEN;

		companion object {
			@JvmStatic
			fun fromNative(nativeMode: Int): WindowMode {
				for (mode in WindowMode.entries) {
					if (mode.ordinal == nativeMode) {
						return mode
					}
				}
				return WINDOWED
			}
		}
	}

	private data class SubWindowState(
		val window: PopupWindow,
		var positionX: Int,
		var positionY: Int,
		val mode: WindowMode,
		val flags: Int,
		val anchorView: View,
		var transientParentId: Int,
		val transientChildrenIds: ArrayList<Int> = ArrayList()
	)

	private val subWindows = ConcurrentHashMap<Int, SubWindowState>()

	fun createSubWindow(
		godot: Godot,
		transientParentId: Int,
		subWindowId: Int,
		mode: WindowMode,
		flags: Int,
		exclusive: Boolean,
		positionX: Int,
		positionY: Int,
		sizeWidth: Int,
		sizeHeight: Int): Boolean {
		val anchorView = godot.getRenderView()?.view

		if (anchorView == null) {
			Log.e(TAG, "Unable to retrieve valid anchor view for sub window $subWindowId")
			return false
		}

		godot.runOnHostThread {
			// Create a popup window, and assign a render view to it
			val renderView = godot.createRenderView(subWindowId)
			val window = PopupWindow(renderView.view, sizeWidth, sizeHeight, true).apply {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					isTouchModal = exclusive
				}

				setOnDismissListener { deleteSubWindow(subWindowId) }
			}

			subWindows[subWindowId] = SubWindowState(window, positionX, positionY, mode, flags, anchorView, transientParentId)
			addTransientChild(transientParentId, subWindowId)
			Log.d(TAG, "Created sub window state for id $subWindowId: ${subWindows[subWindowId]}")
		}
		return true
	}

	fun setWindowTransientParent(subWindowId: Int, parentWindowId: Int) {
		subWindows[subWindowId]?.let {
			val previousTransientParentId = it.transientParentId
			removeTransientChild(previousTransientParentId, subWindowId)

			it.transientParentId = parentWindowId
			addTransientChild(parentWindowId, subWindowId)
		}
	}

	private fun addTransientChild(parentWindowId: Int, childWindowId: Int) {
		if (parentWindowId == INVALID_WINDOW_ID) {
			return
		}
		subWindows[parentWindowId]?.transientChildrenIds?.add(childWindowId)
	}

	private fun removeTransientChild(parentWindowId: Int, childWindowId: Int) {
		if (parentWindowId == INVALID_WINDOW_ID) {
			return
		}
		subWindows[parentWindowId]?.transientChildrenIds?.remove(childWindowId)
	}

	fun showSubWindow(subWindowId: Int) {
		subWindows[subWindowId]?.let {
			Log.d(TAG, "Showing sub window $subWindowId")
			it.window.showAtLocation(it.anchorView, Gravity.NO_GRAVITY, it.positionX, it.positionY)
		}
	}

	fun deleteSubWindow(subWindowId: Int) {
		val windowState = subWindows.remove(subWindowId) ?: return

		// Delete any children first.
		for (childId in windowState.transientChildrenIds) {
			deleteSubWindow(childId)
		}

		val giveFocusToParent = windowState.window.contentView.hasFocus()
		windowState.window.dismiss()

		if (windowState.transientParentId != INVALID_WINDOW_ID) {
			removeTransientChild(windowState.transientParentId, subWindowId)
			subWindows[windowState.transientParentId]?.let {
				if (giveFocusToParent) {
					it.window.contentView.requestFocus()
				}
			}

		}
	}

	fun getSubWindowPositionX(subWindowId: Int) = subWindows[subWindowId]?.positionX ?: 0

	fun getSubWindowPositionY(subWindowId: Int) = subWindows[subWindowId]?.positionY ?: 0

	fun setSubWindowPosition(subWindowId: Int, positionX: Int, positionY: Int) {
		subWindows[subWindowId]?.let {
			it.positionX = positionX
			it.positionY = positionY

			it.window.update(positionX, positionY, -1, -1)
		}
	}

	fun setSubWindowSize(subWindowId: Int, width: Int, height: Int) {
		subWindows[subWindowId]?.let {
			it.window.update(it.positionX, it.positionY, width, height)
		}
	}
}
