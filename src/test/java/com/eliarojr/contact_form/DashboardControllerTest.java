package com.eliarojr.contact_form;

import com.eliarojr.contact_form.controller.AdminController;
import com.eliarojr.contact_form.controller.AppointmentController;
import com.eliarojr.contact_form.controller.UserController;
import com.eliarojr.contact_form.dto.AdminStatsResponse;
import com.eliarojr.contact_form.dto.AdminUserResponse;
import com.eliarojr.contact_form.dto.AppointmentResponse;
import com.eliarojr.contact_form.dto.MessageResponse;
import com.eliarojr.contact_form.entity.enums.MessageStatus;
import com.eliarojr.contact_form.entity.enums.MessageType;
import com.eliarojr.contact_form.service.AdminService;
import com.eliarojr.contact_form.service.AdminStatsService;
import com.eliarojr.contact_form.service.AdminUserService;
import com.eliarojr.contact_form.service.AppointmentService;
import com.eliarojr.contact_form.service.MessageService;
import com.eliarojr.contact_form.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock private UserService userService;
    @Mock private AdminService adminService;
    @Mock private AdminUserService adminUserService;
    @Mock private AdminStatsService adminStatsService;
    @Mock private AppointmentService appointmentService;
    @Mock private MessageService messageService;

    private MockMvc userMockMvc;
    private MockMvc adminMockMvc;
    private MockMvc appointmentMockMvc;

    @BeforeEach
    void setUp() {
        userMockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
        adminMockMvc = MockMvcBuilders.standaloneSetup(
                new AdminController(messageService, adminService, adminStatsService,
                        adminUserService)).build();
        appointmentMockMvc = MockMvcBuilders.standaloneSetup(
                new AppointmentController(appointmentService)).build();
    }

    @Test
    void userMessagesUsePagedResponseAndNormalizedPageSize() throws Exception {
        Page<MessageResponse> response = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 50), 0);
        when(userService.getMyMessages(any(Pageable.class))).thenReturn(response);

        userMockMvc.perform(get("/api/user/messages")
                        .param("page", "-3")
                        .param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getMyMessages(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(50, pageable.getValue().getPageSize());
    }

    @Test
    void adminMessagesForwardFiltersAndPagedRequest() throws Exception {
        Page<MessageResponse> response = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 8), 20);
        when(adminService.getAllMessages(
                eq(MessageStatus.NEW), eq(MessageType.PRAYER_REQUEST), eq("sam"), any(Pageable.class)))
                .thenReturn(response);

        adminMockMvc.perform(get("/api/admin/messages")
                        .param("status", "NEW")
                        .param("type", "PRAYER_REQUEST")
                        .param("search", "sam")
                        .param("page", "2")
                        .param("size", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.totalElements").value(20));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(adminService).getAllMessages(
                org.mockito.ArgumentMatchers.eq(MessageStatus.NEW),
                org.mockito.ArgumentMatchers.eq(MessageType.PRAYER_REQUEST),
                org.mockito.ArgumentMatchers.eq("sam"),
                pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(8, pageable.getValue().getPageSize());
        assertTrue(pageable.getValue().getSort().getOrderFor("createdAt") != null);
    }

    @Test
    void adminUsersUseDefaultPagingAndDescendingCreationDate() throws Exception {
        Page<AdminUserResponse> response = new PageImpl<>(List.of(),
                org.springframework.data.domain.PageRequest.of(0, 8), 0);
        when(adminUserService.getUsers(any(Pageable.class))).thenReturn(response);

        adminMockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(adminUserService).getUsers(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(8, pageable.getValue().getPageSize());
    }

    @Test
    void adminAppointmentsUsePagedResponse() throws Exception {
        Page<AppointmentResponse> response = new PageImpl<>(List.of(),
                org.springframework.data.domain.PageRequest.of(0, 8), 0);
        when(appointmentService.getAllAppointments(any(Pageable.class))).thenReturn(response);

        appointmentMockMvc.perform(get("/api/admin/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentService).getAllAppointments(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(8, pageable.getValue().getPageSize());
    }
}
