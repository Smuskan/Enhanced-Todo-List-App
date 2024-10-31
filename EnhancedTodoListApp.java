import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class EnhancedTodoListApp extends JFrame {
   private DefaultListModel<Task> taskListModel;
   private JList<Task> taskList;
   private JTextField taskInput;
   private JTextField dueDateInput;
   private JComboBox<String> priorityInput;
   private JButton addButton, removeButton, editButton, saveButton, loadButton;
   private JPanel inputPanel, buttonPanel;

   public EnhancedTodoListApp() {
      // Set up the frame
      setTitle("Enhanced To-Do List App");
      setSize(700, 600);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLayout(new GridBagLayout());
      getContentPane().setBackground(new Color(240, 248, 255)); // Alice Blue

      // Create components
      taskListModel = new DefaultListModel<>();
      taskList = new JList<>(taskListModel);
      taskList.setCellRenderer(new TaskListCellRenderer());

      taskInput = new JTextField(15);
      dueDateInput = new JTextField(10);
      priorityInput = new JComboBox<>(new String[] { "Low", "Medium", "High" });

      addButton = new JButton("Add Task");
      removeButton = new JButton("Remove Task");
      editButton = new JButton("Edit Task");
      saveButton = new JButton("Save Tasks");
      loadButton = new JButton("Load Tasks");

      inputPanel = new JPanel();
      inputPanel.setLayout(new FlowLayout());
      inputPanel.add(new JLabel("Task:"));
      inputPanel.add(taskInput);
      inputPanel.add(new JLabel("Due Date (dd/MM/yyyy):"));
      inputPanel.add(dueDateInput);
      inputPanel.add(new JLabel("Priority:"));
      inputPanel.add(priorityInput);

      buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(1, 5));
      buttonPanel.add(addButton);
      buttonPanel.add(removeButton);
      buttonPanel.add(editButton);
      buttonPanel.add(saveButton);
      buttonPanel.add(loadButton);

      // Add components to the frame using GridBagConstraints
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(10, 10, 10, 10);
      add(inputPanel, gbc);

      gbc.gridy = 1;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;
      add(new JScrollPane(taskList), gbc);

      gbc.weighty = 0.0;
      gbc.gridy = 2;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      add(buttonPanel, gbc);

      // Add action listeners
      addButton.addActionListener(e -> addTask());
      removeButton.addActionListener(e -> removeTask());
      editButton.addActionListener(e -> editTask());
      saveButton.addActionListener(e -> saveTasks());
      loadButton.addActionListener(e -> loadTasks());

      taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      // Show the frame
      setVisible(true);
   }

   private void addTask() {
      String taskDescription = taskInput.getText();
      String dueDate = dueDateInput.getText();
      String priority = (String) priorityInput.getSelectedItem();

      if (!taskDescription.trim().isEmpty() && isValidDate(dueDate)) {
         Task newTask = new Task(taskDescription, dueDate, priority);
         taskListModel.addElement(newTask);
         taskInput.setText("");
         dueDateInput.setText("");
         taskInput.requestFocus();
      } else {
         JOptionPane.showMessageDialog(this, "Please enter a valid task and due date!", "Error",
               JOptionPane.ERROR_MESSAGE);
      }
   }

   private void removeTask() {
      int selectedIndex = taskList.getSelectedIndex();
      if (selectedIndex != -1) {
         taskListModel.remove(selectedIndex);
      } else {
         JOptionPane.showMessageDialog(this, "Please select a task to remove!", "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private void editTask() {
      int selectedIndex = taskList.getSelectedIndex();
      if (selectedIndex != -1) {
         Task selectedTask = taskListModel.get(selectedIndex);
         String updatedTask = JOptionPane.showInputDialog(this, "Edit Task:", selectedTask.getDescription());
         String updatedDate = JOptionPane.showInputDialog(this, "Edit Due Date (dd/MM/yyyy):",
               selectedTask.getDueDate());
         String updatedPriority = (String) JOptionPane.showInputDialog(this, "Select Priority:", "Edit Priority",
               JOptionPane.QUESTION_MESSAGE, null, new String[] { "Low", "Medium", "High" },
               selectedTask.getPriority());
         if (updatedTask != null && !updatedTask.trim().isEmpty() && isValidDate(updatedDate)) {
            selectedTask.setDescription(updatedTask);
            selectedTask.setDueDate(updatedDate);
            selectedTask.setPriority(updatedPriority);
            taskList.repaint();
         }
      } else {
         JOptionPane.showMessageDialog(this, "Please select a task to edit!", "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private void saveTasks() {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
         for (int i = 0; i < taskListModel.size(); i++) {
            writer.write(taskListModel.get(i).toString());
            writer.newLine();
         }
         JOptionPane.showMessageDialog(this, "Tasks saved successfully!");
      } catch (IOException e) {
         JOptionPane.showMessageDialog(this, "Error saving tasks!", "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private void loadTasks() {
      try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
         String line;
         taskListModel.clear();
         while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 4) {
               Task task = new Task(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]));
               taskListModel.addElement(task);
            }
         }
         JOptionPane.showMessageDialog(this, "Tasks loaded successfully!");
      } catch (IOException e) {
         JOptionPane.showMessageDialog(this, "Error loading tasks!", "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private boolean isValidDate(String dateString) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      dateFormat.setLenient(false);
      try {
         dateFormat.parse(dateString);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   private class Task {
      private String description;
      private String dueDate;
      private String priority;
      private boolean completed;

      public Task(String description, String dueDate, String priority) {
         this.description = description;
         this.dueDate = dueDate;
         this.priority = priority;
         this.completed = false; // Default to not completed
      }

      public Task(String description, String dueDate, String priority, boolean completed) {
         this.description = description;
         this.dueDate = dueDate;
         this.priority = priority;
         this.completed = completed;
      }

      public String getDescription() {
         return description;
      }

      public void setDescription(String description) {
         this.description = description;
      }

      public String getDueDate() {
         return dueDate;
      }

      public void setDueDate(String dueDate) {
         this.dueDate = dueDate;
      }

      public String getPriority() {
         return priority;
      }

      public void setPriority(String priority) {
         this.priority = priority;
      }

      public boolean isCompleted() {
         return completed;
      }

      public void setCompleted(boolean completed) {
         this.completed = completed;
      }

      @Override
      public String toString() {
         return description + "," + dueDate + "," + priority + "," + completed;
      }
   }

   private class TaskListCellRenderer extends DefaultListCellRenderer {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            int cellHeight) {
         super.getListCellRendererComponent(list, value, index, isSelected, cellHeight);
         Task task = (Task) value;

         if (task.isCompleted()) {
            setForeground(Color.GRAY);
            setText("<html><strike>" + task.getDescription() + "</strike> (Due: " + task.getDueDate() + ", Priority: "
                  + task.getPriority() + ")</html>");
         } else {
            setForeground(Color.BLACK);
            setText(task.getDescription() + " (Due: " + task.getDueDate() + ", Priority: " + task.getPriority() + ")");
         }

         // Set background color based on priority
         if (task.getPriority().equals("High")) {
            setBackground(Color.RED);
         } else if (task.getPriority().equals("Medium")) {
            setBackground(Color.YELLOW);
         } else {
            setBackground(Color.GREEN);
         }

         if (isSelected) {
            setBackground(Color.BLUE);
            setForeground(Color.WHITE);
         }

         return this;
      }
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(EnhancedTodoListApp::new);
   }
}
