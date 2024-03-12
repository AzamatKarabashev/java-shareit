package ru.practicum.shareit.booking.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingResponseDto toBookingResponseDto(Booking source);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "start", source = "bookingRequestDto.start"),
            @Mapping(target = "end", source = "bookingRequestDto.end"),
            @Mapping(target = "item", source = "item"),
            @Mapping(target = "booker", source = "booker"),
            @Mapping(target = "status", ignore = true)
    })
    Booking toBooking(BookingRequestDto bookingRequestDto, Item item, User booker);

    @AfterMapping
    default void setStatusToWaiting(@MappingTarget Booking booking) {
        booking.setStatus(BookingStatus.WAITING);
    }

    List<BookingResponseDto> toBookingResponseDtoList(List<Booking> source);
}