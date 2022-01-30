//package http
//
//import com.github.fge.jsonschema.main.JsonSchemaFactory
//
//trait ValidatorService {
//  def validate(document: String, schemaId: String): Unit
//}
//
//case class ValidatorServiceImpl() extends ValidatorService {
//
//  val jsonSchemaFactory = JsonSchemaFactory.byDefault()
//
//  override def validate(document: String, schemaId: String): Unit =
//    jsonSchemaFactory.getJsonSchema("").validate()
//
//}
