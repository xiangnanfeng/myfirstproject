package com.pinyougou.pages.service.impl;

import com.pinyougou.pages.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class PageDleleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage)message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            for (Long id : goodsIds) {
                itemPageService.delePage(id);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
