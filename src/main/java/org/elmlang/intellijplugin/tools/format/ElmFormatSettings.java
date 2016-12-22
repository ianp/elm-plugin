package org.elmlang.intellijplugin.tools.format;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "ElmFormatSettings", storages = { @Storage("ElmFormatSettings.xml") })
public class ElmFormatSettings implements PersistentStateComponent<ElmFormatSettings> {

    private String executable = "/usr/local/bin/elm-format";

    private boolean formatOnSave;

    @Nullable
    @Override
    public ElmFormatSettings getState() {
        return this;
    }

    @Override
    public void loadState(ElmFormatSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

    public boolean isFormatOnSave() {
        return formatOnSave;
    }

    public void setFormatOnSave(boolean formatOnSave) {
        this.formatOnSave = formatOnSave;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }
}
