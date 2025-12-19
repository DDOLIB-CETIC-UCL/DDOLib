# 嵌套动态规划模型：一型装配线平衡 + 人机协同调度

## 问题描述

这是一个**两层嵌套的动态规划模型**：
- **外层**：一型装配线平衡问题（SALBP-1），负责将任务分配到不同工位
- **内层**：单工位人机协同调度问题，负责在单个工位内优化人机协同执行

### 问题定义

给定：
- 任务集合 $T = \{1, 2, \ldots, n\}$
- 每个任务 $t$ 的三种执行时间：$p_t^h$（人工）、$p_t^r$（机器人）、$p_t^c$（协同）
- 前继关系 $P \subseteq T \times T$
- 循环时间 $C$（cycle time）
- 每个工位配置：1个人 + 最多1个机器人

目标：最小化工位数量 $m$，使得每个工位的完成时间（makespan）≤ $C$

---

## 外层模型：装配线平衡（任务到工位分配）

### 状态表示

\[
\langle R, s, n_r \rangle
\]

其中：
- $R \subseteq T$：**剩余未分配任务集合**
- $s$：**当前工位编号**（从1开始）
- $n_r \in \{0, 1, \ldots, s\}$：**剩余可用机器人数量**

初始状态：
\[
\langle T, 1, m_{robot} \rangle
\]
其中 $m_{robot}$ 是可用机器人总数（例如：每个工位最多1个）

### 决策域（外层）

在状态 $\langle R, s, n_r \rangle$ 下，决策是选择一个**任务子集** $S \subseteq R$ 分配给当前工位 $s$。

**可行性条件**：
1. **前继约束**：$S$ 中的任务必须满足前继关系
   - 如果 $t \in S$ 且 $(u, t) \in P$，则 $u \notin R$（前继已分配）或 $u \in S$（前继在同一工位）
   
2. **非空约束**：$S \neq \emptyset$（至少分配一个任务）

3. **可达性**：$S$ 的任务必须是 eligible 的（所有前继要么已分配，要么在 $S$ 中）

### 内层调用：单工位调度

对于选定的任务子集 $S$ 和机器人分配决策 $has\_robot \in \{0, 1\}$：

**调用内层模型**求解单工位最优调度：
\[
\text{makespan}(S, has\_robot) = \text{SolveStationScheduling}(S, has\_robot)
\]

**内层模型**（基于 DP1205）：
- 输入：任务集 $S$，是否有机器人 $has\_robot$
- 状态：$\langle r_h, r_r, E \rangle$
  - $r_h$：人的最早可用时间
  - $r_r$：机器人的最早可用时间（如果有）
  - $E$：任务的最早开始时间向量（编码分配状态）
- 输出：该工位的 makespan（$\max(r_h, r_r)$ 在终止状态）

### 循环时间约束

\[
\text{makespan}(S, has\_robot) \leq C
\]

如果违反约束，该决策不可行。

### 状态转移（外层）

从状态 $\langle R, s, n_r \rangle$ 选择任务集 $S$ 和机器人分配 $has\_robot$：

\[
\langle R, s, n_r \rangle \xrightarrow{(S, has\_robot)} \langle R', s', n_r' \rangle
\]

其中：
- $R' = R \setminus S$（移除已分配任务）
- $s' = s + 1$（进入下一个工位）
- $n_r' = n_r - has\_robot$（更新剩余机器人数）

### 转移代价（外层）

\[
\text{cost} = 
\begin{cases}
1 & \text{if } S \neq \emptyset \text{ and makespan}(S, has\_robot) \leq C \\
+\infty & \text{otherwise (不可行)}
\end{cases}
\]

代价为**打开一个新工位**的成本（1个工位）。

### 目标函数

\[
\min \sum_{\text{transitions}} \text{cost}
\]

即最小化使用的工位数量。

### 终止条件

到达状态 $\langle \emptyset, s^*, n_r^* \rangle$，即所有任务已分配完毕。

最优解的工位数 = $s^* - 1$

---

## 内层模型：单工位人机协同调度

（详见 DP1205.md）

### 状态表示

\[
\langle r_h, r_r, E \rangle
\]

- $r_h$：人的最早可用时间
- $r_r$：机器人的最早可用时间
- $E = (E_1, \ldots, E_{|S|})$：任务的编码状态
  - $E_t \geq 0$：任务 $t$ 未分配，$E_t$ 是最早开始时间
  - $E_t < 0$：任务 $t$ 已分配，完成时间为 $-E_t$

### 决策域（内层）

对于 eligible 任务 $t$，选择执行模式 $m \in \{h, r, c\}$：
- $h$：仅人工
- $r$：仅机器人（如果 $has\_robot = 1$）
- $c$：人机协同（如果 $has\_robot = 1$）

### 目标函数（内层）

\[
\min \max(r_h, r_r) \quad \text{at terminal state}
\]

即最小化该工位的 makespan。

---

## 嵌套求解流程

### 伪代码

```
function SolveNestedSALBP1_HRC(T, P, cycle_time, m_robot):
    # 外层DP：装配线平衡
    DP_outer = {}
    initial_state = <T, 1, m_robot>
    DP_outer[initial_state] = 0  # 初始状态代价为0
    
    for state <R, s, n_r> in DP_outer:
        if R is empty:
            continue  # 所有任务已分配
        
        # 枚举所有可行任务子集 S ⊆ R
        for S in FeasibleSubsets(R, P):
            # 枚举机器人分配决策
            for has_robot in {0, 1} if n_r > 0 else {0}:
                # 调用内层模型求解单工位调度
                makespan = SolveStationScheduling(S, has_robot)
                
                # 检查循环时间约束
                if makespan <= cycle_time:
                    # 可行转移
                    next_state = <R \ S, s + 1, n_r - has_robot>
                    cost = DP_outer[state] + 1  # 增加1个工位
                    
                    if next_state not in DP_outer or cost < DP_outer[next_state]:
                        DP_outer[next_state] = cost
    
    # 找到终止状态的最优解
    min_stations = min(cost for state, cost in DP_outer if state.R is empty)
    return min_stations

function SolveStationScheduling(S, has_robot):
    # 内层DP：单工位调度（基于DP1205）
    DP_inner = {}
    if has_robot:
        initial = <0, 0, [0, 0, ..., 0]>  # |S|个任务
    else:
        initial = <0, INF, [0, 0, ..., 0]>  # 机器人不可用
    
    DP_inner[initial] = 0
    
    for state <r_h, r_r, E> in DP_inner:
        for t in EligibleTasks(S, E):
            for mode in {h, r, c}:
                if mode in {r, c} and not has_robot:
                    continue  # 无机器人时跳过
                
                # 计算转移
                next_state = Transition(state, t, mode)
                # 更新DP
                ...
    
    # 返回终止状态的makespan
    terminal_state = find state where all tasks in S are assigned
    return max(terminal_state.r_h, terminal_state.r_r)
```

---

## 实现考虑

### 1. 任务子集枚举优化

直接枚举 $2^{|R|}$ 个子集是不可行的。优化策略：

- **增量构建**：从小到大构建任务子集
- **前继剪枝**：只考虑满足前继关系的子集
- **启发式搜索**：优先考虑"紧密"的任务组

### 2. 状态空间压缩

- **R 的表示**：使用 BitSet 或整数编码
- **对称性消除**：相同任务集但不同工位编号的状态可能等价

### 3. 下界估计

- **外层下界**：$\lceil \sum_{t \in R} \min_m p_t^m / C \rceil$
- **内层下界**：任务总时间的下界

### 4. 机器人分配策略

- **固定配置**：每个工位配1个机器人（$n_r = s$）
- **有限配置**：总共 $k$ 个机器人，需优化分配

---

## 状态表示对比

| 层级 | 状态 | 含义 | 决策 |
|------|------|------|------|
| **外层** | $\langle R, s, n_r \rangle$ | 剩余任务、工位号、剩余机器人 | 选择任务子集+机器人分配 |
| **内层** | $\langle r_h, r_r, E \rangle$ | 资源可用时间、任务状态 | 选择任务+执行模式 |

---

## 示例

假设：
- 5个任务：$T = \{1, 2, 3, 4, 5\}$
- 循环时间：$C = 200$
- 2个机器人可用

### 外层决策示例

1. **工位1**：分配 $S_1 = \{1, 2\}$，配1个机器人
   - 内层求解：makespan = 150 ≤ 200 ✓
   
2. **工位2**：分配 $S_2 = \{3, 4\}$，配1个机器人
   - 内层求解：makespan = 180 ≤ 200 ✓
   
3. **工位3**：分配 $S_3 = \{5\}$，配0个机器人
   - 内层求解：makespan = 120 ≤ 200 ✓

结果：使用3个工位

---

## 扩展方向

1. **二型装配线平衡（SALBP-2）**：固定工位数，最小化循环时间
2. **动态机器人重分配**：机器人可在工位间移动
3. **多机器人协同**：每个工位多个机器人
4. **鲁棒性优化**：考虑任务时间的不确定性

---

## 参考文档

- 内层模型：`DP1205.md` - 单工位人机协同调度
- 外层模型：经典 SALBP-1 + 机器人分配决策
