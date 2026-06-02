/**************************************************************************/
/*  BaseEditorFunctionService.kt                                          */
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

package org.godotengine.editor.appfunctions

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeniedException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.godotengine.editor.GodotEditor
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotActivity
import org.godotengine.godot.GodotIO
import org.ini4j.Wini
import java.io.File
import kotlin.collections.iterator

/**
 * Entry point to expose the editor functionality using Android AppFunctions API.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
	serviceName = "EditorFunctionService",
	appFunctionXmlFileName = "editor_function_service",
)
abstract class BaseEditorFunctionService : AppFunctionService() {

	companion object {
		private val TAG = BaseEditorFunctionService::class.java.simpleName

		private const val PROJECT_METADATA_FILE_NAME = "project.godot"

		private fun isEditorRunning(context: Context): Boolean {
			val godot = Godot.getInstance(context)
			return godot.isInitialized() && godot.getActivity() != null
		}

		private fun getEditorProjectConfigFile(context: Context): File {
			return File(GodotIO.getDataDir(context), "godot/projects.cfg")
		}
	}

	/**
	 * The item describing a project in the Project Manager list.
	 */
	@AppFunctionSerializable(isDescribedByKDoc = true)
	data class ProjectMetadata(
		val path: String,
		val favorite: Boolean,
		val projectName: String,
		val description: String,
		val icon: String,
		val mainScene: String,
		val lastEdited: Long,
		val version: Int,
	)

	/**
	 * Returns a list of all the [ProjectMetadata].
	 */
	@AppFunction(isDescribedByKDoc = true)
	suspend fun listProjects() : List<ProjectMetadata> {
		val projectPaths = mutableMapOf<String, Boolean>()
		val editorProjectConfigFile = getEditorProjectConfigFile(this)
		try {
			val editorProjectConfigs = Wini(editorProjectConfigFile)

			for ((sectionPath, section) in editorProjectConfigs) {
				projectPaths[sectionPath] = section["favorite"].toBoolean()
			}
		} catch (e: Exception) {
			Log.e(TAG, "Unable to parse editor project configs $editorProjectConfigFile", e)
		}

		if (projectPaths.isEmpty()) {
			return emptyList()
		}

		val projectMetadataList = mutableListOf<ProjectMetadata>()
		for ((path, favorite) in projectPaths) {
			Log.d(TAG, "Loading data for project under $path")
			try {
				val projectConfigFile = File(path, PROJECT_METADATA_FILE_NAME)
				val projectConfig = Wini(projectConfigFile)
				val applicationSection = projectConfig["application"] ?: continue

				val projectMetadata = ProjectMetadata(
					path = path,
					favorite = favorite,
					lastEdited = projectConfigFile.lastModified(),
					version = projectConfig.get("", "config_version")?.toInt() ?: 0,
					projectName = applicationSection.get("config/name", "Unnamed Project").removeSurrounding("\""),
					description = applicationSection.get("config/description", "").removeSurrounding("\""),
					mainScene = applicationSection.get("run/main_scene", "").removeSurrounding("\""),
					icon = applicationSection.get("config/icon", "").removeSurrounding("\""))

				projectMetadataList.add(projectMetadata)
			} catch (e: Exception) {
				Log.e(TAG, "Unable to load data for project under $path", e)
			}
		}

		return projectMetadataList
	}

	/**
	 * Removes the project under the given path.
	 *
	 * This is a non-destructive operation which only removes the project from the editor's index.
	 *
	 * @param projectPath Path to the project to remove.
	 *
	 * @return True if the project was successfully removed from the editor's index.
	 */
	@AppFunction(isDescribedByKDoc = true)
	suspend fun removeProject(projectPath: String): Boolean {
		// Check if the given project path exists.
		val projectDir = File(projectPath)
		if (!projectDir.exists()) {
			throw AppFunctionInvalidArgumentException("No Godot project exists at the given path: $projectPath")
		}

		// Update the editor metadata to remove the specified project.
		val editorProjectConfigFile = getEditorProjectConfigFile(this)
		try {
			val editorProjectConfigs = Wini(editorProjectConfigFile)
			val removedSection = editorProjectConfigs.remove(projectPath)
			if (removedSection == null) {
				Log.w(TAG, "Project metadata is missing from the editor configs.")
				return false
			}

			editorProjectConfigs.store()
		} catch (e: Exception) {
			Log.e(TAG, "Unable to remove project $projectPath from editor project configs $editorProjectConfigFile", e)
			return false
		}

		if (isEditorRunning(this)) {
			// Check whether we are currently editing the project we are about to remove.
			val godot = Godot.getInstance(this)
			val editorActivity = godot.getActivity() as GodotEditor?
			if (editorActivity != null &&
				((godot.isEditorHint() && editorActivity.getCurrentProjectPath() == projectPath) ||
					godot.isProjectManagerHint())) {
				Log.d(TAG, "Reloading project manager...")
				editorActivity.launchProjectManager()
			}
		}

		return true
	}

	/**
	 * Edits the project under the given path.
	 *
	 * TODO: need to add another version of this method that's only active when the editor is running.
	 * This version should only be enabled when the editor is not running.
	 *
	 * @param projectPath Path to the project to edit.
	 * @param createIfNecessary True if the project should be created if it doesn't exist already.
	 *
	 * @return True if the project was successfully opened for editing, false otherwise.
	 */
	@AppFunction(isDescribedByKDoc = true)
	suspend fun openProject(
		projectPath: String,
		createIfNecessary: Boolean
	): PendingIntent = withContext(Dispatchers.IO) {
		// Check if the project exists.
		val projectDir = File(projectPath)
		if (!projectDir.exists()) {
			if (!createIfNecessary) {
				throw AppFunctionInvalidArgumentException("No Godot project exists at the given path: $projectPath")
			}

			// Attempt to create the project directory.
			if (!projectDir.mkdirs()) {
				throw AppFunctionDeniedException("Unable to create project directory $projectDir")
			}
			Log.d(TAG, "Created project directory $projectDir")

			val projectMetadata = File(projectDir, PROJECT_METADATA_FILE_NAME)
			projectMetadata.createNewFile()
			Log.d(TAG, "Created project metadata $projectMetadata")

			// TODO: update the editor config file to include the newly added project.
		}

		// TODO: The code block below should be moved to a version of this method that's only active when the editor is running. In that scenario, this method should be disabled.
		{
			if (isEditorRunning(this@BaseEditorFunctionService)) {
				val editorActivity = Godot.getInstance(this@BaseEditorFunctionService).getActivity() as GodotEditor?
				if (editorActivity != null) {
					Log.d(TAG, "Reloading project $projectPath")
					editorActivity.onNewGodotInstanceRequested(arrayOf("--editor", "--path", projectPath))
				}
			}
		}

		// Craft an intent to open the project.
		val editorIntent = Intent()
			.setComponent(ComponentName(this@BaseEditorFunctionService, GodotEditor::class.java))
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra(GodotActivity.EXTRA_COMMAND_LINE_PARAMS, arrayOf("--editor", "--path", projectPath))

		Log.d(TAG, "Returning editor pending intent for project $projectPath")

		return@withContext PendingIntent.getActivity(
			this@BaseEditorFunctionService,
			0,
			editorIntent,
			PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
		)
	}
}
