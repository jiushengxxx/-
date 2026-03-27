package com.ct.service.BackgroundService;

import com.ct.mapper.BackgroundMapper.BProductMapper;
import com.ct.model.ForegroundModel.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BProductService {

    @Autowired
    private BProductMapper mapper;

    /** 查询后台商品列表 */
    public List<Map<String,Object>> selectProductList(Product pro) {
        return mapper.selectProductList(pro);
    }

    /** 插入推荐商品 */
    public int insertRecommendByPid(String pid) {
        return mapper.insertRecommendByPid(pid);
    }

    /** 删除推荐商品 */
    public int deleteRecommendByPid(String pid) {
        return  mapper.deleteRecommendByPid(pid);
    }

    /** 查询商品评论列表 */
    public List<Map<String, Object>> selectProductCommentList(Product pro) {
        return mapper.selectProductCommentList(pro);
    }
}