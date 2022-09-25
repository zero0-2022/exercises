### 01  请你说一说什么是线程和进程？
**进程**：内存运行的一个应用程序，是系统运行程序的基本单位，程序的一次执行过程。
**线程**：进程中的一个执行单元，负责当前进程的任务执行，一个进程会有多个线程。

#### 区别
每个进程拥有独立的内存空间，线程之间的堆空间和方法区共享，线程栈空间和程序计数器是独立的。

#### 关系
一个进程会产生多个线程。

#### 线程的上下文切换
CPU 分配给线程的时间片用完后，会被切换出去，等待下一次调度，为了保证下次执行正确性，需要在切换出去前保存当前进程的运行时环境，也就是上下文，下次切换到该线程时再加载记录的上下文。

#### 线程的并发与并行有啥区别
- **并发 Concurrency**：同一时间内，多个任务都在执行，**单位时间内不一定是同时执行**
- **并行 Parallel**：单位时间内，多个任务同时执行，**单位时间内一定是同时执行**

### 02 使用了多线程会带来什么问题呢？
#### 线程安全问题
运行程序代码的进程的多个线程同时执行，如果每次运行一段代码的结果与单线程执行结果一致，且变量的值和预期的是一样的，就是线程安全的，反之则是线程不安全。

#### 原子性、有序性和可见性
并发编程的三个特性：原子性、有序性和可见性。
**原子性**：一系列操作，要么全执行并且不被打断，要么就都不执行。
**有序性**：程序代码按照编写顺序先后执行。虚拟机在进行代码编译时，对于那些改变顺序之后不会对最终结果造成影响的代码，虚拟机不一定会按照编写的代码的顺序来执行，有可能将代码指令重排序。实际上，对于有些代码进行重排序之后，虽然对变量的值没有造成影响，但有可能会出现线程安全问题。
**可见性**：当一个线程修改了共享变量的值，其他线程会马上知道这个修改。当其他线程要读取这个变量的时候，最终会去内存中读取，而不是从缓存中读取。

### 03 什么是死锁？如何排查死锁？
**死锁**：Deadlock，指两个或两个以上的线程持有不同系统资源的锁，线程彼此都等待获取对方的锁来完成自己的任务，但是没有让出自己持有的锁，线程就会无休止等待下去。线程竞争的资源可以是：锁、网络连接、通知事件，磁盘、带宽，以及一切可以被称作“资源”的东西。

**排查** 
一、使用 JDK 自带的 jstack 工具
```sh
jstack 4178 # 4178：Java应用程序进程号

: <<EOF 
输出信息中发现有 deadlock
...
Found 1 deadlock.
EOF
```

二、使用 JDK 提供的图形化工具 jconsole 


### 04 说一说 synchronized 和 volatile 的原理与区别
**synchronized**：JVM 是通过进入和退出Monitor对象来实现代码块同步和方法同步的，代码块同步使用的是 `monitorenter` 和  `monitorexit` 指令实现的，而方法同步是通过 `Access flags` 后面的标识来确定该方法是否为同步方法。在 jdk1.6 之前，synchronized 被称为重量级锁，在 jdk1.6 中，为了减少获得锁和释放锁带来的性能开销，引入了偏向锁和轻量级锁。
**volatile**：实现内存可见性主要是通过**内存屏障**，**内存屏障（Memory Barrier）** 是一种 CPU 指令，用于控制特定条件下的重排序和可见性问题。Java 编译会根据内存屏障的规则禁止重排序。

**区别：**
特点 | Volatile | Synchronized
----- | ----- | ----
加锁 | 否 | 是
阻塞进程 | 否 | 是 
保证原子性 | 否 | 是
保证可见性 | 是 | 是
性能 | 很好 | 很差

#### 什么是 JMM 内存模型？
JMM 是一组规则和规范，从抽象角度看，JMM 定义了线程和主内存之间的抽象关系。

#### 什么是 happends-before 规则？
在 JMM 中使用 happends-before 规则约束编译器优化行为，Java 允许编译器优化，但是不能无条件优化。

### 05 为什么使用线程池？如何创建线程池？
#### 为什么使用线程池？
频繁创建线程或销毁线程的开销很大，会降低系统整体性能。而线程池可以复用和维护多个线程，降低创建和销毁的损耗。
#### 如何创建线程池？
##### 自动创建线程池
- newFixedThreadPool: 固定线程池，无任务阻塞队列
- newSingleThreadExecutor: 一个线程线程的线程池，无界任务阻塞队列
- newCachedThreadPool：可缓存线程的无界线程池，可以自动回收多余线程
- newScheduledThreadPool：定时任务线程池
##### 手动创建线程池
利用标准线程池构造器
```Java
/**  
 * corePoolSize    : 线程池中常驻的线程数量。核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存活时间 keepAliveTime 的限制，除非将 allowCoreThreadTimeOut 设置为 true。  
 * maximumPoolSize : 线程池所能容纳的最大线程数。超过这个数的线程将被阻塞。当任务队列为没有设置大小的LinkedBlockingQueue时，这个值无效。  
 * keepAliveTime   : 当线程数量多于 corePoolSize 时，空闲线程的存活时长，超过这个时间就会被回收  
 * unit            : keepAliveTime 的时间单位  
 * workQueue       : 存放待处理任务的队列  
 * threadFactory   : 线程工厂  
 * handler         : 拒绝策略，拒绝无法接收添加的任务  

 */
ThreadPoolExecutor(int corePoolSize,  
                   int maximumPoolSize,  
                   long keepAliveTime,  
                   TimeUnit unit,  
                   BlockingQueue<Runnable> workQueue,  
                   ThreadFactory threadFactory,  
                   RejectedExecutionHandler handler) { ... ... }


// 例子
ExecutorService pool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
```

### 06 ThreadLocal 中 Map 的 key 为什么要使用弱引用？

#### 阿里 Java 开发手册中，为什么说不清理自定义的 ThreadLocal 变量会导致内存泄漏呢？

