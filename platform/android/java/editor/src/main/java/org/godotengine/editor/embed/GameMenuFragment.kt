/**************************************************************************/
/*  GameMenuFragment.kt                                                   */
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

package org.godotengine.editor.embed

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import org.godotengine.editor.R

/**
 * Implements the game menu interface for the Android editor.
 */
class GameMenuFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

	companion object {
		val TAG = GameMenuFragment::class.java.simpleName
	}

	/**
	 * Used to be notified of events fired when interacting with the game menu
	 */
	interface GameMenuListener {

		/**
		 * Kotlin representation of the RuntimeNodeSelect::SelectMode enum in 'scene/debugger/scene_debugger.h'
		 */
		enum class RuntimeNodeSelectMode {
			SINGLE,
			LIST
		}

		/**
		 * Kotlin representation of the RuntimeNodeSelect::NodeType enum in 'scene/debugger/scene_debugger.h'
		 */
		enum class RuntimeNodeType {
			NONE,
			TYPE_2D,
			TYPE_3D
		}

		/**
		 * Kotlin representation of the EditorDebuggerNode::CameraOverride in 'editor/debugger/editor_debugger_node.h'
		 */
		enum class CameraMode {
			NONE,
			IN_GAME,
			EDITORS
		}

		fun suspendGame(suspended: Boolean)
		fun dispatchNextFrame()
		fun toggleSelectionVisibility(enabled: Boolean)
		fun overrideCamera(enabled: Boolean)
		fun selectRuntimeNode(nodeType: RuntimeNodeType)
		fun selectRuntimeNodeSelectMode(selectMode: RuntimeNodeSelectMode)
		fun reset2DCamera()
		fun reset3DCamera()
		fun manipulateCamera(mode: CameraMode)
	}

	private val pauseButton: View? by lazy {
		view?.findViewById(R.id.game_menu_pause_button)
	}
	private val nextFrameButton: View? by lazy {
		view?.findViewById(R.id.game_menu_next_frame_button)
	}
	private val unselectNodesButton: RadioButton? by lazy {
		view?.findViewById(R.id.game_menu_unselect_nodes_button)
	}
	private val select2DNodesButton: RadioButton? by lazy {
		view?.findViewById(R.id.game_menu_select_2d_nodes_button)
	}
	private val select3DNodesButton: RadioButton? by lazy {
		view?.findViewById(R.id.game_menu_select_3d_nodes_button)
	}
	private val guiVisibilityButton: View? by lazy {
		view?.findViewById(R.id.game_menu_gui_visibility_button)
	}
	private val toolSelectButton: RadioButton? by lazy {
		view?.findViewById(R.id.game_menu_tool_select_button)
	}
	private val listSelectButton: RadioButton? by lazy {
		view?.findViewById(R.id.game_menu_list_select_button)
	}
	private val cameraButton: View? by lazy {
		view?.findViewById(R.id.game_menu_camera_button)
	}
	private val cameraOptionsButton: View? by lazy {
		view?.findViewById(R.id.game_menu_camera_options_button)
	}

	private val popupMenu: PopupMenu by lazy {
		PopupMenu(context, cameraOptionsButton).apply {
			setOnMenuItemClickListener(this@GameMenuFragment)
			inflate(R.menu.camera_options_menu)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				menu.setGroupDividerEnabled(true)
			}
		}
	}

	private var menuListener: GameMenuListener? = null

	override fun onAttach(context: Context) {
		super.onAttach(context)
		val parentActivity = activity
		if (parentActivity is GameMenuListener) {
			menuListener = parentActivity
		} else {
			val parentFragment = parentFragment
			if (parentFragment is GameMenuListener) {
				menuListener = parentFragment
			}
		}
	}

	override fun onDetach() {
		super.onDetach()
		menuListener = null
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, bundle: Bundle?): View? {
		return inflater.inflate(R.layout.game_menu_fragment_layout, container, false)
	}

	override fun onViewCreated(view: View, bundle: Bundle?) {
		super.onViewCreated(view, bundle)

		pauseButton?.setOnClickListener {
			val isActivated = !it.isActivated
			menuListener?.suspendGame(isActivated)
			it.isActivated = isActivated
		}
		nextFrameButton?.setOnClickListener {
			menuListener?.dispatchNextFrame()
		}
		unselectNodesButton?.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				menuListener?.selectRuntimeNode(GameMenuListener.RuntimeNodeType.NONE)
			}
		}
		select2DNodesButton?.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				menuListener?.selectRuntimeNode(GameMenuListener.RuntimeNodeType.TYPE_2D)
			}
		}
		select3DNodesButton?.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				menuListener?.selectRuntimeNode(GameMenuListener.RuntimeNodeType.TYPE_3D)
			}
		}
		guiVisibilityButton?.setOnClickListener {
			val isActivated = !it.isActivated
			menuListener?.toggleSelectionVisibility(!isActivated)
			it.isActivated = isActivated
		}
		toolSelectButton?.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				menuListener?.selectRuntimeNodeSelectMode(GameMenuListener.RuntimeNodeSelectMode.SINGLE)
			}
		}
		listSelectButton?.setOnCheckedChangeListener { buttonView, isChecked ->
			if (isChecked) {
				menuListener?.selectRuntimeNodeSelectMode(GameMenuListener.RuntimeNodeSelectMode.LIST)
			}
		}
		cameraOptionsButton?.setOnClickListener {
			popupMenu.show()
		}
		cameraButton?.setOnClickListener {
			val isActivated = !it.isActivated
			menuListener?.overrideCamera(isActivated)
			it.isActivated = isActivated

			updatePopupMenu()
		}

		updatePopupMenu()
	}

	private fun updatePopupMenu() {
		cameraOptionsButton?.isEnabled = cameraButton?.isActivated ?: false
	}

	override fun onMenuItemClick(item: MenuItem): Boolean {
		return when(item.itemId) {
			R.id.menu_reset_2d_camera -> {
				menuListener?.reset2DCamera()
				true
			}

			R.id.menu_reset_3d_camera -> {
				menuListener?.reset3DCamera()
				true
			}

			R.id.menu_manipulate_camera_in_game -> {
				if (!item.isChecked) {
					item.isChecked = true
					menuListener?.manipulateCamera(GameMenuListener.CameraMode.IN_GAME)
				}
				true
			}

			R.id.menu_manipulate_camera_from_editors -> {
				if (!item.isChecked) {
					item.isChecked = true
					menuListener?.manipulateCamera(GameMenuListener.CameraMode.EDITORS)
				}
				true
			}

			else -> false
		}
	}
}
