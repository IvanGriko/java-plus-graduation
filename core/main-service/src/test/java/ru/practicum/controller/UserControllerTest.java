package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.controller.UserController;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Disabled("Выполнять только при запущенных Discovery and Config servers")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    void createUser_WithValidData_ReturnsCreatedUser() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("Test User")
                .build();
        UserDto expectedUser = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
        when(userService.create(any(NewUserRequestDto.class))).thenReturn(expectedUser);
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
        verify(userService).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ReturnsConflict() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("duplicate@example.com")
                .name("Test User")
                .build();
        when(userService.create(any(NewUserRequestDto.class)))
                .thenThrow(new ConflictException("User with email duplicate@example.com already exists",
                        "Integrity constraint has been violated"));
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
        verify(userService).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithBlankEmail_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("")
                .name("Test User")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("invalid-email")
                .name("Test User")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooShortEmail_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("a@b.c")
                .name("Test User")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooLongEmail_ReturnsBadRequest() throws Exception {
        String longEmail = "a".repeat(250) + "@example.com";
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email(longEmail)
                .name("Test User")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithBlankName_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooShortName_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name("A")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithTooLongName_ReturnsBadRequest() throws Exception {
        String longName = "A".repeat(251);
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name(longName)
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithNullEmail_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email(null)
                .name("Test User")
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void createUser_WithNullName_ReturnsBadRequest() throws Exception {
        NewUserRequestDto requestDto = NewUserRequestDto.builder()
                .email("test@example.com")
                .name(null)
                .build();
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any(NewUserRequestDto.class));
    }

    @Test
    void deleteUser_WithValidId_ReturnsNoContent() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).delete(userId);
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());
        verify(userService).delete(userId);
    }

    @Test
    void deleteUser_WithNonExistentId_ReturnsNotFound() throws Exception {
        Long userId = 999L;
        doThrow(new NotFoundException("User with id=" + userId + " was not found"))
                .when(userService).delete(userId);
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());
        verify(userService).delete(userId);
    }

    @Test
    void deleteUser_WithInvalidId_ReturnsBadRequest() throws Exception {
        Long invalidUserId = -1L;
        mockMvc.perform(delete("/admin/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
        verify(userService, never()).delete(any(Long.class));
    }

    @Test
    void deleteUser_WithZeroId_ReturnsBadRequest() throws Exception {
        Long invalidUserId = 0L;
        mockMvc.perform(delete("/admin/users/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
        verify(userService, never()).delete(any(Long.class));
    }

    @Test
    void getUsers_WithoutParameters_ReturnsDefaultPagedUsers() throws Exception {
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build(),
                UserDto.builder().id(2L).email("user2@example.com").name("User 2").build()
        );
        when(userService.findByIdListWithOffsetAndLimit(null, 0, 10))
                .thenReturn(expectedUsers);
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"))
                .andExpect(jsonPath("$[1].name").value("User 2"));
        verify(userService).findByIdListWithOffsetAndLimit(null, 0, 10);
    }

    @Test
    void getUsers_WithCustomPagination_ReturnsPagedUsers() throws Exception {
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(3L).email("user3@example.com").name("User 3").build()
        );
        when(userService.findByIdListWithOffsetAndLimit(null, 5, 5))
                .thenReturn(expectedUsers);
        mockMvc.perform(get("/admin/users")
                        .param("from", "5")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].email").value("user3@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 3"));
        verify(userService).findByIdListWithOffsetAndLimit(null, 5, 5);
    }

    @Test
    void getUsers_WithSpecificIds_ReturnsFilteredUsers() throws Exception {
        List<Long> userIds = Arrays.asList(1L, 3L);
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build(),
                UserDto.builder().id(3L).email("user3@example.com").name("User 3").build()
        );
        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(expectedUsers);
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(3L));
        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }

    @Test
    void getUsers_WithEmptyResult_ReturnsEmptyList() throws Exception {
        when(userService.findByIdListWithOffsetAndLimit(null, 0, 10))
                .thenReturn(Arrays.asList());
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(userService).findByIdListWithOffsetAndLimit(null, 0, 10);
    }

    @Test
    void getUsers_WithSingleId_ReturnsFilteredUser() throws Exception {
        List<Long> userIds = Arrays.asList(1L);
        List<UserDto> expectedUsers = Arrays.asList(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build()
        );
        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(expectedUsers);
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].name").value("User 1"));
        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }

    @Test
    void getUsers_WithNonExistentIds_ReturnsEmptyList() throws Exception {
        List<Long> userIds = Arrays.asList(999L, 1000L);
        when(userService.findByIdListWithOffsetAndLimit(userIds, 0, 10))
                .thenReturn(Arrays.asList());
        mockMvc.perform(get("/admin/users")
                        .param("ids", "999,1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(userService).findByIdListWithOffsetAndLimit(userIds, 0, 10);
    }
}