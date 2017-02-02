package uk.ac.ic.doc.nonogram.model;

import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Line {

    private Nonogram nonogram;
    private Integer index;
    private LineType type;
    private List<Block> blocks;

    public Line(int index, LineType type, Nonogram nonogram) {
        this.index = index;
        this.type = type;
        this.nonogram = nonogram;
        this.blocks = new ArrayList<>();
    }

    public Nonogram getNonogram() {
        return nonogram;
    }

    public int getIndex() {
        return index;
    }

    public LineType getType() {
        return type;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public CountableValueRange<Integer> getValidBlockRange(Block block) {

        if (!blocks.contains(block)) {
            throw new IllegalArgumentException("Line does not contain block");
        }

        int minStart = getMinStart(block);
        int maxStart = getMaxEnd(block);

        maxStart -= block.getSize();
        maxStart += 1;

        return ValueRangeFactory.createIntValueRange(minStart, maxStart);
    }

    public int getMinStart(Block block) {
        int minStart = 0;
        ListIterator<Block> iterator = blocks.listIterator();
        Block previous = null;

        while (iterator.hasNext()) {
            Block current = iterator.next();

            if (previous != null && previous.getColor() == current.getColor()) {
                minStart += 1;
            }

            if (current == block) {
                break;
            } else {
                minStart += current.getSize();
            }

            previous = current;
        }
        return minStart;
    }

    public int getMaxEnd(Block block) {
        int lineLength = type == LineType.ROW ? nonogram.getWidth() : nonogram.getHeight();
        int maxEnd = lineLength;
        ListIterator<Block> iterator = blocks.listIterator(blocks.size());
        Block previous = null;

        while (iterator.hasPrevious()) {
            Block current = iterator.previous();

            if (previous != null && previous.getColor() == current.getColor()) {
                maxEnd -= 1;
            }

            if (current == block) {
                break;
            } else {
                maxEnd -= current.getSize();
            }

            previous = current;
        }
        return maxEnd;
    }

    @Override
    public String toString() {
        return (type == LineType.ROW ? "Row " : "Column ") + index;
    }
}
