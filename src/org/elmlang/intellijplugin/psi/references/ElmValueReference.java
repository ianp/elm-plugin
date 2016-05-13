package org.elmlang.intellijplugin.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.elmlang.intellijplugin.psi.*;
import org.elmlang.intellijplugin.psi.scope.ElmScope;
import org.elmlang.intellijplugin.utils.Function3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ElmValueReference extends ElmReferenceBase {

    public ElmValueReference(PsiElement element) {
        super(element);
    }

    private ElmValueReference(PsiElement element, PsiElement referencingElement, TextRange rangeInElement) {
        super(element, referencingElement, rangeInElement);
    }

    @Override
    protected Function3<PsiElement, PsiElement, TextRange, ElmReference> constructor() {
        return ElmValueReference::new;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ElmScope.scopeFor((ElmLowerCaseId)this.referencingElement)
                .filter(this::theSameNameOrEmpty)
                .findFirst()
                .map(o -> o.orElse(null))
                .orElse(null);
    }
}
