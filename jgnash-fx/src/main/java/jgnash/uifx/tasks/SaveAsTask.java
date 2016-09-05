/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2016 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.tasks;

import java.io.File;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import jgnash.engine.DataStoreType;
import jgnash.engine.EngineFactory;
import jgnash.uifx.StaticUIMethods;
import jgnash.uifx.views.main.MainView;
import jgnash.util.ResourceUtils;

/**
 * Save File As Task.
 *
 * @author Craig Cavanaugh
 */
public class SaveAsTask extends Task<Void> {

    private static final int FORCED_DELAY = 1500;
    private static final int INDETERMINATE = -1;

    private final File file;

    private SaveAsTask(final File file) {
        this.file = file;
    }

    public static void start() {
        final ResourceBundle resources = ResourceUtils.getBundle();

        final File current = new File(EngineFactory.getActiveDatabase());

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(current.getParentFile());
        fileChooser.setTitle(resources.getString("Title.SaveAs"));

        final DataStoreType[] types = DataStoreType.values();
        final String[] ext = new String[types.length];
        final StringBuilder description = new StringBuilder(resources.getString("Label.jGnashFiles") + " (");

        for (int i = 0; i < types.length; i++) {
            ext[i] = "*." + types[i].getDataStore().getFileExt();

            description.append("*.");
            description.append(types[i].getDataStore().getFileExt());

            if (i < types.length - 1) {
                description.append(", ");
            }
        }
        description.append(')');

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description.toString(), ext));

        final File newFile = fileChooser.showSaveDialog(MainView.getInstance().getPrimaryStage());

        if (newFile != null) {
            final SaveAsTask saveAsTask = new SaveAsTask(newFile);
            new Thread(saveAsTask).start();
            StaticUIMethods.displayTaskProgress(saveAsTask);
        }
    }

    @Override
    protected Void call() throws Exception {

        final ResourceBundle resources = ResourceUtils.getBundle();

        try {
            updateMessage(resources.getString("Message.PleaseWait"));
            updateProgress(INDETERMINATE, Long.MAX_VALUE);

            EngineFactory.saveAs(file.getAbsolutePath());

            updateProgress(1, 1);
            updateMessage(resources.getString("Message.FileSaveComplete"));
            Thread.sleep(FORCED_DELAY * 2);
        } catch (final Exception exception) {
            Platform.runLater(() -> StaticUIMethods.displayException(exception));
        }

        return null;
    }
}
