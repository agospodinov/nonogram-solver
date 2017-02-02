package uk.ac.ic.doc.nonogram.ui;

import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import uk.ac.ic.doc.nonogram.controller.NonogramSolver;
import uk.ac.ic.doc.nonogram.model.Block;
import uk.ac.ic.doc.nonogram.model.Line;
import uk.ac.ic.doc.nonogram.model.LineType;
import uk.ac.ic.doc.nonogram.model.Nonogram;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NonogramForm extends JFrame {
    public static final int CELL_SIZE = 30;

    private JPanel mainPanel;
    private JButton solveButton;
    private JButton newNonogramButton;
    private JPanel nonogramPanel;
    private JPanel[][] nonogramCells;
    private JPanel columnInputPanel;
    private JPanel rowInputPanel;
    private JPanel colorPanel;
    private JCheckBox updateLiveCheckBox;
    private JButton openNonogramButton;
    private List<Color> colors;

    private NonogramSolver nonogramSolver;

    public static void main(String[] args) {
        NonogramForm form = new NonogramForm();
        form.setVisible(true);
    }

    private NonogramForm() {
        super("Nonogram Solver");
        nonogramSolver = new NonogramSolver();

        colors = new ArrayList<>();

        newNonogramButton.addActionListener(this::onNewNonogramButtonClicked);
        openNonogramButton.addActionListener(this::onOpenNonogramButtonClicked);
        solveButton.addActionListener(this::onSolveButtonClicked);

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
    }

    private void onNewNonogramButtonClicked(ActionEvent e) {
        try {
            String widthString = JOptionPane.showInputDialog("Enter width: ");
            Integer width = Integer.parseInt(widthString);

            String heightString = JOptionPane.showInputDialog("Enter height: ");
            Integer height = Integer.parseInt(heightString);

            nonogramSolver.initialize(width, height);

            colors.clear();
            colors.add(Color.BLACK);
            createNonogramTable(width, height);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainPanel, "Invalid value, please enter a number");
        }
    }

    private void onOpenNonogramButtonClicked(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        chooser.setFileFilter(filter);

        if (chooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));

                String[] sizes = reader.readLine().split(" ");
                if (sizes.length != 2) {
                    throw new IllegalArgumentException("Invalid file");
                }

                Integer width = Integer.parseInt(sizes[0]);
                Integer height = Integer.parseInt(sizes[1]);

                nonogramSolver.initialize(width, height);
                colors.clear();

                Integer colorCount = Integer.parseInt(reader.readLine());
                for (int i = 0; i < colorCount; i++) {
                    String[] colorValues = reader.readLine().split(" ");

                    Integer red = Integer.parseInt(colorValues[0]);
                    Integer green = Integer.parseInt(colorValues[1]);
                    Integer blue = Integer.parseInt(colorValues[2]);

                    colors.add(new Color(red, green, blue));
                }

                List<Line> lines = nonogramSolver.getNonogram().getLines();
                for (int i = 0; i < height + width; i++) {
                    Line line = lines.get(i);
                    String[] values = reader.readLine().split(" ");
                    String[] colorValues = Collections.<String>nCopies(values.length, "1")
                            .toArray(new String[values.length]);

                    if (colors.size() > 1) {
                        String[] colorInputValues = reader.readLine().split(" ");

                        if (colorValues.length != colorInputValues.length) {
                            throw new IllegalArgumentException("Colors length is not the same as values length on line " + i);
                        }

                        System.arraycopy(colorInputValues, 0, colorValues, 0, colorValues.length);
                    }

                    List<Block> newBlocks = new ArrayList<>();
                    for (int j = 0; j < values.length; j++){
                        String value = values[j];
                        String colorValue = colorValues[j];

                        Integer blockSize = Integer.parseInt(value);
                        Integer color = Integer.parseInt(colorValue);

                        newBlocks.add(new Block(i, blockSize, color - 1, line));
                    }

                    line.setBlocks(newBlocks);
                }

                createNonogramTable(width, height);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error reading file");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(mainPanel, "File isn't in the right format: " + ex.getMessage());
            }

        }
    }

    private void onSolveButtonClicked(ActionEvent e) {
        if (nonogramSolver.isSolving()) {
            nonogramSolver.terminate();
            solveButton.setText("Solve");
        } else {
            nonogramSolver.solve(this::onSolutionUpdated);
            solveButton.setText("Terminate");
        }
    }

    private void onSolutionUpdated(BestSolutionChangedEvent<Nonogram> event) {
        try {
            if (updateLiveCheckBox.isSelected() && event.isNewBestSolutionInitialized()) {
                SwingUtilities.invokeAndWait(() -> {
                    Nonogram newBestSolution = event.getNewBestSolution();

                    for (int j = 0; j < newBestSolution.getHeight(); j++) {
                        for (int i = 0; i < newBestSolution.getWidth(); i++) {
                            JPanel panel = nonogramCells[j][i];
                            panel.setBackground(Color.WHITE);
                        }
                    }

                    newBestSolution.getBlocks().forEach(block -> {
                        if (block.getLine().getType() == LineType.ROW) {
                            int rowIndex = block.getLine().getIndex();
                            int columnIndex = block.getStartIndex();

                            for (int i = columnIndex; i < columnIndex + block.getSize(); i++) {
                                JPanel panel = nonogramCells[rowIndex][i];
                                panel.setBackground(colors.get(block.getColor()));
                            }
                        } else {
                            int rowIndex = block.getStartIndex();
                            int columnIndex = block.getLine().getIndex();

                            for (int i = rowIndex; i < rowIndex + block.getSize(); i++) {
                                JPanel panel = nonogramCells[i][columnIndex];
                                panel.setBackground(colors.get(block.getColor()));
                            }
                        }
                    });
                });
            }
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private void createNonogramTable(int width, int height) {
        nonogramPanel.removeAll();
        columnInputPanel.removeAll();
        rowInputPanel.removeAll();

        nonogramPanel.setMinimumSize(new Dimension(width * CELL_SIZE, height * CELL_SIZE));
        nonogramPanel.setMaximumSize(new Dimension(width * CELL_SIZE, height * CELL_SIZE));
        nonogramPanel.setLayout(new GridLayout(height, width));

        nonogramCells = new JPanel[height][width];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                nonogramPanel.add(panel);
                nonogramCells[j][i] = panel;
            }
        }

        resetLineInputCells(width, columnInputPanel, true);
        resetLineInputCells(height, rowInputPanel, false);

        resetColorPanel();

        solveButton.setEnabled(true);
        pack();
    }

    private void resetColorPanel() {
        colorPanel.removeAll();

        colorPanel.setPreferredSize(new Dimension(2 * (colors.size() + 1) * CELL_SIZE, 2 * CELL_SIZE));
        colorPanel.setLayout(new GridLayout(1, colors.size() + 1));

        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            int number = i + 1;

            JPanel panel = new JPanel();
            JLabel label = new JLabel(String.valueOf(number));
            panel.add(label);
            panel.setBackground(color);
            label.setForeground(getLabelColor(color));
            panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Color newColor = JColorChooser.showDialog(null, "Choose a color", color);
                    if (newColor != null) {
                        colors.set(number - 1, newColor);
                        resetColorPanel();
                    }
                }
            });

            colorPanel.add(panel);
        }

        JPanel addColorPanel = new JPanel();
        addColorPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Color color = JColorChooser.showDialog(null, "Choose a color", null);
                if (color != null) {
                    colors.add(color);
                    resetColorPanel();
                }
            }
        });
        addColorPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        colorPanel.add(addColorPanel);

        revalidate();
        pack();
    }

    private void resetLineInputCells(int longDimension, JPanel inputPanel, boolean isColumn) {
        Integer lineLength = 1;
        for (int i = 0; i < longDimension; i++) {
            Line line;
            if (isColumn) {
                line = nonogramSolver.getNonogram().getColumn(i);
            } else {
                line = nonogramSolver.getNonogram().getRow(i);
            }

            lineLength = Math.max(lineLength, line.getBlocks().size());
        }

        int width = isColumn ? longDimension : lineLength;
        int height = isColumn ? lineLength : longDimension;
        inputPanel.removeAll();
        inputPanel.setPreferredSize(new Dimension(width * CELL_SIZE, height * CELL_SIZE));
        inputPanel.setLayout(new GridLayout(height, width));

        for (int i = 0; i < (isColumn ? lineLength : longDimension); i++) {
            for (int j = 0; j < (!isColumn ? lineLength : longDimension); j++) {
                Line line = isColumn
                        ? nonogramSolver.getNonogram().getColumn(j)
                        : nonogramSolver.getNonogram().getRow(i);

                int startBlock = lineLength - line.getBlocks().size();
                int currentLineBlock = isColumn ? i : j;

                String blockSizeLabel = "";
                Color blockColor = null;

                if (startBlock < lineLength && currentLineBlock >= startBlock) {
                    Block block = line.getBlocks().get(currentLineBlock - startBlock);
                    blockSizeLabel = block.getSize().toString();
                    blockColor = colors.get(block.getColor());
                }

                JPanel panel = new JPanel();
                JLabel label = new JLabel(blockSizeLabel);
                if (blockColor != null) {
                    panel.setBackground(blockColor);
                    label.setForeground(getLabelColor(blockColor));
                }

                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        List<String> blockSizes = line.getBlocks().stream()
                                .map(Block::getSize)
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        List<String> blockColors = line.getBlocks().stream()
                                .map(Block::getColor)
                                .map(i -> i + 1)
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        String input = JOptionPane.showInputDialog(mainPanel,
                                "Input values separated by space",
                                String.join(" ", blockSizes));

                        String[] values = input.split(" ");
                        String[] colorValues = Collections.<String>nCopies(values.length, "1")
                                .toArray(new String[values.length]);

                        if (colors.size() > 1) {
                            String colorInput = JOptionPane.showInputDialog(mainPanel,
                                    "Input color values separated by space",
                                    String.join(" ", blockColors));

                            String[] colorInputValues = colorInput.split(" ");

                            if (colorValues.length != colorInputValues.length) {
                                JOptionPane.showMessageDialog(NonogramForm.this, "Invalid number of colors");
                                return;
                            }

                            System.arraycopy(colorInputValues, 0, colorValues, 0, colorValues.length);
                        }

                        try {
                            List<Block> newBlocks = new ArrayList<>();
                            for (int i = 0; i < values.length; i++){
                                String value = values[i];
                                String colorValue = colorValues[i];

                                Integer blockSize = Integer.parseInt(value);
                                Integer color = Integer.parseInt(colorValue);

                                newBlocks.add(new Block(i, blockSize, color - 1, line));
                            }

                            line.setBlocks(newBlocks);

                            resetLineInputCells(longDimension, inputPanel, isColumn);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(mainPanel, "Invalid value, please enter a number");
                        }
                    }
                });

                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                panel.add(label);
                inputPanel.add(panel);
            }
        }

        revalidate();
        pack();
    }

    private Color getLabelColor(Color blockColor) {
        Color labelColor;
        double darkness = 1 - (0.299 * blockColor.getRed()
                + 0.587 * blockColor.getGreen()
                + 0.114 * blockColor.getBlue()) / 255.0;

        if (darkness < 0.5) {
            labelColor = Color.BLACK;
        } else {
            labelColor = Color.WHITE;
        }
        return labelColor;
    }
}
