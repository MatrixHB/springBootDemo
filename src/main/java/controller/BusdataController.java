package controller;

import dao.BusDataDao;
import entities.BusData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
public class BusdataController {

    @Autowired
    BusDataDao busDataDao;

    //查询节点信息
    @GetMapping("/busdata")
    public String list(Model model) throws Exception{

        Map<Integer, BusData> map = busDataDao.getMap();
        Collection<BusData> list = map.values();
        model.addAttribute("busdata",list);
        //thymrleaf默认拼串“classpath:/busdata/list.html”
        return "busdata/list";
    }

    //来到节点添加页面
    @GetMapping("/addBus")
    public String toAddPage(){
        return "busdata/add";
    }

    //添加节点信息完成并回到列表页面
    //springMVC自动将请求参数和入参对象的属性进行一一绑定，要求请求参数的名字和入参对象javaBean中属性名一致
    @PostMapping("/bus")
    public String addBus(BusData busData){
        busDataDao.saveBusData(busData);
        //表示重定向到“查询节点信息”的请求
        return "redirect:/busdata";
    }

    //来到节点修改页面（与添加页面二合一），busNumber是请求路径上的变量
    @GetMapping("/busdata/{busNumber}")
    public String toEditPage(@PathVariable Integer busNumber, Model model){

        //需要回显待编辑节点的信息，所以要传回一个与busNumber对应的model
        BusData busData = busDataDao.getMap().get(busNumber);
        model.addAttribute("bus",busData);

        return "busdata/add";
    }

    //修改节点信息完成并回到列表页面
    @PutMapping("/bus")
    public String editBus(BusData busData){
        busDataDao.saveBusData(busData);
        return "redirect:/busdata";
    }

    @DeleteMapping("/busdata/{busNumber}")
    public String deleteBus(@PathVariable Integer busNumber){
        busDataDao.deleteBusData(busNumber);
        return "redirect:/busdata";
    }

}

