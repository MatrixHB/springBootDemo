package demo;

import dao.BusDataDao;
import dao.BusDataDaoMyBatis;
import entities.BusData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootdemoApplication.class)
public class SpringbootdemoApplicationTests {

    @Autowired
    RedisTemplate<String, BusData> busRedisTemplate;

    @Autowired
    BusDataDao busDataDao;

    @Autowired
    BusDataDaoMyBatis busDataDaoMyBatis;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    JavaMailSenderImpl mailSender;

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

    @Test
    public void mailTest() throws Exception{
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        //邮件设置
        helper.setSubject("邮件测试");
        helper.setText("<b style='color:red'>这是一封测试邮件，请忽略</b>",true);
        helper.setTo("yibinhaha@163.com");
        helper.setFrom("435856474@qq.com");
        //添加附件
        helper.addAttachment("1.txt",new File("C:\\GY-data\\IeeeIsland.txt"));
        mailSender.send(mimeMessage);
    }

    @Test
    public void aopTest(){
        BusData busData = busDataDaoMyBatis.queryById(2);
    }

}
