package http

import akka.util.Timeout
import model.StatusEnum.{ERROR, SUCCESS}
import model._
import org.scalatestplus.mockito.MockitoSugar
import slick.jdbc.JdbcBackend.DatabaseDef

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait SchemaValidatorEndpointData extends MockitoSugar {

  implicit val timeout: Timeout = Timeout.durationToTimeout(FiniteDuration(5, SECONDS))

  implicit val database = mock[DatabaseDef]

  val schemaSample =
    """{"$schema":"http://json-schema.org/draft-04/schema#","title":"/etc/fstab","description":"JSON representation of /etc/fstab","type":"object","properties":{"swap":{"$ref":"#/definitions/mntent"}},"patternProperties":{"^/([^/]+(/[^/]+)*)?$":{"$ref":"#/definitions/mntent"}},"required":["/","swap"],"additionalProperties":false,"definitions":{"mntent":{"title":"mntent","description":"An fstab entry","type":"object","properties":{"device":{"type":"string"},"fstype":{"type":"string"},"options":{"type":"array","minItems":1,"items":{"type":"string"}},"dump":{"type":"integer","minimum":0},"fsck":{"type":"integer","minimum":0}},"required":["device","fstype"],"additionalItems":false}}}"""

  val successfulDownloadSchemaService: SchemaService[Future] = new SchemaServiceImpl() {

    override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Right(SchemaValidatorResponse(
        ActionEnum.DOWNLOAD,
        Some(schemaId),
        SUCCESS,
        data = Some(schemaSample)
      )))

  }

  val successfulUploadSchemaService: SchemaService[Future] = new SchemaServiceImpl() {

    override def upload(
        schema: String,
        schemaId: String
    ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Right(SchemaValidatorResponse(
        ActionEnum.UPLOAD,
        Some(schemaId),
        SUCCESS
      )))

  }

  val duplicatedKeyUploadSchemaService: SchemaService[Future] = new SchemaServiceImpl() {

    override def upload(
        schema: String,
        schemaId: String
    ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Left(DuplicatedKeyError(schemaId)))

  }

  val notFoundErrorSchemaService: SchemaService[Future] = new SchemaServiceImpl() {

    override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Left(SchemaNotFoundError(schemaId)))

  }

  val unexpectedErrorSchemaService: SchemaService[Future] = new SchemaServiceImpl() {

    override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Left(UnexpectedError("unexpected error")))

  }

  val validatorService: ValidatorServiceImpl = mock[ValidatorServiceImpl]

  val successfulValidatorService: ValidatorServiceImpl = new ValidatorServiceImpl() {

    override def validate(
        document: String,
        schemaId: String,
        schema: String
    ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = Future.successful(Right {
      SchemaValidatorResponse(
        ActionEnum.VALIDATE_DOCUMENT,
        Some(schemaId),
        SUCCESS
      )
    })

  }

  val invalidValidatorService: ValidatorServiceImpl = new ValidatorServiceImpl() {

    override def validate(
        document: String,
        schemaId: String,
        schema: String
    ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] =
      Future.successful(Right {
        SchemaValidatorResponse(
          ActionEnum.VALIDATE_DOCUMENT,
          Some(schemaId),
          ERROR,
          message = Some("missing mandatory property")
        )
      })

  }

}
