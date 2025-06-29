import model.BankersModel;
import ui.BankersGUI;
import algorithm.BankersAlgorithm; // 假设该类在 algorithm 包下，需根据实际情况修改

/**
 * 银行家算法模拟器的主类
 */
public class Main {
    public static void main(String[] args) {
        // 创建模型（3种资源，5个进程）
        BankersModel model = new BankersModel(3, 5);
        
        // 设置总资源
        int[] totalResources = {10, 5, 7};
        model.setTotalResources(totalResources);
        
        // 设置每个进程的最大需求和已分配资源
        int[][] maxResources = {
            {7, 5, 3},
            {3, 2, 2},
            {9, 0, 2},
            {2, 2, 2},
            {4, 3, 3}
        };
        
        int[][] allocatedResources = {
            {0, 1, 0},
            {2, 0, 0},
            {3, 0, 2},
            {2, 1, 1},
            {0, 0, 2}
        };
        
        model.setMaxResources(maxResources);
        model.setAllocatedResources(allocatedResources);
        
        // 创建算法实例并计算初始安全序列
        BankersAlgorithm algorithm = new BankersAlgorithm(model);
        int[] initialSafeSequence = algorithm.isSafeState(model);

        // 创建并显示GUI，传递初始安全序列
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BankersGUI gui = new BankersGUI(model, initialSafeSequence);
                gui.setVisible(true);
            }
        });
    }
}