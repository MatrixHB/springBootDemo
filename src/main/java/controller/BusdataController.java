package controller;

import dao.BusDataDao;
import entities.BusData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class BusdataController {

    @Autowired
    BusDataDao busDataDao;

    @GetMapping("/busdata")
    public String list(Model model) throws Exception{

        List<BusData> list = busDataDao.queryBusData();
        model.addAttribute("busdata",list);
        //thymrleaf默认拼串“classpath:/busdata/list.html”
        return "busdata/list";
    }
}

