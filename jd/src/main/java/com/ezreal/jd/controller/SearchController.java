package com.ezreal.jd.controller;

import com.ezreal.jd.po.Result;
import com.ezreal.jd.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 搜索商品
     */
    @RequestMapping("list")
    public String list(Model model,String queryString,String catalog_name,String price, Integer page,String sort){

        Result result = searchService.searchProduct(queryString, catalog_name, price, page, sort);
        model.addAttribute("result",result);
        //参数数据回显
        model.addAttribute("queryString",queryString);
        model.addAttribute("catalog_name",catalog_name);
        model.addAttribute("price",price);
        model.addAttribute("sort",sort);

        return "product_list";
    }

}
