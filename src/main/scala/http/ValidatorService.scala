package http

import cats.data.EitherT
import cats.syntax.either._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import model.ActionEnum.VALIDATE_DOCUMENT
import model.StatusEnum.{ERROR, SUCCESS}
import model._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait ValidatorService[F[_]] {

  def validate(
      document: String,
      schemaId: String,
      schema: String
  ): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

}

case class ValidatorServiceImpl()(implicit ec: ExecutionContext) extends ValidatorService[Future] {

  val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault()
  val jsonMapper: ObjectMapper             = new ObjectMapper()

  override def validate(
      document: String,
      schemaId: String,
      schema: String
  ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {

    val response = for {
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
  ): Future[Either[SchemaValidatorError, ProcessingReport]] =
    Future.successful(Try(jsonSchema.validate(documentAsJson))
      .toEither
      .leftMap(e => ValidateJsonError(e.getMessage)))

  private def readJsonSchema(schemaJsonNode: JsonNode): Future[Either[SchemaValidatorError, JsonSchema]] =
    Future.successful(Try(jsonSchemaFactory.getJsonSchema(schemaJsonNode))
      .toEither
      .leftMap(e => JsonSchemaReaderError(e.getMessage)))

  private def readJson(json: String): Future[Either[SchemaValidatorError, JsonNode]] =
    Future.successful(Try(jsonMapper.readTree(json))
      .toEither
      .leftMap {
        e: Throwable => JsonReaderError(e.getMessage)
      })

  //  private def getSchema(schemaId: String): Future[Either[SchemaError, String]] =
  //    Future.successful(Right(
  //      """{"$schema":"http://json-schema.org/draft-04/schema#","title":"/etc/fstab","description":"JSON representation of /etc/fstab","type":"object","properties":{"swap":{"$ref":"#/definitions/mntent"}},"patternProperties":{"^/([^/]+(/[^/]+)*)?$":{"$ref":"#/definitions/mntent"}},"required":["/","swap"],"additionalProperties":false,"definitions":{"mntent":{"title":"mntent","description":"An fstab entry","type":"object","properties":{"device":{"type":"string"},"fstype":{"type":"string"},"options":{"type":"array","minItems":1,"items":{"type":"string"}},"dump":{"type":"integer","minimum":0},"fsck":{"type":"integer","minimum":0}},"required":["device","fstype"],"additionalItems":false}}}"""
  //    ))

  //good document

}
