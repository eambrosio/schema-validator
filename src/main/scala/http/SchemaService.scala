package http

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax._
import model.ActionEnum.LIST
import model.StatusEnum.SUCCESS
import model._
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SchemaService[F[_]] {

  def upload(schema: String, schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

  def download(schemaId: String): F[Either[SchemaValidatorError, SchemaValidatorResponse]]

  def list: F[Either[SchemaValidatorError, SchemaValidatorResponse]]
}

case class SchemaServiceImpl()(implicit db: DatabaseDef, ec: ExecutionContext)
    extends SchemaService[Future]
    with LazyLogging {

  override def upload(
      schema: String,
      schemaId: String
  ): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val insertQuery = sqlu"INSERT INTO schemas(id,schema) VALUES($schemaId,$schema)"
    val selectQuery = sql"SELECT id FROM schemas WHERE id=$schemaId".as[String]

    val process = selectQuery
      .flatMap(result =>
        if (result.isEmpty) {
          insertQuery.map(_ => Right(SchemaValidatorResponse(ActionEnum.UPLOAD, Some(schemaId), StatusEnum.SUCCESS)))
        } else DBIO.successful(Left(DuplicatedKeyError(schemaId)))
      )

    db.run(process.transactionally)
  }

  override def download(schemaId: String): Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val selectQuery = sql"SELECT schema FROM schemas WHERE id=$schemaId".as[String]

    val result = selectQuery.asTry.map {
      case Success(value) if value.isEmpty =>
        Left(SchemaNotFoundError(schemaId))

      case Success(value) if value.nonEmpty =>
        Right(SchemaValidatorResponse(ActionEnum.DOWNLOAD, Some(schemaId), StatusEnum.SUCCESS, data = value.headOption))

      case Failure(exception) =>
        logger.error(s"unexpected error while retrieving schema: ${exception.getMessage}")
        Left(UnexpectedError(exception.getMessage))
    }

    db.run(result)
  }

  override def list: Future[Either[SchemaValidatorError, SchemaValidatorResponse]] = {
    val selectQuery = sql"SELECT * FROM schemas".as[(String, String)]

    val result = selectQuery.asTry.map {
      case Success(value) =>
        Right(SchemaValidatorResponse(
          LIST,
          None,
          SUCCESS,
          data = Some(value.toList.map(s => Schema(s._1, s._2)).asJson.noSpaces)
        ))

      case Failure(exception) =>
        logger.error(s"unexpected error while retrieving the list of schemas: ${exception.getMessage}")
        Left(UnexpectedError(exception.getMessage))
    }

    db.run(result)
  }

}
