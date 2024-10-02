/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
  $Id: QueueData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package com.msb.kmq.nameserver.data;
//主要保存队列相关的信息（节点名称、队列数量）
public class QueueData implements Comparable<QueueData> {
    private String brokerName;
    private int QueueNums;


    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getQueueNums() {
        return QueueNums;
    }

    public void setQueueNums(int queueNums) {
        QueueNums = queueNums;
    }



    @Override
    public int compareTo(QueueData o) {
        return this.brokerName.compareTo(o.getBrokerName());
    }

}
