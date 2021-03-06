/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Authors:
 *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.metamorphosis.example.cache;

import static com.taobao.metamorphosis.example.Help.initMetaConfig;

import java.util.concurrent.Executor;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.utils.AddrUtil;

import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.MessageSessionFactory;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;
import com.taobao.metamorphosis.client.consumer.ConsumerConfig;
import com.taobao.metamorphosis.client.consumer.MessageConsumer;
import com.taobao.metamorphosis.client.consumer.MessageIdCache;
import com.taobao.metamorphosis.client.consumer.MessageListener;
import com.taobao.metamorphosis.client.consumer.SimpleFetchManager;


/**
 * 异步消息消费者，使用集中式缓存memcached来防止重复
 * 
 * @author boyan
 * @Date 2011-5-17
 * 
 */
public class AsyncConsumer {
    public static void main(final String[] args) throws Exception {
        // 使用memcached防止重复消息
        MemcachedClient mc = new XMemcachedClient(AddrUtil.getAddresses("localhost:11211"));
        MessageIdCache msgIdCache = new MemcachedMessageIdCache(mc);
        SimpleFetchManager.setMessageIdCache(msgIdCache);

        // New session factory,强烈建议使用单例
        final MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(initMetaConfig());

        // subscribed topic
        final String topic = "meta-test";
        // consumer group
        final String group = "meta-example";
        // create consumer,强烈建议使用单例
        ConsumerConfig consumerConfig = new ConsumerConfig(group);
        // 默认最大获取延迟为5秒，这里设置成100毫秒，请根据实际应用要求做设置。
        consumerConfig.setMaxDelayFetchTimeInMills(100);
        final MessageConsumer consumer = sessionFactory.createConsumer(consumerConfig);
        // subscribe topic
        consumer.subscribe(topic, 1024 * 1024, new MessageListener() {

            @Override
            public void recieveMessages(final Message message) {
                System.out.println("Receive message " + new String(message.getData()));
            }


            @Override
            public Executor getExecutor() {
                // Thread pool to process messages,maybe null.
                return null;
            }
        });
        // complete subscribe
        consumer.completeSubscribe();

    }

}