package http

trait SchemaValidatorService {

  def validate(schema: String): Unit
  def upload(schema: String): Unit
  def download(schema: String): Unit
}
