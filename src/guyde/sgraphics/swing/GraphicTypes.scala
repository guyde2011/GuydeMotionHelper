package guyde.sgraphics.swing

case class Size(width : Int, height : Int){
  lazy val area = width * height
}

case class Point(x : Int, y : Int)

trait Polygon { 
  val length : Int
  val points : Array[Point]
}

case class Triangle(point1 : Point, point2 : Point, point3 : Point) extends Polygon{
  val points = Array(point1, point2, point3)
  val length = 3
}

case class Rectangle(x1 : Int, y1 : Int, width : Int, height : Int){
  val points = Array(Point(x1,y1), Point(x1 + width, y1 + height))
}

trait Updatable{
  def update()
}