package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import entities.BusData;
import org.springframework.stereotype.Repository;

import javax.security.auth.login.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusDataDao {

    private static Map<Integer, BusData> map = new HashMap<>(600);

    private static String driver = "oracle.jdbc.driver.OracleDriver";
    private static String url = "jdbc:oracle:thin:@10.15.194.25:1521:idpora";
    private static String username = "RDS-GY";
    private static String password = "123456";

    {
        try {
            map = queryBusData();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<Integer, BusData> queryBusData() throws Exception{

        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement stmt = conn.createStatement();
        String sql = "select * from BUSDATA";
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
