package ru.practicum.shareit.request.repository.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findItemRequestsByRequestorId(Long requestorId);

    @Query("SELECT r FROM ItemRequest r WHERE r.requestor.id <> :requestorId")
    Page<ItemRequest> findItemRequestsByRequestorId(@Param("requestorId") Long requestorId, Pageable pageable);
}
