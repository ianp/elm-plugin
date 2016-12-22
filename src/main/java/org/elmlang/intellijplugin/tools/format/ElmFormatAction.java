package org.elmlang.intellijplugin.tools.format;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.io.File;

public class ElmFormatAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        ElmFormatSettings settings = ServiceManager.getService(ElmFormatSettings.class);
        String exec = settings.getExecutable();
        if (exec != null && new File(exec).canExecute()) {
            Editor editor = editorForEvent(event);
            if (editor != null) {
                ElmFormatHelper.format(editor.getDocument(), exec);
            }
        } else {
            String msg = "You need to configure the path to elm-format in the <a href=\"#settings\">tool settings</a>. If you need to install elm-format instructions can be found <a href=\"#instructions\">here</a>";
            Notifications.Bus.notify(
                    new Notification("ElmFormat", "Elm Format Missing", msg, NotificationType.WARNING, new MyLinkListener()));
        }
    }

    @Override
    public void update(AnActionEvent event) {
        ElmFormatSettings settings = ServiceManager.getService(ElmFormatSettings.class);
        String exec = settings.getExecutable();
        event.getPresentation().setEnabled(exec != null && new File(exec).canExecute() && editorForEvent(event) != null);
    }

    private Editor editorForEvent(AnActionEvent event) {
        Project project = event.getData(CommonDataKeys.PROJECT);
        return project != null ? FileEditorManager.getInstance(project).getSelectedTextEditor() : null;
    }

    private static class MyLinkListener extends NotificationListener.Adapter {
        @Override
        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
            switch (event.getDescription()) {
                case "#settings":
                    ShowSettingsUtil.getInstance().showSettingsDialog(null, ElmSettingsFormUI.class);
                    break;
                case "#instructions":
                    BrowserUtil.browse("https://github.com/avh4/elm-format#installation-");
                    break;
                default:
                    System.out.println(event.getDescription());
            }
        }
    }
}
