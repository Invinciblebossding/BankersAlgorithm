package model;

/**
 * 银行家算法的数据模型，存储系统资源和进程信息
 */
public class BankersModel {
    private int resourceTypes; // 资源类型数量
    private int processCount;  // 进程数量
    
    private int[] totalResources;     // 每种资源的总数
    private int[][] allocatedResources; // 每个进程已分配的资源
    private int[][] maxResources;       // 每个进程的最大需求
    private int[] availableResources;   // 可用资源数量
    private boolean[] isCompleted;      // 进程是否已完成
    
    public BankersModel(int resourceTypes, int processCount) {
        this.resourceTypes = resourceTypes;
        this.processCount = processCount;
        
        totalResources = new int[resourceTypes];
        allocatedResources = new int[processCount][resourceTypes];
        maxResources = new int[processCount][resourceTypes];
        availableResources = new int[resourceTypes];
        isCompleted = new boolean[processCount];
    }
    
    // Getters and Setters
    public int getResourceTypes() {
        return resourceTypes;
    }
    
    public int getProcessCount() {
        return processCount;
    }
    
    public int[] getTotalResources() {
        return totalResources;
    }
    
    public void setTotalResources(int[] totalResources) {
        this.totalResources = totalResources;
        calculateAvailableResources();
    }
    
    public int[][] getAllocatedResources() {
        return allocatedResources;
    }
    
    public void setAllocatedResources(int[][] allocatedResources) {
        this.allocatedResources = allocatedResources;
        calculateAvailableResources();
    }

    public boolean isProcessCompleted(int processId) {
        return isCompleted[processId];
    }
    
    public int[][] getMaxResources() {
        return maxResources;
    }
    
    public void setMaxResources(int[][] maxResources) {
        this.maxResources = maxResources;
    }
    
    public int[] getAvailableResources() {
        return availableResources;
    }
    
    // 计算可用资源：总资源 - 已分配资源
    private void calculateAvailableResources() {
        // 初始化可用资源为总资源
        for (int i = 0; i < resourceTypes; i++) {
            availableResources[i] = totalResources[i];
        }
        
        // 减去已分配的资源
        for (int i = 0; i < processCount; i++) {
            for (int j = 0; j < resourceTypes; j++) {
                availableResources[j] -= allocatedResources[i][j];
            }
        }
    }
    
    // 获取进程的需求资源
    public int[] getNeedResources(int processId) {
        int[] need = new int[resourceTypes];
        for (int i = 0; i < resourceTypes; i++) {
            need[i] = maxResources[processId][i] - allocatedResources[processId][i];
        }
        return need;
    }
    
    // 分配资源
    public void allocateResources(int processId, int[] request) {
        for (int i = 0; i < resourceTypes; i++) {
            allocatedResources[processId][i] += request[i];
            availableResources[i] -= request[i];
        }
    }
    
    // 释放资源
    public void releaseResources(int processId) {
        if (!isCompleted[processId]) {
            for (int i = 0; i < resourceTypes; i++) {
                availableResources[i] += allocatedResources[processId][i];
                allocatedResources[processId][i] = 0;
                maxResources[processId][i] = 0; // 清除最大需求，使需求为0
            }
            isCompleted[processId] = true;
        }
    }
}