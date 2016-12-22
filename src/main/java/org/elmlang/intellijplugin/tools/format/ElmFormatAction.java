package org.elmlang.intellijplugin.tools.format;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

public class ElmFormatAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        System.out.println(event);
        ElmFormatSettings settings = ServiceManager.getService(ElmFormatSettings.class);
        if (settings.getExecutable() != null) {
            Project project = event.getData(CommonDataKeys.PROJECT);
            if (project != null) {
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                if (editor != null) {
                    System.out.println("formatting with " + settings.getExecutable());
                    ElmFormatHelper.format(editor.getDocument(), settings.getExecutable());
                }
            }
        } else {
            String msg = "You need to configure the path to elm-format in the tool settings.";
            Notifications.Bus.notify(new Notification("Elm Format", "Not Configured", msg, NotificationType.WARNING));
        }
    }
}
