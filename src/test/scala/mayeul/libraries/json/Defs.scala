package mayeul.libraries.json

case class Child(name: String, age: Int, id: Option[Int], m: Map[Int, String])
case class ChildReduced(name: String, age: Int)
