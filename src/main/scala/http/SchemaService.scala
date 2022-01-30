package http

import cats.data.EitherT
import cats.syntax.either._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import model.ActionEnum.VALIDATE_DOCUMENT
import model.{JsonReaderError, JsonSchemaReaderError, SchemaError, SchemaValidatorResponse, ValidateJsonError}
import model.StatusEnum.{ERROR, SUCCESS}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait SchemaService[F[_]] {

  def upload(schema: String, schemaId: String): F[Either[String, SchemaValidatorResponse]]

  def download(schemaId: String): F[Either[String, SchemaValidatorResponse]]

  def validate(document: String, schemaId: String): F[Either[SchemaError, SchemaValidatorResponse]]
}

case class SchemaServiceImpl()(implicit ec: ExecutionContext) extends SchemaService[Future] {

  val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault()
  val jsonMapper: ObjectMapper             = new ObjectMapper()

  override def upload(schema: String, schemaId: String): Future[Either[String, SchemaValidatorResponse]] = ???

  override def download(schemaId: String): Future[Either[String, SchemaValidatorResponse]] = ???

  override def validate(document: String, schemaId: String): Future[Either[SchemaError, SchemaValidatorResponse]] = {
    val response = for {
      schema         <- EitherT(getSchema(schemaId))
      schemaJsonNode <- EitherT(readJson(schema))
      documentAsJson <- EitherT(readJson(document))
      jsonSchema     <- EitherT(readJsonSchema(schemaJsonNode))
      result         <- EitherT(validateJson(documentAsJson, jsonSchema))
    } yield {

      if (result.isSuccess)
        SchemaValidatorResponse(VALIDATE_DOCUMENT, schemaId, SUCCESS)
      else
        SchemaValidatorResponse(VALIDATE_DOCUMENT, schemaId, ERROR, result)
    }

    response.value

  }

  private def validateJson(
      documentAsJson: JsonNode,
      jsonSchema: JsonSchema
  ): Future[Either[SchemaError, ProcessingReport]] =
    Future.successful(Try(jsonSchema.validate(documentAsJson))
      .toEither
      .leftMap(e => ValidateJsonError(e.getMessage)))

  private def readJsonSchema(schemaJsonNode: JsonNode): Future[Either[SchemaError, JsonSchema]] =
    Future.successful(Try(jsonSchemaFactory.getJsonSchema(schemaJsonNode))
      .toEither
      .leftMap(e => JsonSchemaReaderError(e.getMessage)))

  private def readJson(schema: String): Future[Either[SchemaError, JsonNode]] =
    Future.successful(Try(jsonMapper.readTree(schema))
      .toEither
      .leftMap {
        e: Throwable => JsonReaderError(e.getMessage)
      })

  private def getSchema(schemaId: String): Future[Either[SchemaError, String]] =
    Future.successful(Right(
      """{"$schema":"http://json-schema.org/draft-04/schema#","title":"/etc/fstab","description":"JSON representation of /etc/fstab","type":"object","properties":{"swap":{"$ref":"#/definitions/mntent"}},"patternProperties":{"^/([^/]+(/[^/]+)*)?$":{"$ref":"#/definitions/mntent"}},"required":["/","swap"],"additionalProperties":false,"definitions":{"mntent":{"title":"mntent","description":"An fstab entry","type":"object","properties":{"device":{"type":"string"},"fstype":{"type":"string"},"options":{"type":"array","minItems":1,"items":{"type":"string"}},"dump":{"type":"integer","minimum":0},"fsck":{"type":"integer","minimum":0}},"required":["device","fstype"],"additionalItems":false}}}"""
    ))

  //good document
}
