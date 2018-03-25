package guyde.sgraphics.swing

trait GraphicsApp extends Frameable {
  private def _init(args : Array[String]) = {

    setupApp()
    init(args)
    start()
  }
  
  def init(args : Array[String]){}
  
  
  final def main(args : Array[String]) = {

    _init(args)
  }
}
