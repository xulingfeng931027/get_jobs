package com.getjobs.application.service;

import com.getjobs.application.entity.UserDeviceEntity;
import com.getjobs.application.mapper.UserDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备绑定管理服务
 * 限制用户最多绑定2台设备
 */
@Slf4j
@Service
public class DeviceService {

    /**
     * 最大绑定设备数
     */
    private static final int MAX_DEVICES = 2;

    @Autowired
    private UserDeviceMapper userDeviceMapper;

    /**
     * 绑定设备
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @param deviceName 设备名称
     * @return 绑定结果
     */
    @Transactional
    public Map<String, Object> bindDevice(Long userId, String deviceFingerprint, String deviceName) {
        Map<String, Object> result = new HashMap<>();

        // 检查设备是否已绑定
        UserDeviceEntity existingDevice = userDeviceMapper.selectByFingerprint(userId, deviceFingerprint);
        if (existingDevice != null) {
            // 更新最后登录时间
            userDeviceMapper.updateLastLoginAt(existingDevice.getId(), LocalDateTime.now());
            result.put("success", true);
            result.put("message", "设备已存在，已更新登录时间");
            result.put("deviceId", existingDevice.getId());
            return result;
        }

        // 检查设备数量限制
        int currentCount = userDeviceMapper.countByUserId(userId);
        if (currentCount >= MAX_DEVICES) {
            result.put("success", false);
            result.put("message", "设备绑定已达上限（最多" + MAX_DEVICES + "台），请先解绑旧设备");
            result.put("deviceCount", currentCount);
            result.put("maxDevices", MAX_DEVICES);
            return result;
        }

        // 绑定新设备
        UserDeviceEntity device = new UserDeviceEntity();
        device.setUserId(userId);
        device.setDeviceFingerprint(deviceFingerprint);
        device.setDeviceName(deviceName != null ? deviceName : "未知设备");
        device.setLastLoginAt(LocalDateTime.now());
        device.setCreatedAt(LocalDateTime.now());
        userDeviceMapper.insert(device);

        log.info("[设备绑定] 用户{}绑定新设备：{}, 当前绑定数：{}/{}", 
            userId, deviceFingerprint, currentCount + 1, MAX_DEVICES);

        result.put("success", true);
        result.put("message", "设备绑定成功");
        result.put("deviceId", device.getId());
        result.put("deviceCount", currentCount + 1);
        result.put("maxDevices", MAX_DEVICES);

        return result;
    }

    /**
     * 解绑设备
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return 是否成功
     */
    @Transactional
    public boolean unbindDevice(Long userId, String deviceFingerprint) {
        int rows = userDeviceMapper.deleteByFingerprint(userId, deviceFingerprint);
        if (rows > 0) {
            log.info("[设备绑定] 用户{}解绑设备：{}", userId, deviceFingerprint);
            return true;
        }
        return false;
    }

    /**
     * 查询用户绑定的所有设备
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    public List<UserDeviceEntity> getUserDevices(Long userId) {
        return userDeviceMapper.selectByUserId(userId);
    }

    /**
     * 验证设备是否已绑定
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return true=已绑定
     */
    public boolean isDeviceBound(Long userId, String deviceFingerprint) {
        UserDeviceEntity device = userDeviceMapper.selectByFingerprint(userId, deviceFingerprint);
        return device != null;
    }

    /**
     * 获取用户当前绑定的设备数量
     *
     * @param userId 用户ID
     * @return 设备数量
     */
    public int getDeviceCount(Long userId) {
        return userDeviceMapper.countByUserId(userId);
    }
}
