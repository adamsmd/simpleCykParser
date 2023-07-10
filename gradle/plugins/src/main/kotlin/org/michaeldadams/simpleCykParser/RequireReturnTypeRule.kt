package org.michaeldadams.simpleCykParser

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/* TODO:
LongStringTemplateRule.kt:3:34 'Rule' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
LongStringTemplateRule.kt:4:38 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
LongStringTemplateRule.kt:8:32 'Rule' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
LongStringTemplateRule.kt:15:29 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RequireReturnTypeRule.kt:3:34 'Rule' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RequireReturnTypeRule.kt:4:38 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RequireReturnTypeRule.kt:7:31 'Rule' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RequireReturnTypeRule.kt:13:29 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RequireReturnTypeRule.kt:15:60 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RequireReturnTypeRule.kt:18:44 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RequireReturnTypeRule.kt:19:44 'ElementType' is deprecated. Marked for removal in KtLint 0.50. For now kept for backward compatibility with custom rulesets compiled with Ktlint 0.48 or before
RuleSetProvider.kt:3:34 'RuleProvider' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:4:34 'RuleSetProviderV2' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:6:43 'RuleSetProviderV2' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:6:76 'RuleSetProviderV2' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:7:40 'RuleProvider' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:9:7 'RuleProvider' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
RuleSetProvider.kt:10:7 'RuleProvider' is deprecated. Deprecated since ktlint 0.49.0. Custom rulesets have to be migrated to RuleSetProviderV3. See changelog 0.49.
*/

class RequireReturnTypeRule : Rule("require-return-type") {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {
    if (node.elementType == ElementType.FUN) {
      val children = node.getChildren(null)
      val index = children.indexOfLast { it.elementType == ElementType.COLON }
      if (index < 0 ||
        index + 2 > children.lastIndex ||
        children[index + 1].elementType != ElementType.WHITE_SPACE ||
        children[index + 2].elementType != ElementType.TYPE_REFERENCE) {
        emit(node.startOffset, "Missing return type on function", false)
      }
    }
  }
}
