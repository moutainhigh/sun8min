package com.sun8min.redpacket.provider;

import com.sun8min.redpacket.api.RedpacketService;
import com.sun8min.redpacket.dao.RedpacketDao;
import com.sun8min.redpacket.entity.Redpacket;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Service(version = "${service.version}")
public class RedpacketServiceImpl implements RedpacketService {

    @Autowired
    RedpacketDao redpacketDao;

    @Override
    public int deleteByPrimaryKey(Long RedpacketId) {
        return redpacketDao.deleteByPrimaryKey(RedpacketId);
    }

    @Override
    public int insert(Redpacket record) {
        return redpacketDao.insert(record);
    }

    @Override
    public Redpacket selectByPrimaryKey(Long RedpacketId) {
        return redpacketDao.selectByPrimaryKey(RedpacketId);
    }

    @Override
    public List<Redpacket> selectAll() {
        return redpacketDao.selectAll();
    }

    @Override
    public int updateByPrimaryKey(Redpacket record) {
        return redpacketDao.updateByPrimaryKey(record);
    }

    @Override
    public BigDecimal findAmountByUserId(Long userId) {
        return redpacketDao.findByUserId(userId).getRedpacketAmount();
    }

    @Override
    public Redpacket findByUserId(Long userId) {
        return redpacketDao.findByUserId(userId);
    }
}
