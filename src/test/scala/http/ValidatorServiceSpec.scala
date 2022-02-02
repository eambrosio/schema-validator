package http

import io.circe.syntax.EncoderOps
import model.ActionEnum.VALIDATE_DOCUMENT
import model.SchemaValidatorResponse
import model.StatusEnum.{ERROR, SUCCESS}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class ValidatorServiceSpec extends AnyFlatSpec with Matchers with EitherValues {

  val schema =
    """{"$schema":"http://json-schema.org/draft-04/schema#","title":"/etc/fstab","description":"JSON representation of /etc/fstab","type":"object","properties":{"swap":{"$ref":"#/definitions/mntent"}},"patternProperties":{"^/([^/]+(/[^/]+)*)?$":{"$ref":"#/definitions/mntent"}},"required":["/","swap"],"additionalProperties":false,"definitions":{"mntent":{"title":"mntent","description":"An fstab entry","type":"object","properties":{"device":{"type":"string"},"fstype":{"type":"string"},"options":{"type":"array","minItems":1,"items":{"type":"string"}},"dump":{"type":"integer","minimum":0},"fsck":{"type":"integer","minimum":0}},"required":["device","fstype"],"additionalItems":false}}}"""

  val goodDocument =
    """{"/":{"device":"/dev/sda1","fstype":"btrfs","options":["ssd"]},"swap":{"device":"/dev/sda2","fstype":"swap"},"/tmp":{"device":"tmpfs","fstype":"tmpfs","options":["size=64M"]},"/var/lib/mysql":{"device":"/dev/data/mysql","fstype":"btrfs"}}"""

  val badDocument =
    """{"swap":{"device":"/dev/sda2"},"/tmp":{"device":"tmpfs","fstype":"tmpfs","options":["size=64M"]},"/var/lib/mysql":{"device":"/dev/data/mysql","fstype":"btrfs"}}"""

  it should "return a successful response" in {

    val service = ValidatorServiceImpl()
    Await.result(service.validate(goodDocument, "1", schema), Duration.Inf).value shouldBe SchemaValidatorResponse(
      VALIDATE_DOCUMENT,
      Some("1"),
      SUCCESS
    )
  }

  it should "return a response with status error" in {

    val service = ValidatorServiceImpl()
    Await.result(service.validate(badDocument, "1", schema), Duration.Inf).value shouldBe SchemaValidatorResponse(
      VALIDATE_DOCUMENT,
      Some("1"),
      ERROR,
      Some("object has missing required properties ([\"/\"])")
    )
  }
}
