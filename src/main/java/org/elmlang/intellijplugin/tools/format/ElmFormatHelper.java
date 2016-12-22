package org.elmlang.intellijplugin.tools.format;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.elmlang.intellijplugin.ElmFileType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ElmFormatHelper {

    /**
     * Format a document using {@code elm-format}.
     *
     * @param document the document to format.
     * @param exec the path to the {@code elm-format} executable, read from settings.
     */
    public static void format(Document document, String exec) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !(file.getFileType() instanceof ElmFileType)) { return; }

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                String source = document.getText();
                ProcessBuilder pb = new ProcessBuilder(exec, "--yes", "--stdin", "--elm-version", "0.18");
                pb.redirectInput(ProcessBuilder.Redirect.PIPE);
                pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
                Process process = pb.start();
                try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));
                 Writer stdin = new OutputStreamWriter(process.getOutputStream(), UTF_8)) {
                    stdin.append(source).close();
                    StringBuilder builder = new StringBuilder(source.length() + 64);
                    for (String line; (line = stdout.readLine()) != null;) {
                        builder.append(line).append("\n");
                    }
                    String formatted = builder.toString();
                    if (!formatted.isEmpty()) {
                        ApplicationManager.getApplication().runWriteAction(() ->
                                CommandProcessor.getInstance().runUndoTransparentAction(() -> document.setText(formatted)));
                    }
                }
                process.waitFor();
            } catch (IOException e) {
                Notifications.Bus.notify(new Notification(
                        "Elm Format", "Formatting Failed", "Could not format file: " + e.getMessage(), NotificationType.ERROR));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private ElmFormatHelper() {}
}
