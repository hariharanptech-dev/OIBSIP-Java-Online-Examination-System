package com.exampro.ui;

import com.exampro.ui.components.*;
import com.exampro.ui.theme.Theme;
import com.exampro.model.*;
import com.exampro.service.*;
import com.exampro.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/** Main application frame for ExamPro. */
public final class AppFrame extends JFrame {
    private final Clock clock = new Clock() {
        public long nanoTime() { return System.nanoTime(); }
    };
    private final TimerService timer = new TimerService(clock, 30 * 60);
    private final List<ShuffledQuestion> questions = new ArrayList<>();
    private final Integer[] answers;
    private int current = 0;
    private final Overlay overlay = new Overlay();
    private final DonutChart donut = new DonutChart();
    private final JLabel timerLabel = new JLabel();
    private final JLabel questionLabel = new JLabel();
    private final JPanel optionsPanel = new JPanel();
    private final RoundedButton prevBtn = new RoundedButton("Previous", true);
    private final RoundedButton nextBtn = new RoundedButton("Next", false);
    private final RoundedButton submitBtn = new RoundedButton("Submit Exam", false);

    public AppFrame() {
        super("ExamPro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

            // Load real exam questions
            questions.addAll(loadExamQuestions());
            answers = new Integer[questions.size()];

            buildUI();
            timer.start();
            new javax.swing.Timer(1000, e -> tick()).start();
        }

        private List<ShuffledQuestion> loadExamQuestions() {
            List<Question> pool = new ArrayList<>();

            // Java Questions
            pool.add(new Question(1, "Java", "Which of the following is NOT a Java primitive type?",
                List.of("int", "boolean", "String", "double"), 2, "String is a reference type (class), not a primitive."));
            pool.add(new Question(2, "Java", "What is the default value of an uninitialized instance variable of type int?",
                List.of("0", "null", "undefined", "1"), 0, "Primitive int fields default to 0."));
            pool.add(new Question(3, "Java", "Which keyword is used to define a constant in Java?",
                List.of("const", "final", "static", "immutable"), 1, "final makes a variable constant after initialization."));
            pool.add(new Question(4, "Java", "What does the 'static' keyword mean when applied to a method?",
                List.of("Method belongs to the class, not instances", "Method cannot be overridden", "Method is thread-safe", "Method returns void"), 0, "Static methods belong to the class, not to any instance."));
            pool.add(new Question(5, "Java", "Which collection class allows duplicate elements and maintains insertion order?",
                List.of("HashSet", "ArrayList", "TreeSet", "HashMap"), 1, "ArrayList allows duplicates and maintains insertion order."));

            // Database Questions
            pool.add(new Question(6, "Database", "What does ACID stand for in database transactions?",
                List.of("Atomicity, Consistency, Isolation, Durability", "Availability, Consistency, Isolation, Durability", "Atomicity, Concurrency, Isolation, Durability", "Atomicity, Consistency, Integrity, Durability"), 0, "ACID: Atomicity, Consistency, Isolation, Durability."));
            pool.add(new Question(7, "Database", "Which SQL clause is used to filter rows after grouping?",
                List.of("WHERE", "HAVING", "GROUP BY", "ORDER BY"), 1, "HAVING filters groups; WHERE filters rows before grouping."));
            pool.add(new Question(8, "Database", "What is a foreign key?",
                List.of("A unique identifier for a table", "A column that references a primary key in another table", "An index for faster queries", "A key used for encryption"), 1, "Foreign key creates a referential link to another table's primary key."));
            pool.add(new Question(9, "Database", "Which normal form eliminates transitive dependencies?",
                List.of("1NF", "2NF", "3NF", "BCNF"), 2, "3NF eliminates transitive dependencies (non-key depends on non-key)."));
            pool.add(new Question(10, "Database", "What is the purpose of an index in a database?",
                List.of("To enforce uniqueness", "To speed up data retrieval", "To store backup data", "To encrypt sensitive columns"), 1, "Indexes improve query performance by avoiding full table scans."));

            // Operating Systems Questions
            pool.add(new Question(11, "OS", "What is a context switch?",
                List.of("Changing the CPU clock speed", "Saving and restoring process state to switch execution", "Moving data between RAM and disk", "Switching between user and kernel mode"), 1, "Context switch saves current process state and loads another's."));
            pool.add(new Question(12, "OS", "Which scheduling algorithm can cause starvation?",
                List.of("Round Robin", "First-Come-First-Served", "Shortest Job First", "Priority Scheduling"), 3, "Priority scheduling can starve low-priority processes."));
            pool.add(new Question(13, "OS", "What is a deadlock?",
                List.of("A process waiting for I/O", "Two or more processes waiting for each other indefinitely", "A process using 100% CPU", "A terminated process"), 1, "Deadlock: circular wait where each process holds a resource needed by another."));
            pool.add(new Question(14, "OS", "What is virtual memory?",
                List.of("Memory stored on CPU cache", "An abstraction giving each process its own address space", "Physical RAM only", "Memory shared between all processes"), 1, "Virtual memory provides each process a private address space, backed by RAM/disk."));
            pool.add(new Question(15, "OS", "Which of the following is a synchronization primitive?",
                List.of("Semaphore", "Thread", "Process", "Scheduler"), 0, "Semaphores are used for process/thread synchronization."));

            // Networking Questions
            pool.add(new Question(16, "Networking", "Which protocol operates at the Transport layer (Layer 4)?",
                List.of("IP", "TCP", "HTTP", "Ethernet"), 1, "TCP is a Layer 4 transport protocol."));
            pool.add(new Question(17, "Networking", "What is the default port for HTTPS?",
                List.of("80", "443", "22", "53"), 1, "HTTPS uses port 443 by default."));
            pool.add(new Question(18, "Networking", "What does DNS resolve?",
                List.of("MAC addresses to IP addresses", "Domain names to IP addresses", "IP addresses to MAC addresses", "Port numbers to service names"), 1, "DNS resolves human-readable domain names to IP addresses."));
            pool.add(new Question(19, "Networking", "Which of the following is a private IP address range?",
                List.of("8.8.8.0/24", "192.168.0.0/16", "1.1.1.0/24", "203.0.113.0/24"), 1, "192.168.0.0/16 is reserved for private networks (RFC 1918)."));
            pool.add(new Question(20, "Networking", "What is the purpose of a subnet mask?",
                List.of("To encrypt network traffic", "To divide an IP address into network and host portions", "To assign IP addresses automatically", "To route packets between networks"), 1, "Subnet mask determines which bits represent network vs host."));

            // Security Questions
            pool.add(new Question(21, "Security", "What is a man-in-the-middle attack?",
                List.of("Brute-forcing a password", "Intercepting and possibly altering communication between two parties", "Injecting SQL into a database query", "Flooding a server with requests"), 1, "MITM: attacker secretly relays and possibly alters communication."));
            pool.add(new Question(22, "Security", "Which hashing algorithm is considered cryptographically broken?",
                List.of("SHA-256", "MD5", "SHA-3", "bcrypt"), 1, "MD5 has known collision vulnerabilities and is not cryptographically secure."));
            pool.add(new Question(23, "Security", "What is the principle of least privilege?",
                List.of("Users should have admin rights", "Users should have only the permissions they need", "All users should have equal access", "Permissions should be granted temporarily"), 1, "Least privilege: minimum access necessary to perform a task."));
            pool.add(new Question(24, "Security", "What does a digital signature provide?",
                List.of("Confidentiality only", "Authentication, integrity, and non-repudiation", "Encryption only", "Availability only"), 1, "Digital signatures verify sender identity, message integrity, and prevent denial."));
            pool.add(new Question(25, "Security", "What is SQL injection?",
                List.of("Injecting malware into a database", "Inserting malicious SQL code via user input to manipulate queries", "Backing up a database", "Optimizing SQL queries"), 1, "SQL injection exploits unsanitized user input in database queries."));

            // Shuffle and pick 15 questions for the exam
            Collections.shuffle(pool, new Random());
            return ExamShuffler.build(pool, 15, new Random());
        }

    private void buildUI() {
        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        top.setBackground(Theme.DARK.surface);
        timerLabel.setFont(Ui.font(Font.BOLD, 28));
        timerLabel.setForeground(Theme.DARK.text);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        top.add(Ui.label("ExamPro", 22, true, Theme.DARK.text), BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center - question area
        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        center.setBackground(Theme.DARK.bg);
        center.setOpaque(true);

        questionLabel.setFont(Ui.font(Font.PLAIN, 20));
        questionLabel.setForeground(Theme.DARK.text);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        center.add(questionLabel, BorderLayout.NORTH);

        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(Theme.DARK.bg);
        optionsPanel.setOpaque(true);
        JScrollPane scroll = new JScrollPane(optionsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.DARK.bg);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        center.add(scroll, BorderLayout.CENTER);

        // Bottom - navigation
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.setBackground(Theme.DARK.surface);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn.addActionListener(e -> navigate(1));
        submitBtn.addActionListener(e -> confirmSubmit());
        bottom.add(prevBtn);
        bottom.add(nextBtn);
        bottom.add(submitBtn);
        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // Overlay glass pane
        setGlassPane(overlay);
        overlay.setVisible(true);

        renderQuestion();
    }

    private void renderQuestion() {
        ShuffledQuestion sq = questions.get(current);
        questionLabel.setText("<html><b>Q" + (current + 1) + "/" + questions.size() + " [" + sq.question().topic() + "]</b> " + sq.question().prompt() + "</html>");

        optionsPanel.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
                final int idx = i;
                OptionCard card = new OptionCard(i);
                card.setOptionText(sq.optionText(idx));
                if (answers[current] != null && answers[current] == idx) card.setSelected(true);
                card.addActionListener(e -> { answers[current] = idx; group.clearSelection(); card.setSelected(true); });
                group.add(card);
                optionsPanel.add(card);
                optionsPanel.add(Box.createVerticalStrut(10));
            }
            optionsPanel.revalidate();
            optionsPanel.repaint();

            boolean isLast = current == questions.size() - 1;
            prevBtn.setEnabled(current > 0);
            nextBtn.setVisible(!isLast);
            nextBtn.setEnabled(!isLast);
            submitBtn.setVisible(isLast);
            submitBtn.setText("Submit Exam");
        }

    private void navigate(int delta) {
        int next = current + delta;
        if (next >= 0 && next < questions.size()) {
            current = next;
            renderQuestion();
        }
    }

    private void confirmSubmit() {
        overlay.confirm("Submit Exam", "Are you sure you want to submit?\nUnanswered questions will be marked incorrect.", "Submit", "Cancel", this::submitExam);
    }

    private void submitExam() {
        ResultService.Result result = ResultService.score(questions, answers, 50);
        donut.setScore(result.correct(), result.total());
        overlay.confirm("Exam Complete", "Score: " + result.scoreText() + " (" + String.format("%.1f", result.percent()) + "%)\n" + (result.passed() ? "PASSED" : "FAILED"), "OK", null, () -> System.exit(0));
    }

    private void tick() {
        if (timer.isExpired()) {
            submitExam();
            return;
        }
        TimerService.Phase phase = timer.phase();
        String text = TimerService.format(timer.remainingSeconds());
        timerLabel.setText(text);
        switch (phase) {
            case NORMAL -> timerLabel.setForeground(Theme.DARK.text);
            case AMBER -> timerLabel.setForeground(Theme.DARK.warning);
            case RED, FINAL_MINUTE -> timerLabel.setForeground(Theme.DARK.danger);
            case EXPIRED -> timerLabel.setForeground(Theme.DARK.danger);
        }
        int warn = timer.pollWarning();
        if (warn > 0) overlay.toast(warn + " minute" + (warn > 1 ? "s" : "") + " remaining!");
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}