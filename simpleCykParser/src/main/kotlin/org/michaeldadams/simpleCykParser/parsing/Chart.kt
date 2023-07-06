package org.michaeldadams.simpleCykParser.parsing

import com.charleskorn.kaml.IncorrectTypeException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import org.michaeldadams.simpleCykParser.collections.AutoMap
import org.michaeldadams.simpleCykParser.collections.QueueSet
import org.michaeldadams.simpleCykParser.collections.mapToYaml
import org.michaeldadams.simpleCykParser.collections.scalarToYaml
import org.michaeldadams.simpleCykParser.collections.setToYaml
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder



// nt -> prod | prod | prod
// prod -> prod/2 prod.2
// prod/2 -> prod/1 prod.1
// prod/1 -> prod/0 prod.0
// prod/0 -> epsilon

// fun Production.toCompletePartialProduction
// fun Production.toInitialPartialProduction
// toPartial(0)
// toPartial(1)
// toPartial(-1)

// TODO: when to do "this."

data class PartialProduction(val production: Production, val consumed: Int) {
  init {
    require(consumed >= 0)
    require(consumed <= production.rhs.size)
  }
  val lastConsumed: Symbol get() = production.rhs[consumed]
  val isComplete: Boolean get() = consumed == production.rhs.size
  fun consume(): PartialProduction? =
    if (this.isComplete) null else PartialProduction(production, consumed + 1)
}

// TODO: lastPartial?

fun Production.toPartial(consumed: Int): PartialProduction {
  require(consumed >= 0)
  require(consumed <= this.rhs.size)
  return PartialProduction(this, consumed)
}

class AsStringSerializer<T>(name: String) : KSerializer<T> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: T) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): T = TODO()
}

// TODO: move to Chart.kt
data class Chart(val parseRules: ProcessedParseRules, val size: Int) {
  // get left
  // get right
  // get children
  // get parses at
  // fromTokens
  // fromSymbols

  val symbols = Symbols()
  inner class Symbols {
    // Symbols for a start and end.
    // TODO: rename to symbols
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<Symbol>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Productions for a start, end, and symbol.  Null if "Symbol" is present but has no production.
    // TODO: rename to productions
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<Symbol, QueueSet<Production?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }

    // End for a start and symbol.
    private val ends: AutoMap<Int, AutoMap<Symbol, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to find parses
    operator fun get(start: Int, end: Int): Set<Symbol> = keys[start][end]

    // Used to get productions for parses
    operator fun get(start: Int, end: Int, symbol: Symbol): Set<Production?> = entries[start][end][symbol]

    // Used to get 'rightEnd'
    // TODO: production.toCompletePartialProduction
    operator fun get(start: Int, symbol: Symbol): Set<Int> = ends[start][symbol]

    // TODO: document
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<Symbol, Production?>>): Unit {
      val (start, end) = entry.first
      val (symbol, production) = entry.second
      // val ((start, end), (symbol, production)) = entry // TODO: in argument?
      // NOTE: just an assertion and does not show how built
      // TODO: add always adds ProductionEntry (via initialUses)
      // chart.symbols += Pair(start, end) to Pair(symbol, production)
      if (production !in entries[start][end][symbol]) {
        keys[start][end] += symbol
        entries[start][end][symbol] += production
        ends[start][symbol] += end
        // If not in map, then has no initial uses
        for (newProduction in parseRules.initialUses.getOrDefault(symbol, emptySet())) {
          // NOTE: Addition goes up not down (we don't have info for down)
          productions += Pair(start, end) to Pair(newProduction.toPartial(1), start)
        }
      }
    }

    private val toYamlImpl =
      mapToYaml<Int, _>( // TODO: alternative to _
        // scalarToYaml<Int>(),
        mapToYaml<Int, _>(
          // scalarToYaml<Int>(),
          mapToYaml<Symbol, _>(
            // scalarToYaml<Symbol>(),
            setToYaml(
              scalarToYaml<Production?>()))))

    fun theSerializer() =
      MapSerializer(
        Int.serializer(),
        MapSerializer(
          Int.serializer(),
          MapSerializer<Symbol, _>(
            AsStringSerializer<Symbol>("Symbol"),
            SetSerializer<Production?>(
              AsStringSerializer<Production>("Production").nullable
            )
          )
        )
      )

    inner class TheSerializer() : KSerializer<Symbols> {
      private val delegateSerializer = theSerializer()
      override val descriptor = SerialDescriptor("Symbols", delegateSerializer.descriptor)

      override fun serialize(encoder: Encoder, value: Symbols): Unit {
        encoder.encodeSerializableValue(delegateSerializer, this@Symbols.entries)
      }

      override fun deserialize(decoder: Decoder): Symbols {
        val map = decoder.decodeSerializableValue(delegateSerializer)
        val result = Symbols()
        TODO() // TODO: put entries in result
        return result
      }
    }

    fun toYaml(path: YamlPath): YamlNode = toYamlImpl(this.entries, path)
  }

  val productions = Productions()
  inner class Productions {
    // PartialProductions for a start and end.
    // TODO: rename to productions
    private val keys: AutoMap<Int, AutoMap<Int, QueueSet<PartialProduction>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Splitting position for a start, end and PartialProduction.  Null if PartialProduction is present but has no splitting position.
    // TODO: rename to splits
    private val entries: AutoMap<Int, AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int?>>>> =
      AutoMap { AutoMap { AutoMap { QueueSet() } } }

    // End for a start and PartialProduction.
    private val ends: AutoMap<Int, AutoMap<PartialProduction, QueueSet<Int>>> =
      AutoMap { AutoMap { QueueSet() } }

    // Used to get 'leftChild'
    operator fun get(start: Int, end: Int): QueueSet<PartialProduction> = keys[start][end]

    // Used to get division between children
    operator fun get(start: Int, end: Int, partial: PartialProduction): QueueSet<Int?> = entries[start][end][partial]

    // Not used
    operator fun get(start: Int, partial: PartialProduction): QueueSet<Int> = ends[start][partial]

    // TODO: document
    operator fun plusAssign(entry: Pair<Pair<Int, Int>, Pair<PartialProduction, Int?>>): Unit {
      val (start, end) = entry.first
      val (partial, previous) = entry.second // TODO: in argument?
      // TODO: add always adds Symbol of the partialProd is complete
      if (previous !in entries[start][end][partial]) {
        keys[start][end] += partial
        entries[start][end][partial] += previous
        ends[start][partial] += end
        if (partial.isComplete) {
          // NOTE: Addition goes up not down (we don't have info for down)
          symbols += Pair(start, end) to Pair(partial.production.lhs, partial.production)
        }
      }
    }
  }

  init {
    for (position in 0..size) {
      for (production in parseRules.nullable) {
        for (consumed in 0..production.rhs.size) {
          productions += Pair(position, position) to Pair(production.toPartial(consumed), position)
        }
      }
    }
  }

  fun toYaml(path: YamlPath = YamlPath.root): YamlNode {
    val location = path.endLocation
    // this.symbols.entries
    TODO()
    // val symbolsYaml = 
    // withPath
    // val root = YamlMap(
  }
}
