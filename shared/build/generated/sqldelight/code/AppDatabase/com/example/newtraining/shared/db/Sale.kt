package com.example.newtraining.shared.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Sale(
  public val id: Long,
  public val itemId: Long,
  public val playerId: Long,
  public val price: Double,
  public val discount: Double,
  public val paid: Double,
  public val debt: Double,
  public val date: Long
) {
  public override fun toString(): String = """
  |Sale [
  |  id: $id
  |  itemId: $itemId
  |  playerId: $playerId
  |  price: $price
  |  discount: $discount
  |  paid: $paid
  |  debt: $debt
  |  date: $date
  |]
  """.trimMargin()
}
