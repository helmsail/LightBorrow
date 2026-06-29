package com.helmsail.lightborrow.mcp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import com.helmsail.lightborrow.mcp.mapper.AssetMapper;
import com.helmsail.lightborrow.mcp.mapper.BorrowMapper;
import com.helmsail.lightborrow.mcp.mapper.TransferMapper;
import com.helmsail.lightborrow.mcp.model.entity.AssetEntity;
import com.helmsail.lightborrow.mcp.model.entity.BorrowEntity;
import com.helmsail.lightborrow.mcp.model.entity.TransferEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AssetService {

    private final AssetMapper assetMapper;
    private final BorrowMapper borrowMapper;
    private final TransferMapper transferMapper;

    public AssetService(AssetMapper assetMapper, BorrowMapper borrowMapper,
                        TransferMapper transferMapper) {
        this.assetMapper = assetMapper;
        this.borrowMapper = borrowMapper;
        this.transferMapper = transferMapper;
    }

    public List<Map<String, Object>> queryAsset(String code, String name, String keyword,
                                                 int limit, int offset) {
        LambdaQueryWrapper<AssetEntity> wrapper = Wrappers.lambdaQuery();
        if (code != null && !code.isBlank()) {
            wrapper.eq(AssetEntity::getCode, code);
        }
        if (name != null && !name.isBlank()) {
            wrapper.like(AssetEntity::getName, name);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(AssetEntity::getName, keyword)
                    .or().like(AssetEntity::getDescription, keyword));
        }
        wrapper.orderByAsc(AssetEntity::getId);

        IPage<AssetEntity> page = assetMapper.selectPage(
                new Page<>(offset / Math.max(limit, 1) + 1, Math.min(limit, 100)), wrapper);

        return page.getRecords().stream().map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("code", e.getCode());
            map.put("name", e.getName());
            map.put("description", e.getDescription());
            map.put("status", e.getStatus());
            map.put("created_at", e.getCreatedAt());
            map.put("updated_at", e.getUpdatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> queryMyBorrows(String userId, int limit, int offset) {
        IPage<Map<String, Object>> page = borrowMapper.selectMyBorrows(
                new Page<>(offset / Math.max(limit, 1) + 1, Math.min(limit, 100)), userId);
        return page.getRecords();
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitBorrow(String userId, String assetCode, String reason, String expectedReturnAt) {
        LambdaQueryWrapper<AssetEntity> wrapper = Wrappers.<AssetEntity>lambdaQuery()
                .eq(AssetEntity::getCode, assetCode);
        AssetEntity asset = assetMapper.selectOne(wrapper);
        if (asset == null) {
            throw new BusinessException("资产不存在: " + assetCode);
        }

        BorrowEntity borrow = new BorrowEntity();
        borrow.setUserId(userId);
        borrow.setAssetId(asset.getId());
        borrow.setReason(reason);
        borrow.setExpectedReturnAt(expectedReturnAt != null ? LocalDate.parse(expectedReturnAt) : null);
        borrow.setStatus("pending");
        borrowMapper.insert(borrow);
        log.info("[MCP] 借用申请提交 userId={}, assetCode={}", userId, assetCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitTransfer(String fromUserId, String borrowId, String toUserId) {
        TransferEntity transfer = new TransferEntity();
        transfer.setFromUserId(fromUserId);
        transfer.setBorrowId(Integer.valueOf(borrowId));
        transfer.setToUserId(toUserId);
        transfer.setStatus("pending");
        transferMapper.insert(transfer);
        log.info("[MCP] 转借发起 fromUserId={}, borrowId={}, toUserId={}", fromUserId, borrowId, toUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelBorrow(String borrowId, String userId) {
        int rows = borrowMapper.update(
                Wrappers.<BorrowEntity>lambdaUpdate()
                        .set(BorrowEntity::getStatus, "cancelled")
                        .eq(BorrowEntity::getId, Integer.valueOf(borrowId))
                        .eq(BorrowEntity::getUserId, userId));
        if (rows == 0) {
            log.warn("[MCP] 取消借用失败: borrowId={}, userId={}", borrowId, userId);
            throw new BusinessException("取消借用失败：未找到对应的借用记录或无权操作");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmTransfer(String transferId, String userId) {
        int rows = transferMapper.update(
                Wrappers.<TransferEntity>lambdaUpdate()
                        .set(TransferEntity::getStatus, "confirmed")
                        .eq(TransferEntity::getId, Integer.valueOf(transferId))
                        .eq(TransferEntity::getToUserId, userId));
        if (rows == 0) {
            log.warn("[MCP] 确认转借失败: transferId={}, userId={}", transferId, userId);
            throw new BusinessException("确认转借失败：未找到对应的转借记录或无权操作");
        }
    }
}
