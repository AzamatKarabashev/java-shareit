package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemDto {
    private Long id;
    private Long bookerId;

    @Override
    public String toString() {
        return "BookingItemDto{" +
                "id=" + id +
                ", bookerId=" + bookerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingItemDto that = (BookingItemDto) o;
        return Objects.equals(id, that.id) && Objects.equals(bookerId, that.bookerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookerId);
    }
}
