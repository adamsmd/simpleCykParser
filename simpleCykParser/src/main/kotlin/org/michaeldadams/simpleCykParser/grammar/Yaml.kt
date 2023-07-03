import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlList
import kotlin.text.Regex
import kotlin.text.toRegex

fun parseYaml(string: String): Map<String, YamlNode> =
  when (val yaml = Yaml.default.parseToYamlNode(string)) {
    is YamlMap -> yaml.entries.mapKeys { it.key.content }
    else -> TODO()
  }

inline fun <reified T : YamlNode> Map<String, YamlNode>.get(key: String, notFound: String, wrongType: String): T =
  (this[key] ?: TODO(notFound)) as? T ?: TODO(wrongType)

fun YamlMap.asPair(): Pair<String, String> {
  if (this.entries.size < 1) TODO()
  if (this.entries.size > 1) TODO()
  val p = this.entries.toList().single()
  return p.first.content to (p.second as? YamlScalar ?: TODO()).content
}

fun lexRulesFromYamlMap(yamlMap: Map<String, YamlNode>): LexRules {
  val whitespace: Regex = yamlMap.get<YamlScalar>("whitespace", "TODO", "TODO").content.toRegex()

  val rules = yamlMap.get<YamlList>("terminals", "TODO", "TODO").items.map {
    (it as? YamlMap ?: TODO()).asPair()
  }.map { LexRule(Terminal(it.first), it.second.toRegex()) }

  return LexRules(whitespace, rules)
}

fun parseRulesFromYamlMap(yamlMap: Map<String, YamlNode>, ): ParseRules {
  val start: String = yamlMap.get<YamlScalar>("start", "TODO", "TODO").content
  val productionsYaml: Map<YamlScalar, YamlNode> = yamlMap.get<YamlMap>("productions", "TODO", "TODO").entries
  val nonterminals: Set<String> = productionsYaml.keys.map { it.content }.toSet()
  val productions = productionsYaml.entries.map { entry ->
    val productions = (entry.value as? YamlList ?: TODO()).items
      .map {
        val (name, rhsString) = when (it) {
          is YamlMap -> it.asPair()
          is YamlScalar -> null to it.content
          else -> TODO()
        }
        val rhs = rhsString
          .split("\\p{IsWhite_Space}+".toRegex())
          .filter { it.isNotEmpty() }
          .map { if (it in nonterminals) NonTerminal(it) else Terminal(it) }
        Production(NonTerminal(entry.key.content), name, rhs)
      }.toSet()
    NonTerminal(entry.key.content) to productions
  }.toMap()
  return ParseRules(NonTerminal(start), productions)
  // ((yamlMap[PRODUCTIONS] ?: throw TODO()) as?
  // TODO()
}

/*

whitespace: \s+
terminals:
  - STRING: '"[^"]"'
  - NUM: \d+
  - IF: if
  - (: (
start: S
productions:
  S:
    - F: if ( S ) then { else }
    - F: '" S "'
    - S S
    - ""
  T: []

*/
