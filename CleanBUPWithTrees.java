package cleanbupwithtrees;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class CleanBUPWithTrees extends JFrame {
    // Store bins selected for collection in the last optimization
    private final List<Bin> selectedBins = new ArrayList<>();

    private static final int FLOORS = 5;
    private static final int BINS_PER_FLOOR = 3;
    private static final int THRESHOLD = 70;
    private static final int CAPACITY_PER_FLOOR = 2;

    private final List<Bin> bins = new ArrayList<>();
    private final JTextArea resultArea = new JTextArea(15, 40);
    private final BinNetworkPanel networkPanel = new BinNetworkPanel();

    public CleanBUPWithTrees() {
        setTitle("Clean BUP - 0/1 Knapsack DP Approach");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initBins();
        initUI();
    }

    private void initBins() {
        int id = 0;
        int binSpacingX = 80;
        int binSpacingY = 80;
        int startX = 150;
        int startY = 100;

        for (int floor = 1; floor <= FLOORS; floor++) {
            for (int i = 0; i < BINS_PER_FLOOR; i++) {
                int x = startX + (i * binSpacingX);
                int y = startY + (floor * binSpacingY);
                bins.add(new Bin(id, floor, x, y));
                id++;
            }
        }
    }

    private void initUI() {
        JPanel inputPanel = new JPanel(new GridLayout(FLOORS + 1, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Bin Fill Levels"));

        // Add a panel for global settings
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        JLabel thresholdLabel = new JLabel("Threshold: " + THRESHOLD + "%");
        JLabel capacityLabel = new JLabel("Max bins/floor: " + CAPACITY_PER_FLOOR);
        settingsPanel.add(thresholdLabel);
        settingsPanel.add(Box.createHorizontalStrut(10));
        settingsPanel.add(capacityLabel);
        inputPanel.add(settingsPanel);

        for (int floor = 1; floor <= FLOORS; floor++) {
            JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            floorPanel.setBorder(BorderFactory.createTitledBorder("Floor " + floor));
            for (int i = 0; i < BINS_PER_FLOOR; i++) {
                Bin bin = bins.get((floor - 1) * BINS_PER_FLOOR + i);
                JLabel label = new JLabel("Bin " + bin.id);
                label.setToolTipText("Set fill level for Bin " + bin.id);
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));
                spinner.setPreferredSize(new Dimension(50, 24));
                bin.spinner = spinner;
                floorPanel.add(label);
                floorPanel.add(spinner);
            }
            inputPanel.add(floorPanel);
        }

        JButton adviseBtn = new JButton("Optimize Collection Path");
        adviseBtn.setFont(new Font("Arial", Font.BOLD, 14));
        adviseBtn.setBackground(new Color(0, 120, 215));
        adviseBtn.setForeground(Color.WHITE);
        adviseBtn.setFocusPainted(false);
        adviseBtn.setToolTipText("Click to optimize the collection path using Knapsack DP");
        adviseBtn.addActionListener(e -> {
            optimizePath();
            networkPanel.repaint();
        });

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(245, 245, 245));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Optimization Result"));
        rightPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        rightPanel.add(adviseBtn, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(networkPanel, BorderLayout.EAST);
    }

    private void optimizePath() {
        updateBinFillLevels();

        // Clear and update selectedBins
        selectedBins.clear();

        StringBuilder sb = new StringBuilder("Optimized Bin Collection Order (0/1 Knapsack DP):\n");
        sb.append("Threshold: ").append(THRESHOLD).append("%\n");
        sb.append("Max bins per floor: ").append(CAPACITY_PER_FLOOR).append("\n\n");

        // Sort floors by priority (most critical first)
        List<Integer> sortedFloors = new ArrayList<>();
        for (int i = 1; i <= FLOORS; i++) {
            sortedFloors.add(i);
        }
        sortedFloors.sort((f1, f2) -> Integer.compare(getFloorPriority(f2), getFloorPriority(f1)));

        // Process each floor in priority order
        for (int floor : sortedFloors) {
            List<Bin> floorBins = getBinsForFloor(floor);
            List<Bin> binsToCollect = solve01KnapsackForFloor(floorBins);

            // Add to selectedBins for coloring
            selectedBins.addAll(binsToCollect);

            sb.append("Floor ").append(floor).append(" Collection:\n");
            if (binsToCollect.isEmpty()) {
                sb.append("  No bins above threshold (").append(THRESHOLD).append("%)\n");
            } else {
                for (Bin bin : binsToCollect) {
                    sb.append(String.format("  Collect Bin %d (Fill: %d%%)%n", bin.id, bin.fillLevel));
                }
            }
            sb.append("\n");
        }

        resultArea.setText(sb.toString());
        networkPanel.repaint();
    }

private List<Bin> solve01KnapsackForFloor(List<Bin> floorBins) {
    // Filter bins that need collection (above threshold)
    List<Bin> binsNeedingCollection = new ArrayList<>();
    for (Bin bin : floorBins) {
        if (bin.fillLevel >= THRESHOLD) {
            binsNeedingCollection.add(bin);
        }
    }
    
    // If no bins need collection, return empty list
    if (binsNeedingCollection.isEmpty()) {
        return Collections.emptyList();
    }
    
    int n = binsNeedingCollection.size();
    int capacity = CAPACITY_PER_FLOOR;
    
    // DP table where dp[i][w] represents the maximum value that can be attained
    // with the first i items and weight limit w
    int[][] dp = new int[n + 1][capacity + 1];
    
    // Build DP table
    for (int i = 1; i <= n; i++) {
        Bin currentBin = binsNeedingCollection.get(i - 1);
        for (int w = 1; w <= capacity; w++) {
            // Each bin has weight = 1 (takes one collection slot)
            int binWeight = 1;
            int binValue = currentBin.fillLevel;
            
            if (binWeight <= w) {
                dp[i][w] = Math.max(
                    dp[i - 1][w],  // Don't take this bin
                    dp[i - 1][w - binWeight] + binValue  // Take this bin
                );
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }
    
    // Traceback to find which bins to collect
    List<Bin> result = new ArrayList<>();
    int remainingCapacity = capacity;
    
    for (int i = n; i > 0 && remainingCapacity > 0; i--) {
        if (dp[i][remainingCapacity] != dp[i - 1][remainingCapacity]) {
            // This bin was included
            result.add(binsNeedingCollection.get(i - 1));
            remainingCapacity -= 1; // Each bin has weight 1
        }
    }
    
    // Shuffle the result for random order
    Collections.shuffle(result);
    return result;
}

    private void updateBinFillLevels() {
        for (Bin bin : bins) {
            bin.fillLevel = (int) bin.spinner.getValue();
        }
    }

    private List<Bin> getBinsForFloor(int floor) {
        List<Bin> floorBins = new ArrayList<>();
        for (Bin bin : bins) {
            if (bin.floor == floor) {
                floorBins.add(bin);
            }
        }
        return floorBins;
    }

    private int getFloorPriority(int floor) {
        int count = countBinsAboveThreshold(floor);
        int totalFill = getTotalFillPercentage(floor);
        
        // Prioritize floors with most critical bins first
        // Weight: (number of critical bins * 10000) + total fill percentage
        return (count * 10000) + totalFill;
    }

    private int countBinsAboveThreshold(int floor) {
        int count = 0;
        for (Bin bin : bins) {
            if (bin.floor == floor && bin.fillLevel >= THRESHOLD) {
                count++;
            }
        }
        return count;
    }

    private int getTotalFillPercentage(int floor) {
        int sum = 0;
        for (Bin bin : bins) {
            if (bin.floor == floor) {
                sum += bin.fillLevel;
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CleanBUPWithTrees().setVisible(true));
    }

    class BinNetworkPanel extends JPanel {
        public BinNetworkPanel() {
            setPreferredSize(new Dimension(400, 600));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("Bin Visualization"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Draw legend
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(Color.RED);
            g2.fillRect(20, 20, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString("Above Threshold", 40, 32);
            g2.setColor(Color.GREEN);
            g2.fillRect(20, 40, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString("Below Threshold", 40, 52);

            // Draw bins
            for (Bin bin : bins) {
                Color fillColor;
                if (selectedBins.contains(bin)) {
                    fillColor = new Color(255, 165, 0); // Orange for selected
                } else if (bin.fillLevel >= THRESHOLD) {
                    fillColor = Color.RED;
                } else {
                    fillColor = Color.GREEN;
                }
                g2.setColor(fillColor);
                g2.fillOval(bin.x, bin.y, 20, 20);
                g2.setColor(Color.BLACK);
                g2.drawOval(bin.x, bin.y, 20, 20);
                g2.drawString("" + bin.id, bin.x, bin.y - 5);
            }
            // Draw legend for selected bins
            g2.setColor(new Color(255, 165, 0));
            g2.fillRect(20, 60, 15, 15);
            g2.setColor(Color.BLACK);
            g2.drawString("Selected for Collection", 40, 72);
        }
    }
}

class Bin {
    int id, floor, fillLevel, x, y;
    JSpinner spinner;

    public Bin(int id, int floor, int x, int y) {
        this.id = id;
        this.floor = floor;
        this.x = x;
        this.y = y;
        this.fillLevel = 0;
    }
}
