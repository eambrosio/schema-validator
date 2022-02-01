package model

import com.github.fge.jsonschema.core.report.ProcessingReport

import scala.jdk.CollectionConverters._

case class SchemaValidatorResponse(
    action: String,
    id: String,
    status: String,
    message: Option[String] = None,
    document: Option[String] = None
)

object SchemaValidatorResponse {

  def apply(action: String, id: String, status: String, result: ProcessingReport): SchemaValidatorResponse = {

    val message = result.iterator.asScala.toList.map(_.getMessage).head

    SchemaValidatorResponse(action, id, status, Some(message))
  }

}
