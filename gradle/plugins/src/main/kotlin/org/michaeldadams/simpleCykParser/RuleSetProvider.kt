package org.michaeldadams.simpleCykParser

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

class LongStringTemplateRuleSetProvider : RuleSetProviderV2("local-rules", RuleSetProviderV2.NO_ABOUT) {
  override fun getRuleProviders(): Set<RuleProvider> =
    setOf(
      RuleProvider { RequireReturnTypeRule() },
      RuleProvider { LongStringTemplateRule() },
    )
}
