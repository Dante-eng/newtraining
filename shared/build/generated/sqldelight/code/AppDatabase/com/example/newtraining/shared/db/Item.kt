package com.example.newtraining.shared.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Item(
  public val id: Long,
  public val name: String,
  public val price: Double,
  public val type: String,
  public val sellPrice: Double?
) {
  public override fun toString(): String = """
  |Item [
  |  id: $id
  |  name: $name
  |  price: $price
  |  type: $type
  |  sellPrice: $sellPrice
  |]
  """.trimMargin()
}
