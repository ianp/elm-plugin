package org.elmlang.intellijplugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.elmlang.intellijplugin.ElmFileType;
import org.elmlang.intellijplugin.ElmLanguage;
import org.elmlang.intellijplugin.psi.impl.ElmPsiImplUtil;
import org.elmlang.intellijplugin.utils.TypeFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    public Stream<ElmValueDeclarationBase> getValueDeclarations() {
        return ElmPsiImplUtil.getValueDeclarations(this);
    }

    @NotNull
    public Stream<ElmLowerCaseId> getExposedValues() {
        return Optional.ofNullable(this.getModuleDeclaration())
                .map(this::getExposedValues)
                .orElse(Stream.empty());
    }

    @NotNull
    public Optional<ElmLowerCaseId> getExposedValueByName(String name) {
        return this.getExposedValues().filter(e -> e.getText().equals(name))
                .findFirst();
    }

    @NotNull
    public Stream<ElmUpperCaseId> getInternalTypes() {
        return getTypes(TypeFilter.always(true));
    }

    @NotNull
    public Stream<ElmUpperCaseId> getExposedTypes(TypeFilter inputTypeFilter) {
        return getTypes(TypeFilter.and(inputTypeFilter, this.getExposedTypeFilter()));
    }

    @NotNull
    private Stream<ElmUpperCaseId> getTypes(TypeFilter typeFilter) {
        return Stream.concat(
                this.getTypeAliases(typeFilter),
                this.getUnionTypesAndMembers(typeFilter)
        );
    }

    private TypeFilter getExposedTypeFilter() {
        return Optional.ofNullable(this.getModuleDeclaration())
                .map(ElmExposingBase::getExposedTypeFilter)
                .orElse(TypeFilter.always(false));
    }

    @NotNull
    private Stream<ElmUpperCaseId> getUnionTypesAndMembers(TypeFilter typeFilter) {
        return Arrays.stream(this.getChildren())
                .filter(e -> e instanceof ElmTypeDeclaration)
                .map(e -> (ElmTypeDeclaration) e)
                .flatMap(e -> {
                    String typeName = e.getUpperCaseId().getText();
                    return Stream.concat(
                            Stream.of(e.getUpperCaseId())
                                    .filter(id -> typeFilter.testType(typeName)),
                            getUnionMembers(e.getUnionMemberList(), typeName, typeFilter)
                    );
                });
    }

    private static Stream<ElmUpperCaseId> getUnionMembers(List<ElmUnionMember> unionMemberList, String typeName, TypeFilter typeFilter) {
        return unionMemberList.stream()
                .map(ElmUnionMember::getUpperCaseId)
                .filter(id -> typeFilter.testTypeMember(typeName, id.getText()));
    }

    @NotNull
    private Stream<ElmUpperCaseId> getTypeAliases(TypeFilter typeFilter) {
        return Arrays.stream(this.getChildren())
                .filter(e -> e instanceof ElmTypeAliasDeclaration)
                .map(e -> ((ElmTypeAliasDeclaration) e).getUpperCaseId())
                .filter(e -> typeFilter.testType(e.getText()));
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
    private Stream<ElmLowerCaseId> getExposedValues(@NotNull ElmModuleDeclaration module) {
        return this.getAllDefinedValues(module.getLowerCaseFilter());
    }

    @NotNull
    private Stream<ElmLowerCaseId> getAllDefinedValues(Predicate<ElmLowerCaseId> predicate) {
        return this.getValueDeclarations()
                .map(ElmPsiImplUtil::getDefinedValues)
                .reduce(Stream.empty(), Stream::concat)
                .filter(predicate);
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