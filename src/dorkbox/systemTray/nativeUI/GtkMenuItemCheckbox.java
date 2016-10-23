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
package dorkbox.systemTray.nativeUI;

import java.awt.event.ActionListener;
import java.io.File;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import dorkbox.systemTray.Checkbox;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.jna.linux.GCallback;
import dorkbox.systemTray.jna.linux.Gobject;
import dorkbox.systemTray.jna.linux.Gtk;
import dorkbox.systemTray.util.ImageUtils;
import dorkbox.systemTray.util.MenuCheckboxHook;

class GtkMenuItemCheckbox extends GtkMenuBaseItem implements MenuCheckboxHook, GCallback {
    private static File transparentIcon = null;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final NativeLong nativeLong;

    private final GtkMenu parent;
    private final Pointer _native = Gtk.gtk_check_menu_item_new_with_mnemonic("");


    // these have to be volatile, because they can be changed from any thread
    private volatile Checkbox menuItem;
    private volatile Pointer image;

    // The mnemonic will ONLY show-up once a menu entry is selected. IT WILL NOT show up before then!
    // AppIndicators will only show if you use the keyboard to navigate
    // GtkStatusIconTray will show on mouse+keyboard movement
    private volatile char mnemonicKey = 0;

    /**
     * called from inside dispatch thread. ONLY creates the menu item, but DOES NOT attach it!
     * this is a FLOATING reference. See: https://developer.gnome.org/gobject/stable/gobject-The-Base-Object-Type.html#floating-ref
     */
    GtkMenuItemCheckbox(final GtkMenu parent) {
        this.parent = parent;

        // cannot be done in a static initializer, because the tray icon size might not yet have been determined
        if (transparentIcon == null) {
            transparentIcon = ImageUtils.getTransparentImage(ImageUtils.ENTRY_SIZE);
        }

        nativeLong = Gobject.g_signal_connect_object(_native, "activate", this, null, 0);
    }

    // called by native code
    @Override
    public
    int callback(final Pointer instance, final Pointer data) {
        if (menuItem != null) {
            final ActionListener cb = menuItem.getCallback();
            if (cb != null) {
                try {
                    Gtk.proxyClick(menuItem, cb);
                } catch (Exception e) {
                    SystemTray.logger.error("Error calling menu entry checkbox {} click event.", menuItem.getText(), e);
                }
            }
        }

        return Gtk.TRUE;
    }

    public
    boolean hasImage() {
        return true;
    }

    public
    void setSpacerImage(final Pointer _native, final boolean everyoneElseHasImages) {
        // no op
    }

    @Override
    public
    void setEnabled(final Checkbox menuItem) {
        Gtk.dispatch(new Runnable() {
            @Override
            public
            void run() {
                Gtk.gtk_widget_set_sensitive(_native, menuItem.getEnabled());
            }
        });
    }

    @Override
    public
    void setText(final Checkbox menuItem) {
        final String textWithMnemonic;

        if (mnemonicKey != 0) {
            String text = menuItem.getText();

            // they are CASE INSENSITIVE!
            int i = text.toLowerCase()
                        .indexOf(mnemonicKey);

            if (i >= 0) {
                textWithMnemonic = text.substring(0, i) + "_" + text.substring(i);
            }
            else {
                textWithMnemonic = menuItem.getText();
            }
        }
        else {
            textWithMnemonic = menuItem.getText();
        }

        Gtk.dispatch(new Runnable() {
            @Override
            public
            void run() {
                Gtk.gtk_menu_item_set_label(_native, textWithMnemonic);
                Gtk.gtk_widget_show_all(_native);
            }
        });
    }

    @Override
    public
    void setCallback(final Checkbox menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public
    void setChecked(final Checkbox checkbox) {
        Gtk.dispatch(new Runnable() {
            @Override
            public
            void run() {
                Gtk.gtk_check_menu_item_set_active(_native, checkbox.getChecked());
            }
        });
    }

    @Override
    public
    void setShortcut(final Checkbox menuItem) {
        this.mnemonicKey = Character.toLowerCase(menuItem.getShortcut());

        setText(menuItem);
    }

    @Override
    void onDeleteMenu(final Pointer parentNative) {
        onDeleteMenu(parentNative, _native);
    }

    @Override
    void onCreateMenu(final Pointer parentNative, final boolean hasImagesInMenu) {
        onCreateMenu(parentNative, _native, hasImagesInMenu);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public
    void remove() {
        Gtk.dispatch(new Runnable() {
            @Override
            public
            void run() {
                Gtk.gtk_container_remove(parent._nativeMenu, _native);
                Gtk.gtk_menu_shell_deactivate(parent._nativeMenu, _native);

                GtkMenuItemCheckbox.super.remove();

                menuItem = null;
                if (image != null) {
                    Gtk.gtk_widget_destroy(image);
                    image = null;
                }

                Gtk.gtk_widget_destroy(_native);

                parent.remove(GtkMenuItemCheckbox.this);
            }
        });
    }
}
