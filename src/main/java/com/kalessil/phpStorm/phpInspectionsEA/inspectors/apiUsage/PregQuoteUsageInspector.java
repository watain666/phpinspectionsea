package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.UseSuggestedReplacementFixer;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PregQuoteUsageInspector extends PhpInspection {
    private static final String messageMissing   = "Please provide regex delimiter as the second argument for proper escaping.";
    private static final String messageSeparator = "The separator value is platform-dependent, consider using '/' instead.";

    @NotNull
    @Override
    public String getShortName() {
        return "PregQuoteUsageInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Proper preg_quote(...) usage";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                if (this.shouldSkipAnalysis(reference, StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS)) { return; }

                final String functionName = reference.getName();
                if (functionName != null && functionName.equals("preg_quote")) {
                    final PsiElement[] arguments = reference.getParameters();
                    if (arguments.length == 1) {
                        holder.registerProblem(
                                reference,
                                MessagesPresentationUtil.prefixWithEa(messageMissing)
                        );
                    } else if (arguments.length == 2) {
                        final PsiElement escaped = arguments[1];
                        if (escaped instanceof ConstantReference && escaped.getText().equals("DIRECTORY_SEPARATOR")) {
                            holder.registerProblem(
                                    escaped,
                                    MessagesPresentationUtil.prefixWithEa(messageSeparator),
                                    new UseSlashSeparatorFix("'/'")
                            );
                        }
                    }
                }
            }
        };
    }

    private static final class UseSlashSeparatorFix extends UseSuggestedReplacementFixer {
        private static final String title = "Use '/' instead";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        UseSlashSeparatorFix(@NotNull String expression) {
            super(expression);
        }
    }
}
