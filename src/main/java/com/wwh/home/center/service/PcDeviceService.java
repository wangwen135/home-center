package com.wwh.home.center.service;

import com.wwh.home.center.model.entity.PcDevice;
import java.util.List;

public interface PcDeviceService {
    List<PcDevice> getAllDevices();

    void addDevice(PcDevice device);

    void updateDevice(PcDevice device);

    void deleteDevice(Long id);
}
