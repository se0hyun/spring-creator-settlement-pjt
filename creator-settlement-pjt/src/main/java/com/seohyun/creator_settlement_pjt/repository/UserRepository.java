package com.seohyun.creator_settlement_pjt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.seohyun.creator_settlement_pjt.entity.User;
import com.seohyun.creator_settlement_pjt.entity.Role;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByRole(Role role); // 정산 생성 시 모든 크리에이터 순회

}
