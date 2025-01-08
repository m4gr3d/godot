/**************************************************************************/
/*  editor_main_screen.cpp                                                */
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

#include "editor_main_screen.h"

#include "core/io/config_file.h"
#include "editor/editor_node.h"
#include "editor/editor_settings.h"
#include "editor/editor_string_names.h"
#include "editor/plugins/editor_plugin.h"
#include "scene/gui/box_container.h"
#include "scene/gui/button.h"

void EditorMainScreen::_notification(int p_what) {
	switch (p_what) {
		case NOTIFICATION_READY: {
			if (buttons.has(EDITOR_3D) && buttons[EDITOR_3D]->is_visible()) {
				// If the 3D editor is enabled, use this as the default.
				select(EDITOR_3D);
				return;
			}

			// Switch to the first main screen plugin that is enabled. Usually this is
			// 2D, but may be subsequent ones if 2D is disabled in the feature profile.
			for (const KeyValue<int, Button *> &E : buttons) {
				Button *editor_button = E.value;
				if (editor_button->is_visible()) {
					select(E.key);
					return;
				}
			}

			select(-1);
		} break;
		case NOTIFICATION_THEME_CHANGED: {
			for (const KeyValue<int, Button *> &E : buttons) {
				Button *tb = E.value;
				EditorPlugin *p_editor = editor_table[E.key];
				Ref<Texture2D> icon = p_editor->get_plugin_icon();

				if (icon.is_valid()) {
					tb->set_button_icon(icon);
				} else if (has_theme_icon(p_editor->get_plugin_name(), EditorStringName(EditorIcons))) {
					tb->set_button_icon(get_theme_icon(p_editor->get_plugin_name(), EditorStringName(EditorIcons)));
				}
			}
		} break;
	}
}

void EditorMainScreen::set_button_container(HBoxContainer *p_button_hb) {
	button_hb = p_button_hb;
}

void EditorMainScreen::save_layout_to_config(Ref<ConfigFile> p_config_file, const String &p_section) const {
	int selected_main_editor_idx = -1;
	for (const KeyValue<int, Button *> &E : buttons) {
		if (E.value->is_pressed()) {
			selected_main_editor_idx = E.key;
			break;
		}
	}
	if (selected_main_editor_idx != -1) {
		p_config_file->set_value(p_section, "selected_main_editor_idx", selected_main_editor_idx);
	} else {
		p_config_file->set_value(p_section, "selected_main_editor_idx", Variant());
	}
}

void EditorMainScreen::load_layout_from_config(Ref<ConfigFile> p_config_file, const String &p_section) {
	int selected_main_editor_idx = p_config_file->get_value(p_section, "selected_main_editor_idx", -1);
	if (buttons.has(selected_main_editor_idx)) {
		callable_mp(this, &EditorMainScreen::select).call_deferred(selected_main_editor_idx);
	}
}

void EditorMainScreen::set_button_enabled(int p_index, bool p_enabled) {
	ERR_FAIL_COND(!buttons.has(p_index));
	buttons[p_index]->set_visible(p_enabled);
	if (!p_enabled && buttons[p_index]->is_pressed()) {
		select(EDITOR_2D);
	}
}

bool EditorMainScreen::is_button_enabled(int p_index) const {
	ERR_FAIL_COND_V(!buttons.has(p_index), false);
	return buttons[p_index]->is_visible();
}

int EditorMainScreen::_get_current_main_editor() const {
	for (const KeyValue<int, EditorPlugin *> &E : editor_table) {
		if (E.value == selected_plugin) {
			return E.key;
		}
	}

	return 0;
}

void EditorMainScreen::select_next() {
	HashMap<int, Button *>::Iterator E = buttons.find(_get_current_main_editor());

	do {
		if (!E || E == buttons.last()) {
			E = buttons.begin();
		} else {
			++E;
		}
	} while (!E->value->is_visible());

	select(E->key);
}

void EditorMainScreen::select_prev() {
	HashMap<int, Button *>:: Iterator E = buttons.find(_get_current_main_editor());

	do {
		if (!E || E == buttons.begin()) {
			E = buttons.last();
		} else {
			--E;
		}
	} while (!E->value->is_visible());

	select(E->key);
}

void EditorMainScreen::select_by_name(const String &p_name) {
	ERR_FAIL_COND(p_name.is_empty());

	for (const KeyValue<int, Button *> &E : buttons) {
		if (E.value->get_text() == p_name) {
			select(E.key);
			return;
		}
	}

	ERR_FAIL_MSG("The editor name '" + p_name + "' was not found.");
}

void EditorMainScreen::select(int p_index) {
	if (EditorNode::get_singleton()->is_changing_scene()) {
		return;
	}

	ERR_FAIL_COND(!buttons.has(p_index));
	ERR_FAIL_COND(!editor_table.has(p_index));

	if (!buttons[p_index]->is_visible()) { // Button hidden, no editor.
		return;
	}

	for (const KeyValue<int, Button *> &E : buttons) {
		E.value->set_pressed_no_signal(E.key == p_index);
	}

	EditorPlugin *new_editor = editor_table[p_index];
	ERR_FAIL_NULL(new_editor);

	if (selected_plugin == new_editor) {
		return;
	}

	if (selected_plugin) {
		selected_plugin->make_visible(false);
	}

	selected_plugin = new_editor;
	selected_plugin->make_visible(true);
	selected_plugin->selected_notify();

	EditorData &editor_data = EditorNode::get_editor_data();
	int plugin_count = editor_data.get_editor_plugin_count();
	for (int i = 0; i < plugin_count; i++) {
		editor_data.get_editor_plugin(i)->notify_main_screen_changed(selected_plugin->get_plugin_name());
	}

	EditorNode::get_singleton()->update_distraction_free_mode();
}

int EditorMainScreen::get_selected_index() const {
	for (const KeyValue<int, EditorPlugin *> &E : editor_table) {
		if (selected_plugin == E.value) {
			return E.key;
		}
	}
	return -1;
}

int EditorMainScreen::get_plugin_index(EditorPlugin *p_editor) const {
	int screen = -1;
	for (const KeyValue<int, EditorPlugin *> &E : editor_table) {
		if (p_editor == E.value) {
			screen = E.key;
			break;
		}
	}
	return screen;
}

EditorPlugin *EditorMainScreen::get_selected_plugin() const {
	return selected_plugin;
}

VBoxContainer *EditorMainScreen::get_control() const {
	return main_screen_vbox;
}

void EditorMainScreen::add_main_plugin(EditorPlugin *p_editor) {
	Button *tb = memnew(Button);
	tb->set_toggle_mode(true);
	tb->set_theme_type_variation("MainScreenButton");
	tb->set_name(p_editor->get_plugin_name());
	tb->set_text(p_editor->get_plugin_name());

	Ref<Texture2D> icon = p_editor->get_plugin_icon();
	if (icon.is_null() && has_theme_icon(p_editor->get_plugin_name(), EditorStringName(EditorIcons))) {
		icon = get_editor_theme_icon(p_editor->get_plugin_name());
	}
	if (icon.is_valid()) {
		tb->set_button_icon(icon);
		// Make sure the control is updated if the icon is reimported.
		icon->connect_changed(callable_mp((Control *)tb, &Control::update_minimum_size));
	}

	int main_screen_index = p_editor->get_main_screen_index();
	if (main_screen_index == -1) {
		// Get the next largest index
		int candidate_index = EDITOR_MAX;
		for (const KeyValue<int, Button *> &E : buttons) {
			candidate_index = MAX(candidate_index, E.key + 1);
		}
		main_screen_index = candidate_index;
	}

	tb->connect(SceneStringName(pressed), callable_mp(this, &EditorMainScreen::select).bind(main_screen_index));

	buttons.insert(main_screen_index, tb);
	button_hb->add_child(tb);
	editor_table.insert(main_screen_index, p_editor);
}

void EditorMainScreen::remove_main_plugin(EditorPlugin *p_editor) {
	int plugin_index = get_plugin_index(p_editor);
	ERR_FAIL_COND(plugin_index == -1);
	ERR_FAIL_COND(!buttons.has(plugin_index));
	ERR_FAIL_COND(!editor_table.has(plugin_index));

	if (buttons[plugin_index]->is_pressed()) {
		select(EDITOR_SCRIPT);
	}

	memdelete(buttons[plugin_index]);
	buttons.erase(plugin_index);

	if (selected_plugin == p_editor) {
		selected_plugin = nullptr;
	}

	editor_table.erase(plugin_index);
}

EditorMainScreen::EditorMainScreen() {
	main_screen_vbox = memnew(VBoxContainer);
	main_screen_vbox->set_name("MainScreen");
	main_screen_vbox->set_v_size_flags(Control::SIZE_EXPAND_FILL);
	main_screen_vbox->add_theme_constant_override("separation", 0);
	add_child(main_screen_vbox);
}
