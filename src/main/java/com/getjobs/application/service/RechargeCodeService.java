package com.getjobs.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.getjobs.application.entity.RechargeCodeEntity;
import com.getjobs.application.entity.RechargeLogEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.mapper.RechargeCodeMapper;
import com.getjobs.application.mapper.RechargeLogMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import com.getjobs.application.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值码管理服务
 */
@Service
public class RechargeCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除易混淆字符
    @Autowired
    private RechargeCodeMapper rechargeCodeMapper;
    @Autowired
    private RechargeLogMapper rechargeLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserBalanceMapper userBalanceMapper;

    /**
     * 批量生成充值码
     */
    @Transactional
    public Map<String, Object> generateCodes(int count, int amount, int bonus, String createdBy) {
        Map<String, Object> result = new HashMap<>();
        String batchNo = generateBatchNo();
        List<String> codes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String code = generateUniqueCode();
            RechargeCodeEntity entity = new RechargeCodeEntity();
            entity.setCode(code);
            entity.setAmount(amount);
            entity.setBonus(bonus);
            entity.setTotalValue(amount + bonus);
            entity.setStatus(0); // 0=未激活
            entity.setBatchNo(batchNo);
            entity.setCreatedBy(createdBy);
            entity.setCreatedAt(LocalDateTime.now());

            rechargeCodeMapper.insert(entity);
            codes.add(code);
        }

        result.put("batchNo", batchNo);
        result.put("count", count);
        result.put("codes", codes);
        return result;
    }

    /**
     * 查询充值码列表
     */
    public Map<String, Object> listCodes(int page, int size, Integer status, String batchNo) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<RechargeCodeEntity> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (batchNo != null && !batchNo.isEmpty()) {
            queryWrapper.eq("batch_no", batchNo);
        }
        queryWrapper.orderByDesc("created_at");

        Page<RechargeCodeEntity> pageRequest = new Page<>(page, size);
        Page<RechargeCodeEntity> pageResult = rechargeCodeMapper.selectPage(pageRequest, queryWrapper);

        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("totalPages", pageResult.getPages());

        return result;
    }

    /**
     * 查询单个充值码详情
     */
    public RechargeCodeEntity getCodeDetail(Long id) {
        return rechargeCodeMapper.selectById(id);
    }

    /**
     * 冻结充值码
     */
    @Transactional
    public boolean freezeCode(Long id) {
        RechargeCodeEntity entity = rechargeCodeMapper.selectById(id);
        if (entity == null) return false;
        if (entity.getStatus() == 1) {
            throw new RuntimeException("已激活的充值码不能冻结");
        }
        if (entity.getStatus() == 2) {
            throw new RuntimeException("充值码已冻结");
        }

        entity.setStatus(2); // 2=冻结
        return rechargeCodeMapper.updateById(entity) > 0;
    }

    /**
     * 作废充值码
     */
    @Transactional
    public boolean invalidateCode(Long id) {
        RechargeCodeEntity entity = rechargeCodeMapper.selectById(id);
        if (entity == null) return false;
        if (entity.getStatus() == 1) {
            throw new RuntimeException("已激活的充值码不能作废");
        }

        entity.setStatus(3); // 3=作废
        return rechargeCodeMapper.updateById(entity) > 0;
    }

    /**
     * 充值码统计
     */
    public Map<String, Object> getCodeStats(String batchNo) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<RechargeCodeEntity> queryWrapper = new QueryWrapper<>();
        if (batchNo != null && !batchNo.isEmpty()) {
            queryWrapper.eq("batch_no", batchNo);
        }

        long total = rechargeCodeMapper.selectCount(queryWrapper);
        queryWrapper.eq("status", 0);
        long unused = rechargeCodeMapper.selectCount(queryWrapper);
        queryWrapper.eq("status", 1);
        long activated = rechargeCodeMapper.selectCount(queryWrapper);
        queryWrapper.eq("status", 2);
        long frozen = rechargeCodeMapper.selectCount(queryWrapper);
        queryWrapper.eq("status", 3);
        long invalidated = rechargeCodeMapper.selectCount(queryWrapper);

        result.put("total", total);
        result.put("unused", unused);
        result.put("activated", activated);
        result.put("frozen", frozen);
        result.put("invalidated", invalidated);

        return result;
    }

    /**
     * 用户端 - 激活充值码
     */
    @Transactional
    public Map<String, Object> activateCode(Long userId, String code) {
        Map<String, Object> result = new HashMap<>();

        // 查询充值码
        RechargeCodeEntity rechargeCode = rechargeCodeMapper.selectByCode(code);
        if (rechargeCode == null) {
            throw new RuntimeException("充值码不存在");
        }
        if (rechargeCode.getStatus() == 1) {
            throw new RuntimeException("充值码已被使用");
        }
        if (rechargeCode.getStatus() == 2) {
            throw new RuntimeException("充值码已冻结，请联系管理员");
        }
        if (rechargeCode.getStatus() == 3) {
            throw new RuntimeException("充值码已作废");
        }

        // 查询用户余额
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        int balanceBefore = balance != null ? balance.getBalance() : 0;

        // 更新余额
        if (balance == null) {
            balance = new UserBalanceEntity();
            balance.setUserId(userId);
            balance.setBalance(rechargeCode.getTotalValue());
            balance.setTotalRecharge(rechargeCode.getTotalValue());
            userBalanceMapper.insert(balance);
        } else {
            balance.setBalance(balance.getBalance() + rechargeCode.getTotalValue());
            balance.setTotalRecharge(balance.getTotalRecharge() + rechargeCode.getTotalValue());
            userBalanceMapper.updateById(balance);
        }

        // 更新充值码状态
        rechargeCode.setStatus(1);
        rechargeCode.setActivatedBy(userId);
        rechargeCode.setActivatedAt(LocalDateTime.now());
        rechargeCodeMapper.updateById(rechargeCode);

        // 记录日志
        RechargeLogEntity log = new RechargeLogEntity();
        log.setUserId(userId);
        log.setCode(code);
        log.setAmount(rechargeCode.getAmount());
        log.setBonus(rechargeCode.getBonus());
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceBefore + rechargeCode.getTotalValue());
        log.setCreatedAt(LocalDateTime.now());
        rechargeLogMapper.insert(log);

        result.put("amount", rechargeCode.getAmount());
        result.put("bonus", rechargeCode.getBonus());
        result.put("totalValue", rechargeCode.getTotalValue());
        result.put("balanceAfter", balanceBefore + rechargeCode.getTotalValue());

        return result;
    }

    /**
     * 生成唯一充值码 (GJ2025-XXXX-XXXX-XXXX 格式)
     */
    private String generateUniqueCode() {
        StringBuilder code = new StringBuilder("GJ2025-");
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append("-");
            }
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }

        // 检查是否已存在
        if (rechargeCodeMapper.selectByCode(code.toString()) != null) {
            return generateUniqueCode(); // 递归重试
        }

        return code.toString();
    }

    /**
     * 生成批次号
     */
    private String generateBatchNo() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("BATCH-%04d%02d%02d-%04d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                RANDOM.nextInt(10000));
    }
}
