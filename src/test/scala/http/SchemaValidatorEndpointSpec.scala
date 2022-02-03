package http

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import model.ActionEnum.{DOWNLOAD, UPLOAD, VALIDATE_DOCUMENT}
import model.StatusEnum.{ERROR, SUCCESS}
import model.{ActionEnum, SchemaValidatorResponse, StatusEnum}
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchemaValidatorEndpointSpec
    extends AnyFlatSpec
    with Matchers
    with GivenWhenThen
    with ScalatestRouteTest
    with SchemaValidatorEndpointData
    with FailFastCirceSupport
    with AutoDerivation {

  behavior of "SchemaValidatorEndpoint"

  it should "return a status 200 and an existing schema" in {
    Given("an endpoint")
    And("a service which returns an existing schema")
    val endpoint = SchemaValidatorEndpoint(successfulDownloadSchemaService, validatorService)

    When("we perform a GET request to /schema/test-1")

    Get("/schema/test-1") ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected schema")
        And("the status is 200")

        responseAs[SchemaValidatorResponse].data should contain(schemaSample)
        response.status shouldBe StatusCodes.OK
      }
  }

  it should "return  nothing when the schema does not exist" in {
    Given("an endpoint")
    And("a service which returns nothing")
    val endpoint = SchemaValidatorEndpoint(notFoundErrorSchemaService, validatorService)

    When("we perform a GET request to /schema/test-1")

    Get("/schema/test-1") ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected schema")
        And("the status is 200")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          ActionEnum.DOWNLOAD,
          Some("test-1"),
          StatusEnum.ERROR,
          message = Some("Schema with id 'test-1' not found")
        )
        response.status shouldBe StatusCodes.BadRequest
      }
  }

  it should "return an internal error when something goes wrong" in {
    Given("an endpoint")
    And("a service which returns an unexpected error")
    val endpoint = SchemaValidatorEndpoint(unexpectedErrorSchemaService, validatorService)

    When("we perform a GET request to /schema/test-1")

    Get("/schema/test-1") ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected schema")
        And("the status is 200")

        responseAs[String] shouldBe "unexpected error"
        response.status shouldBe StatusCodes.InternalServerError
      }
  }

  it should "upload a schema an return a status 201 when no duplicates" in {
    Given("an endpoint")
    And("a service which uploads successfully a schema")
    val endpoint = SchemaValidatorEndpoint(successfulUploadSchemaService, validatorService)

    When("we perform a POST request to /schema/test-1")

    Post("/schema/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected json")
        And("the status is 200")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          UPLOAD,
          Some("test-1"),
          SUCCESS
        )
        response.status shouldBe StatusCodes.OK
      }
  }

  it should "not upload a schema when the schema already exists an return a status 201" in {
    Given("an endpoint")
    And("a service which uploads successfully a schema")
    val endpoint = SchemaValidatorEndpoint(duplicatedKeyUploadSchemaService, validatorService)

    When("we perform a POST request to /schema/test-1")

    Post("/schema/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected json")
        And("the status is 200")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          UPLOAD,
          Some("test-1"),
          ERROR,
          message = Some("Schema with id 'test-1' already exists")
        )
        response.status shouldBe StatusCodes.BadRequest
      }
  }

  it should "return an internal error when something goes wrong when performing POST /schema/test-1" in {
    Given("an endpoint")
    And("a service which returns an unexpected error")
    val endpoint = SchemaValidatorEndpoint(successfulDownloadSchemaService, validatorService)

    When("we perform a POST request to /schema/test-1")

    Post("/schema/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the status is 500")

        response.status shouldBe StatusCodes.InternalServerError
      }
  }

  it should "return an error when validating a document against a non existing schema" in {
    Given("an endpoint")
    And("a service which returns not found schema error")
    val endpoint = SchemaValidatorEndpoint(notFoundErrorSchemaService, validatorService)

    When("we perform a POST request to /validate/test-1")

    Post("/validate/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected error")
        And("the status is 400")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          DOWNLOAD,
          Some("test-1"),
          ERROR,
          message = Some("Schema with id 'test-1' not found")
        )
        response.status shouldBe StatusCodes.BadRequest
      }
  }

  it should "return an error when validating a not valid document against an existing schema" in {
    Given("an endpoint")
    And("a service which returns invalid schema")
    val endpoint = SchemaValidatorEndpoint(successfulDownloadSchemaService, invalidValidatorService)

    When("we perform a POST request to /validate/test-1")

    Post("/validate/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the response contains the expected error")
        And("the status is 200")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          VALIDATE_DOCUMENT,
          Some("test-1"),
          ERROR,
          message = Some("missing mandatory property")
        )
        response.status shouldBe StatusCodes.OK
      }
  }

  it should "return a successful message when validating a valid document against an existing schema" in {
    Given("an endpoint")
    And("a service which returns a valid schema")
    val endpoint = SchemaValidatorEndpoint(successfulDownloadSchemaService, successfulValidatorService)

    When("we perform a POST request to /validate/test-1")

    Post("/validate/test-1").withEntity(ContentTypes.`application/json`, schemaSample) ~>
      endpoint.routes ~>
      check {
        Then("the response contains the a successful msg")
        And("the status is 200")

        responseAs[SchemaValidatorResponse] shouldBe SchemaValidatorResponse(
          VALIDATE_DOCUMENT,
          Some("test-1"),
          SUCCESS
        )
        response.status shouldBe StatusCodes.OK
      }
  }
}
