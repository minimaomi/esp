1、启动Zookeeper，Zookeeper部署的是单点的。

bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

2、启动Kafka服务，使用 kafka-server-start.sh 启动 kafka 服务
bin/kafka-server-start.sh config/server.properties

3、创建topic

使用 kafka-topics.sh 创建单分区单副本的 topic hello
bin/kafka-topics.sh --create --zookeeper worker:2181 --replication-factor 1 --partitions 1 --topic hello
bin/kafka-topics.sh --create --zookeeper master:2181 --replication-factor 1 --partitions 1 --topic topic-seq3

查看 topic 列表

bin/kafka-topics.sh --list --zookeeper worker:2181

4、产生消息，创建消息生产者

bin/kafka-console-producer.sh --broker-list worker:9092 --topic hello
bin/kafka-console-producer.sh --bootstrap-server worker:9092 --topic topicName --property parse.key=true
5、消费消息，创建消息消费者

bin/kafka-console-consumer.sh --bootstrap-server worker:9092 --topic hello --from-beginning
D:\dev\workspace\hadoop\kafkacli\src\kafka.properties
D:/dev/workspace/hadoop/kafkacli/src/kafka.properties
D:/dev/workspace/hadoop/kafkacli/docs/kafka.properties

4、产生消息，创建消息生产者
bin/kafka-topics.sh --create --bootstrap-server worker:9092 --replication-factor 1 --partitions 1 --topic my-replicated-topic
bin/kafka-console-consumer.sh --bootstrap-server worker:9092 --topic my-replicated-topic --from-beginning


bin/kafka-console-producer.sh --broker-list master:9092 --topic topic-seq2
bin/kafka-console-consumer.sh --bootstrap-server master:9092 --topic wordcount-output --from-beginning


********************* kafka cluster **********************************
bin/kafka-topics.sh --describe --zookeeper master:2181 --topic WordsWithCountsTopic
bin/kafka-topics.sh --describe --zookeeper master:2181,worker:2181,worker2:2181 --topic topic_cluster102

bin/kafka-topics.sh --list --zookeeper worker:2181
bin/kafka-topics.sh --list --zookeeper master:2181,worker:2181,worker2:2181 --topic topic_cluster101
bin/kafka-topics.sh --create --zookeeper master:2181,worker:2181,worker2:2181 --replication-factor 1 --partitions 6 --topic topic_cluster101
bin/kafka-topics.sh --create --zookeeper master:2181,worker:2181,worker2:2181 --replication-factor 1 --partitions 3 --topic itheima_topic

bin/kafka-topics.sh --delete --zookeeper master:2181,worker:2181,worker2:2181 --topic itheima_topic
bin/kafka-topics.sh --delete --zookeeper master:2181,worker:2181,worker2:2181 --topic topic_cluster102
bin/kafka-topics.sh --delete --zookeeper master:2181 --topic topic_cluster101

bin/kafka-topics.sh  --describe --zookeeper server1:2181,server2:2181,server3:2181 --topic itcast_topic
bin/kafka-console-producer.sh --broker-list master:9092,worker:9092,worker2:9092 --topic topic_cluster100
bin/kafka-console-producer.sh --broker-list 192.168.0.76:9092,192.168.0.77:9092,192.168.0.8:9092 --topic topic_cluster100

kafka-topics.sh --describe --zookeeper mini01:2181 --topic zhang 
2 Topic:zhang    PartitionCount:4    ReplicationFactor:3    Configs:3     
Topic: zhang    Partition: 0    Leader: 1    Replicas: 1,2,0    Isr: 1,2,04     
Topic: zhang    Partition: 1    Leader: 2    Replicas: 2,0,1    Isr: 2,0,15     
Topic: zhang    Partition: 2    Leader: 0    Replicas: 0,1,2    Isr: 0,1,26     
Topic: zhang    Partition: 3    Leader: 1    Replicas: 1,0,2    Isr: 1,0,2


delete /brokers/topics/itcast_topic
deleteall   /brokers/topics/itcast_topic