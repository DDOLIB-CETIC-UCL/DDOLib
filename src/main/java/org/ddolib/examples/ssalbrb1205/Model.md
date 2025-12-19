\section{Introduction}

A set of tasks $T$ has to be scheduled.
Each task $t$ can be executed in three mode:
\begin{itemize}
	\item With a human ($h$)
	\item With a robot ($r$)
	\item With a collaboration between a robot and a human ($c$)
\end{itemize}
The processing time of a task depends on the collaboration mode to execute it.
Those are denoted $p_t^m$ where $m \in \{h,r,c\}$.

Given one robot and one human, the goal is to schedule the execution of the tasks and decide in what mode to execute them so as to minimize the makespan of the schedule.

\section{Dynamic Programming Formulation}

The state representation is
\[
\langle r_h, r_r, \mathbf{E} \rangle
\]
where:
\begin{itemize}
	\item $r_h$ is the earliest time at which the \textbf{human} is available,
	\item $r_r$ is the earliest time at which the \textbf{robot} is available,
	\item $\mathbf{E}$ is a vector encoding both assignment status and timing information for every task:
	\begin{itemize}
		\item If $E_t \geq 0$: task $t$ is \textbf{unassigned}, and $E_t$ represents its earliest start time (determined by the maximum completion time of its already-scheduled predecessors),
		\item If $E_t < 0$: task $t$ is \textbf{assigned}, and its completion time is $-E_t$.
	\end{itemize}
\end{itemize}

The root state is
\[
\langle 0, 0, \mathbf{0} \rangle,
\]
i.e., both resources are available at time 0, and all tasks are unassigned with earliest start time 0 (no predecessors completed yet).

\subsection{Domain}

Let $\text{Pred}(t)$ denote the set of predecessors of task $t$:
\[
\text{Pred}(t) = \{\, u \in T \mid (u,t) \in P \,\}.
\]

At a state $s = \langle r_h, r_r, \mathbf{E} \rangle$, a task $t$ is \emph{eligible} if it is unassigned and all its predecessors have already been assigned:
\[
t \text{ is eligible in } s
\iff
E_t \geq 0 \ \wedge\ \forall u \in \text{Pred}(t):\ E_u < 0.
\]

The domain of decisions at state $s$ is then:
\[
\text{Dom}(s) = \{\, (t,m) \mid t \text{ is eligible in } s,\ m \in \{h,r,c\} \,\},
\]
where $m = h$ (human), $m = r$ (robot), or $m = c$ (collaboration).

\subsection{Transition Function}

Consider a state
\[
s = \langle r_h, r_r, \mathbf{E} \rangle
\]
and a decision $(t,m) \in \text{Dom}(s)$.

We denote by $p_t^m$ the processing time of task $t$ in mode $m \in \{h,r,c\}$.

The actual start time $s_t$ of task $t$ depends on both the resource availability and the earliest start time from precedence constraints:
\[
s_t =
\begin{cases}
	\max(r_h, E_t) & \text{if } m = h,\\[4pt]
	\max(r_r, E_t) & \text{if } m = r,\\[4pt]
	\max(r_h, r_r, E_t) & \text{if } m = c.
\end{cases}
\]

In words: a task may begin only when (i) the resource(s) required by its execution mode are free and (ii) all of its predecessors have finished (captured by $E_t$).

The completion time of task $t$ is:
\[
C_t = s_t + p_t^m.
\]

The successor state
\[
s' = \langle r_h', r_r', \mathbf{E}' \rangle
\]
is then defined as follows:

\begin{itemize}
	\item Resource availability:
	\[
	(r_h', r_r') =
	\begin{cases}
		(C_t,\ r_r) & \text{if } m = h,\\[4pt]
		(r_h,\ C_t) & \text{if } m = r,\\[4pt]
		(C_t,\ C_t)   & \text{if } m = c.
	\end{cases}
	\]
	
	\item Task status and timing vector: Update $\mathbf{E}'$ as follows:
	\[
	E_u' =
	\begin{cases}
		-C_t & \text{if } u = t \text{ (mark task } t \text{ as assigned with completion time } C_t \text{)},\\[4pt]
		\max(E_u, C_t) & \text{if } u \neq t,\ E_u \geq 0,\ (t, u) \in P \text{ (update successor's earliest start time)},\\[4pt]
		E_u & \text{otherwise (keep unchanged).}
	\end{cases}
	\]
	
	This update serves two purposes: (1) marks task $t$ as assigned by storing $-C_t$, and (2) ensures that unassigned successors of task $t$ cannot start before $C_t$.
\end{itemize}

\subsection{Transition Cost}

We define the makespan at state $s$ as:
\[
\text{MK}(s) = \max(r_h,\ r_r).
\]

The cost of a transition from $s$ to $s'$ induced by decision $(t,m)$ is the increase in makespan:
\[
c(s, t, m) = \text{MK}(s') - \text{MK}(s)
= \max(r_h', r_r') - \max(r_h, r_r).
\]

\section{Handling Precedence Constraints}

We are given a set of precedence constraints
\[
P \subseteq T \times T,\quad (u,t) \in P \ \Rightarrow\ u \text{ must finish before } t \text{ can start}.
\]

For each task $t$, we define:
\[
\text{Pred}(t) = \{\, u \in T \mid (u,t) \in P \,\}.
\]

At a state $s = \langle r_h, r_r, \mathbf{E} \rangle$, a task $t$ is eligible if and only if it is unassigned and all its predecessors have been assigned:
\[
t \text{ is eligible in } s
\iff
E_t \geq 0 \ \wedge\ \forall u \in \text{Pred}(t):\ E_u < 0.
\]

This condition is exactly how precedence constraints are enforced in the domain definition: tasks whose predecessors are still unassigned (with $E_u \geq 0$) cannot appear in the domain and thus cannot be scheduled.

The vector $\mathbf{E}$ is dynamically updated as tasks are scheduled. When task $t$ is assigned and completed at time $C_t$:
\begin{itemize}
	\item Task $t$ is marked as assigned by setting $E_t' = -C_t$ (negative value encodes the completion time),
	\item All unassigned successors $u$ such that $(t,u) \in P$ have their earliest start time updated to $E_u' = \max(E_u, C_t)$ (only if $E_u \geq 0$).
\end{itemize}
This guarantees that no task starts before all its predecessors have finished.

\section{Objective Function}

The goal is to find a complete schedule (a state where all tasks are assigned) that minimizes the makespan:
\[
\min \max(r_h, r_r)
\]
subject to $E_t < 0$ for all $t \in T$ (all tasks are assigned) and all precedence constraints are satisfied.