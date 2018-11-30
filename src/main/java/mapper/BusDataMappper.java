package mapper;

import entities.BusData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BusDataMappper {

    @Select("select * from busdatatest where busnumber = #{id}")
    public BusData getBusById(Integer id);

    //动态sql语句
    @Update("<script>" +
            "update busdatatest set busnumber=#{busNumber},busname=#{busName},busload=#{busLoad}" +
                "<if test='deviceName != null'>" +
                ",devicename=#{deviceName}" +
                "</if> " +
            "where busnumber=#{busNumber}" +
            "</script>")
    public void updateBus(BusData bus);

}
