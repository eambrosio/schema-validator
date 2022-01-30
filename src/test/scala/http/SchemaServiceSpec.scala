package http

import model.ActionEnum.VALIDATE_DOCUMENT
import model.SchemaValidatorResponse
import model.StatusEnum.{ERROR, SUCCESS}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SchemaServiceSpec extends AnyFlatSpec with Matchers with EitherValues {

  val goodDocument =
    """{"/":{"device":"/dev/sda1","fstype":"btrfs","options":["ssd"]},"swap":{"device":"/dev/sda2","fstype":"swap"},"/tmp":{"device":"tmpfs","fstype":"tmpfs","options":["size=64M"]},"/var/lib/mysql":{"device":"/dev/data/mysql","fstype":"btrfs"}}"""

  val badDocument =
    """{"swap":{"device":"/dev/sda2"},"/tmp":{"device":"tmpfs","fstype":"tmpfs","options":["size=64M"]},"/var/lib/mysql":{"device":"/dev/data/mysql","fstype":"btrfs"}}"""

  it should "return a successful response" in {

    val service = SchemaServiceImpl()
    Await.result(service.validate(goodDocument, "1"), Duration.Inf).value shouldBe SchemaValidatorResponse(
      VALIDATE_DOCUMENT,
      "1",
      SUCCESS
    )
  }
  it should "return a response with status error" in {

    val service = SchemaServiceImpl()
    Await.result(service.validate(badDocument, "1"), Duration.Inf).value shouldBe SchemaValidatorResponse(
      VALIDATE_DOCUMENT,
      "1",
      ERROR,
      Some("object has missing required properties ([\"/\"])")
    )
  }
}
