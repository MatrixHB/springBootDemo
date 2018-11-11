package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import entities.BusData;
import org.springframework.stereotype.Repository;

import javax.security.auth.login.Configuration;
import java.util.ArrayList;
import java.util.List;

public class BusDataDao {

    private static String driver = "oracle.jdbc.driver.OracleDriver";
    private static String url = "jdbc:oracle:thin:@10.15.194.25:1521:idpora";
    private static String username = "RDS-GY";
    private static String password = "123456";

    public List<BusData> queryBusData() throws Exception {
        List<BusData> list = new ArrayList<BusData>(600);

        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement stmt = conn.createStatement();
        String sql = "select * from BUSDATA";
        ResultSet res = stmt.executeQuery(sql);

        while (res.next()) {
            BusData busData = new BusData();
            busData.setBusNumber(res.getInt("busnumber"));
            busData.setBusName(res.getString("busname"));
            busData.setBusLoad(res.getDouble("busload"));
            busData.setDeviceName(res.getString("devicename"));
            list.add(busData);
        }
        res.close();
        stmt.close();
        conn.close();

        return list;
    }
}
