package shakey.internal

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.{ProvenShape, Column, Tag}

class Stocks(tag: Tag)
extends Table[(String, Double, Long)](tag, "STOCKS") {
  def symbol: Column[String] = column[String]("SYMBOL", O.PrimaryKey)

  def ratePerSec: Column[Double] = column[Double]("RATE_PER_SEC")

  def time: Column[Long] = column[Long]("last_time")

  def * : ProvenShape[(Int, Double, Long)] =
    (symbol, ratePerSec, time)
}
