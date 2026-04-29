package com.seohyun.creator_settlement_pjt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.seohyun.creator_settlement_pjt.repository.SaleRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.CourseRepository;
import com.seohyun.creator_settlement_pjt.repository.UserRepository;
import com.seohyun.creator_settlement_pjt.repository.CancelRecordRepository;
import com.seohyun.creator_settlement_pjt.repository.FeeRecordRepository;
import com.seohyun.creator_settlement_pjt.dto.CancelRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.CancelResponseDTO;
import com.seohyun.creator_settlement_pjt.dto.EnrollmentRequestDTO;
import com.seohyun.creator_settlement_pjt.dto.EnrollmentResponseDTO;
import com.seohyun.creator_settlement_pjt.entity.CancelRecord;
import com.seohyun.creator_settlement_pjt.entity.Course;
import com.seohyun.creator_settlement_pjt.entity.FeeRecord;
import com.seohyun.creator_settlement_pjt.entity.Role;
import com.seohyun.creator_settlement_pjt.entity.SaleRecord;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.exception.BusinessException;
import com.seohyun.creator_settlement_pjt.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final SaleRecordRepository saleRecordRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CancelRecordRepository cancelRecordRepository;
    private final FeeRecordRepository feeRecordRepository;

    @Transactional
    public EnrollmentResponseDTO enroll(EnrollmentRequestDTO enrollmentRequestDTO) {
        
        Course course = courseRepository.findById(enrollmentRequestDTO.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        User student = userRepository.findById(enrollmentRequestDTO.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_ENROLL_ROLE);
        }

        if (enrollmentRequestDTO.getPaidAmount() != course.getPrice()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        if (saleRecordRepository.existsByStudentAndCourse(student, course)) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }

        FeeRecord feeRecord = feeRecordRepository.findActiveFeeRate(enrollmentRequestDTO.getPaidAt())
                .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));

        SaleRecord saleRecord = saleRecordRepository.save(SaleRecord.builder()
                .course(course)
                .student(student)
                .paidAmount(enrollmentRequestDTO.getPaidAmount())   // 샘플 데이터에 따른 변경
                .paidAt(enrollmentRequestDTO.getPaidAt())
                .feeRate(feeRecord.getFeeRate())
                .build());

        return EnrollmentResponseDTO.builder()
                .id(saleRecord.getId())
                .courseTitle(course.getTitle())
                .studentName(student.getName())
                .paidAmount(saleRecord.getPaidAmount())     // 샘플 데이터에 따른 변경
                .paidAt(saleRecord.getPaidAt())
                .build();
    }

    @Transactional
    public CancelResponseDTO cancel(Long saleRecordId, CancelRequestDTO cancelRequestDTO) {
        SaleRecord saleRecord = saleRecordRepository.findById(saleRecordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SALE_RECORD_NOT_FOUND));

        if (cancelRecordRepository.existsBySaleRecord(saleRecord)) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELLED);
        }

        if (cancelRequestDTO.getCancelAmount() > saleRecord.getPaidAmount()) {
            throw new BusinessException(ErrorCode.CANCEL_AMOUNT_EXCEEDED);
        }

        FeeRecord feeAtCancel = feeRecordRepository.findActiveFeeRate(cancelRequestDTO.getCanceledAt())
                .orElseThrow(() -> new BusinessException(ErrorCode.FEE_RATE_NOT_FOUND));

        CancelRecord cancelRecord = cancelRecordRepository.save(CancelRecord.builder()
                .saleRecord(saleRecord)
                .cancelAmount(cancelRequestDTO.getCancelAmount())
                .canceledAt(cancelRequestDTO.getCanceledAt())
                .feeRate(feeAtCancel.getFeeRate())
                .build());

        return CancelResponseDTO.builder()
                .id(cancelRecord.getId())
                .saleRecordId(saleRecord.getId())
                .courseTitle(saleRecord.getCourse().getTitle())
                .studentName(saleRecord.getStudent().getName())
                .paidAmount(saleRecord.getPaidAmount())     // 샘플 데이터에 따른 변경  
                .cancelAmount(cancelRecord.getCancelAmount())     // 샘플 데이터에 따른 변경
                .canceledAt(cancelRecord.getCanceledAt())
                .feeRateAtCancel(cancelRecord.getFeeRate())
                .build();
    }
}
