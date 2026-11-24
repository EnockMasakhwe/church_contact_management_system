package com.eliarojr.contact_form.repository;

import com.eliarojr.contact_form.entity.Message;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN FETCH m.user")
    List<Message> findAllWithUser();
    List<Message> findByUser_IdOrderByCreatedAtDesc(Long userId);
    Page<Message> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT m FROM Message m JOIN m.user u
            WHERE (:status IS NULL OR m.status = :status)
              AND (:type IS NULL OR m.type = :type)
              AND (:search IS NULL OR
                   LOWER(m.message) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Message> findAdminPage(
            @Param("status") MessageStatus status,
            @Param("type") MessageType type,
            @Param("search") String search,
            Pageable pageable);
    Long countByStatus(MessageStatus status);
}
