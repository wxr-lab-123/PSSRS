package com.hjm.service.impl;

import com.hjm.pojo.Entity.Message;
import com.hjm.mapper.MessageMapper;
import com.hjm.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hjm
 * @since 2025-11-30
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

}
