import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;


 // Enhanced AVL Tree Visualizer with detailed rotation logging and visualization.
 
public class EnhancedAVLTreeVisualizer extends JFrame {
    static class Node {
        int value;
        Node left, right;
        int height;
        int x, y;

        // Flags for visualization
        boolean isUnbalanced = false;
        boolean isRotated = false;

        public Node(int value) {
            this.value = value;
            this.height = 1;
            this.left = this.right = null;
        }
    }

    static class AVLTree {
        Node root;
        List<String> rotationLogs = new ArrayList<>(); // Logs of rotations
        List<Node> rotatedNodes = new ArrayList<>();   // Nodes involved in the latest rotation

        public AVLTree() {
            this.root = null;
        }

        public int height(Node N) {
            return (N == null) ? 0 : N.height;
        }

        public int getBalance(Node N) {
            return (N == null) ? 0 : height(N.left) - height(N.right);
        }

        public Node rightRotate(Node y) {
            Node x = y.left; // X banega next root node
            Node T2 = x.right;


            // Main rotation Yaha huaa hai
            x.right = y;
            y.left = T2;

            y.height = Math.max(height(y.left), height(y.right)) + 1;
            x.height = Math.max(height(x.left), height(x.right)) + 1;

            rotationLogs.add("Right Rotation on node " + y.value + " with left child " + x.value);
            rotatedNodes.clear();
            rotatedNodes.add(y);
            rotatedNodes.add(x);

            return x;
        }

        public Node leftRotate(Node x) {
            Node y = x.right;
            Node T2 = y.left;

            y.left = x;
            x.right = T2;

            x.height = Math.max(height(x.left), height(x.right)) + 1;
            y.height = Math.max(height(y.left), height(y.right)) + 1;

            rotationLogs.add("Left Rotation on node " + x.value + " with right child " + y.value);
            rotatedNodes.clear();
            rotatedNodes.add(x);
            rotatedNodes.add(y);

            return y;
        }

        public Node insert(Node node, int value) {
            if (node == null) return new Node(value);

            if (value < node.value)
                node.left = insert(node.left, value);
            else if (value > node.value)
                node.right = insert(node.right, value);
            else
                return node;

            node.height = 1 + Math.max(height(node.left), height(node.right));

            int balance = getBalance(node);

            if (balance > 1 && value < node.left.value)
                return rightRotate(node);

            if (balance < -1 && value > node.right.value)
                return leftRotate(node);

            if (balance > 1 && value > node.left.value) {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }

            if (balance < -1 && value < node.right.value) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }

            node.isUnbalanced = (balance > 1 || balance < -1);
            return node;
        }

        public Node delete(Node root, int value) {
            if (root == null) return root;

            if (value < root.value)
                root.left = delete(root.left, value);
            else if (value > root.value)
                root.right = delete(root.right, value);
            else {
                if ((root.left == null) || (root.right == null)) {
                    Node temp = (root.left != null) ? root.left : root.right;
                    if (temp == null) {
                        temp = root;
                        root = null;
                    } else
                        root = temp;
                } else {
                    Node temp = minNode(root.right);
                    root.value = temp.value;
                    root.right = delete(root.right, temp.value);
                }
            }

            if (root == null) return root;

            root.height = Math.max(height(root.left), height(root.right)) + 1;

            int balance = getBalance(root);

            if (balance > 1 && getBalance(root.left) >= 0)
                return rightRotate(root);

            if (balance > 1 && getBalance(root.left) < 0) {
                root.left = leftRotate(root.left);
                return rightRotate(root);
            }

            if (balance < -1 && getBalance(root.right) <= 0)
                return leftRotate(root);

            if (balance < -1 && getBalance(root.right) > 0) {
                root.right = rightRotate(root.right);
                return leftRotate(root);
            }

            root.isUnbalanced = (balance > 1 || balance < -1);
            return root;
        }

        public Node minNode(Node node) {
            if (node.left == null) return node;
            return minNode(node.left);
        }

        public void inorderTraversal(Node node, List<Node> nodes) {
            if (node != null) {
                inorderTraversal(node.left, nodes);
                nodes.add(node);
                inorderTraversal(node.right, nodes);
            }
        }

        public void clearLogs() {
            rotationLogs.clear();
            rotatedNodes.clear();
        }
    }

    private AVLTree avlTree;
    private JTextField valueField;
    private JButton insertButton, deleteButton, traverseButton;
    private JTextArea operationLog;
    private DrawingPanel drawingPanel;
    private ConcurrentLinkedQueue<List<Node>> rotationHighlightQueue = new ConcurrentLinkedQueue<>();

    public EnhancedAVLTreeVisualizer() {
        avlTree = new AVLTree();
        initComponents();
    }

    private void initComponents() {
        setTitle("Enhanced AVL Tree Visualizer");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel enterLabel = new JLabel("Enter Value:");
        controlPanel.add(enterLabel);

        valueField = new JTextField(5);
        controlPanel.add(valueField);

        insertButton = new JButton("Insert");
        controlPanel.add(insertButton);

        deleteButton = new JButton("Delete");
        controlPanel.add(deleteButton);

        traverseButton = new JButton("Inorder Traversal");
        controlPanel.add(traverseButton);

        JButton resetButton = new JButton("Reset Tree");
        controlPanel.add(resetButton);

        add(controlPanel, BorderLayout.NORTH);

        operationLog = new JTextArea(10, 30);
        operationLog.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(operationLog);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Operation Log"));
        add(logScrollPane, BorderLayout.SOUTH);

        drawingPanel = new DrawingPanel(avlTree);
        add(drawingPanel, BorderLayout.CENTER);

        insertButton.addActionListener(e -> insertValue());
        deleteButton.addActionListener(e -> deleteValue());
        traverseButton.addActionListener(e -> traverseInorder());
        resetButton.addActionListener(e -> resetTree());

        Timer highlightTimer = new Timer(1500, e -> {
            if (!rotationHighlightQueue.isEmpty()) {
                List<Node> nodesToHighlight = rotationHighlightQueue.poll();
                if (nodesToHighlight != null) {
                    drawingPanel.setRotatedNodes(nodesToHighlight);
                }
            } else {
                drawingPanel.clearRotatedNodes();
            }
        });
        highlightTimer.start();
    }

    private void insertValue() {
        String input = valueField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int value = Integer.parseInt(input);
            operationLog.append("Inserting: " + value + "\n");
            new AnimationThread(value, true).start();
            valueField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteValue() {
        String input = valueField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int value = Integer.parseInt(input);
            operationLog.append("Deleting: " + value + "\n");
            new AnimationThread(value, false).start();
            valueField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void traverseInorder() {
        List<Node> nodes = new ArrayList<>();
        avlTree.inorderTraversal(avlTree.root, nodes);
        StringBuilder traversalResult = new StringBuilder("Inorder Traversal: ");
        for (Node node : nodes) {
            traversalResult.append(node.value).append(" ");
        }
        operationLog.append(traversalResult.toString() + "\n");
    }

    private void resetTree() {
        avlTree = new AVLTree();
        operationLog.append("Tree has been reset.\n");
        drawingPanel.setTree(avlTree);
        drawingPanel.repaint();
    }

    private class AnimationThread extends Thread {
        private final int value;
        private final boolean isInsert;

        public AnimationThread(int value, boolean isInsert) {
            this.value = value;
            this.isInsert = isInsert;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                if (isInsert) {
                    avlTree.root = avlTree.insert(avlTree.root, value);
                } else {
                    avlTree.root = avlTree.delete(avlTree.root, value);
                }

                rotationHighlightQueue.offer(new ArrayList<>(avlTree.rotatedNodes));
                operationLog.append("AVL Operation Complete. Logs: " + avlTree.rotationLogs + "\n");

                drawingPanel.repaint();
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnhancedAVLTreeVisualizer visualizer = new EnhancedAVLTreeVisualizer();
            visualizer.setVisible(true);
        });
    }

    class DrawingPanel extends JPanel {
        private final int NODE_RADIUS = 40;
        private AVLTree tree;
        private List<Node> rotatedNodes;

        public DrawingPanel(AVLTree tree) {
            this.tree = tree;
            this.rotatedNodes = new ArrayList<>();
            setBackground(Color.WHITE);
        }

        public void setTree(AVLTree tree) {
            this.tree = tree;
        }

        public void setRotatedNodes(List<Node> nodes) {
            this.rotatedNodes = nodes;
            repaint();
        }

        public void clearRotatedNodes() {
            this.rotatedNodes.clear();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (tree != null) {
                drawTree(g, tree.root, getWidth() / 2, 50, 60);
            }
        }

        private void drawTree(Graphics g, Node node, int x, int y, int offset) {
    if (node != null) {
        // Set node color based on rotation status
        g.setColor(rotatedNodes.contains(node) ? Color.RED : Color.gray);
        
        // Draw left child
        if (node.left != null) {
            g.drawLine(x, y, x - offset, y + 50);
            drawTree(g, node.left, x - offset, y + 50, offset / 2);
        }
        
        // Draw right child
        if (node.right != null) {
            g.drawLine(x, y, x + offset, y + 50);
            drawTree(g, node.right, x + offset, y + 50, offset / 2);
        }

        // Draw node
        g.fillOval(x - NODE_RADIUS / 2, y - NODE_RADIUS / 2, NODE_RADIUS, NODE_RADIUS);
        g.setColor(Color.WHITE); // Set text color to white
        g.drawString(String.valueOf(node.value), x - 5, y + 5);
        
        // Optionally draw a border around the node
        g.setColor(Color.BLACK); // Set border color
        g.drawOval(x - NODE_RADIUS / 2, y - NODE_RADIUS / 2, NODE_RADIUS, NODE_RADIUS);
    }
}

    }
}
