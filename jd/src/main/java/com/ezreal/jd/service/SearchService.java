package com.ezreal.jd.service;

import com.ezreal.jd.po.Result;

public interface SearchService {
    Result searchProduct(String queryStr,String catalog_name,String price,Integer page,String sort);
}
