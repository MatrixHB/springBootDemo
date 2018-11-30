package demo;

import dao.BusDataDaoMyBatis;
import entities.BusData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLOutput;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootdemoApplication.class)
public class SpringbootdemoApplicationTests {

    @Autowired
    RedisTemplate<String, BusData> busRedisTemplate;

    @Autowired
    BusDataDaoMyBatis busDataDaoMyBatis;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void redisTest() {
        BusData busData = busDataDaoMyBatis.queryById(2);
        busRedisTemplate.opsForValue().set("bus002", busData);
    }

    @Test
    public void mqProducerTest(){
        BusData busData = busDataDaoMyBatis.queryById(2);
        rabbitTemplate.convertAndSend("cxlab.topic","*.busdata",busData);
    }

    @Test
    public void mqConsumerTest(){
        BusData busData = (BusData) rabbitTemplate.receiveAndConvert("rdsgy.busdata");
        System.out.println(busData.toString());
    }

}
