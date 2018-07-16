package puzzle.battleship;

import controller.ElementController;
import model.gameboard.ElementData;

import java.awt.event.MouseEvent;

public class BattleShipCellController extends ElementController {
    @Override
    public void changeCell(MouseEvent e, ElementData data) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.isControlDown()) {
                this.boardView.getSelectionPopupMenu().show(boardView, this.boardView.getCanvas().getX() + e.getX(), this.boardView.getCanvas().getY() + e.getY());
            } else {
                // DO STUFF WHEN ELEMENT IS CLICKED ON
            }
        }
    }
}