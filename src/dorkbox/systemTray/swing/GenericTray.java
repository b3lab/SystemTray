/*
 * Copyright 2016 dorkbox, llc
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

import javax.swing.JComponent;

import dorkbox.systemTray.Menu;
import dorkbox.systemTray.MenuEntry;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.util.ImageUtils;

public abstract
class GenericTray extends SwingMenu {
    /**
     * Called in the EDT
     */
    GenericTray(final SystemTray systemTray, final Menu parent, final JComponent _native) {
        super(systemTray, parent, _native);

        ImageUtils.determineIconSize();
    }

    public
    String getStatus() {
        synchronized (menuEntries) {
            MenuEntry menuEntry = menuEntries.get(0);
            if (menuEntry instanceof EntryStatus) {
                return menuEntry.getText();
            }
        }

        return null;
    }

    public
    void setStatus(final String statusText) {
        final SwingMenu _this = this;
        dispatchAndWait(new Runnable() {
            @Override
            public
            void run() {
                synchronized (menuEntries) {
                    // status is ALWAYS at 0 index...
                    Entry menuEntry = null;
                    if (!menuEntries.isEmpty()) {
                        menuEntry = (Entry) menuEntries.get(0);
                    }

                    if (menuEntry instanceof EntryStatus) {
                        // set the text or delete...

                        if (statusText == null) {
                            // delete
                            remove(menuEntry);
                        }
                        else {
                            // set text
                            menuEntry.setText(statusText);
                        }

                    } else {
                        // create a new one
                        menuEntry = new EntryStatus(_this, statusText);
                        // status is ALWAYS at 0 index...
                        menuEntries.add(0, menuEntry);
                    }
                }
            }
        });
    }
}