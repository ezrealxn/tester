package com.ezreal.jd.service.impl;

import com.ezreal.jd.po.Product;
import com.ezreal.jd.po.Result;
import com.ezreal.jd.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService{
    //注入HttpSolrServer
    @Autowired
    private HttpSolrServer httpSolrServer;

    /**
     * 搜索商品
     * @param queryStr
     * @param catalog_name
     * @param price
     * @param page
     * @param sort
     * @return
     */
    @Override
    public Result searchProduct(String queryStr, String catalog_name, String price, Integer page, String sort) {
        //建立查询对象
        SolrQuery sq = new SolrQuery();
        //设置搜索关键词（如果关键词为空，搜索全部）
        if(StringUtils.isNotBlank(queryStr)){
            sq.setQuery(queryStr);
        }else{
            sq.setQuery("*:*");
        }
        //设置默认搜索域
        sq.set("df","product_keywords");

        //设置过滤条件
        if(StringUtils.isNotBlank(catalog_name)){
            catalog_name="product_catalog_name:"+catalog_name;
        }
        //商品价格
        if(StringUtils.isNotBlank(price)){
            String[] arr = price.split("-");
            price = "product_price:["+arr[0]+"  TO "+arr[1]+"]";
        }

        sq.setFilterQueries(catalog_name,price);
        //设置分页
        if(page==null){
            page = 1;
        }

        int pageSize = 10;
        sq.setStart((page-1)*pageSize);
        sq.setRows(pageSize);

        if("1".equals(sort)){
            sq.setSort("product_price", SolrQuery.ORDER.asc);
        }else{
            sq.setSort("product_price", SolrQuery.ORDER.desc);
        }

        //设置高亮显示
        sq.setHighlight(true);
        sq.addHighlightField("product_name");
        sq.setHighlightSimplePre("<font color='red'>");
        sq.setHighlightSimplePost("</font>");

        QueryResponse queryResponse = null;

        try {
            queryResponse = httpSolrServer.query(sq);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        //从queryResponse中获取数据
        SolrDocumentList searchResults = queryResponse.getResults();

        //获取高亮数据
        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();

        //处理结果集
        Result result = new Result();

        //设置当前页
        result.setCurPage(page);

        //设置页数
        int totals = (int) searchResults.getNumFound();
        int pageCount = 0;
        if(totals%pageSize==0){
            pageCount = totals/pageSize;
        }else{
            pageCount = totals/pageSize+1;
        }
        result.setPageCount(pageCount);

        //设置总页数
        result.setRecordCount(totals);

        //封装集合数据
        List<Product> productList = new ArrayList<>();
        for (SolrDocument doc : searchResults) {
            String pid = doc.get("id").toString();

            String pname = "";
            List<String> list = highlighting.get(pid).get("product_name");
            if(list!=null && list.size()>0){
                pname = list.get(0);
            }else{
                pname = doc.get("product_name").toString();
            }

            String prcture = doc.get("product_picture").toString();

            String productPrice = doc.get("product_price").toString();

            //创建商品对象
            Product product = new Product();
            product.setPid(pid);
            product.setName(pname);
            product.setPicture(prcture);
            product.setPrice(productPrice);

            productList.add(product);

        }

        result.setProductList(productList);

        return result;
    }
}
