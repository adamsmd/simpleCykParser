import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlList
import kotlin.text.Regex
import kotlin.text.toRegex

sealed interface Symbol
data class Terminal(val name: String) : Symbol
data class NonTerminal(val name: String) : Symbol

data class LexRule(val terminal: Terminal, val regex: Regex)
data class LexRules(val whitespace: Regex, val lexRules: List<LexRule>)

data class Production(val lhs: NonTerminal, val name: String?, val rhs: List<Symbol>)
data class ParseRules(val start: NonTerminal, val productions: Map<NonTerminal, Set<Production>>)

data class Grammar(val lexRules: LexRules, val parseRules: ParseRules)

// fun Grammar.check(): Pair<Set<String>, Set<String>> {
//   // all non-terminals have entry in .productions
//   // all terminals have entry in .rules
// }
