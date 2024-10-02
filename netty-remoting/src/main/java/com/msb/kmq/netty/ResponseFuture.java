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
package com.msb.kmq.netty;

import com.msb.kmq.coder.KMessage;
import io.netty.channel.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResponseFuture {
    private final int opaque;
    private final Channel channel;
    private final KMessage request;
    private volatile KMessage response;
    private final long timeoutMillis;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    private volatile boolean sendRequestOK = true;


    public ResponseFuture(Channel channel, int opaque, KMessage request, long timeoutMillis) {
        this.opaque = opaque;
        this.channel = channel;
        this.request = request;
        this.timeoutMillis = timeoutMillis;
    }

    public KMessage waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }

    public void putResponse(final KMessage response) {
        this.response = response;
        this.countDownLatch.countDown();
    }


}
