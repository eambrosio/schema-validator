package http

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import model.{JsonReaderError, JsonSchemaReaderError, SchemaValidatorError, ValidateJsonError}
import cats.syntax.either._

import scala.util.Try

object JsonUtils {

  val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault()
  val jsonMapper: ObjectMapper             = new ObjectMapper()

  def validateJson(
      documentAsJson: JsonNode,
      jsonSchema: JsonSchema
  ): Either[SchemaValidatorError, ProcessingReport] =
    Try(jsonSchema.validate(documentAsJson))
      .toEither
      .leftMap(e => ValidateJsonError(e.getMessage))

  def readJsonSchema(schemaJsonNode: JsonNode): Either[SchemaValidatorError, JsonSchema] =
    Try(jsonSchemaFactory.getJsonSchema(schemaJsonNode))
      .toEither
      .leftMap(e => JsonSchemaReaderError(e.getMessage))

  def readJson(json: String): Either[SchemaValidatorError, JsonNode] =
    Try(jsonMapper.readTree(json))
      .toEither
      .leftMap {
        e: Throwable => JsonReaderError(e.getMessage)
      }

}
