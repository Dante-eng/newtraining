package com.example.newtraining.shared.db

import kotlin.Long
import kotlin.String

public data class Player(
  public val id: Long,
  public val uniqueId: String,
  public val fullName: String,
  public val age: Long,
  public val height: Long,
  public val gender: String,
  public val medicalCondition: String,
  public val pictureUri: String?
) {
  public override fun toString(): String = """
  |Player [
  |  id: $id
  |  uniqueId: $uniqueId
  |  fullName: $fullName
  |  age: $age
  |  height: $height
  |  gender: $gender
  |  medicalCondition: $medicalCondition
  |  pictureUri: $pictureUri
  |]
  """.trimMargin()
}
