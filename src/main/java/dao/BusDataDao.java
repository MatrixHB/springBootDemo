package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import entities.BusData;
import mapper.BusDataMappper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.crypto.KeyGenerator;
import javax.security.auth.login.Configuration;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class BusDataDao {

    @Autowired
    DataSource dataSource;

    private static Map<Integer, BusData> map = new HashMap<>(600);

    //原本采用初始化代码块进行map的初始化，但出现错误
    //错误原因是@Autowired注解会在初始化代码块之后执行，所以queryBusData中的datasource为空
//    {
//        try {
//            map = queryBusData();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    @Cacheable(value="bus", keyGenerator = "myKeyGenerator", cacheManager = "concurrentMapCacheManager")
    public Map<Integer, BusData> queryBusData() throws Exception{
        System.out.println(dataSource.getClass());
        Connection conn = dataSource.getConnection();

        Statement stmt = conn.createStatement();
        String sql = "select * from BUSDATATEST";
        ResultSet res = stmt.executeQuery(sql);

        while (res.next()) {
            BusData busData = new BusData();
            int busNumber = res.getInt("busnumber");
            if(map.keySet().contains(busNumber))
                continue;
            busData.setBusNumber(busNumber);
            busData.setBusName(res.getString("busname"));
            busData.setBusLoad(res.getDouble("busload"));
            busData.setDeviceName(res.getString("devicename"));
            map.put(busData.getBusNumber(),busData);
        }
        res.close();
        stmt.close();
        conn.close();

        return map;
    }

    public void saveBusData(BusData busData){

        map.put(busData.getBusNumber(), busData);
    }

    public void deleteBusData(Integer busNumber){

        map.remove(busNumber);
    }

    public static Map<Integer, BusData> getMap() {
        return map;
    }

}
