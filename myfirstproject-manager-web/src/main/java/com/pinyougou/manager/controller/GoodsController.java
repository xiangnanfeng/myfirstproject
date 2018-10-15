package com.pinyougou.manager.controller;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.*;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination queueAddSearchDestination;

	@Autowired
	private Destination queueDeleteSearchDestination;

	@Autowired
	private Destination topicAddPageDestination;

	@Autowired
	private Destination topicDelePageDestination;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}

	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			if("1".equals(status)){
				List<TbItem> itemList = goodsService.findItemListByIdAndStatus(ids, status);
				if(itemList.size()>0){
					final String itemListString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueAddSearchDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							TextMessage textMessage = session.createTextMessage(itemListString);
							return textMessage;
						}
					});
				}
				for (final Long id : ids) {
					jmsTemplate.send(topicAddPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							TextMessage textMessage = session.createTextMessage("" + id);
							return textMessage;
						}
					});
				}
			}
			return  new Result(true,"审核成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"审核失败");
		}
	}

	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/del")
	public Result del(final Long[] ids){
		try {
			goodsService.del(ids);
				jmsTemplate.send(queueDeleteSearchDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						ObjectMessage objectMessage = session.createObjectMessage(ids);
						return objectMessage;
					}
				});

				jmsTemplate.send(topicDelePageDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						ObjectMessage objectMessage = session.createObjectMessage(ids);
						return objectMessage;
					}
				});

			return new Result(true,"删除成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"删除失败！");
		}
	}

}
