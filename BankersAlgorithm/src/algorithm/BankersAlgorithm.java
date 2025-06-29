package algorithm;

import model.BankersModel;

/**
 * 银行家算法的核心实现，包括安全性检查和资源请求处理
 */
public class BankersAlgorithm {
    private BankersModel model;
    private int[] lastSafeSequence;  // 保存最近一次的安全序列

    public int[] getLastSafeSequence() {
        return lastSafeSequence;
    }

    // 标记进程完成并释放资源
    public void completeProcess(int processId) {
        if (processId >= 0 && processId < model.getProcessCount()) {
            model.releaseResources(processId);
        }
    }
    
    public BankersAlgorithm(BankersModel model) {
        this.model = model;
    }
    
    // 处理资源请求
    public boolean processRequest(int processId, int[] request) {
        // 检查请求是否超过需求
        int[] need = model.getNeedResources(processId);
        for (int i = 0; i < model.getResourceTypes(); i++) {
            if (request[i] > need[i]) {
                System.out.println("错误：请求超过进程的最大需求");
                return false;
            }
        }
        
        // 检查请求是否超过可用资源
        int[] available = model.getAvailableResources();
        for (int i = 0; i < model.getResourceTypes(); i++) {
            if (request[i] > available[i]) {
                System.out.println("错误：资源不足，需等待");
                return false;
            }
        }
        
        // 尝试分配资源
        BankersModel tempModel = cloneModel(model);
        
        // 在临时模型上分配资源
        tempModel.allocateResources(processId, request);
        
        // 检查安全性
        int[] safeSequence = isSafeState(tempModel);
        if (safeSequence != null) {
            // 如果安全，则在实际模型上分配资源并记录安全序列
            model.allocateResources(processId, request);
            // 保存安全序列供GUI显示（可通过模型或算法类暴露）
            this.lastSafeSequence = safeSequence;
            return true;
        } else {
            this.lastSafeSequence = null;
            return false;
        }
    }                                      
    
    // 检查系统是否处于安全状态
    // 返回安全序列（安全时）或null（不安全时）
    // 公共方法供外部调用获取安全序列
    public int[] isSafeState(BankersModel model) {
        int processCount = model.getProcessCount();
        int resourceTypes = model.getResourceTypes();
        
        // 复制可用资源
        int[] work = model.getAvailableResources().clone();
        
        // 标记进程是否完成
        boolean[] finish = new boolean[processCount];
        
        // 存储安全序列
        int[] safeSequence = new int[processCount];
        int count = 0;
        
        // 查找可以完成的进程
        while (count < processCount) {
            boolean found = false;
            for (int i = 0; i < processCount; i++) {
                if (!finish[i]) {
                    // 检查进程i的需求是否可以被满足
                    int[] need = model.getNeedResources(i);
                    boolean canAllocate = true;
                    for (int j = 0; j < resourceTypes; j++) {
                        if (need[j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }
                    
                    if (canAllocate) {
                        // 分配资源并回收
                        for (int j = 0; j < resourceTypes; j++) {
                            work[j] += model.getAllocatedResources()[i][j];
                        }
                        
                        finish[i] = true;
                        safeSequence[count] = i;
                        count++;
                        found = true;
                    }
                }
            }
            
            if (!found) {
                break; // 没有找到可以完成的进程
            }
        }
        
        // 如果所有进程都能完成，则系统处于安全状态
        if (count == processCount) {
            return safeSequence;
        } else {
            return null;
        }
    }
    
    // 克隆模型用于安全性检查
    // 生成安全状态的已分配资源
    public int[][] generateSafeAllocatedResources() {
        int processCount = model.getProcessCount();
        int resourceTypes = model.getResourceTypes();
        int[][] max = model.getMaxResources();
        int[] total = model.getTotalResources();
        int maxAttempts = 100;  // 最大尝试次数
        int attempt = 0; 

        while (attempt < maxAttempts) {
            int[][] allocated = new int[processCount][resourceTypes];
            int[] remaining = total.clone();

            // 为每个进程分配不超过最大需求且不超过剩余资源的随机值
            for (int i = 0; i < processCount; i++) {
                for (int j = 0; j < resourceTypes; j++) {
                    // 确保至少分配1个资源，同时不超过最大需求和剩余资源
                    int maxPossible = Math.min(max[i][j], remaining[j]);
                    allocated[i][j] = maxPossible > 0 ? (int)(Math.random() * maxPossible + 1) : 0;
                    remaining[j] -= allocated[i][j];
                }
            }

            // 创建临时模型检查安全性
            BankersModel tempModel = cloneModel(model);
            tempModel.setAllocatedResources(allocated);
            if (isSafeState(tempModel) != null) {
                return allocated;  // 找到安全状态，返回分配方案
            }
            attempt++;
        }

        // 如果多次尝试后仍未找到安全状态，返回默认分配
        return new int[processCount][resourceTypes];
    }

    // 生成不安全状态的已分配资源
    public int[][] generateUnsafeAllocatedResources() {
        int processCount = model.getProcessCount();
        int resourceTypes = model.getResourceTypes();
        int[][] max = model.getMaxResources();
        int[] total = model.getTotalResources();
        int maxAttempts = 100;  // 最大尝试次数
        int attempt = 0;

        while (attempt < maxAttempts) {
            int[][] allocated = new int[processCount][resourceTypes];

            // 先分配大部分资源给第一个进程
            for (int j = 0; j < resourceTypes; j++) {
                allocated[0][j] = Math.min(max[0][j], total[j] - 1);
            }

            // 剩余资源随机分配
            int[] remaining = total.clone();
            for (int j = 0; j < resourceTypes; j++) {
                remaining[j] -= allocated[0][j];
            }

            for (int i = 1; i < processCount; i++) {
                for (int j = 0; j < resourceTypes; j++) {
                    allocated[i][j] = (int)(Math.random() * Math.min(max[i][j], remaining[j] + 1));
                    remaining[j] -= allocated[i][j];
                }
            }

            // 创建临时模型检查安全性
            BankersModel tempModel = cloneModel(model);
            tempModel.setAllocatedResources(allocated);
            if (isSafeState(tempModel) == null) {
                return allocated;  // 找到不安全状态，返回分配方案
            }
            attempt++;
        }

        // 如果多次尝试后仍未找到不安全状态，强制创建一个不安全状态
        int[][] allocated = new int[processCount][resourceTypes];
        for (int j = 0; j < resourceTypes; j++) {
            allocated[0][j] = max[0][j];  // 第一个进程分配所有最大需求
        }
        return allocated;
    }

    private BankersModel cloneModel(BankersModel original) {
        BankersModel clone = new BankersModel(
            original.getResourceTypes(), 
            original.getProcessCount()
        );
        
        clone.setTotalResources(original.getTotalResources().clone());
        
        int[][] allocated = new int[original.getProcessCount()][];
        for (int i = 0; i < original.getProcessCount(); i++) {
            allocated[i] = original.getAllocatedResources()[i].clone();
        }
        clone.setAllocatedResources(allocated);
        
        int[][] max = new int[original.getProcessCount()][];
        for (int i = 0; i < original.getProcessCount(); i++) {
            max[i] = original.getMaxResources()[i].clone();
        }
        clone.setMaxResources(max);
        
        return clone;
    }
}