package com.seohyun.creator_settlement_pjt.service;

import com.seohyun.creator_settlement_pjt.dto.SaleRecordResponseDTO;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.Role;
import com.seohyun.creator_settlement_pjt.exception.BusinessException;
import com.seohyun.creator_settlement_pjt.exception.ErrorCode;
import com.seohyun.creator_settlement_pjt.repository.SaleRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleRecordService {

    private final SaleRecordRepository saleRecordRepository;
    private final UserRepository userRepository;

    public List<SaleRecordResponseDTO> getSaleRecords(Long creatorId, LocalDate from, LocalDate to) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (creator.getRole() != Role.CREATOR) {
            throw new BusinessException(ErrorCode.INVALID_SALE_RECORD_ROLE);
        }

        if (from != null && to != null) {
            LocalDateTime start = from.atStartOfDay();
            LocalDateTime end = to.atTime(23, 59, 59);
            return saleRecordRepository
                    .findByCourseCreatorAndPaidAtBetweenOrderByPaidAtDesc(creator, start, end)
                    .stream()
                    .map(SaleRecordResponseDTO::from)
                    .toList();
        }

        return saleRecordRepository
                .findByCourseCreatorOrderByPaidAtDesc(creator)
                .stream()
                .map(SaleRecordResponseDTO::from)
                .toList();
    }
}
