package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;


@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	/**
	 * 查询所有品牌
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();		
	}
	/**
	 * 分页查询品牌
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(Integer page,Integer size) {
		PageResult pageResult = brandService.findPage(page, size);
		return pageResult;
	}
	/**
	 * 添加品牌
	 * @param tbBrand
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand tbBrand) {
		try {
			brandService.add(tbBrand);
			return new Result(true, "添加品牌成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "添加失败");
		}	
	}
	/**
	 * 根据id查询品牌
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		return brandService.findOne(id);
	}
	/**
	 * 根据返回过来的品牌对象id更新品牌
	 * @param tbBrand
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand tbBrand) {
		try {
			brandService.update(tbBrand);
			return new Result(true, "更新品牌成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "更新失败");
		}
	}
	/**
	 * 删除品牌
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			// TODO: handle exception
			return new Result(false, "删除失败");
		}
	}
	/**
	 * 根据传过来的值进行模糊查询，然后分页
	 * @param tbBrand
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult findPage(@RequestBody TbBrand tbBrand,Integer page,Integer size) {
		
		return brandService.findPage(tbBrand, page, size);
		
	}
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectBrandList();
	}
	
}
