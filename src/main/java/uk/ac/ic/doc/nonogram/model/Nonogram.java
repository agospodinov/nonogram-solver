package uk.ac.ic.doc.nonogram.model;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PlanningSolution
public class Nonogram implements Solution<SimpleScore> {

    private Integer width;
    private Integer height;
    private List<Line> lines;
    private SimpleScore score;

    public Nonogram() {
    }

    public Nonogram(Integer width, Integer height) {
        this.width = width;
        this.height = height;
        this.lines = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            lines.add(new Line(i, LineType.ROW, this));
        }

        for (int i = 0; i < width; i++) {
            lines.add(new Line(i, LineType.COLUMN, this));
        }
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    @PlanningEntityCollectionProperty
    public List<Block> getBlocks() {
        return lines.stream().flatMap(line -> line.getBlocks().stream()).collect(Collectors.toList());
    }

    public Line getRow(int index) {
        if (index < 0 || index >= height) {
            throw new IllegalArgumentException("Invalid row index");
        }

        return lines.get(index);
    }

    public Line getColumn(int index) {
        if (index < 0 || index >= width) {
            throw new IllegalArgumentException("Invalid column index");
        }

        return lines.get(height + index);
    }

    @Override
    public SimpleScore getScore() {
        return score;
    }

    @Override
    public void setScore(SimpleScore score) {
        this.score = score;
    }

    @Override
    public Collection<?> getProblemFacts() {
        return lines;
    }
}
