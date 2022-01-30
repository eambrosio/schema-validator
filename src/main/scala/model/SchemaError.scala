package model

sealed abstract class SchemaError(msg: String)

case class JsonReaderError(msg: String)       extends SchemaError(s"Error while reading a Json: $msg")
case class JsonSchemaReaderError(msg: String) extends SchemaError(s"Error while reading a Json Schema: $msg")
case class ValidateJsonError(msg: String)     extends SchemaError(s"Error while validating a Json: $msg")
