package model

object ActionEnum extends Enumeration {
  type ActionEnum = String
  val UPLOAD            = "upload"
  val DOWNLOAD          = "download"
  val VALIDATE_DOCUMENT = "validateDocument"
}
