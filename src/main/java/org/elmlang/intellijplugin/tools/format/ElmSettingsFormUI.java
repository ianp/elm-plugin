package org.elmlang.intellijplugin.tools.format;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.labels.LinkLabel;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
        textField.setToolTipText("The path to the elm-format command");

        executable = new TextFieldWithBrowseButton(textField);
        executable.addBrowseFolderListener(null, null, null, new FileChooserDescriptor(true, false, false, false, false, false));
        formatOnSave = new JCheckBox("Format on save", settings.isFormatOnSave());

        JPanel panel = new JPanel(new MigLayout());
        JLabel executableLabel = new JLabel("Executable");
        executableLabel.setLabelFor(textField);
        LinkLabel<String> instructions = new LinkLabel<>("Installation instructions", null,
                (label, data) -> BrowserUtil.browse(data), "https://github.com/avh4/elm-format#installation-");
        panel.add(executableLabel);
        panel.add(executable, "grow, wrap");
        panel.add(new JLabel(""));
        panel.add(instructions, "grow, wrap");
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
// FIXME: according to the link below, this should display an error in the UI, but it  doesn't.
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/207554825-How-to-display-form-validation-errors-in-settings-panel
//        File file = new File(executable.getText());
//        if (formatOnSave.isSelected()) {
//            if (!file.exists()) {
//                throw new IllegalStateException("No such command: " + file.getName());
//            }
//            if (!file.canExecute()) {
//                throw new IllegalStateException("You do not have permission to execute " + file.getName());
//            }
//        }
        settings.setExecutable(executable.getText());
        settings.setFormatOnSave(formatOnSave.isSelected());
    }

    @Override
    public void reset() {
        executable.setText(settings.getExecutable());
        formatOnSave.setSelected(settings.isFormatOnSave());
    }
}
