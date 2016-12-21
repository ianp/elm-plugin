package org.elmlang.intellijplugin.psi.impl;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.elmlang.intellijplugin.psi.*;

public class ElmCaseOfImpl extends ElmPsiElement implements ElmCaseOf {

    public ElmCaseOfImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ElmVisitor) {
            ((ElmVisitor)visitor).visitPsiElement(this);
        }
        else super.accept(visitor);
    }

    public FoldingDescriptor getFoldingDescriptor() {
        return new FoldingDescriptor(getNode(), getTextRange());
    }

    public String getFoldingPlaceholderText() {
        ElmCaseOfHeader header =  PsiTreeUtil.findChildOfType(this, ElmCaseOfHeader.class);
        return header != null ? header.getText() : "case ... of";
    }

}
