package guyde.sgraphics.swing

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyListener

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel

import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.UIManager
import scala.collection.mutable.ListBuffer
import java.awt.Graphics
import javax.swing.SpinnerNumberModel
import javax.swing.JSpinner
import javax.swing.JSpinner.DefaultEditor

abstract class SimplePanel(name: String) extends JPanel with Frameable with Updatable {

  UIManager.setLookAndFeel(new NimbusLookAndFeel())
  setLayout(null)

  setup()

  def setup()

  trait Offset {
    def x: Int
    def y: Int

    def addComp(c: JComponent) = {}
  }

  case class CordOffset(x: Int, y: Int) extends Offset
  case class SizeOffset(xRel: Double, yRel: Double) extends Offset {
    val x = (xRel * getWidth).toInt
    val y = (yRel * getHeight).toInt
  }

  class AutoOffset(offset: Offset, offX: Int = 0, offY: Int = 0, ignX : Boolean = false, ignY : Boolean = true) extends Offset {
    var lastX: Int = offset.x
    var lastY: Int = offset.y

    override def addComp(c: JComponent) {
      if (!ignX) lastX += c.getPreferredSize.width + offX
      if (!ignY) lastY += c.getPreferredSize.height + offY
    }

    def x = lastX
    def y = lastY

  }

  def withFrame(name: String) = {
    frame.setLayout(null)
    frame.setSize(dim.width, dim.height)
    frame.setLocationRelativeTo(null)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.add(this)
    frame
  }

  def dim = Size(getWidth, getHeight)

  def dim_=(size: Size) = setSize(size.width, size.height)

  protected var offset: Offset = CordOffset(0, 0)

  protected def size(x: Double, y: Double) = SizeOffset(x, y)
  protected def offset(x: Int, y: Int) = CordOffset(x, y)
  protected def auto(x: Int = 0, y: Int = 0, ignX : Boolean = false, ignY : Boolean = true) = 
    new AutoOffset(offset, x, y, ignX, ignY)
  var frame = new JFrame(name)

  protected def getFrame: JFrame = frame

  protected var cps = 50

  def preStart()

  def start() {
    preStart()
    setupApp()
  }
  
  private var updatables = ListBuffer[Updatable]()

  def createThread(frame: JFrame) = new Thread(new Runnable() {
    def run() {
      while (true) {

        update()
        updatables.foreach { _.update() }
        frame.repaint()
        repaint()

        Thread sleep (1000l / cps, if (cps > 1000) (1000000 / cps) else 0)
      }
    }

  })
  
  override def paintComponent(g : Graphics){
    super.paintComponent(g)
    draw()
  }  
  
  def draw() = {}
  def drawComps() = {}
  
  def setupApp() {
    val frame = withFrame(name)

    frame.setVisible(true)
    createThread(frame).start()
  }

  override def addKeyListener(l: KeyListener) = {
    super.addKeyListener(l)
    frame.addKeyListener(l)
  }

  def addComp(comp: JComponent) = {
    add(comp)
    comp.setLocation(offset.x + comp.getX, offset.y + comp.getY)
    offset.addComp(comp)
    comp.setSize(comp.getPreferredSize)
    
    comp match {
      case u : Updatable => updatables += u
      case _ => 
    }
  }

  case class Slider(min: Int, max: Int, defValue: Int) extends JSlider(min, max, defValue) {
    addComp(Slider.this)

    def value = getValue
  }

  case class Button(text: String, onPress: (() => Unit)) extends JButton(text) {
    addActionListener(new ActionListener() {
      def actionPerformed(a: ActionEvent) {
        onPress()
      }
    })
    addComp(Button.this)

  }

  case class Label(text: String) extends JLabel(text) {
    addComp(Label.this)
  }
  
  def createCanvas(size : Size) : DrawCanvas = {
    val canvas = new DrawCanvas 
    canvas.dim = size
    addComp(canvas)
    return canvas 
  }
    
  
  case class NumberSpinner(cur : Double, min : Double, max : Double, step : Double, text : String) extends JSpinner(new SpinnerNumberModel(cur, min, max, step)){
     def value = NumberSpinner.this.getValue.asInstanceOf[Double]
     val panel = new JPanel{
       add(new JLabel(text))
       add(NumberSpinner.this)
     }
     addComp(panel)
  }
  def createCanvas(width : Int, height : Int) : DrawCanvas = createCanvas(Size(width, height))
  

}