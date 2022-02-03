package http

import cats.syntax.either._
import http.JsonUtils.{readJson, readJsonSchema, validateJson}
import model.ActionEnum.VALIDATE_DOCUMENT
import model.StatusEnum.{ERROR, SUCCESS}
import model._

import scala.concurrent.{ExecutionContext, Future}

trait ValidatorService[F[_]] {

  def validate(
      document: String,
      schemaId: String,
      schema: String
  ): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

}

case class ValidatorServiceImpl()(implicit ec: ExecutionContext) extends ValidatorService[Future] {

  override def validate(
      document: String,
      schemaId: String,
      schema: String
  ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {

    val process = for {
      schemaJsonNode <- readJson(schema)
      documentAsJson <- readJson(document)
      jsonSchema     <- readJsonSchema(schemaJsonNode)
      result         <- validateJson(documentAsJson, jsonSchema)
    } yield result

    Future.successful(process match {
      case Right(report) =>
        if (report.isSuccess)
          SchemaValidatorResponse(VALIDATE_DOCUMENT, Some(schemaId), SUCCESS).asRight[SchemaValidatorError]
        else SchemaValidatorResponse(VALIDATE_DOCUMENT, schemaId, ERROR, report).asRight[SchemaValidatorError]

      case Left(error) => error.asLeft[SchemaValidatorResponse]
    })

  }

}
