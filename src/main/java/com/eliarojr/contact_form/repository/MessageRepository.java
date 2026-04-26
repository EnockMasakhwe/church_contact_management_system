package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN FETCH m.user")
    List<Message> findAllWithUser();
    List<Message> findByUser_IdOrderByCreatedAtDesc(Long userId);
    Long countByStatus(MessageStatus status);
}
