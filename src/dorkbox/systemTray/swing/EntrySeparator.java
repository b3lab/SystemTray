/*
 * Copyright 2014 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.systemTray.swing;

import java.io.File;

import javax.swing.JSeparator;

import dorkbox.systemTray.MenuSpacer;
import dorkbox.systemTray.SystemTrayMenuAction;

class EntrySeparator extends Entry implements MenuSpacer {

    // this is ALWAYS called on the EDT.
    EntrySeparator(final SwingMenu parent) {
        super(parent, new JSeparator(JSeparator.HORIZONTAL));
    }

    // called in the EDT thread
    @Override
    void renderText(final String text) {
    }

    @Override
    void setImage_(final File imageFile) {
    }

    @Override
    void removePrivate() {
    }

    @Override
    public
    void setShortcut(final char key) {
    }

    @Override
    public
    boolean hasImage() {
        return false;
    }

    @Override
    public
    void setCallback(final SystemTrayMenuAction callback) {
    }
}