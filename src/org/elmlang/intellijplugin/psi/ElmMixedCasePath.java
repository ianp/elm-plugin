package org.elmlang.intellijplugin.psi;

import com.intellij.psi.PsiElement;

import java.util.List;

public interface ElmMixedCasePath extends PsiElement {
    List<ElmUpperCaseId> getUpperCaseIdList();
}
