package org.elmlang.intellijplugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.elmlang.intellijplugin.ElmFileType;
import org.elmlang.intellijplugin.ElmLanguage;
import org.elmlang.intellijplugin.psi.impl.ElmPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class ElmFile extends PsiFileBase implements ElmWithValueDeclarations {
    public ElmFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ElmLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ElmFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return this.getModuleName("Elm File");
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    @Nullable
    public String getModuleName() {
        return this.getModuleName(null);
    }

    public List<ElmImportClause> getImportClauses() {
        return new LinkedList<>(PsiTreeUtil.findChildrenOfType(this, ElmImportClause.class));
    }

    @NotNull
    @Override
    public List<ElmValueDeclarationBase> getValueDeclarations() {
        return ElmPsiImplUtil.getValueDeclarations(this);
    }

    @NotNull
    public List<ElmLowerCaseId> getExposedValues() {
        return Optional.ofNullable(this.getModuleDeclaration())
                .map(this::getExposedValues)
                .orElse(Collections.emptyList());
    }

    @Nullable
    private String getModuleName(String defaultValue) {
        return Optional.ofNullable(this.getModuleDeclaration())
                .map(ElmModuleDeclaration::getModuleName)
                .map(Optional::ofNullable)
                .map(m -> m.map(PsiElement::getText).orElse(defaultValue))
                .orElse(defaultValue);
    }

    @NotNull
    private  List<ElmLowerCaseId> getExposedValues(@NotNull ElmModuleDeclaration module) {
        return module.isExposingAll() ? this.getAllDefinedValues() : module.getLowerCaseIdList();
    }

    @NotNull
    private List<ElmLowerCaseId> getAllDefinedValues() {
        List<ElmLowerCaseId> result = new LinkedList<>();
        ElmPsiImplUtil.getValueDeclarations(this).stream()
            .map(ElmPsiImplUtil::getDefinedValues)
            .forEach(result::addAll);
        return result;
    }

    @Nullable
    private ElmModuleDeclaration getModuleDeclaration() {
        for (PsiElement elem = this.getFirstChild(); elem != null; elem = elem.getNextSibling()) {
            if (elem instanceof ElmModuleDeclaration) {
                return (ElmModuleDeclaration) elem;
            } else if (elem instanceof ElmImportClause) {
                break;
            }
        }

        return null;
    }
}