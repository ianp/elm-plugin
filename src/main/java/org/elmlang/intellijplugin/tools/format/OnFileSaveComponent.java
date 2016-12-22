package org.elmlang.intellijplugin.tools.format;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

public class OnFileSaveComponent implements ApplicationComponent {

    private MessageBusConnection busConnection;

    @Override
    public void initComponent() {
        busConnection = ApplicationManager.getApplication().getMessageBus().connect();
        busConnection.subscribe(AppTopics.FILE_DOCUMENT_SYNC,
                new FileDocumentManagerAdapter() {
                    @Override
                    public void beforeDocumentSaving(@NotNull Document document) {
                        ElmFormatSettings settings = ServiceManager.getService(ElmFormatSettings.class);
                        if (settings.isFormatOnSave() && settings.getExecutable() != null) {
                            ElmFormatHelper.format(document, settings.getExecutable());
                        }
                    }
                });
    }

    @Override
    public void disposeComponent() {
        if (busConnection != null) { busConnection.disconnect(); }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Elm Format";
    }
}
