package com.wwh.home.center.service.impl;

import com.wwh.home.center.dao.mapper.PcDeviceMapper;
import com.wwh.home.center.model.entity.PcDevice;
import com.wwh.home.center.service.PcDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PcDeviceServiceImpl implements PcDeviceService {

    @Autowired
    private PcDeviceMapper pcDeviceMapper;

    @Override
    public List<PcDevice> getAllDevices() {
        return pcDeviceMapper.selectList(null);
    }

    @Override
    public void addDevice(PcDevice device) {
        LocalDateTime now = LocalDateTime.now();
        device.setId(null);
        device.setCreateTime(now);
        device.setUpdateTime(now);
        pcDeviceMapper.insert(device);
        log.info("添加PC设备成功，name={}", device.getName());
    }

    @Override
    public void updateDevice(PcDevice device) {
        device.setUpdateTime(LocalDateTime.now());
        pcDeviceMapper.updateById(device);
        log.info("更新PC设备成功，id={}", device.getId());
    }

    @Override
    public void deleteDevice(Long id) {
        pcDeviceMapper.deleteById(id);
        log.info("删除PC设备成功，id={}", id);
    }
}
