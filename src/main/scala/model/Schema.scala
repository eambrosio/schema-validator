package model

//import slick.lifted.{CaseClassShape, Rep}

case class Schema(id: String, schema: String)

//object Schema {
//
//  case class SchemaLifted(id: Rep[String], schema: Rep[String])
//
//  implicit object SchemaLifted extends CaseClassShape(SchemaLifted.tupled, Schema.tupled)
//}
