package org.elmlang.intellijplugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.elmlang.intellijplugin.psi.*;
import org.elmlang.intellijplugin.psi.references.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class ElmPsiImplUtil {
    public static String getName(ElmUpperCaseId element) {
        return element.getText();
    }

    public static PsiElement setName(ElmUpperCaseId element, String newName) {
        Optional.ofNullable(element.getParent())
                .filter(e -> e instanceof ElmUpperCasePath)
                .flatMap(e -> Optional.ofNullable(e.getParent()))
                .filter(e -> e instanceof ElmModuleDeclaration)
                .ifPresent(e -> Messages.showWarningDialog(element.getProject(), "Unfortunately, functionality of renaming module names has not been implemented yet.", "It's not implemented yet"));
        return setName(element, ElmTypes.UPPER_CASE_IDENTIFIER, ElmElementFactory::createUpperCaseId, newName);
    }

    public static String getName(ElmLowerCaseId element) {
        return element.getText();
    }

    public static PsiElement setName(ElmLowerCaseId element, String newName) {
        return setName(element, ElmTypes.LOWER_CASE_IDENTIFIER, ElmElementFactory::createLowerCaseId, newName);
    }

    private static <T extends PsiElement> PsiElement setName(T element, IElementType elementType, BiFunction<Project, String, T> elementFactory, String newName) {
        ASTNode node = element.getNode().findChildByType(elementType);
        if (node != null) {
            Optional.ofNullable(elementFactory.apply(element.getProject(), newName))
                    .ifPresent(id -> element.getNode().replaceChild(node, id.getFirstChild().getNode()));
        }
        return element;
    }

    public static PsiElement getNameIdentifier(ElmLowerCaseId element) {
        ASTNode node = element.getNode();
        if (node != null) {
            return node.getPsi();
        } else {
            return null;
        }
    }

    public static PsiElement getNameIdentifier(ElmUpperCaseId element) {
        ASTNode node = element.getNode();
        if (node != null) {
            return node.getPsi();
        } else {
            return null;
        }
    }

    public static FoldingDescriptor getFoldingDescriptor(ElmCaseOfBranch element) {
        return new FoldingDescriptor(element.getNode(), element.getTextRange());
    }

    public static FoldingDescriptor getFoldingDescriptor(ElmImportsList element) {
        List<ElmImportClause> imports = element.getImportClauseList();
        if (imports.isEmpty()) { return null; }
        ElmImportClause first = imports.get(0);
        ElmImportClause last = imports.get(imports.size() - 1);
        return new FoldingDescriptor(element.getNode(),
                new TextRange(first.getTextRange().getStartOffset(), last.getTextRange().getEndOffset()));
    }

    public static FoldingDescriptor getFoldingDescriptor(ElmValueDeclarationBase element) {
        return new FoldingDescriptor(element.getNode(), element.getTextRange());
    }

    public static String getFoldingPlaceholderText(ElmCaseOfBranch element) {
        return element.getPattern().getText();
    }

    public static String getFoldingPlaceholderText(ElmImportsList element) {
        return "imports (" + element.getImportClauseList().size() + ")";
    }

    public static String getFoldingPlaceholderText(ElmValueDeclarationBase element) {
        if (element.getFunctionDeclarationLeft() != null) {
            return element.getFunctionDeclarationLeft().getText();
        } else if (element.getOperatorDeclarationLeft() != null) {
            return element.getOperatorDeclarationLeft().getText();
        } else if (element.getPattern() != null) {
            return element.getPattern().getText();
        } else {
            return "...";
        }
    }

    public static Stream<ElmReference> getReferencesStream(ElmExpression element) {
        return getReferencesInAncestor(
                element,
                Stream.concat(
                        element.getListOfOperandsList().stream()
                                .flatMap(ElmPsiImplUtil::getReferencesStream),
                        element.getBacktickedFunctionList().stream()
                                .flatMap(ElmPsiImplUtil::getReferencesStream)
                )
        );
    }

    public static Stream<ElmReference> getReferencesStream(ElmBacktickedFunction element) {
        return getReferencesInAncestor(
                element,
                PsiTreeUtil.findChildrenOfAnyType(element, ElmLowerCasePathImpl.class, ElmMixedCasePathImpl.class).stream()
                        .flatMap(ElmPsiElement::getReferencesStream)
        );
    }

    private static Stream<ElmReference> getReferencesInAncestor(PsiElement ancestor, Stream<ElmReference> references) {
        return references
                .map(r -> r.referenceInAncestor(ancestor));
    }

    public static Stream<ElmReference> getReferencesStream(ElmListOfOperands element) {
        Stream<ElmReference> references = Arrays.stream(element.getChildren())
                .flatMap(child -> {
                    if (child instanceof ElmWithExpression) {
                        return ElmPsiImplUtil.getReferencesStream(((ElmWithExpression) child));
                    } else if (child instanceof ElmWithExpressionList) {
                        return ElmPsiImplUtil.getReferencesStream(((ElmWithExpressionList) child));
                    } else if (child instanceof ElmLowerCasePathImpl) {
                        return ((ElmLowerCasePathImpl) child).getReferencesStream();
                    } else {
                        return Stream.empty();
                    }
                });
        return getReferencesInAncestor(element, references);
    }

    public static Stream<ElmReference> getReferencesStream(ElmWithExpressionList element) {
        return getReferencesInAncestor(
                element,
                element.getExpressionList().stream()
                        .flatMap(ElmPsiImplUtil::getReferencesStream)
        );
    }

    public static Stream<ElmReference> getReferencesStream(ElmWithExpression element) {
        return getReferencesStream(element.getExpression())
                .map(r -> r.referenceInAncestor(element));
    }

    public static Stream<ElmReference> getReferencesStream(ElmRecord record) {

        Stream<ElmReference> recordBase = Optional.ofNullable(record.getLowerCaseId())
                .map(id -> new ElmValueReference(id).referenceInAncestor(record))
                .map(Stream::of)
                .orElse(Stream.empty());

        Stream<ElmReference> fields = record.getFieldList().stream()
                .map(f ->
                        ElmPsiImplUtil.getReferencesStream(f)
                                .map(r -> r.referenceInAncestor(record))
                )
                .reduce(Stream.empty(), Stream::concat);

        return Stream.concat(recordBase, fields);
    }

    public static ElmUpperCasePath getModuleName(ElmModuleDeclaration module) {
        return PsiTreeUtil.findChildOfType(module, ElmUpperCasePath.class);
    }

    public static ElmUpperCasePath getModuleName(ElmImportClause module) {
        return PsiTreeUtil.findChildOfType(module, ElmUpperCasePath.class);
    }

    public static boolean isExposingAll(ElmModuleDeclaration element) {
        return isAnyChildDoubleDot(element) || !ElmTreeUtil.isAnyChildOfType(element, ElmTypes.LEFT_PARENTHESIS);
    }

    public static boolean isExposingAll(ElmExposingClause element) {
        return isAnyChildDoubleDot(element);
    }

    public static boolean isExposingAll(ElmExposedUnionConstructors element) {
        return isAnyChildDoubleDot(element);
    }

    private static boolean isAnyChildDoubleDot(PsiElement element) {
        return ElmTreeUtil.isAnyChildOfType(element, ElmTypes.DOUBLE_DOT);
    }

    @NotNull
    public static Stream<ElmValueDeclarationBase> getValueDeclarations(ElmWithValueDeclarations element) {
        return Arrays.stream(element.getChildren())
                .filter(e -> e instanceof ElmValueDeclarationBase)
                .map(e -> (ElmValueDeclarationBase) e);
    }

    @NotNull
    public static Stream<ElmLowerCaseId> getDeclarationsFromPattern(@Nullable ElmPattern pattern) {
        if (pattern == null) {
            return Stream.empty();
        }

        return Stream.concat(
                Stream.concat(
                        pattern.getLowerCaseIdList().stream(),
                        pattern.getPatternList().stream()
                                .flatMap(ElmPsiImplUtil::getDeclarationsFromPattern)
                ),
                pattern.getUnionPatternList().stream().flatMap(ElmPsiImplUtil::getDeclarationsFromParentPattern)
        );

    }

    private static Stream<ElmLowerCaseId> getDeclarationsFromParentPattern(ElmWithPatternList parentPattern) {
        return parentPattern.getPatternList().stream()
                .flatMap(ElmPsiImplUtil::getDeclarationsFromPattern);
    }

    public static Stream<ElmLowerCaseId> getDefinedValues(ElmValueDeclarationBase element) {
        return Arrays.stream(element.getChildren())
                .map(child -> {
                    if (child instanceof ElmPattern) {
                        return getDeclarationsFromPattern((ElmPattern) child);
                    } else if (child instanceof ElmWithSingleId) {
                        return Stream.of(((ElmWithSingleId) child).getLowerCaseId());
                    } else {
                        return Stream.<ElmLowerCaseId>empty();
                    }
                })
                .reduce(Stream.empty(), Stream::concat);
    }

    public static Stream<ElmReference> getReferencesStream(ElmTypeAnnotationBase typeAnnotation) {
        return Optional.ofNullable(typeAnnotation.getLowerCaseId())
                .map(e -> Stream.of((ElmReference) new ElmTypeAnnotationReference(e)))
                .orElse(Stream.empty());
    }

    public static Stream<ElmReference> getReferencesStream(ElmExposingClause element) {
        return getReferencesStream(element, ElmImportedValueReference::new, ElmImportedTypeReference::new);
    }

    public static Stream<ElmReference> getReferencesStream(ElmModuleDeclaration element) {
        return getReferencesStream(element, ElmExposedValueReference::new, ElmExposedTypeReference::new);
    }

    private static Stream<ElmReference> getReferencesStream(ElmExposingBase element,
                                                            Function<ElmLowerCaseId, ElmReference> valueReferenceConstructor,
                                                            Function<ElmUpperCaseId, ElmReference> typeReferenceConstructor) {
        return Stream.concat(
                getTypeReferences(element, typeReferenceConstructor),
                element.getLowerCaseIdList().stream()
                        .map(id -> valueReferenceConstructor.apply(id).referenceInAncestor(element))
        );
    }

    private static Stream<ElmReference> getTypeReferences(ElmExposingBase element,
                                                          Function<ElmUpperCaseId, ElmReference> referenceConstructor) {
        return element.getExposedUnionList().stream()
                .flatMap(e -> Stream.concat(
                        Stream.of(
                                referenceConstructor.apply(e.getUpperCaseId())
                                        .referenceInAncestor(element)
                        ),
                        getExposedUnionMembersReferences(e.getExposedUnionConstructors(), referenceConstructor)
                                .map(r -> r.referenceInAncestor(element))
                ));
    }

    private static Stream<ElmReference> getExposedUnionMembersReferences(@Nullable ElmExposedUnionConstructors element,
                                                                         Function<ElmUpperCaseId, ElmReference> referenceConstructor) {
        return Optional.ofNullable(element)
                .map(e -> e.getUpperCaseIdList().stream().map(referenceConstructor))
                .orElse(Stream.empty());
    }
}
