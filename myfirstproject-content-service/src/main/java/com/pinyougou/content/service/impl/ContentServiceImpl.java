package com.pinyougou.content.service.impl;
import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page=  (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {

		Long categoryId = content.getCategoryId();

		redisTemplate.boundHashOps("content").delete(categoryId);

		contentMapper.insert(content);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){//更新的时候要注意，用户有可能更改了广告的分类id，所有要先查出被更改前的分类id，再比较看看，如果
		//确实改变，那就需要将缓存中的两处都要删除
		Long id = content.getId();

		TbContent tbContent = contentMapper.selectByPrimaryKey(id);

		Long categoryId = tbContent.getCategoryId();

		redisTemplate.boundHashOps("content").delete(categoryId);

		contentMapper.updateByPrimaryKey(content);
		//longValue方法将包装类Long 的引用转换为基本数据类型
		//
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);

			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());

			contentMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();

		if(content!=null){
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}

		}

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findContenByCategegoryId(Long categegoryId) {

		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categegoryId);

		if(contentList==null){
			TbContentExample example = new TbContentExample();

			Criteria criteria = example.createCriteria();

			criteria.andCategoryIdEqualTo(categegoryId);

			criteria.andStatusEqualTo("1");
			//在查询时根据sort_order字段排序
			example.setOrderByClause("sort_order");

			contentList = contentMapper.selectByExample(example);

			redisTemplate.boundHashOps("content").put(categegoryId,contentList);
			System.out.println("走的数据库！");
			return contentList;
		}
		System.out.println("走的缓存");
		return contentList;
	}

}
