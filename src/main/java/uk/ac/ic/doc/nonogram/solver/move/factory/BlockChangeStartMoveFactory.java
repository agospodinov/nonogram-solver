package uk.ac.ic.doc.nonogram.solver.move.factory;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import uk.ac.ic.doc.nonogram.model.Block;
import uk.ac.ic.doc.nonogram.model.Line;
import uk.ac.ic.doc.nonogram.model.Nonogram;
import uk.ac.ic.doc.nonogram.solver.move.BlockChangeStartMove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockChangeStartMoveFactory implements MoveListFactory<Nonogram> {
    public List<Move> createMoveList(Nonogram nonogram) {
        List<Move> moves = new ArrayList<>();
        for (Line line : nonogram.getLines()) {
            for (Block block : line.getBlocks()) {
                Iterator<Integer> iterator = block.getValidBlockRange().createOriginalIterator();
                while (iterator.hasNext()) {
                    moves.add(new BlockChangeStartMove(block, iterator.next()));
                }
            }
        }
        return moves;
    }
}
