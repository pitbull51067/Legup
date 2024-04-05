package edu.rpi.legup.puzzle.binary.rules;

import edu.rpi.legup.model.gameboard.Board;
import edu.rpi.legup.model.gameboard.PuzzleElement;
import edu.rpi.legup.model.rules.DirectRule;
import edu.rpi.legup.model.rules.ContradictionRule;
import edu.rpi.legup.model.tree.TreeNode;
import edu.rpi.legup.model.tree.TreeTransition;
import edu.rpi.legup.puzzle.binary.BinaryBoard;
import edu.rpi.legup.puzzle.binary.BinaryCell;
import edu.rpi.legup.puzzle.binary.BinaryType;

import java.util.ArrayList;
import java.util.Set;

public class SurroundPairDirectRule extends DirectRule {

    public SurroundPairDirectRule() {
        super("BINA-BASC-0001",
                "Surround Pair",
                "If two adjacent tiles have the same value, surround the tiles with the other value.",
                "edu/rpi/legup/images/binary/rules/SurroundPairDirectRule.png");
    }
    
    @Override
    public String checkRuleRawAt(TreeTransition transition, PuzzleElement puzzleElement) {
        BinaryBoard board = (BinaryBoard) transition.getBoard();
        BinaryBoard originalBoard = (BinaryBoard) transition.getParents().get(0).getBoard();
        ContradictionRule contraRule = new ThreeAdjacentContradictionRule();
//        BinaryCell c = (BinaryCell) modified.getPuzzleElement(puzzleElement);
//        System.out.println(c.getLocation());
//
//        System.out.println("BEFORE: " + c.getType());
//        if (c.getData() == 0)
//            c.setData(1);
//        else if (c.getData() == 1)
//            c.setData(0);
//
//        System.out.println("AFTER: " + c.getType());
//        modified.setCell(c.getLocation().x, c.getLocation().y, c);
//
        BinaryBoard modified = originalBoard.copy();
        BinaryCell binaryCell = (BinaryCell) puzzleElement;

        System.out.println("ORIG" + binaryCell.getData());
        System.out.println("AFTER" + Math.abs(binaryCell.getData() - 1));
        modified.getPuzzleElement(puzzleElement).setData(Math.abs(binaryCell.getData() - 1));

//
        System.out.println(contraRule.checkContradictionAt(modified, puzzleElement));
//
        if (contraRule.checkContradictionAt(modified, puzzleElement) == null) {
            return null;
        }
        return "Grouping of Three Ones or Zeros found";
//        BinaryCell cell = (BinaryCell) board.getPuzzleElement(puzzleElement);
//        if(cell.getType() == BinaryType.UNKNOWN){
//            return "Only ONE and ZERO allowed for this rule!";
//        }
//        BinaryBoard modified = board.copy();
//        Set<PuzzleElement> changedCells = board.getModifiedData();
//        for (PuzzleElement p : changedCells) {
//            BinaryCell c = (BinaryCell) board.getPuzzleElement(p);
//        }
//
//        if (c.getData() == 0)
//            modified.getPuzzleElement(puzzleElement).setData(1);
//        else if (c.getData() == 1)
//            modified.getPuzzleElement(puzzleElement).setData(0);
//        if(contraRule.checkContradictionAt(modified, puzzleElement) != null){
//            return "Grouping of three ONEs or ZEROs found";
//        }

//
//
//        Set<PuzzleElement> changedCells = board.getModifiedData();
//        System.out.println("SIZE" + changedCells.size());
//        for (PuzzleElement p : changedCells) {
//            BinaryCell c = (BinaryCell) board.getPuzzleElement(p);
//            System.out.println(c.getLocation());
//            if (c.getData() == 0)
//                c.setData(1);
//            else if (c.getData() == 1)
//                c.setData(0);
//            System.out.println("HI");
//        }

//        for (PuzzleElement p : changedCells) {
//            BinaryCell c = (BinaryCell) board.getPuzzleElement(p);
//            //System.out.println(c.getLocation());
//            if (c.getData() == 0)
//                c.setData(1);
//            else if (c.getData() == 1)
//                c.setData(0);
//        }
    }

    @Override
    public Board getDefaultBoard(TreeNode node) {
        return null;
    }

}