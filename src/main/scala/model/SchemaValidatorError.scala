package model

sealed abstract class SchemaValidatorError(val msg: String)

case class JsonReaderError(override val msg: String) extends SchemaValidatorError(s"Error while reading a Json: $msg")

case class JsonSchemaReaderError(override val msg: String)
    extends SchemaValidatorError(s"Error while reading a Json Schema: $msg")

case class ValidateJsonError(override val msg: String)
    extends SchemaValidatorError(s"Error while validating a Json: $msg")

case class UnexpectedError(override val msg: String) extends SchemaValidatorError(s"Unexpected error happened: $msg")
case class SchemaNotFoundError(id: String)           extends SchemaValidatorError(s"Schema with id '$id' not found")
case class DuplicatedKeyError(id: String)            extends SchemaValidatorError(s"Schema with id '$id' already exists")
