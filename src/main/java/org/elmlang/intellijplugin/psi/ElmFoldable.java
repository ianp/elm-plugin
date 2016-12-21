package org.elmlang.intellijplugin.psi;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.psi.PsiElement;

public interface ElmFoldable extends PsiElement {
    FoldingDescriptor getFoldingDescriptor();

    String getFoldingPlaceholderText();
}
