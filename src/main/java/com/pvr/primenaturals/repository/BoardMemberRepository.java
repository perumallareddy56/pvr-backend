package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {
    List<BoardMember> findByActiveTrueOrderByDisplayOrderAsc();
    List<BoardMember> findAllByOrderByDisplayOrderAsc();
}
