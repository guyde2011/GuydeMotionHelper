package guyde.sgraphics.swing.test

import guyde.sgraphics.swing.GraphicsApp
import guyde.sgraphics.swing.SimplePanel
import guyde.sgraphics.swing.Size
import guyde.sgraphics.swing.SwingCanvas
import guyde.sgraphics.swing.JCanvas
import guyde.sgraphics.swing.DrawCanvas
import javax.imageio.ImageIO
import java.awt.Color
import scala.collection.mutable.MutableList
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object TestApp extends SimplePanel("Path Creation Tool") with GraphicsApp{
  
  offset = offset(17,15)
  offset = auto(5, 0, false, true)
  
  val ro = Size(90, 100)
  val record = Button("Add", addPt)
  val remove = Button("Remove", remPt)
  val goBack = Button("Go Back", comeBack)
  val export = Button("Export", exportClip)
  offset = offset(offset.x,10)
  offset = auto(5, 0, false, true)
    var slider3 = NumberSpinner(0.01,0,0.2,0.005, "Accuracy:")
  var slider = NumberSpinner(ro.width / 200d,0,8.23,0.1, "X:")
  var listP = MutableList[(Double, Double)]()
  
  def addPt() {
    listP += ((slider.value, slider2.value))
  }
  
  def remPt() {
    listP = listP.dropRight(1)
  }
  
  def exportClip(){
    val cp = Toolkit.getDefaultToolkit.getSystemClipboard
    cp.setContents(new StringSelection(listP.map(x => ((x._1 - listP(0)._1).toFloat, (x._2 - listP(0)._2).toFloat, slider3.value.toFloat))
        .mkString("PathFactory path = new PathFactory()", ".connectLine", 
            """;
ArenaMap map = new ArenaMap();
path.construct(map);""")), null)
  }
  
  def comeBack() {
    if (listP.isEmpty){
      slider.setValue(ro.width / 200d)
      slider2.setValue(ro.height / 200d)
    } else {
      slider.setValue(listP.last._1)
      slider2.setValue(listP.last._2)
    }
  }
  
  offset = offset(offset.x,10)
  offset = auto(0, 5, true, false)
  var slider2 = NumberSpinner(ro.height / 200d,0,8.23,0.1, "Y:")

  



  offset = offset(17,offset.y)
  val canvas = createCanvas(823,823)
  val img = ImageIO.read(getClass.getResourceAsStream("BlueAlliance.png"))  
  
  def preStart(){

  }
  
  def update(){
    
  }
  val si = Size(10,10)
  
  override def draw(){
   // canvas.color(255)
    canvas.scope{
      canvas.graphics.scale(canvas.dim.width.toDouble / img.getWidth, canvas.dim.height.toDouble / img.getHeight)
      canvas.graphics.drawImage(img, 0, 0, null)
    }
    canvas.color(Color.green)
    canvas.scope{
      canvas.translate((slider.value * 100).toInt, 823 - ((slider2.value * 100).toInt))
      if (!listP.isEmpty)
      canvas.rotate(math.atan2(slider.value - listP.last._1, slider2.value - listP.last._2))
      canvas.translate(- ro.width / 2, - ro.height /2)
      canvas.rectangle(0,0, ro)
    }
    canvas.color(Color.RED)
    canvas.brush(2)
    if (listP.size > 0){
      var lastPt = listP(0)
      listP.foreach( pt => {
   
      canvas.ellipse((pt._1 * 100).toInt + si.width / 2, 823 - ((pt._2 * 100).toInt + si.height / 2), si)
      canvas.line((pt._1 * 100).toInt + si.width / 2, 823 - (pt._2 * 100).toInt - si.height / 2,
       (lastPt._1 * 100).toInt + si.width / 2, 823 - (lastPt._2 * 100).toInt - si.height / 2)
      lastPt = pt
      })
    
    }
  }
  
  def setup(){
    dim = Size(873, 932)

  }
}