package com.pinyougou.pages.service.impl;

import com.pinyougou.pages.service.ItemPageService;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageAddListener implements MessageListener {

    private ItemPageService itemPageService;


    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        try {
            String id = textMessage.getText();

            boolean boo = itemPageService.getItemHtml(Long.parseLong(id));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
