package uk.ac.ic.doc.nonogram.model;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Block {

    private Line line;
    private Integer startIndex;
    private Integer index;
    private Integer size;
    private Integer color;

    public Block(Integer index, Integer size, Integer color, Line line) {
        this.line = line;
        this.startIndex = 0;
        this.index = index;
        this.size = size;
        this.color = color;
    }

    @ValueRangeProvider(id = "blockRange")
    public CountableValueRange<Integer> getValidBlockRange() {
        return line.getValidBlockRange(this);
    }

    public Integer getMinStartIndex() {
        return line.getMinStart(this);
    }

    public Integer getMaxStartIndex() {
        return line.getMaxEnd(this) - size + 1;
    }

    @PlanningVariable(valueRangeProviderRefs = {"blockRange"})
    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getColor() {
        return color;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "Block {" +
                line +
                ", " + color +
                ", " + size +
                '}';
    }
}
