package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class MissingHashElementArrowInspector extends PhpInspection {
    private static final String message = "It's probably was intended to use ' => ' here (tweak formatting if not).";

    @NotNull
    @Override
    public String getShortName() {
        return "MissingHashElementArrowInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Missing hash element arrow";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(@NotNull ArrayCreationExpression expression) {
                if (this.shouldSkipAnalysis(expression, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final PsiElement[] children = expression.getChildren();
                if (children.length > 2 && children[0] instanceof ArrayHashElement) {
                    final ArrayHashElement element = (ArrayHashElement) children[0];
                    if (element.getKey() instanceof StringLiteralExpression) {
                        for (int position = 0; position < children.length - 1; ++position) {
                            final PsiElement current = children[position];
                            if (! (current instanceof ArrayHashElement) && current.getFirstChild() instanceof StringLiteralExpression) {
                                final PsiElement comma = current.getNextSibling();
                                if (OpenapiTypesUtil.is(comma, PhpTokenTypes.opCOMMA)) {
                                    final PsiElement space = comma.getNextSibling();
                                    if (space instanceof PsiWhiteSpace && space.getText().equals(" ")) {
                                        final PsiElement previous = current.getPrevSibling();
                                        if (previous instanceof PsiWhiteSpace && previous.getText().contains("\n")) {
                                            holder.registerProblem(
                                                    comma,
                                                    MessagesPresentationUtil.prefixWithEa(message)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}
