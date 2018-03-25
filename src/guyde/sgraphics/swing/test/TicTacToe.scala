package guyde.sgraphics.swing.test

import guyde.sgraphics.swing.GraphicsApp
import guyde.sgraphics.swing.SwingCanvas
import guyde.sgraphics.swing.Size

object TicTacToe extends SwingCanvas("Tic Tac Toe") with GraphicsApp {

  sealed trait BoardSpace

  case object X extends BoardSpace
  case object O extends BoardSpace
  case object Empty extends BoardSpace

  val board = Array.fill[BoardSpace](9)(Empty)

  def preStart() {
    board(1) = X
    board(2) = X
    board(3) = X
    board(4) = O
    board(7) = O
    board(8) = O
  }

  def setup() {
    dim = Size(800, 600)
  }

  def draw() {
    color(255)
    rectangle(0, 0, dim)
    drawBoard()
  }

  def drawBoard() {
    translate(100, 100)
    color(0)
    scope {
      scale(5,5)
      line(24,-4,24,83)
      line(52,-4,52,83)
      line(-4, 24, 83, 24)
      line(-4,52,83,52)
    }
    for (
      row <- 0 to 2;
      col <- 0 to 2
    ) {
      scope {
        scale(10, 10)
        translate(col * 14, row * 14)
        val space = board(row * 3 + col)

        space match {
          case X => {
            line(0, 0, 10, 10)
            line(10, 0, 0, 10)
          }
          case O => ovalBound(0, 0, 10, 10)
          case Empty =>
        }
      }
    }
  }

  def resetGame() {
    (0 to 8).foreach(board(_) = Empty)
  }

  def update() {

  }
}