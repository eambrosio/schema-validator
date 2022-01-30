package model

object StatusEnum extends Enumeration {
  type StatusEnum = String
  val SUCCESS = "success"
  val ERROR   = "error"
}
