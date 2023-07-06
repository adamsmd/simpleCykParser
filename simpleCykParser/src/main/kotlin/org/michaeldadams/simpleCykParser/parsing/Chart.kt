package org.michaeldadams.simpleCykParser.parsing

import org.michaeldadams.simpleCykParser.collections.QueueSet
import org.michaeldadams.simpleCykParser.collections.QueueMap
import org.michaeldadams.simpleCykParser.grammar.Production
import org.michaeldadams.simpleCykParser.grammar.Symbol
import org.michaeldadams.simpleCykParser.grammar.Terminal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
  val isComplete: Boolean get() = consumed == production.rhs.size
  fun consume(): Pair<PartialProduction, Symbol>? =
    if (this.isComplete) null
    else Pair(PartialProduction(production, consumed + 1), production.rhs[consumed])
}

// TODO: lastPartial?

fun Production.toPartial(consumed: Int): PartialProduction {
  require(consumed >= 0)
  require(consumed <= this.rhs.size)
  return PartialProduction(this, consumed)
}

class AsStringSerializer<T>(name: String) : KSerializer<T> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: T): Unit {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): T = TODO()
}

// TODO: move to Chart.kt
// @Serializable
data class Chart(val parseRules: ProcessedParseRules, val size: Int) {
  // TODO: size is inclusive
  constructor(parseRules: ProcessedParseRules, vararg terminals: Terminal) :
    this(parseRules, terminals.size) {
    for ((start, terminal) in terminals.withIndex()) {
      this.addSymbol(Pair(start, start + 1) to Pair(terminal, null))
    }
  }
  constructor(parseRules: ProcessedParseRules, vararg terminals: String) :
    this(parseRules, *terminals.map(::Terminal).toTypedArray())
  // get left
  // get right
  // get children
  // get parses at
  // fromTokens
  // fromSymbols

  // Productions for a start, end, and symbol.  Null if "Symbol" is present but has no production.
  // TODO: rename to productions (or symbols)
  private val _symbols: QueueMap<Int, QueueMap<Int, QueueMap<Symbol, QueueSet<Production?>>>> =
    QueueMap { QueueMap { QueueMap { QueueSet() } } }
  val symbols: QueueMap<Int, QueueMap<Int, QueueMap<Symbol, Set<Production?>>>> = _symbols

  // End for a start and symbol.
  // Used to get 'rightEnd'
  // TODO: rename to symbolEnds
  // TODO: production.toCompletePartialProduction
  private val _symbolEnds: QueueMap<Int, QueueMap<Symbol, QueueSet<Int>>> =
    QueueMap { QueueMap { QueueSet() } }
  val symbolEnds: QueueMap<Int, QueueMap<Symbol, Set<Int>>> = _symbolEnds

  // Splitting position for a start, end and PartialProduction.  Null if PartialProduction is present but has no splitting position (e.g., due to empty or just being asserted).
  // TODO: rename to splits (or productions)
  private val _productions: QueueMap<Int, QueueMap<Int, QueueMap<PartialProduction, QueueSet<Int?>>>> =
  QueueMap { QueueMap { QueueMap { QueueSet() } } }
  val productions: QueueMap<Int, QueueMap<Int, QueueMap<PartialProduction, Set<Int?>>>> = _productions
  
  // End for a start and PartialProduction.
  // TODO: rename to productionEnds
  // Used to get division between children
  private val _productionEnds: QueueMap<Int, QueueMap<PartialProduction, QueueSet<Int>>> =
  QueueMap { QueueMap { QueueSet() } }
  val productionEnds: QueueMap<Int, QueueMap<PartialProduction, Set<Int>>> = _productionEnds

  // TODO: remove pairs
  fun addSymbol(entry: Pair<Pair<Int, Int>, Pair<Symbol, Production?>>): Unit {
    val (start, end) = entry.first
    val (symbol, production) = entry.second
    // val ((start, end), (symbol, production)) = entry // TODO: in argument?
    // NOTE: just an assertion and does not show how built
    // TODO: add always adds ProductionEntry (via initialUses)
    // chart.symbols += Pair(start, end) to Pair(symbol, production)
    if (production !in symbols[start][end][symbol]) {
      _symbols[start][end][symbol].add(production) // TODO: '+='?
      _symbolEnds[start][symbol].add(end)
      // If not in map, then has no initial uses
      for (newProduction in parseRules.initialUses.getOrDefault(symbol, emptySet())) {
        // NOTE: Addition goes up not down (we don't have info for down)
        this.addProduction(Pair(start, end) to Pair(newProduction.toPartial(1), start))
      }
    }
  }

  // TODO: remove pairs
  fun addProduction(entry: Pair<Pair<Int, Int>, Pair<PartialProduction, Int?>>): Unit {
    val (start, end) = entry.first
    val (partial, previous) = entry.second // TODO: in argument?
    // TODO: add always adds Symbol of the partialProd is complete
    if (previous !in productions[start][end][partial]) {
      // keys[start][end] += partial
      _productions[start][end][partial].add(previous)
      _productionEnds[start][partial].add(end)
      if (partial.isComplete) {
        // NOTE: Addition goes up not down (we don't have info for down)
        this.addSymbol(Pair(start, end) to Pair(partial.production.lhs, partial.production))
      }
    }
  }

  init {
    for (position in 0..size) {
      for (production in parseRules.nullable) {
        for (consumed in 0..production.rhs.size) {
          val split = if (consumed == 0) null else position
          this.addProduction(Pair(position, position) to Pair(production.toPartial(consumed), split))
        }
      }
    }
  }
}

// TODO: we don't need inner, but kotlin compilation errors if we omit inner
class SymbolsSerializer : KSerializer<Chart> {
  private val delegateSerializer =
    MapSerializer(
      Int.serializer(),
      MapSerializer(
        Int.serializer(),
        MapSerializer(
          AsStringSerializer<Symbol>("Symbol"),
          SetSerializer(
            AsStringSerializer<Production>("Production").nullable
          )
        )
      )
    )
  override val descriptor = SerialDescriptor("Symbols", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: Chart): Unit {
    encoder.encodeSerializableValue(delegateSerializer, value.symbols)
  }

  override fun deserialize(decoder: Decoder): Chart {
    TODO()
    // val map = decoder.decodeSerializableValue(delegateSerializer)
    // val result = Symbols()

    // for ((start, startValue) in map) {
    //   for ((end, endValue) in startValue) {
    //     for ((symbol, symbolValue) in endValue) {
    //       for (production in symbolValue) {
    //         result += Pair(start, end) to Pair(symbol, production)
    //       }
    //     }
    //   }
    // }

    // return result
  }
}

class ProductionsSerializer : KSerializer<Chart> {
  private val delegateSerializer =
    MapSerializer(
      Int.serializer(),
      MapSerializer(
        Int.serializer(),
        MapSerializer(
          AsStringSerializer<PartialProduction>("PartialProduction"),
          SetSerializer(
            Int.serializer().nullable
          )
        )
      )
    )
  override val descriptor = SerialDescriptor("Productions", delegateSerializer.descriptor)

  override fun serialize(encoder: Encoder, value: Chart): Unit {
    encoder.encodeSerializableValue(delegateSerializer, value.productions)
  }

  override fun deserialize(decoder: Decoder): Chart {
    TODO()
    // val map = decoder.decodeSerializableValue(delegateSerializer)
    // val result = Productions()

    // for ((start, startValue) in map) {
    //   for ((end, endValue) in startValue) {
    //     for ((partial, partialValue) in endValue) {
    //       for (split in partialValue) {
    //         result += Pair(start, end) to Pair(partial, split)
    //       }
    //     }
    //   }
    // }

    // return result
  }
}
