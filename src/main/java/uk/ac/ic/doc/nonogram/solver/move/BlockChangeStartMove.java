package uk.ac.ic.doc.nonogram.solver.move;

import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import uk.ac.ic.doc.nonogram.model.Block;

import java.util.Collection;
import java.util.Collections;

public class BlockChangeStartMove extends AbstractMove {

    private Block block;
    private Integer toStartIndex;

    public BlockChangeStartMove(Block block, Integer toStartIndex) {
        this.block = block;
        this.toStartIndex = toStartIndex;
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return !block.getStartIndex().equals(toStartIndex);
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new BlockChangeStartMove(block, block.getStartIndex());
    }

    protected void doMoveOnGenuineVariables(ScoreDirector scoreDirector) {
        scoreDirector.beforeVariableChanged(block, "startIndex"); // before changes are made
        block.setStartIndex(toStartIndex);
        scoreDirector.afterVariableChanged(block, "startIndex"); // after changes are made
    }

    public Collection<? extends Object> getPlanningEntities() {
        return Collections.singletonList(block);
    }

    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toStartIndex);
    }

    @Override
    public String toString() {
        return block + " => (" + toStartIndex + ", " + (toStartIndex + block.getSize()) + ")";
    }
}
