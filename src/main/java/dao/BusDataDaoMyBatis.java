package dao;

import entities.BusData;
import mapper.BusDataMappper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public class BusDataDaoMyBatis {
    @Autowired
    DataSource dataSource;

    @Autowired
    BusDataMappper busDataMappper;

    public BusData queryById(Integer id){
        return busDataMappper.getBusById(id);
    }

    public void update(BusData busData){
        busDataMappper.updateBus(busData);
    }
}
