package model

object ActionEnum extends Enumeration {
  type ActionEnum = String
  val UPLOAD            = "uploadSchema"
  val DOWNLOAD          = "downloadSchema"
  val LIST              = "listSchemas"
  val VALIDATE_DOCUMENT = "validateDocument"
}
