## 项目性能测试报告
### 01 测试目的
性能压测监控平台搭建、压测分析实战练习。

### 02 测试工具
- JMeter
- Docker
- Grafana
- InfluxDB
- Prometheus
- node_exporter

### 03 测试环境
#### 3.1 环境
指标 | 参数
---   | --- 
机器 | 4C 8G
集群规模 | 单机
数据库   | 4C 8G
被压测服务网络带宽 | 100Mbps


#### 3.2 环境变量和启动参数
```bash
#!/bin/sh
# Copyright 1999-2022 Geek NB Group Holding Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

export JAVA_HOME=/usr/local/hero/jdk1.8.0_261
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=${JAVA_HOME}/bin:$PATH

#===========================================================================================
# init
#===========================================================================================

export SERVER="hero_web"
export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
# 获取当前目录
export BASE_DIR=`cd $(dirname $0)/.; pwd`
# 默认加载路径
export DEFAULT_SEARCH_LOCATIONS="classpath:/,classpath:/config/,file:./,file:./config/"
# 自定义默认加载配置文件路径
export CUSTOM_SEARCH_LOCATIONS=${DEFAULT_SEARCH_LOCATIONS},file:${BASE_DIR}/conf/


#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="${JAVA_OPT} -server -Xms512m -Xmx512m -Xmn256 -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
JAVA_OPT="${JAVA_OPT} -jar ${BASE_DIR}/${SERVER}-*.jar"
JAVA_OPT="${JAVA_OPT} ${JAVA_OPT_EXT}"
JAVA_OPT="${JAVA_OPT} --spring.config.location=${CUSTOM_SEARCH_LOCATIONS}"
# 创建日志文件目录
if [ ! -d "${BASE_DIR}/logs" ]; then
  mkdir ${BASE_DIR}/logs
fi

# 输出变量
echo "$JAVA ${JAVA_OPT}"
# 检查start.out日志输出文件
if [ ! -f "${BASE_DIR}/logs/${SERVER}.out" ]; then
  touch "${BASE_DIR}/logs/${SERVER}.out"
fi
#===========================================================================================
# 启动服务
#===========================================================================================
# 启动服务
echo "$JAVA ${JAVA_OPT}" > ${BASE_DIR}/logs/${SERVER}.out 2>&1 &
nohup $JAVA ${JAVA_OPT} hero_web.hero_web >> ${BASE_DIR}/logs/${SERVER}.out 2>&1 &
echo "server is starting，you can check the ${BASE_DIR}/logs/${SERVER}.out"
```



### 04 测试场景
模拟低延时场景，用户访问接口并发逐渐增加的过程。接口的响应时间为20ms，线程梯度:5、 10、15、20、25、30、35、40个线程，5000次。  

一共测试两次，对应的数据包大小分别是3.8kB, 1.1kB。

### 05 核心接口的测试结果
#### 05.1 数据包 3.8 kB 的测试结果
![](attachments/Pasted%20image%2020220828043707.png)

![](attachments/Pasted%20image%2020220828043644.png)

![](attachments/Pasted%20image%2020220828043751.png)

![](attachments/Pasted%20image%2020220828043632.png)


![](attachments/Pasted%20image%2020220828044410.png)

![](attachments/Pasted%20image%2020220828044458.png)


#### 05.2 数据包 1.1 kB 的测试结果

![](attachments/Pasted%20image%2020220828103146.png)

![](attachments/Pasted%20image%2020220828103255.png)

![](attachments/Pasted%20image%2020220828103336.png)

![](attachments/Pasted%20image%2020220828103511.png)

![](attachments/Pasted%20image%2020220828104923.png)

![](attachments/Pasted%20image%2020220828104745.png)

### 06 测试结论
1. 两个接口在当前的测试场景基本没有性能瓶颈，数据包差异大小从结果来看是【**每秒网络带宽使用**】的eth0_out区别最明显，另一个是网络接收的数据包总大小区别。
2. 响应数据包 3.8 kB 测试在 "线程组\*25"的结果中的相应时间最大值为18.4s，判断是网络抖动造成，原因一是从响应时间\[平均值\]，\[99%百分位\]与前后两个线程组的值进行比较来看，没有出现异常；原因二是自身的分位点和平均值来看，也没有拉高整体的响应时长。所以判断为瞬时的网络抖动。







