# 嵌套动态规划模型：一型装配线平衡 + 人机协同调度

## 模型概述

这是一个**两层嵌套的动态规划模型**，用于解决"一型装配线平衡问题（SALBP-1）+ 人机协同调度"。

### 问题结构

```
外层（装配线平衡）：决定任务到工位的分配
    ↓
内层（单工位调度）：优化每个工位内的人机协同执行
    ↓
目标：最小化工位数量，约束每个工位的makespan ≤ cycle time
```

## 文件说明

| 文件 | 说明 |
|------|------|
| **NestedSALBPState.java** | 外层状态：`<R, s, n_r>` |
| **NestedSALBPProblem.java** | 外层问题定义（调用内层问题）|
| **NestedSALBPRelax.java** | 外层松弛 |
| **NestedSALBPRanking.java** | 外层状态排序 |
| **NestedSALBPFastLowerBound.java** | 外层下界估计 |
| **NestedSALBPDdoMain.java** | 主程序 |

## 使用方法

### 编译

```bash
javac -cp "lib/*" ssalbrb1205nested/*.java ssalbrb1205/*.java
```

### 运行

```bash
java -cp ".;lib/*" org.ddolib.examples.ssalbrb1205nested.NestedSALBPDdoMain <data_file> <cycle_time> <num_robots>
```

### 参数说明

- **data_file**: 任务数据文件路径（默认：`data/test_5tasks_1.alb`）
- **cycle_time**: 循环时间约束（默认：200）
- **num_robots**: 可用机器人总数（默认：3）

### 示例

```bash
# 使用默认参数
java -cp ".;lib/*" org.ddolib.examples.ssalbrb1205nested.NestedSALBPDdoMain

# 指定参数
java -cp ".;lib/*" org.ddolib.examples.ssalbrb1205nested.NestedSALBPDdoMain data/test_5tasks_2.alb 250 5
```

## 输出说明

程序输出包括：

1. **问题实例信息**
   - Instance: 数据文件名
   - Cycle Time: 循环时间
   - Total Robots: 可用机器人数

2. **解决方案详情**
   - 每个工位分配的任务
   - 是否配备机器人
   - 工位的makespan

3. **搜索统计信息**
   - 搜索时间
   - 状态数量
   - 目标函数值（工位数）

### 输出示例

```
=== 嵌套动态规划：一型装配线平衡 + 人机协同调度 ===
Instance: data/test_5tasks_1.alb
Cycle Time: 200
Total Robots: 3

=== Solution Found ===
Number of stations: 2

Station 1:
  Tasks: [0, 1, 2]
  Has Robot: Yes
  Makespan: 180

Station 2:
  Tasks: [3, 4]
  Has Robot: Yes
  Makespan: 150

Total stations used: 2

SearchStatistics[...]
```

## 算法特点

### 外层决策（装配线平衡）

- **状态**: `<R, s, n_r>`
  - R: 剩余未分配任务
  - s: 当前工位编号
  - n_r: 剩余机器人数

- **决策**: 选择任务子集 + 机器人分配
  
- **约束**: 
  - 前继关系
  - 工位makespan ≤ cycle time

### 内层求解（单工位调度）

- **完整的DDO求解器**（基于 `ssalbrb1205` 包）
- **状态**: `<r_h, r_r, E>`
- **组件**:
  - `SSALBRBProblem`: 问题定义
  - `SSALBRBRelax`: 状态松弛
  - `SSALBRBRanking`: 状态排序
  - `SSALBRBFastLowerBound`: 下界估计
- **目标**: 最小化工位makespan

### 性能优化

1. **任务子集枚举优化**
   - 启发式搜索：优先考虑单任务和小子集
   - 前继剪枝：只生成满足前继约束的子集
   - 大小限制：限制最大子集大小（默认5）

2. **Makespan缓存**
   - 缓存 `(任务子集, 机器人配置) -> makespan`
   - 避免重复求解相同子问题

3. **分层宽度控制**
   - 外层宽度：500（较大，保证解质量）
   - 内层宽度：100（较小，加快速度）

## 扩展方向

### 当前实现

- ✅ 外层模型：装配线平衡（完整DDO）
- ✅ 内层模型：单工位调度（完整DDO）
- ✅ 基本约束和下界
- ✅ 状态松弛和排序
- ✅ Makespan缓存

### 可改进

- ⏳ 更强的下界估计（考虑前继约束）
- ⏳ 动态机器人重分配策略
- ⏳ 多目标优化（工位数 + 负载均衡）
- ⏳ 并行求解（多线程枚举任务子集）

## 理论参考

详见文档：`DP_Nested_SALBP1_HRC.md`

- 外层模型的数学表示
- 内层模型的调用机制
- 状态转移和代价函数
- 松弛和下界理论

## 注意事项

1. **内存占用**
   - 任务子集枚举可能产生大量状态
   - 建议使用合理的Width（默认500）

2. **计算时间**
   - 外层枚举 + 内层求解
   - 对于大规模问题，考虑启发式剪枝

3. **数据格式**
   - 与单工位模型相同的数据格式
   - 需要在 `<order strength>` 后有两个空行
