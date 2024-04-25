package MineSweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JButton restartButton = new JButton("Restart");

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + mineCount);
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());

        // Create JLabel for mine input prompt
        JLabel mineInputLabel = new JLabel("Enter number of mines:");
        mineInputLabel.setHorizontalAlignment(JLabel.CENTER);
        textPanel.add(mineInputLabel, BorderLayout.WEST);

        // Create JTextField for mine count input
        JTextField mineInput = new JTextField(10);
        mineInput.setHorizontalAlignment(JTextField.CENTER);
        mineInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = mineInput.getText();
                try {
                    int newMineCount = Integer.parseInt(input);
                    if (newMineCount >= 0 && newMineCount <= numRows * numCols) {
                        mineCount = newMineCount;
                        textLabel.setText("Minesweeper: " + mineCount);
                        resetGame();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please enter a valid number between 0 and " + (numRows * numCols), "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        textPanel.add(mineInput, BorderLayout.CENTER);
        textPanel.add(textLabel, BorderLayout.SOUTH);

        frame.add(textPanel, BorderLayout.NORTH);

        // Add restart button
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        textPanel.add(restartButton, BorderLayout.EAST);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel, BorderLayout.CENTER);

        initializeBoard();
        setMines();

        frame.setVisible(true);
    }

    void initializeBoard() {
        boardPanel.removeAll(); // Clear existing tiles
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        // left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText().isEmpty()) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        // right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().isEmpty() && tile.isEnabled()) {
                                tile.setText("🚩");
                            } else if (tile.getText().equals("🚩")) {
                                tile.setText("");
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
        boardPanel.revalidate(); // Refresh the panel
    }

    void setMines() {
        mineList = new ArrayList<>();
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
        }

        gameOver = true;
        textLabel.setText("Game Over!");
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;

        // top 3
        minesFound += countMine(r - 1, c - 1); // top left
        minesFound += countMine(r - 1, c); // top
        minesFound += countMine(r - 1, c + 1); // top right

        // left and right
        minesFound += countMine(r, c - 1); // left
        minesFound += countMine(r, c + 1); // right

        // bottom 3
        minesFound += countMine(r + 1, c - 1); // bottom left
        minesFound += countMine(r + 1, c); // bottom
        minesFound += countMine(r + 1, c + 1); // bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            // Recursively check adjacent tiles if no mines found nearby
            checkMine(r - 1, c - 1); // top left
            checkMine(r - 1, c); // top
            checkMine(r - 1, c + 1); // top right
            checkMine(r, c - 1); // left
            checkMine(r, c + 1); // right
            checkMine(r + 1, c - 1); // bottom left
            checkMine(r + 1, c); // bottom
            checkMine(r + 1, c + 1); // bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        return mineList.contains(board[r][c]) ? 1 : 0;
    }

    void resetGame() {
        tilesClicked = 0;
        gameOver = false;
        initializeBoard();
        setMines();
    }

    
}
