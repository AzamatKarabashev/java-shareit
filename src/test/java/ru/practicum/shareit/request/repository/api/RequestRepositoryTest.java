package ru.practicum.shareit.request.repository.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @PersistenceContext
    private EntityManager em;

    private User user;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Test User")
                .email("test@test.com")
                .build();
        em.persist(user);

        itemRequest = ItemRequest.builder()
                .description("Description")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        em.persist(itemRequest);
    }

    @Test
    public void testFindItemRequestsByRequestorIdWhenRecordsExistThenReturnList() {
        List<ItemRequest> result = requestRepository.findItemRequestsByRequestorId(user.getId());
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo(itemRequest.getDescription());
        assertThat(result.get(0).getRequestor()).isEqualTo(user);
    }

    @Test
    public void testFindItemRequestsByRequestorIdWhenNoRecordsThenReturnEmptyList() {
        List<ItemRequest> result = requestRepository.findItemRequestsByRequestorId(-1L);
        assertThat(result).isEmpty();
    }
}
