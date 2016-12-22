package org.elmlang.intellijplugin.tools.format;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;

public class ElmSettingsFormUI implements SearchableConfigurable {

    private TextFieldWithBrowseButton executable;
    private JCheckBox formatOnSave;

    private final ElmFormatSettings settings = ServiceManager.getService(ElmFormatSettings.class);

    @NotNull
    @Override
    public String getId() {
        return "preference.elm.tool.format";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Elm Format";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preference.elm.tool.format";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JTextField textField = new JTextField(settings.getExecutable());

        executable = new TextFieldWithBrowseButton(textField);
        executable.setToolTipText("The path to the elm-format command");
        executable.addBrowseFolderListener(null, null, null, new FileChooserDescriptor(true, false, false, false, false, false));
        formatOnSave = new JCheckBox("Format on save", settings.isFormatOnSave());

        JPanel panel = new JPanel(new MigLayout());
        JLabel executableLabel = new JLabel("Executable");
        executableLabel.setLabelFor(executable);
        panel.add(executableLabel);
        panel.add(executable, "grow, wrap");
        panel.add(new JLabel(""));
        panel.add(formatOnSave);
        return panel;
    }

    @Override
    public boolean isModified() {
        return formatOnSave.isSelected() != settings.isFormatOnSave()
            || !executable.getText().equals(settings.getExecutable());
    }

    @Override
    public void apply() throws ConfigurationException {
        File file = new File(executable.getText());
        if (formatOnSave.isSelected() && !file.canExecute()) {
            throw new IllegalStateException("You do not have permission to execute " + file.getName());
        }
        settings.setExecutable(executable.getText());
        settings.setFormatOnSave(formatOnSave.isSelected());
    }

    @Override
    public void reset() {
        executable.setText(settings.getExecutable());
        formatOnSave.setSelected(settings.isFormatOnSave());
    }
}
