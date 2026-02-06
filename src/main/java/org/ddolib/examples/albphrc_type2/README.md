# 二型 ALBP-HRC DDO 求解器

## 问题定义

**二型装配线平衡问题（Type-II ALBP）with 人机协同（HRC）**

- **输入**：
  - 任务集合 T = {0, 1, ..., n-1}
  - 前序约束 P ⊆ T × T
  - 最大工位数 m
  - 可用机器人数 Q
  - 每个任务的三种执行时间：
    - p_i^H：人工模式
    - p_i^R：机器人模式
    - p_i^HR：协作模式

- **目标**：最小化节拍时间 C

- **约束**：
  - 每个任务分配到一个工位
  - 前序约束满足
  - 工位数 ≤ m
  - 使用机器人数 ≤ Q

## 与一型问题的核心区别

| 特性 | 一型 ALBP-HRC | 二型 ALBP-HRC |
|------|---------------|---------------|
| **输入** | 给定节拍时间 C | 给定工位数 m |
| **目标** | 最小化工位数 | 最小化节拍时间 C |
| **状态** | 不需要记录负载 | 必须记录 maxLoadSoFar |
| **代价函数** | 新开工位 = +1 | newMax - oldMax |
| **路径总代价** | Σ(新开工位) = m | maxLoadSoFar = C |
| **下界** | ⌈总工作量 / C⌉ | ⌈总工作量 / m⌉ |

## 状态设计

```java
State = (
    completedTasks,          // 已完成任务集合
    stationIndex,            // 当前工位编号 (1..m)
    currentStationLoad,      // 当前工位负载
    currentStationHasRobot,  // 当前工位是否有机器人
    usedRobots,              // 已使用机器人数量（不含当前工位）
    maxLoadSoFar             // 当前路径的最大工位负载（目标函数）
)
```

**关键**：`maxLoadSoFar` 是目标函数，必须显式记录！

## 决策编码

```
decision = task * 3 + mode
```

其中 mode ∈ {0=Human, 1=Robot, 2=Collaboration}

**与一型的区别**：
- 一型：`decision = task * 2 + robotFlag`（robotFlag 表示是否为新工位分配机器人）
- 二型：`decision = task * 3 + mode`（显式指定执行模式，因为影响负载计算）

## 转移函数

### 情况 A：任务加入当前工位

```
newLoad = currentStationLoad + duration(task, mode)
newMaxLoad = max(maxLoadSoFar, newLoad)

nextState = (
    completedTasks ∪ {task},
    stationIndex,
    newLoad,
    currentStationHasRobot,
    usedRobots,
    newMaxLoad
)
```

### 情况 B：开新工位

```
newMaxLoad = max(maxLoadSoFar, duration(task, mode))

nextState = (
    completedTasks ∪ {task},
    stationIndex + 1,
    duration(task, mode),
    needsRobot(mode),
    usedRobots + (currentStationHasRobot ? 1 : 0),
    newMaxLoad
)
```

**约束检查**：`stationIndex + 1 ≤ m`

## 代价函数（关键！）

```java
transitionCost = newMaxLoad - oldMaxLoad
```

**这是二型问题的核心**：
- 路径总代价 = Σ transitionCost = maxLoadSoFar
- 最短路径 = 最小节拍时间 C

**示例**：
```
初始: maxLoadSoFar = 0
任务1 (时间=10) → maxLoadSoFar = 10, cost = 10 - 0 = 10
任务2 (时间=5)  → maxLoadSoFar = 15, cost = 15 - 10 = 5
开新工位
任务3 (时间=12) → maxLoadSoFar = 15, cost = 15 - 15 = 0 (因为 12 < 15)
任务4 (时间=8)  → maxLoadSoFar = 20, cost = 20 - 15 = 5

总代价 = 10 + 5 + 0 + 5 = 20 = maxLoadSoFar ✓
```

## 下界计算

### 基本思路

```
LB = ⌈总工作量 / 工位数⌉
```

### 转换率方法

1. 计算每个任务的转换率：
   - Cr_i^R = p_i^R / p_i^H
   - Cr_i^HR = p_i^HR / (p_i^H - p_i^HR)

2. 按转换率排序，将工作量从人工转换为机器人/协作

3. 平衡后的工作量 LH

4. 下界 = ⌈LH / m⌉

**与一型的区别**：
- 一型：`LB = ⌈LH / C⌉`（除以节拍时间，得到工位数）
- 二型：`LB = ⌈LH / m⌉`（除以工位数，得到节拍时间）

## 文件结构

```
albphrc_type2/
├── Type2State.java           # 状态定义
├── Type2Problem.java         # 问题定义（domain, transition, transitionCost）
├── Type2Relaxation.java      # 松弛操作
├── Type2Ranking.java         # 状态排序
├── Type2FastLowerBound.java  # 快速下界
├── Type2Dominance.java       # 支配规则
└── Type2DdoMain.java         # 主程序
```

## 使用方法

```bash
# 编译
javac -cp "lib/*" src/main/java/org/ddolib/examples/albphrc_type2/*.java

# 运行
java -cp "lib/*:src/main/java" org.ddolib.examples.albphrc_type2.Type2DdoMain \
    data/SALBP1/small_data_set_n=20/instance_n=20_1.alb \
    5 \
    2

# 参数说明：
# - 第1个参数：数据文件路径
# - 第2个参数：最大工位数 m
# - 第3个参数：可用机器人数 Q
```

## 输出示例

```
================================================================================
二型 ALBP-HRC 求解器
================================================================================
数据文件: data/SALBP1/small_data_set_n=20/instance_n=20_1.alb
最大工位数: 5
机器人数: 2
================================================================================

=== 二型 ALBP-HRC 问题 ===
任务数: 20, 最大工位数: 5, 机器人数: 2

开始求解...

================================================================================
找到更好的解！节拍时间: 45
================================================================================

工位分配:
  工位 1 (机器人: 是, 负载: 42): 任务1(R), 任务2(HR), 任务3(R)
  工位 2 (机器人: 否, 负载: 38): 任务4(H), 任务5(H), 任务6(H)
  工位 3 (机器人: 是, 负载: 45): 任务7(HR), 任务8(R), 任务9(HR)
  工位 4 (机器人: 否, 负载: 40): 任务10(H), 任务11(H)
  工位 5 (机器人: 否, 负载: 35): 任务12(H), 任务13(H), 任务14(H)

总工位数: 5
使用机器人数: 2
最大负载: 45

================================================================================
求解完成
================================================================================
最优节拍时间: 45
求解时间: 12.34 秒
================================================================================
```

## 关键设计要点

### 1. maxLoadSoFar 的维护

```java
// 加入当前工位
int newLoad = currentStationLoad + duration;
int newMaxLoad = Math.max(maxLoadSoFar, newLoad);

// 开新工位
int newMaxLoad = Math.max(maxLoadSoFar, duration);
```

### 2. 代价函数的正确性

```java
transitionCost = nextState.maxLoadSoFar() - state.maxLoadSoFar();
```

这保证了：
- 路径总代价 = 最终的 maxLoadSoFar
- 最短路径 = 最小节拍时间

### 3. 工位数约束

```java
if (stationIndex >= maxStations) {
    // 无法开新工位，返回惩罚状态
    return new Type2State(..., Integer.MAX_VALUE / 2);
}
```

### 4. 机器人资源管理

```java
int remainingRobots = totalRobots - usedRobots - (currentStationHasRobot ? 1 : 0);
```

## 理论保证

1. **最优性**：DDO 框架保证找到最优解（如果 beam width 足够大）
2. **可行性**：所有生成的解都满足前序约束、工位数约束、机器人约束
3. **下界有效性**：下界 ≤ 最优节拍时间

## 与一型代码的对应关系

| 一型文件 | 二型文件 | 主要区别 |
|---------|---------|---------|
| NestedSALBPState | Type2State | 增加 maxLoadSoFar |
| NestedSALBPProblem | Type2Problem | 决策编码、代价函数不同 |
| NestedSALBPRelax | Type2Relaxation | 优先级调整 |
| NestedSALBPRanking | Type2Ranking | 优先级调整 |
| NestedSALBPFastLowerBound | Type2FastLowerBound | 除以 m 而非 C |
| NestedSALBPDominance | Type2Dominance | 增加 maxLoadSoFar 比较 |

## 参考文献

- 一型 ALBP-HRC 实现：`src/main/java/org/ddolib/examples/ssalbrb1207nested/`
- DDO 框架文档：`org.ddolib.modeling.Problem`
