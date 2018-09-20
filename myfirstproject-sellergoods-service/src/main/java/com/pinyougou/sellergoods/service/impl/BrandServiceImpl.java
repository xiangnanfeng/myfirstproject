package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;
	/**
	 * 查询所有品牌
	 */
	@Override
	public List<TbBrand> findAll() {
		// TODO Auto-generated method stub
		return brandMapper.selectByExample(null);
	}
	/**
	 * 分页查询
	 */
	@Override
	public PageResult findPage(Integer pageNum,Integer pageSize) {
		// TODO Auto-generated method stub
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	} 
	/**
	 * 添加品牌
	 */
	@Override
	public void add(TbBrand tbBrand) {
		brandMapper.insert(tbBrand);
	}
	/**
	 * 根据品牌id查询品牌
	 */
	@Override
	public TbBrand findOne(Long id) {
		// TODO Auto-generated method stub
		return brandMapper.selectByPrimaryKey(id);
	}
	/**
	 * 更新品牌，根据传递过来的品牌对象id
	 */
	@Override
	public void update(TbBrand tbBrand) {
		// TODO Auto-generated method stub
		brandMapper.updateByPrimaryKey(tbBrand);
	}
	@Override
	public void delete(Long[] ids) {
		// TODO Auto-generated method stub
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}
	@Override
	public PageResult findPage(TbBrand tbBrand, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample example = new TbBrandExample();
		Criteria createCriteria = example.createCriteria();
		if(tbBrand!=null) {
			if(tbBrand.getName()!=null && tbBrand.getName().length()>0) {
				createCriteria.andNameLike("%"+tbBrand.getName()+"%");
			}
			if(tbBrand.getFirstChar()!=null && tbBrand.getFirstChar().length()==1) {
				createCriteria.andFirstCharLike("%"+tbBrand.getFirstChar()+"%");
			}
		}	
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example );
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectBrandList() {
		return brandMapper.selectOptionList();
	}

}
