# 嵌套DDO架构详解

## 完整的嵌套结构

```
┌─────────────────────────────────────────────────────────────────┐
│  外层DDO：装配线平衡 (NestedSALBP)                                  │
│  目标：最小化工位数                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  状态: <R, s, n_r>                                              │
│    - R: 剩余任务                                                 │
│    - s: 当前工位号                                               │
│    - n_r: 剩余机器人数                                           │
│                                                                 │
│  组件:                                                           │
│    ✓ NestedSALBPProblem       - 问题定义                         │
│    ✓ NestedSALBPState         - 状态                            │
│    ✓ NestedSALBPRelax         - 状态松弛                         │
│    ✓ NestedSALBPRanking       - 状态排序                         │
│    ✓ NestedSALBPFastLowerBound - 下界估计                       │
│                                                                 │
│  决策: 选择任务子集 S ⊆ R + 是否配机器人                           │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  对每个决策，调用内层DDO求解:                                │ │
│  │                                                            │ │
│  │  makespan(S, hasRobot) = 内层DDO求解(S, hasRobot)          │ │
│  │                                ↓                           │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │  内层DDO：单工位调度 (SSALBRB1205)                     │ │ │
│  │  │  目标：最小化makespan                                  │ │ │
│  │  ├──────────────────────────────────────────────────────┤ │ │
│  │  │                                                        │ │ │
│  │  │  状态: <r_h, r_r, E>                                   │ │ │
│  │  │    - r_h: 人的可用时间                                  │ │ │
│  │  │    - r_r: 机器人的可用时间                              │ │ │
│  │  │    - E: 任务的最早开始时间向量                           │ │ │
│  │  │                                                        │ │ │
│  │  │  组件:                                                  │ │ │
│  │  │    ✓ SSALBRBProblem         - 问题定义（子集任务）      │ │ │
│  │  │    ✓ SSALBRBState           - 状态                     │ │ │
│  │  │    ✓ SSALBRBRelax           - 状态松弛                 │ │ │
│  │  │    ✓ SSALBRBRanking         - 状态排序                 │ │ │
│  │  │    ✓ SSALBRBFastLowerBound  - 下界估计                 │ │ │
│  │  │                                                        │ │ │
│  │  │  决策: 选择任务 t + 执行模式 (人/机器人/协同)           │ │ │
│  │  │                                                        │ │ │
│  │  │  返回: 最优makespan                                     │ │ │
│  │  │                                                        │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  │                                                            │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  约束检查: makespan ≤ cycleTime ?                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 代码调用流程

### 1. 外层主循环

```java
// NestedSALBPDdoMain.java
Solvers.minimizeDdo(outerModel, ...)
    ↓
// 外层DDO搜索
for each state <R, s, n_r>:
    domain(state)  // 枚举任务子集
        ↓
```

### 2. 外层Domain计算

```java
// NestedSALBPProblem.java - domain()
for each feasible subset S ⊆ R:
    for each hasRobot in {true, false}:
        makespan = computeStationMakespan(S, hasRobot)  // 👈 调用内层
            ↓
```

### 3. 调用内层DDO

```java
// NestedSALBPProblem.java - computeStationMakespan()
subProblem = createSubProblem(S, hasRobot)  // 创建子问题
    ↓
makespan = solveDDO(subProblem)  // 👈 调用内层DDO求解器
    ↓
```

### 4. 内层DDO求解

```java
// NestedSALBPProblem.java - solveDDO()
innerModel = new DdoModel<SSALBRBState>() {
    problem()     → SSALBRBProblem (子问题)
    relaxation()  → SSALBRBRelax
    ranking()     → SSALBRBRanking
    lowerBound()  → SSALBRBFastLowerBound
    width()       → FixedWidth(100)
}

Solvers.minimizeDdo(innerModel, ...)  // 完整的DDO求解
    ↓
返回: 最优makespan
```

### 5. 返回外层

```java
// NestedSALBPProblem.java - domain()
if (makespan <= cycleTime):
    registerDecision(S, hasRobot, makespan)  // ✓ 可行决策
else:
    跳过  // ✗ 不可行
```

## 关键数据流

### 示例：5个任务的求解

```
外层输入: test_5tasks_1.alb (任务 {0,1,2,3,4})
    ↓
外层初始状态: <R={0,1,2,3,4}, s=1, n_r=3>
    ↓
外层枚举决策: S={0,1,2}, hasRobot=true
    ↓
创建子问题:
    - 任务: {0,1,2} (3个任务，重编号为 {0,1,2})
    - 前继关系: 仅保留子集内的关系
    - 时间: 仅复制这3个任务的时间
    ↓
内层DDO求解:
    - 初始状态: <r_h=0, r_r=0, E=[0,0,0]>
    - 使用 SSALBRBRelax 合并状态
    - 使用 SSALBRBRanking 排序状态
    - 使用 SSALBRBFastLowerBound 剪枝
    - 搜索最优调度序列
    ↓
内层返回: makespan = 180
    ↓
外层检查: 180 ≤ 200 (cycleTime) ? ✓
    ↓
外层注册决策: decisionId=0 → (S={0,1,2}, hasRobot=true, makespan=180)
    ↓
外层继续搜索...
```

## 性能参数对比

| 参数 | 外层 | 内层 | 说明 |
|------|------|------|------|
| **Width** | 500 | 100 | 外层需要更大宽度保证解质量 |
| **任务数** | 全部 | 子集 | 内层只处理分配的任务 |
| **搜索深度** | 工位数 | 任务数 | 外层深度=工位数，内层深度=子集大小 |
| **状态数** | 大 | 小 | 外层状态空间大，内层相对小 |

## 缓存机制

```
makespanCache: Map<Set<Integer>, Map<Boolean, Integer>>
                    ↑             ↑         ↑
                  任务子集      有无机器人  makespan

避免重复求解相同的子问题:
- (S={0,1}, hasRobot=true) → makespan=120 (已缓存)
- 下次遇到相同配置，直接返回120
```

## 完整组件清单

### 外层组件 (ssalbrb1205nested/)
- ✅ NestedSALBPState.java
- ✅ NestedSALBPProblem.java (核心：调用内层DDO)
- ✅ NestedSALBPRelax.java
- ✅ NestedSALBPRanking.java
- ✅ NestedSALBPFastLowerBound.java
- ✅ NestedSALBPDdoMain.java

### 内层组件 (ssalbrb1205/)
- ✅ SSALBRBState.java
- ✅ SSALBRBProblem.java (被外层调用)
- ✅ SSALBRBRelax.java (被外层的solveDDO调用)
- ✅ SSALBRBRanking.java (被外层的solveDDO调用)
- ✅ SSALBRBFastLowerBound.java (被外层的solveDDO调用)

## 验证要点

运行时验证嵌套是否正确：

1. ✅ **任务数量**: 内层子问题的任务数 < 外层原始任务数
2. ✅ **前继关系**: 内层只有子集内部的前继关系
3. ✅ **编号映射**: 内层任务从0重新编号
4. ✅ **DDO组件**: 内层使用完整的Relax/Ranking/LowerBound
5. ✅ **缓存命中**: 相同子集不重复求解

## 日志示例

```
=== 外层DDO开始 ===
State: <R={0,1,2,3,4}, s=1, n_r=3>
  枚举子集: {0,1,2}
    内层DDO求解 (3个任务)...
      SSALBRBRelax: 合并状态
      SSALBRBRanking: 排序状态
      SSALBRBFastLowerBound: 计算下界
    内层返回: makespan=180 ✓
  枚举子集: {0,1}
    (缓存命中，跳过求解)
    返回: makespan=120 ✓
  ...
外层决策: 选择 {0,1,2} + 机器人
下一状态: <R={3,4}, s=2, n_r=2>
```

---

**总结**: 现在内层真正使用了完整的DDO求解器（包括Relax、Ranking、LowerBound），而不是简单的贪心！
