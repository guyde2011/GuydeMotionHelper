package guyde.sgraphics.swing

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage

import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

trait DrawingCanvas {
  def dim: Size

  def color(all: Int): Unit = color(new Color(all, all, all))
  def color(red: Int, green: Int, blue: Int): Unit = color(new Color(red, green, blue))
  def color(color: Color): Unit
  def ellipse(x: Int, y: Int, width: Int, height: Int) = oval(x - width / 2, y - height / 2, width, height)
  def ellipse(x: Int, y: Int, size: Size): Unit = ellipse(x, y, size.width, size.height)
  def ellipseBound(x: Int, y: Int, width: Int, height: Int) = ovalBound(x - width / 2, y - height / 2, width, height)
  def ovalBound(x: Int, y: Int, width: Int, height: Int)
  def oval(x: Int, y: Int, width: Int, height: Int)
  def line(x1: Int, y1: Int, x2: Int, y2: Int)
  def rectangle(x: Int, y: Int, width: Int, height: Int)
  def rectangle(x: Int, y: Int, size: Size): Unit = rectangle(x, y, size.width, size.height)
  def rectBound(x: Int, y: Int, width: Int, height: Int)
  def rectBound(x: Int, y: Int, size: Size): Unit = rectBound(x, y, size.width, size.height)
  def polygon(poly: Polygon): Unit = polygon(poly.points)
  def polygon(points: Point*): Unit
  def polygon(points: Array[Point]): Unit
  def brush(size: Int)
  def scale(scaX : Double, scaY : Double)
  def rotate(angle : Double)
  
  def point(x: Int, y: Int)

  def image(img: BufferedImage)

  def draw()
  
 

}

trait BaseCanvas extends DrawingCanvas {
  var graphics: Graphics2D

  def color(color: Color): Unit = graphics.setColor(color)
  def oval(x: Int, y: Int, width: Int, height: Int) = graphics.fillOval(x, y, width, height)
  def ovalBound(x: Int, y: Int, width: Int, height: Int) = graphics.drawOval(x, y, width, height)
  def line(x1: Int, y1: Int, x2: Int, y2: Int) = graphics.drawLine(x1, y1, x2, y2)
  def rectangle(x: Int, y: Int, width: Int, height: Int) = graphics.fillRect(x, y, width, height)
  def rectBound(x: Int, y: Int, width: Int, height: Int) = graphics.drawRect(x, y, width, height)
  def polygon(points: Point*) = polygon(points.toArray)
  def polygon(points: Array[Point]) = graphics.fillPolygon(points.map { _.x }, points.map { _.y }, points.length)
  def brush(size: Int) = graphics.setStroke(new BasicStroke(size))

  def translate(size: Size): Unit = translate(size.width, size.height)
  def translate(w: Double, h: Double) = graphics.translate(w, h)

  def rotate(angle: Double) = graphics.rotate(angle)
  def scale(scX : Double, scY : Double) = graphics.scale(scX, scY)
  def point(x: Int, y: Int) = graphics.drawLine(x, y, x, y)

  def scope(block: => Unit) = {
    val gcur = graphics.create().asInstanceOf[Graphics2D]
    val gold = graphics
    graphics = gcur
    val a = block
    graphics = gold
  }

  def image(img: BufferedImage) = {
    graphics.drawImage(img, 0, 0, null)
  }

  def draw()

}

abstract class ImageCanvas(val image: BufferedImage) extends BaseCanvas {
  def draw() = {}
  var graphics = image.getGraphics.asInstanceOf[Graphics2D]
  def dim = Size(image.getWidth, image.getHeight)

  def this(size: Size) = this(new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR))
}

abstract class CanvasPanel extends JPanel with BaseCanvas{
  protected var shouldStart = false

  setLayout(null)

  case class KeyTracker(c: Char) extends KeyListener {

    var isDown = false

    private var p: (() => Unit) = null
    private var r: (() => Unit) = null

    def keyPressed(e: KeyEvent) = 
      if (e.getKeyChar.toLower == c) {
        if (isDown == false && p != null) 
          p()
        isDown = true
      }
      
    
    def keyReleased(e: KeyEvent) =
      if (e.getKeyChar.toLower == c) {
      if (isDown == true && r != null) {
        r()
      }
      isDown = false

    }
    def keyTyped(e: KeyEvent) = {}

    def onPress(exp: => Unit) = p = () => exp
    def onRelease(exp: => Unit) = r = () => exp

    CanvasPanel.this.addKeyListener(KeyTracker.this)
  }

  setup()

  def start() {
    shouldStart = true
  }

  def setup()

  var graphics: Graphics2D = null

  override def paintComponent(g: Graphics) = {
    graphics = g.asInstanceOf[Graphics2D]
    if (shouldStart)
      draw()
  }

  def dim_=(size: Size) = {
    setSize(size.width, size.height)
    setPreferredSize(new Dimension(size.width, size.height))
  }
  def dim = Size(getWidth, getHeight)

}

trait Frameable {
  def withFrame(name: String): JFrame
  def setupApp()

  protected def getFrame: JFrame

  object window {
    def loc = (getFrame.getX, getFrame.getY)
    def loc_=(tup: (Int, Int)) = getFrame.setLocation(tup._1, tup._2)
  }

  def start()
}

abstract class SwingCanvas(name: String) extends CanvasPanel with Frameable with Updatable{

  private var frame: JFrame = new JFrame(name)

  protected def getFrame = frame

  def withFrame(name: String) = {
    frame.setLayout(null)
    frame.setSize(dim.width, dim.height)
    frame.setLocationRelativeTo(null)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.add(this)
    frame
  }

  override def addKeyListener(l: KeyListener) = {
    super.addKeyListener(l)
    frame.addKeyListener(l)
  }

  var cps: Int = 50

  def preStart()

  def createThread(frame: JFrame) = new Thread(new Runnable() {
    def run() {
      while (!shouldStart) {}
      preStart()
      while (true) {

        update()
        frame.repaint()

        Thread sleep (1000l / cps, if (cps > 1000) (1000000 / cps) else 0)
      }
    }

  })

  def setupApp() {
    val frame = withFrame(name)

    frame.setVisible(true)
    createThread(frame).start()
  }

}

abstract class JCanvas extends SwingCanvas(""){
  def preStart() = {}
  
  start()
}

class DrawCanvas() extends CanvasPanel with Updatable{
  
  private var image : BufferedImage = null
  
  override def paintComponent(g: Graphics) = {
    if (image != null){
      g.drawImage(image, 0, 0, null)
    }
    
    
  }
  
  start()
  
  def update() = {
    
  }
  
  
  
  def draw() = {
   
  }
  
  def setup() = {}
  
  def reset() = {
    if (graphics != null) graphics.dispose()
    image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_3BYTE_BGR)
    graphics = image.getGraphics.asInstanceOf[Graphics2D]
  }
  
  override def setSize(di : Dimension) = {
    super.setSize(di)
    reset()
  }
}
