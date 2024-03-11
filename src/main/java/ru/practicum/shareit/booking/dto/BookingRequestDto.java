package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.validation.StartBeforeEnd;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@StartBeforeEnd
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    @NotNull
    private Long itemId;

    @Override
    public String toString() {
        return "BookingRequestDto{" +
                "start=" + start +
                ", end=" + end +
                ", itemId=" + itemId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingRequestDto that = (BookingRequestDto) o;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, itemId);
    }
}