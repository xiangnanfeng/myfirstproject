package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {
	
	/**
	 * 查询所有品牌
	 * @return
	 */
	public List<TbBrand> findAll();
	/**
	 * 分页查询品牌
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(Integer pageNum,Integer pageSize);
	
	/**
	 * 增加品牌
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
	/**
	 * 根据id查询品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	/**
	 * 更新品牌
	 * @param tbBrand
	 */
	public void update(TbBrand tbBrand);
	/**
	 * 品牌删除
	 * @param id
	 */
	public void delete(Long[] ids);
	/**
	 * 条件查询，根据品牌名称，首字母，然后分页
	 * @param tbBrand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand tbBrand,Integer pageNum,Integer pageSize);

    List<Map> selectBrandList();
}
