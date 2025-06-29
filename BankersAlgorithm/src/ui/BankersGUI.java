package ui;

import model.BankersModel;
import algorithm.BankersAlgorithm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 银行家算法的图形用户界面
 */
public class BankersGUI extends JFrame {
    private BankersModel model;
    private BankersAlgorithm algorithm;
    
    // 界面组件
    private JTable resourceTable;
    private JTable processTable;
    private JTextField requestField;
    private JTextArea resultArea;
    
    private int[] initialSafeSequence;  // 初始安全序列

    public BankersGUI(BankersModel model, int[] initialSafeSequence) {
        this.model = model;
        this.initialSafeSequence = initialSafeSequence;
        this.algorithm = new BankersAlgorithm(model);
        
        initUI();
        // 显示初始安全序列
        if (initialSafeSequence != null) {
            StringBuilder sb = new StringBuilder("初始安全序列: ");
            for (int p : initialSafeSequence) {
                sb.append("P").append(p).append(" ");
            }
            resultArea.append(sb.toString());
            resultArea.append("\n");
        } else {
            resultArea.append("初始状态不安全，无安全序列\n");
        }
    }
    
    private void initUI() {
        setTitle("银行家算法模拟器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建资源表格
        String[] resourceColumns = {"资源", "总量", "可用"};
        Object[][] resourceData = new Object[model.getResourceTypes()][3];
        for (int i = 0; i < model.getResourceTypes(); i++) {
            resourceData[i][0] = "资源" + i;
            resourceData[i][1] = model.getTotalResources()[i];
            resourceData[i][2] = model.getAvailableResources()[i];
        }
        resourceTable = new JTable(resourceData, resourceColumns);
        
        // 创建进程表格
        String[] processColumns = {"进程", "已分配", "最大需求", "需求", "状态"};
        Object[][] processData = new Object[model.getProcessCount()][5];
        for (int i = 0; i < model.getProcessCount(); i++) {
            processData[i][0] = "进程" + i;
            
            // 已分配资源
            StringBuilder allocated = new StringBuilder();
            for (int j = 0; j < model.getResourceTypes(); j++) {
                allocated.append(model.getAllocatedResources()[i][j]);
                if (j < model.getResourceTypes() - 1) {
                    allocated.append(",");
                }
            }
            processData[i][1] = allocated.toString();
            
            // 最大需求
            StringBuilder max = new StringBuilder();
            for (int j = 0; j < model.getResourceTypes(); j++) {
                max.append(model.getMaxResources()[i][j]);
                if (j < model.getResourceTypes() - 1) {
                    max.append(",");
                }
            }
            processData[i][2] = max.toString();
            
            // 需求
            StringBuilder need = new StringBuilder();
            int[] needArray = model.getNeedResources(i);
            for (int j = 0; j < model.getResourceTypes(); j++) {
                need.append(needArray[j]);
                if (j < model.getResourceTypes() - 1) {
                    need.append(",");
                }
            }
            processData[i][3] = need.toString();
            processData[i][4] = "运行中"; // 初始状态设为运行中
        }
        processTable = new JTable(processData, processColumns);
        
        // 创建请求面板
        JPanel requestPanel = new JPanel();
        requestPanel.add(new JLabel("请求 (格式: 进程ID 资源0 资源1...):"));
        requestField = new JTextField(20);
        requestPanel.add(requestField);
        
        JButton submitButton = new JButton("提交请求");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processRequest();
            }
        });
        requestPanel.add(submitButton);
        // 为请求输入框添加回车事件
        requestField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processRequest();
            }
        });

        // 添加进程完成按钮
        requestPanel.add(new JLabel("完成进程ID:"));
        JTextField completeField = new JTextField(5);
        requestPanel.add(completeField);
        requestPanel.add(Box.createHorizontalStrut(15)); // 添加间距
        JButton completeButton = new JButton("完成进程");
        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int processId = Integer.parseInt(completeField.getText().trim());
                    if (processId >= 0 && processId < model.getProcessCount()) {
                        algorithm.completeProcess(processId);
                        resultArea.append("进程" + processId + "已完成，资源已释放\n");
                        updateTables();
                    } else {
                        resultArea.append("错误：无效的进程ID\n");
                    }
                } catch (NumberFormatException ex) {
                    resultArea.append("错误：请输入有效的进程ID\n");
                }
                completeField.setText("");
            }
        });
        requestPanel.add(completeButton);


        // 为完成进程输入框添加回车事件
        completeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completeButton.doClick();
            }
        });
        
        // 创建结果区域
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        
        // 将组件添加到主窗口
        // 使用水平分割面板合并显示资源和进程状态
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(resourceTable), new JScrollPane(processTable));
        splitPane.setResizeWeight(0.5); // 左右各占50%
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
        add(requestPanel, BorderLayout.NORTH);
        // 创建示例按钮面板
        JPanel examplePanel = new JPanel();
        examplePanel.setLayout(new FlowLayout());
        examplePanel.setBorder(BorderFactory.createTitledBorder("示例场景"));

        // 添加安全队列示例按钮
        JButton safeExampleButton = new JButton("有安全队列例子");
        safeExampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetModel();
                int[][] safeAllocated = algorithm.generateSafeAllocatedResources();
                model.setAllocatedResources(safeAllocated);
                updateUIAfterExample();
            }
        });
        examplePanel.add(safeExampleButton);

        // 添加无安全队列示例按钮
        JButton unsafeExampleButton = new JButton("无安全队列例子");
        unsafeExampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetModel();
                int[][] unsafeAllocated = algorithm.generateUnsafeAllocatedResources();
                model.setAllocatedResources(unsafeAllocated);
                updateUIAfterExample();
            }
        });
        examplePanel.add(unsafeExampleButton);

        // 组合结果区域和示例按钮面板
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(resultScrollPane, BorderLayout.CENTER);
        southPanel.add(examplePanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void processRequest() {
        String input = requestField.getText().trim();
        if (input.isEmpty()) {
            resultArea.setText("请输入请求信息");
            return;
        }
        
        try {
            String[] parts = input.trim().split("\\s+"); // 按空格分割，支持多个空格
            int processId = Integer.parseInt(parts[0]);
            
            if (processId < 0 || processId >= model.getProcessCount()) {
                resultArea.setText("错误：无效的进程ID");
                return;
            }
            
            int[] request = new int[model.getResourceTypes()];
            if (parts.length != model.getResourceTypes() + 1) {
                resultArea.setText("错误：请求格式不正确");
                return;
            }
            
            for (int i = 0; i < model.getResourceTypes(); i++) {
                request[i] = Integer.parseInt(parts[i + 1]);
                if (request[i] < 0) {
                    resultArea.setText("错误：资源请求不能为负数");
                    return;
                }
            }
            
            // 处理请求
            resultArea.setText("处理请求: 进程" + processId + "请求 ");
            for (int i = 0; i < model.getResourceTypes(); i++) {
                resultArea.append("资源" + i + "=" + request[i] + " ");
            }
            resultArea.append("\n");
            
            boolean granted = algorithm.processRequest(processId, request);
            if (granted) {
                resultArea.append("请求被批准，系统处于安全状态\n");
                // 显示安全序列
                int[] safeSequence = algorithm.getLastSafeSequence();
                if (safeSequence != null) {
                    StringBuilder sb = new StringBuilder("安全序列: ");
                    for (int p : safeSequence) {
                        sb.append("P").append(p).append(" ");
                    }
                    resultArea.append(sb.toString());
                    resultArea.append("\n");
                }
            } else {
                resultArea.append("请求被拒绝，系统将处于不安全状态\n");
            }
            
            // 更新表格
            updateTables();
            
        } catch (NumberFormatException ex) {
                resultArea.setText("错误：输入格式不正确，必须是数字");
            }
            // 重置输入框内容
            requestField.setText("");
    }
    
    // 重置模型状态
    private void resetModel() {
        // 重置进程完成状态
        for (int i = 0; i < model.getProcessCount(); i++) {
            if (model.isProcessCompleted(i)) {
                // 恢复最大需求（使用初始值）
                int[][] originalMax = {
                    {7, 5, 3},
                    {3, 2, 2},
                    {9, 0, 2},
                    {2, 2, 2},
                    {4, 3, 3}
                };
                model.getMaxResources()[i] = originalMax[i].clone();
            }
        }
        // 重置完成状态数组
        for (int i = 0; i < model.getProcessCount(); i++) {
            try {
                // 通过反射重置私有字段isCompleted
                java.lang.reflect.Field field = model.getClass().getDeclaredField("isCompleted");
                field.setAccessible(true);
                boolean[] isCompleted = (boolean[]) field.get(model);
                isCompleted[i] = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 示例生成后更新UI
    private void updateUIAfterExample() {
        resultArea.setText("");
        int[] newSafeSequence = algorithm.isSafeState(model);
        if (newSafeSequence != null) {
            resultArea.append("生成安全状态示例成功！\n");
            StringBuilder sb = new StringBuilder("安全序列: ");
            for (int p : newSafeSequence) {
                sb.append("P").append(p).append(" ");
            }
            resultArea.append(sb.toString());
            resultArea.append("\n");
        } else {
            resultArea.append("生成不安全状态示例成功！\n");
            resultArea.append("当前系统处于不安全状态，无安全序列\n");
        }
        updateTables();
    }

    private void updateTables() {
        // 更新资源表格
        for (int i = 0; i < model.getResourceTypes(); i++) {
            resourceTable.setValueAt(model.getTotalResources()[i], i, 1);
            resourceTable.setValueAt(model.getAvailableResources()[i], i, 2);
        }
        
        // 更新进程表格
        for (int i = 0; i < model.getProcessCount(); i++) {
            // 已分配资源
            StringBuilder allocated = new StringBuilder();
            for (int j = 0; j < model.getResourceTypes(); j++) {
                allocated.append(model.getAllocatedResources()[i][j]);
                if (j < model.getResourceTypes() - 1) {
                    allocated.append(",");
                }
            }
            processTable.setValueAt(allocated.toString(), i, 1);
            
            // 需求
            StringBuilder need = new StringBuilder();
            int[] needArray = model.getNeedResources(i);
            for (int j = 0; j < model.getResourceTypes(); j++) {
                need.append(needArray[j]);
                if (j < model.getResourceTypes() - 1) {
                    need.append(",");
                }
            }
            processTable.setValueAt(need.toString(), i, 3);
            // 更新进程状态
            String status = model.isProcessCompleted(i) ? "已完成" : "运行中";
            processTable.setValueAt(status, i, 4);
        }
    }
}